// server.js
const express = require('express');
const http = require('http');
const path = require('path');
const socketIO = require('socket.io');
const jwt = require('jsonwebtoken');

const app = express();
const server = http.createServer(app);

app.use(express.json());
app.use(express.static(path.join(__dirname, 'views'))); //나중에 삭제하기기

// Socket.IO 서버 초기화
const io = socketIO(server, {
  path: '/socket.io',
  cors: {
    //모바일도 CORS 설정이 필요한가요?
    origin: "*", // 실제 배포 시에는 허용 도메인 제한 필요
    methods: ["GET", "POST"]
  }
});

// 방 관리를 위한 Map
const rooms = new Map();

// 소켓ID와 사용자 정보를 위한 Map
// { userId, roomId } 형식으로 저장. roomId는 joinRoom 이벤트에서 갱신합니다.
const socketToUser = new Map();

/**
 * Placeholder DB 함수들
 */
async function getRoomFromDB(roomId) {
  console.log("DB에서 room 정보를 불러옵니다:", roomId);
  return {
    roomId,
    maxParticipants: 4,       // DB에 저장된 최대 참가자 수
    yogaCourse: 'default',    // 예시 yogaCourse 값
    participants: new Map()    // socketId -> userId 매핑
  };
}

async function updateRoomParticipantsInDB(roomId, participants) {
  console.log("DB에 참가자 정보 업데이트:", roomId, participants);
  return Promise.resolve();
}

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
 * roomId 정보는 joinRoom 이벤트에서 설정하므로 여기서는 null로 처리합니다.
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
    socketToUser.set(socket.id, { userId: payload.userId, roomId: null });
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

  // 방 생성 이벤트
  socket.on('createRoom', ({ roomId, maxParticipants }) => {
    try {
      if (rooms.has(roomId)) {
        socket.emit('error', { message: '이미 존재하는 방입니다.' });
        return;
      }
      rooms.set(roomId, {
        participants: new Map(), // socketId -> userId 매핑
        maxParticipants,
        readyUsers: new Set()
      });
      console.log(`방 ${roomId} 생성됨 (최대 인원: ${maxParticipants})`);
      socket.emit('roomCreated', { roomId });
    } catch (err) {
      console.error('방 생성 중 오류:', err);
      socket.emit('error', { message: '방 생성 중 오류가 발생했습니다.' });
    }
  });

  // 방 참가 이벤트 (클라이언트가 보내는 userId 대신 JWT의 userId 사용)
  socket.on('joinRoom', ({ roomId }) => {
    try {
      const room = rooms.get(roomId);
      if (!room) {
        socket.emit('error', { message: '존재하지 않는 방입니다.' });
        return;
      }
      if (room.participants.size >= room.maxParticipants) {
        socket.emit('error', { message: '방이 가득 찼습니다.' });
        return;
      }
      if (room.participants.has(socket.id)) {
        socket.emit('error', { message: '이미 방에 참가하였습니다.' });
        return;
      }
      // JWT에서 가져온 socket.userId 사용
      room.participants.set(socket.id, socket.userId);
      // joinRoom 이벤트 발생 시 roomId를 업데이트
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
      
      if (room.readyUsers.size === room.participants.size) {
        console.log(`모든 참가자 (${room.participants.size}) 준비 완료. 방: ${roomId}`);
        io.to(roomId).emit('allReady');
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

  // WebRTC 시그널링 이벤트
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
      socket.to(userInfo.roomId).emit('iceCandidate', {
        userId: userInfo.userId,
        candidate
      });
    } catch (err) {
      console.error('ICE 후보 교환 처리 중 오류:', err);
      socket.emit('error', { message: 'ICE 후보 교환 처리 중 오류가 발생했습니다.' });
    }
  });

  // 연결 해제 이벤트
  socket.on('disconnect', () => {
    try {
      const userInfo = socketToUser.get(socket.id);
      if (!userInfo) return;

      const { roomId, userId } = userInfo;
      const room = rooms.get(roomId);
      if (room) {
        room.participants.delete(socket.id);
        room.readyUsers.delete(userId);

        if (room.participants.size === 0) {
          rooms.delete(roomId);
          console.log(`방 ${roomId}가 비어 삭제되었습니다.`);
        } else {
          console.log(`사용자 ${userId}가 방 ${roomId}에서 나갔습니다. 남은 인원: ${room.participants.size}`);
          io.to(roomId).emit('userLeft', { 
            userId,
            participantCount: room.participants.size 
          });
        }
      }
      socketToUser.delete(socket.id);
    } catch (err) {
      console.error('연결 해제 처리 중 오류:', err);
    }
  });

  // 에러 처리
  socket.on('error', (error) => {
    console.error('Socket error:', error);
    // 클라이언트에 재연결 유도 메시지 전송
    socket.emit('reconnectRequired', { message: '연결에 문제가 발생했습니다.' });
  });

  // 연결 상태 모니터링 추가
  socket.on('ping', () => {
    socket.emit('pong');
  });
});

// 서버 시작
const PORT = process.env.PORT || 8005;
server.listen(PORT, () => {
  console.log(`서버가 포트 ${PORT}에서 실행 중입니다.`);
});
