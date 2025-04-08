package com.d104.data.repository

import android.util.Log
import com.d104.data.local.dao.PreferencesDao
import com.d104.data.remote.api.WebSocketService
import com.d104.data.remote.utils.StompUtils
import com.d104.domain.model.StompErrorException // Keep your exception class if needed
import com.d104.domain.utils.StompConnectionState
import com.d104.domain.repository.WebSocketRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.UUID
import java.util.concurrent.atomic.AtomicLong // For thread-safe timestamp update
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

@Singleton
class WebSocketRepositoryImpl @Inject constructor(
    private val webSocketService: WebSocketService,
    private val datastoreDao: PreferencesDao,
) : WebSocketRepository {

    private val webSocketUrl = "wss://j12d104.p.ssafy.io/ws"
    private val host = "j12d104.p.ssafy.io"
    // --- Heartbeat Configuration ---
    // Client wants 10s outgoing, 10s incoming
    private val CLIENT_OUTGOING_HEARTBEAT = 300000L
    private val CLIENT_INCOMING_HEARTBEAT = 300000L

    // --- State Management ---
    private val _connectionState = MutableStateFlow(StompConnectionState.DISCONNECTED)
    override val connectionState: StateFlow<StompConnectionState> = _connectionState.asStateFlow()

    private var currentRoomId: String? = null
    private var currentTopic: String? = null
    private var currentSubscriptionId: String? = null

    private var messageFlow: SharedFlow<String>? = null
    private var messageFlowJob: Job? = null
    private val repositoryScope = CoroutineScope(Job() + Dispatchers.IO) // Use this scope for background tasks

    // --- Heartbeat State ---
    private var serverOutgoingHeartbeat = 0L // How often the server will send PONGs (server's incoming)
    private var serverIncomingHeartbeat = 0L // How often the server expects PINGs (server's outgoing)
    private var outgoingHeartbeatJob: Job? = null // Job for sending client PINGs
    private var incomingHeartbeatCheckJob: Job? = null // Job for checking server PONGs
    // Using AtomicLong for thread safety as onMessage might run on a different thread than the checker job
    private val lastServerPongTimestamp = AtomicLong(0L)


    // External listener - kept for potential future use or different connection types
    private val externalWebSocketListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) { Log.w("StompRepo", "External listener onOpen called - Unexpected.") }
        override fun onMessage(webSocket: WebSocket, text: String) { /* No-op */ }
        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) { handleDisconnect("WebSocket Closing (External Listener)") }
        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) { handleDisconnect("WebSocket Closed (External Listener)") }
        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) { handleConnectionFailure(t) }
    }

    override suspend fun connect(topic: String): Flow<String> {
        Log.d("StompRepo", "connect() called for topic: $topic")

        // 0. Check existing connection
        if (currentRoomId == topic && _connectionState.value == StompConnectionState.CONNECTED && messageFlow != null) {
            Log.d("StompRepo", "Already connected to room $topic. Returning existing flow.")
            return messageFlow!!
        }

        // 1. Disconnect if switching rooms
        if (_connectionState.value != StompConnectionState.DISCONNECTED && currentRoomId != topic) {
            Log.w("StompRepo", "Switching rooms. Disconnecting from $currentRoomId first.")
            disconnect() // This will call handleDisconnect which stops timers
            _connectionState.first { it == StompConnectionState.DISCONNECTED } // Wait for disconnection
        }

        // 2. Reset state for new connection attempt
        _connectionState.value = StompConnectionState.CONNECTING
        currentRoomId = topic
        currentTopic = "/topic/room/$topic"
        currentSubscriptionId = "sub-$topic-${UUID.randomUUID().toString().take(8)}"
        messageFlowJob?.cancel() // Cancel previous flow observation job
        stopHeartbeatTimers() // Ensure any stray timers are stopped

        Log.d("StompRepo", "Starting channelFlow for topic $topic...")

        // 3. Create channelFlow with internal listener
        val internalFlow = channelFlow<String> {
            val forwardingListener = object : WebSocketListener() {

                override fun onOpen(webSocket: WebSocket, response: Response) {
                    Log.d("StompRepo", ">>> WebSocket Opened (forwardingListener). Preparing CONNECT frame...")
                    launch { // Use channelFlow's scope
                        try {
                            val token = datastoreDao.getAccessToken().first()
                            if (token != null) {
                                // *** Request Heartbeat in CONNECT frame ***
                                val connectFrame = StompUtils.buildConnectFrame(
                                    host,
                                    token,
                                    CLIENT_OUTGOING_HEARTBEAT, // Client wants to send every 10s
                                    CLIENT_INCOMING_HEARTBEAT  // Client wants to receive every 10s
                                )
                                Log.d("StompRepo", ">>> Sending CONNECT frame (Heartbeat requested: $CLIENT_OUTGOING_HEARTBEAT, $CLIENT_INCOMING_HEARTBEAT)")
                                val sent = webSocketService.send(connectFrame)
                                if (sent) {
                                    Log.i("StompRepo", ">>> STOMP CONNECT frame sent successfully.")
                                } else {
                                    Log.e("StompRepo", ">>> Failed to send STOMP CONNECT frame.")
                                    handleConnectionFailure(Exception("Failed to send CONNECT frame"))
                                    close(Exception("Failed to send CONNECT frame"))
                                }
                            } else {
                                Log.e("StompRepo", ">>> Access Token is NULL. Cannot send CONNECT.")
                                handleConnectionFailure(Exception("Access token not available"))
                                close(Exception("Access token not available"))
                            }
                        } catch (e: Exception) {
                            Log.e("StompRepo", "!!! Exception in onOpen launch block !!!", e)
                            handleConnectionFailure(e)
                            close(e)
                        }
                    }
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    // *** Handle Server Heartbeat (PONG) ***
                    if (text == "\n") {
                        Log.v("StompRepo", "<<< Received Server Heartbeat (PONG)")
                        lastServerPongTimestamp.set(System.currentTimeMillis())
                        return // It's just a heartbeat, no further processing needed
                    }

                    Log.d("StompRepo", ">>> Received STOMP Frame (forwardingListener): ${text.take(200)}...")
                    try {
                        val (command, headers, body) = StompUtils.parseFrame(text)
                        Log.d("StompRepo", "Processing Frame: Command=$command, SubId=${headers["subscription"]}")

                        when (command) {
                            "CONNECTED" -> {
                                Log.i("StompRepo", "STOMP CONNECTED received. Headers: $headers")
                                // *** Parse Server Heartbeat Settings ***
                                parseAndStartHeartbeats(headers["heart-beat"])
                                _connectionState.value = StompConnectionState.CONNECTED
                                lastServerPongTimestamp.set(System.currentTimeMillis()) // Initialize pong time on connect
                                sendSubscriptionFrame(currentTopic!!, currentSubscriptionId!!)
                            }
                            "MESSAGE" -> {
                                if (headers["subscription"] == currentSubscriptionId) {
                                    Log.d("StompRepo", "Message for current subscription: ${body.take(100)}...")
                                    // 여기까지 확인
                                    trySend(body) // Emit message to the flow
                                } else {
                                    Log.w("StompRepo", "Ignoring message for other subscription: ${headers["subscription"]}")
                                }
                            }
                            "ERROR" -> {
                                val errorMessage = "STOMP ERROR: ${headers["message"]} - Body: $body"
                                Log.e("StompRepo", errorMessage)
                                // Decide if this error should terminate the flow/connection
                                // For now, just log it. You might want to call handleConnectionFailure or close the flow.
                                // close(StompErrorException(errorMessage)) // Optionally close flow on ERROR
                            }
                            // Add other command handling if needed (RECEIPT, etc.)
                            else -> {
                                Log.d("StompRepo", "Received unhandled STOMP command: $command")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("StompRepo", "Error parsing STOMP frame in onMessage", e)
                        // Don't close the flow immediately here, let onFailure handle underlying issues
                        // close(e) // Avoid closing here unless it's a fatal parsing error
                    }
                }

                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    Log.w("StompRepo", ">>> WebSocket Closing (forwardingListener): Code=$code, Reason=$reason")
                    handleDisconnect("WebSocket Closing in Flow")
                    close()
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    Log.w("StompRepo", ">>> WebSocket Closed (forwardingListener): Code=$code, Reason=$reason")
                    handleDisconnect("WebSocket Closed in Flow")
                    close() // Ensure flow is closed
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    Log.e("StompRepo", ">>> WebSocket Failure (forwardingListener)", t)
                    handleConnectionFailure(t) // Centralized failure handling (stops timers)
                    close(t) // Close the flow with the error
                }
            } // End of forwardingListener

            // Start WebSocket connection using the internal listener
            Log.d("StompRepo", ">>> Calling webSocketService.connect() with forwardingListener...")
            webSocketService.connect(webSocketUrl, forwardingListener)

            // Cleanup when the channelFlow is closed (cancelled or completed)
            awaitClose {
                // 'it' (the cause) is not passed directly to awaitClose.
                // Closure reasons are logged elsewhere (e.g., onFailure, catch, handleDisconnect).
                Log.d("StompRepo", "ChannelFlow for room $topic cleaning up resources on close.")

                // This is the crucial part: Ensure timers are stopped when the flow finishes.
                // Redundant safety check, as handleDisconnect/Failure should already do this,
                // but good practice for direct flow cancellation scenarios.
                stopHeartbeatTimers()
            }
        } // End of channelFlow

        // 4. Share and Observe the Flow
        messageFlow = internalFlow
            .catch { e -> // Catch errors from the upstream channelFlow
                Log.e("StompRepo", "Error caught in shared message flow for $topic", e)
                handleConnectionFailure(e) // Ensure cleanup on flow error
            }
            .shareIn(repositoryScope, SharingStarted.WhileSubscribed(5000), 1) // Replay 1, keep active 5s

        // Launch a job to keep the shared flow active while needed (auto-cancels with scope)
        messageFlowJob = repositoryScope.launch {
            messageFlow?.collect {
                Log.v("StompRepo", "Message observed in shared flow activator: ${it.take(50)}...")
                // Actual processing happens in the ViewModel/UseCase collecting this flow
            }
        }

        // 5. Wait for STOMP Connection (with timeout)
        try {
            Log.d("StompRepo", "Waiting for STOMP connection to $topic...")
            withTimeoutOrNull(15000) { // 15 seconds timeout
                _connectionState.first { it == StompConnectionState.CONNECTED }
            }
            if (_connectionState.value == StompConnectionState.CONNECTED) {
                Log.i("StompRepo", "STOMP connection established successfully for $topic.")
            } else {
                Log.e("StompRepo", "STOMP connection timed out for $topic after 15 seconds.")
                // Trigger disconnect logic if timed out before connecting
                handleDisconnect("Connection Timeout") // Will stop timers etc.
                throw Exception("STOMP connection timed out for room $topic")
            }
        } catch (e: Exception) {
            Log.e("StompRepo", "Error during connection wait/timeout for $topic", e)
            // Failure might have already been handled, but ensure state is correct
            if (_connectionState.value != StompConnectionState.DISCONNECTED) {
                handleConnectionFailure(e) // Ensure cleanup
            }
            throw e // Propagate the error
        }

        Log.i("StompRepo", "connect() method finished for topic $topic. Returning message flow.")
        return messageFlow!! // Flow is ready
    }

    // --- Heartbeat Logic ---

    private fun parseAndStartHeartbeats(heartbeatHeader: String?) {
        stopHeartbeatTimers() // Stop any existing timers before starting new ones

        if (heartbeatHeader == null || heartbeatHeader == "0,0") {
            Log.i("StompRepo", "Heartbeat disabled by server or not negotiated.")
            serverOutgoingHeartbeat = 0L
            serverIncomingHeartbeat = 0L
            return
        }

        try {
            val parts = heartbeatHeader.split(',')
            if (parts.size == 2) {
                // Server's CONNECTED heart-beat: sx, sy
                // sx: server outgoing (how often server sends -> client incoming)
                // sy: server incoming (how often server expects -> client outgoing)
                val serverCanSendEvery = parts[0].trim().toLong()
                val serverExpectsEvery = parts[1].trim().toLong()

                // Client needs to send PINGs based on server's expectation (sy)
                // Client needs to check for PONGs based on server's sending rate (sx)
                serverIncomingHeartbeat = serverCanSendEvery // How often we should expect PONGs from server
                serverOutgoingHeartbeat = serverExpectsEvery // How often we should send PINGs to server

                Log.i("StompRepo", "Server agreed Heartbeat: Server Sends Every=${serverIncomingHeartbeat}ms, Server Expects Every=${serverOutgoingHeartbeat}ms")

                // Start client outgoing PINGs if server expects them (sy > 0) AND client wants to send (CLIENT_OUTGOING_HEARTBEAT > 0)
                // We send based on the MAX of what server expects and what we want to send (but usually server value dictates)
                val clientPingInterval = max(serverOutgoingHeartbeat, CLIENT_OUTGOING_HEARTBEAT)
                if (clientPingInterval > 0) {
                    startOutgoingHeartbeat(clientPingInterval)
                }

                // Start checking for server PONGs if server sends them (sx > 0) AND client wants to check (CLIENT_INCOMING_HEARTBEAT > 0)
                // We check based on the MAX of what server sends and what we expect
                val serverPongInterval = max(serverIncomingHeartbeat, CLIENT_INCOMING_HEARTBEAT)
                if (serverPongInterval > 0) {
                    startIncomingHeartbeatCheck(serverPongInterval)
                }

            } else {
                Log.w("StompRepo", "Malformed heart-beat header received: $heartbeatHeader")
            }
        } catch (e: NumberFormatException) {
            Log.e("StompRepo", "Error parsing heart-beat header: $heartbeatHeader", e)
        }
    }

    private fun startOutgoingHeartbeat(intervalMs: Long) {
        if (intervalMs <= 0) return
        Log.i("StompRepo", "Starting Outgoing Heartbeat (Client PING) every $intervalMs ms")
        outgoingHeartbeatJob = repositoryScope.launch {
            while (isActive) { // Loop while the job is active
                delay(intervalMs)
                try {
                    // Check connection state before sending, although send should handle it
                    if (_connectionState.value == StompConnectionState.CONNECTED) {
                        Log.v("StompRepo", ">>> Sending Client Heartbeat (PING)")
                        if (!webSocketService.send("\n")) {
                            Log.w("StompRepo", "Failed to send client heartbeat (PING). Possible connection issue.")
                            // Consider triggering a disconnect check here if send fails repeatedly
                        }
                    } else {
                        Log.w("StompRepo", "Skipping PING send, not connected.")
                        break // Stop sending if not connected
                    }
                } catch (e: Exception) {
                    if (e is CancellationException) {
                        Log.d("StompRepo", "Outgoing heartbeat job cancelled.")
                        throw e // Re-throw cancellation
                    }
                    Log.e("StompRepo", "Error sending client heartbeat", e)
                    // Decide if this error is fatal and should stop the heartbeat or connection
                }
            }
            Log.d("StompRepo", "Outgoing heartbeat loop finished.")
        }
    }

    private fun startIncomingHeartbeatCheck(expectedIntervalMs: Long) {
        if (expectedIntervalMs <= 0) return
        // Check slightly more frequently than expected * 2 to catch timeout promptly
        val checkInterval = expectedIntervalMs // Check every interval
        val timeoutMillis = expectedIntervalMs * 2 // Timeout after 2 intervals without pong

        Log.i("StompRepo", "Starting Incoming Heartbeat Check. Expecting PONG within $timeoutMillis ms (Checking every $checkInterval ms)")
        lastServerPongTimestamp.set(System.currentTimeMillis()) // Reset timestamp when check starts

        incomingHeartbeatCheckJob = repositoryScope.launch {
            while (isActive) {
                delay(checkInterval)
                val lastPong = lastServerPongTimestamp.get()
                val now = System.currentTimeMillis()
                val timeSinceLastPong = now - lastPong

                if (timeSinceLastPong > timeoutMillis) {
                    Log.w("StompRepo", "!!! Server Heartbeat Timeout! No PONG received in $timeSinceLastPong ms (Threshold: $timeoutMillis ms). Disconnecting.")
                    handleDisconnect("Server heartbeat timeout") // Trigger disconnect
                    break // Stop the check loop
                } else {
                    Log.v("StompRepo", "Server heartbeat OK (Last PONG ${timeSinceLastPong}ms ago)")
                }
            }
            Log.d("StompRepo", "Incoming heartbeat check loop finished.")
        }
    }

    private fun stopHeartbeatTimers() {
        if (outgoingHeartbeatJob?.isActive == true) {
            Log.d("StompRepo", "Stopping Outgoing Heartbeat job.")
            outgoingHeartbeatJob?.cancel()
        }
        outgoingHeartbeatJob = null

        if (incomingHeartbeatCheckJob?.isActive == true) {
            Log.d("StompRepo", "Stopping Incoming Heartbeat Check job.")
            incomingHeartbeatCheckJob?.cancel()
        }
        incomingHeartbeatCheckJob = null

        serverOutgoingHeartbeat = 0L
        serverIncomingHeartbeat = 0L
        lastServerPongTimestamp.set(0L) // Reset timestamp
    }

    // --- Other Methods ---

    override fun send(destination: String, message: String): Boolean {
        if (_connectionState.value != StompConnectionState.CONNECTED) {
            Log.w("StompRepo", "Cannot send message, STOMP not connected.")
            return false
        }
        Log.d("StompRepo", "Sending message to /app/room/$destination: ${message.take(100)}...")
        val sendFrame = StompUtils.buildSendFrame("/app/room/$destination", message)
        Log.d("StompRepo", "Sending message to /app/room/$destination: $sendFrame")
        val success = webSocketService.send(sendFrame)
        if (!success) {
            Log.e("StompRepo", "Failed to send message frame via webSocketService to $destination")
            // Consider if send failure should trigger connection check/error state
        }
        return success
    }

    override fun getCurrentRoomId(): String? {
        return currentRoomId
    }

    override fun disconnect() {
        Log.d("StompRepo", "Disconnect requested by client.")
        // Don't send DISCONNECT frame if already disconnecting or disconnected
        if (_connectionState.value == StompConnectionState.CONNECTED) {
            val disconnectFrame = StompUtils.buildDisconnectFrame()
            webSocketService.send(disconnectFrame) // Attempt graceful disconnect
            Log.d("StompRepo", "Sent STOMP DISCONNECT frame.")
        }
        handleDisconnect("Client request")
    }

    private fun sendSubscriptionFrame(topic: String, subId: String) {
        if (_connectionState.value != StompConnectionState.CONNECTED) {
            Log.w("StompRepo", "Cannot subscribe, not connected.")
            return
        }
        val subscribeFrame = StompUtils.buildSubscribeFrame(topic, subId)
        val sent = webSocketService.send(subscribeFrame)
        if (sent) {
            Log.i("StompRepo", "Sent SUBSCRIBE frame for topic: $topic (ID: $subId)")
        } else {
            Log.e("StompRepo", "Failed to send SUBSCRIBE frame for topic: $topic")
            // This is critical, maybe treat as connection failure
            handleConnectionFailure(Exception("Failed to send SUBSCRIBE frame"))
        }
    }

    private fun handleConnectionFailure(error: Throwable) {
        // Avoid redundant handling if already disconnected
        if (_connectionState.value == StompConnectionState.DISCONNECTED || _connectionState.value == StompConnectionState.ERROR) return

        Log.e("StompRepo", "Handling Connection Failure: ${error.message}", error)
        stopHeartbeatTimers() // Stop heartbeats immediately on failure
        webSocketService.disconnect() // Ensure underlying socket is closed

        // Set ERROR state briefly, then transition to DISCONNECTED to allow reconnect attempts
        _connectionState.value = StompConnectionState.ERROR
        repositoryScope.launch {
            delay(500) // Brief ERROR state display
            // Only transition to DISCONNECTED if still in ERROR state
            if (_connectionState.value == StompConnectionState.ERROR) {
                _connectionState.value = StompConnectionState.DISCONNECTED
            }
        }

        // Clean up flow and state
        messageFlowJob?.cancel()
        messageFlow = null
        currentRoomId = null
        currentTopic = null
        currentSubscriptionId = null
        Log.i("StompRepo", "Connection failure handling complete. State: ${_connectionState.value}")
    }

    private fun handleDisconnect(reason: String) {
        if (_connectionState.value == StompConnectionState.DISCONNECTED) {
            Log.d("StompRepo", "Already disconnected. Ignoring disconnect request (Reason: $reason)")
            return
        }

        Log.i("StompRepo", "Handling disconnect. Reason: $reason. Current state: ${_connectionState.value}")

        stopHeartbeatTimers() // Stop heartbeats first
        webSocketService.disconnect() // Request socket closure

        // Clean up flow and state
        messageFlowJob?.cancel()
        messageFlow = null
        currentRoomId = null
        currentTopic = null
        currentSubscriptionId = null
        _connectionState.value = StompConnectionState.DISCONNECTED // Final state

        Log.i("StompRepo", "Disconnect handling complete. State is now DISCONNECTED.")
    }
}