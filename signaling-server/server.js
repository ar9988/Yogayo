// server.js
const express = require('express');
const http = require('http');
const path = require('path');
const socketIO = require('socket.io');
const jwt = require('jsonwebtoken');

const app = express();
const server = http.createServer(app);

app.use(express.json());
app.use(express.static(path.join(__dirname, 'views'))); // 나중에 삭제할 예정

// Socket.IO 서버 초기화
const io = socketIO(server, {
  path: '/socket.io',
  cors: {
    // 네이티브 모바일 앱은 일반적으로 CORS 제약을 받지 않습니다.
    origin: "*", // 실제 배포 시에는 허용 도메인 제한 필요
    methods: ["GET", "POST"]
  }
});

// Ephemeral room 상태를 위한 메모리 저장소
// HTTP API로 이미 생성된 방은, signaling 서버에서는 해당 roomId로 소켓 룸을 생성하여 메시지 중계에 집중합니다.
const rooms = new Map();

// 소켓ID와 사용자 정보를 위한 Map
// { userId, roomId } 형식. roomId는 joinRoom 이벤트에서 업데이트됩니다.
const socketToUser = new Map();

// 재연결 유예를 위한 Map (userId -> { roomId, oldSocketId, timeout })
const pendingReconnections = new Map();
const RECONNECT_GRACE_PERIOD = 10000; // 10초

/**
 * JWT 검증 함수 (your_jwt_secret는 실제 비밀 키로 대체)
 */
function verifyJWT(token) {
  try {
    return jwt.verify(token, 'your_jwt_secret');
  } catch (err) {
    console.error("JWT 검증 실패:", err);
    return null;
  }
}

/**
 * Socket.IO 인증 미들웨어
 * 클라이언트는 handshake.auth.token에 JWT 토큰을 전달해야 하며,
 * 토큰 검증에 성공하면 socket.userId에 저장합니다.
 * 재연결 시 pendingReconnections에서 이전 상태를 복원합니다.
 */
io.use((socket, next) => {
  try {
    const token = socket.handshake.auth && socket.handshake.auth.token;
    if (!token) {
      return next(new Error('인증 토큰이 필요합니다.'));
    }
    const payload = verifyJWT(token);
    if (!payload) {
      return next(new Error('유효하지 않은 토큰입니다.'));
    }
    socket.userId = payload.userId;
    
    // 추가: 기존 활성 연결 체크 및 중복 연결 제거
    for (const [id, userInfo] of socketToUser.entries()) {
      if (userInfo.userId === socket.userId && id !== socket.id) {
        const existingSocket = io.sockets.sockets.get(id);
        if (existingSocket) {
          existingSocket.emit('duplicateConnection', { message: '새로운 연결이 확인되어 이전 연결을 종료합니다.' });
          existingSocket.disconnect();
          socketToUser.delete(id);
          console.log(`중복 연결 제거: 사용자 ${socket.userId}의 이전 소켓 ${id} 제거됨.`);
        }
      }
    }
    
    // 재연결 처리: pendingReconnections에 기록되어 있으면 재연결 로직 실행
    if (pendingReconnections.has(socket.userId)) {
      const pending = pendingReconnections.get(socket.userId);
      clearTimeout(pending.timeout);
      socketToUser.set(socket.id, { userId: socket.userId, roomId: pending.roomId });
      const room = rooms.get(pending.roomId);
      if (room) {
        room.participants.delete(pending.oldSocketId);
        room.participants.set(socket.id, socket.userId);
        console.log(`사용자 ${socket.userId} 재연결됨. 방: ${pending.roomId}`);
      }
      pendingReconnections.delete(socket.userId);
    } else {
      socketToUser.set(socket.id, { userId: socket.userId, roomId: null });
    }
    next();
  } catch (err) {
    console.error("인증 미들웨어 에러:", err);
    next(new Error('인증 처리 중 오류가 발생했습니다.'));
  }
});


/**
 * Socket.IO 이벤트 처리
 */
io.on('connection', (socket) => {
  console.log('새로운 클라이언트 접속!', socket.id);

  // 방 참가 이벤트
  socket.on('joinRoom', ({ roomId }) => {
    try {
      let room = rooms.get(roomId);
      // 만약 ephemeral room이 없다면 새로 생성 (DB에서 불러오는 대신 HTTP API로 생성된 방을 기준으로 함)
      if (!room) {
        room = {
          participants: new Map(), // socketId -> userId 매핑
          readyUsers: new Set(),
          courseStarted: false,    // 코스 시작 여부 플래그
          maxParticipants: Infinity  // 필요시 제한값 설정
        };
        rooms.set(roomId, room);
      }
      if (room.participants.has(socket.id)) {
        socket.emit('error', { message: '이미 방에 참가하였습니다.' });
        return;
      }
      room.participants.set(socket.id, socket.userId);
      // joinRoom 이벤트 시 roomId 업데이트
      socketToUser.set(socket.id, { userId: socket.userId, roomId });
      
      socket.join(roomId);
      console.log(`사용자 ${socket.userId}가 방 ${roomId}에 참가했습니다.`);
      io.to(roomId).emit('userJoined', { 
        userId: socket.userId,
        participantCount: room.participants.size 
      });
    } catch (err) {
      console.error('방 참가 중 오류:', err);
      socket.emit('error', { message: '방 참가 중 오류가 발생했습니다.' });
    }
  });

  // 준비 상태 처리 이벤트
  socket.on('ready', () => {
    try {
      const userInfo = socketToUser.get(socket.id);
      if (!userInfo || !userInfo.roomId) {
        socket.emit('error', { message: '방 정보가 없습니다.' });
        return;
      }
      const { roomId, userId } = userInfo;
      const room = rooms.get(roomId);
      if (!room) {
        socket.emit('error', { message: '존재하지 않는 방입니다.' });
        return;
      }
      room.readyUsers.add(userId);
      
      // 모든 참가자가 준비되면 courseStarted 플래그를 설정하고, 'allReady' 및 'courseStarted' 이벤트를 발송합니다.
      if (room.readyUsers.size === room.participants.size) {
        room.courseStarted = true;
        console.log(`모든 참가자 (${room.participants.size}) 준비 완료. 방: ${roomId}`);
        io.to(roomId).emit('allReady');
        io.to(roomId).emit('courseStarted'); // 클라이언트는 이 이벤트를 받고 이후 ICE 후보나 Heartbeat만 처리하도록 전환할 수 있음.
      } else {
        console.log(`사용자 ${userId}가 준비 완료. 현재 준비 인원: ${room.readyUsers.size}`);
        io.to(roomId).emit('userReady', { 
          userId,
          readyCount: room.readyUsers.size 
        });
      }
    } catch (err) {
      console.error('준비 상태 처리 중 오류:', err);
      socket.emit('error', { message: '준비 상태 처리 중 오류가 발생했습니다.' });
    }
  });

  // Heartbeat 이벤트 (코스 시작 후 연결 상태 모니터링용)
  socket.on('heartbeat', () => {
    try {
      const userInfo = socketToUser.get(socket.id);
      if (!userInfo || !userInfo.roomId) {
        return;
      }
      // 단순 heartbeat 신호를 룸 내 다른 참가자에게 전달합니다.
      socket.to(userInfo.roomId).emit('heartbeat', { userId: userInfo.userId });
    } catch (err) {
      console.error('Heartbeat 처리 중 오류:', err);
      socket.emit('error', { message: 'Heartbeat 처리 중 오류가 발생했습니다.' });
    }
  });

  // WebRTC 시그널링 이벤트 (코스 시작 전후 동일하게 ICE 후보 및 기타 signaling 메시지 중계)
  socket.on('signal', ({ signal }) => {
    try {
      const userInfo = socketToUser.get(socket.id);
      if (!userInfo || !userInfo.roomId) {
        socket.emit('error', { message: '방 정보가 없습니다.' });
        return;
      }
      socket.to(userInfo.roomId).emit('signal', {
        userId: userInfo.userId,
        signal
      });
    } catch (err) {
      console.error('시그널링 처리 중 오류:', err);
      socket.emit('error', { message: '시그널링 처리 중 오류가 발생했습니다.' });
    }
  });

  // ICE 후보 교환 이벤트
  socket.on('iceCandidate', ({ candidate }) => {
    try {
      const userInfo = socketToUser.get(socket.id);
      if (!userInfo || !userInfo.roomId) {
        socket.emit('error', { message: '방 정보가 없습니다.' });
        return;
      }
      // 코스가 시작된 상태에서는 ICE 후보 변화에 따라 연결 재설정을 위한 로직을 추가할 수 있습니다.
      // 예를 들어, 클라이언트 측에서 ICE 후보 변화가 감지되면 'iceCandidate' 이벤트를 받고, 재연결 절차를 시작할 수 있습니다.
      socket.to(userInfo.roomId).emit('iceCandidate', {
        userId: userInfo.userId,
        candidate
      });
    } catch (err) {
      console.error('ICE 후보 교환 처리 중 오류:', err);
      socket.emit('error', { message: 'ICE 후보 교환 처리 중 오류가 발생했습니다.' });
    }
  });

  // 연결 해제 이벤트: 재연결 유예 기간을 두어 상태 복원을 허용합니다.
  socket.on('disconnect', () => {
    try {
      const userInfo = socketToUser.get(socket.id);
      if (!userInfo) return;

      const { roomId, userId } = userInfo;
      const timeout = setTimeout(() => {
        const room = rooms.get(roomId);
        if (room) {
          room.participants.delete(socket.id);
          room.readyUsers.delete(userId);

          if (room.participants.size === 0) {
            rooms.delete(roomId);
            console.log(`방 ${roomId}가 비어 삭제되었습니다.`);
          } else {
            console.log(`사용자 ${userId}가 방 ${roomId}에서 영구적으로 나갔습니다. 남은 인원: ${room.participants.size}`);
            io.to(roomId).emit('userLeft', { 
              userId,
              participantCount: room.participants.size 
            });
          }
        }
        socketToUser.delete(socket.id);
        pendingReconnections.delete(userId);
      }, RECONNECT_GRACE_PERIOD);

      pendingReconnections.set(userId, { roomId, oldSocketId: socket.id, timeout });
      console.log(`사용자 ${userId}의 연결이 끊어졌습니다. ${RECONNECT_GRACE_PERIOD / 1000}초 내 재연결 대기 중...`);
    } catch (err) {
      console.error('연결 해제 처리 중 오류:', err);
    }
  });

  // 에러 처리
  socket.on('error', (error) => {
    console.error('Socket error:', error);
    // 재연결 유도 메시지 전송
    socket.emit('reconnectRequired', { message: '연결에 문제가 발생했습니다.' });
  });

  // 연결 상태 모니터링 (ping-pong)
  socket.on('ping', () => {
    socket.emit('pong');
  });
});

// 서버 시작
const PORT = process.env.PORT || 8005;
server.listen(PORT, () => {
  console.log(`서버가 포트 ${PORT}에서 실행 중입니다.`);
});
