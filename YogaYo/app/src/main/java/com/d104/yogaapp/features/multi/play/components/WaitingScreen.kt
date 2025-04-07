package com.d104.yogaapp.features.multi.play.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.d104.domain.model.PeerUser

@Composable
fun WaitingScreen(
    myId: String?, // Pass the current user's ID
    userList: Map<String, PeerUser>,
    onReadyClick: () -> Unit // Callback for the button click
) {
    // Use Column as the main container to place the button at the bottom
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // This Column holds the title and the list, taking available space
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .weight(1f) // Takes up all available vertical space, pushing the button down
                .fillMaxWidth() // Ensure this column takes full width
                .padding(horizontal = 16.dp) // Apply horizontal padding here
                .padding(top = 16.dp) // Apply top padding
        ) {
            Text(
                text = "참여자 목록",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp) // Adjusted padding
            )
            LazyColumn(
                modifier = Modifier.fillMaxWidth() // Ensure LazyColumn takes full width
            ) {
                items(userList.keys.toList(), key = { it }) { userId -> // Added key for stability
                    val user = userList[userId]
                    if (user != null) { // Safety check
                        Row(
                            modifier = Modifier
                                .fillMaxWidth() // Ensure row takes full width
                                .padding(vertical = 8.dp), // Added vertical padding
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 원형 테두리가 있는 사용자 아이콘
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(40.dp)
                                    .border(1.dp, Color.Gray, CircleShape)
                                    .clip(CircleShape)
                            ) {
                                Icon(
                                    Icons.Filled.Person,
                                    contentDescription = "Person Icon",
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            // 텍스트 (자신일 경우 "(나)" 추가)
                            val displayName = if (userId == myId) {
                                "${user.nickName} (나)"
                            } else {
                                user.nickName
                            }
                            Text(
                                text = displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 12.dp) // Increased padding
                            )

                            // 체크 아이콘
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(40.dp)
                                    .border(
                                        1.dp,
                                        if (user.isReady) Color.Green else Color.Gray,
                                        CircleShape
                                    )
                                    .clip(CircleShape)
                            ) {
                                Icon(
                                    imageVector = if (user.isReady) Icons.Filled.Check else Icons.Outlined.Check,
                                    contentDescription = if (user.isReady) "Checked" else "Unchecked",
                                    tint = if (user.isReady) Color.Green else Color.Gray,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        } // End of user list Column

        // --- Button Section ---
        // Calculate button visibility and text
        val currentUser = if (myId != null) userList[myId] else null
        val showButton = currentUser != null // Show button only if myId is valid and user is in the list
        val buttonText = if (currentUser?.isReady == true) "준비 취소" else "준비하기"

        // Reserve space for the button to prevent layout jumps when it appears/disappears
        val buttonHeight = 72.dp // Approximate height including padding (Button default min ~40dp + vertical padding 16*2)

        if (showButton) {
            Button(
                onClick = onReadyClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp) // Add padding around the button
                    .height(buttonHeight - 32.dp) // Set button height considering padding
            ) {
                Text(buttonText)
            }
        } else {
            // If button is not shown, add a Spacer to maintain layout consistency
            Spacer(modifier = Modifier.height(buttonHeight))
        }
        // --- End of Button Section ---

    } // End of main Column
}