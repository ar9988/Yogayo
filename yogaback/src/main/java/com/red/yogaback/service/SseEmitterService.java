package com.red.yogaback.service;

import com.red.yogaback.dto.request.RoomRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class SseEmitterService {


    private final Map<String, SseEmitter> emitterMap = new ConcurrentHashMap<>();
    private static final long TIMEOUT = 60 * 1000;
    private static final long RECONNECTION_TIMEOUT = 1000L;

    public SseEmitter subscribe(List<RoomRequest> allRooms) {
        SseEmitter emitter = createEmitter();
        String clientId = UUID.randomUUID().toString();

        emitter.onTimeout(()->{
            log.info("서버 타임 아웃 : id = {}", clientId);
            emitter.complete();
        });

        emitter.onError(e ->{
            log.info("SSE 서버 에러 발생 : id ={}, message ={}",clientId,e.getMessage());
            emitter.complete();
        });

        emitter.onCompletion(()->{
            if (emitterMap.remove(clientId) != null){
                log.info("SSE Emitter 캐시 삭제: id = {}",clientId);
            }
            log.info("SSE 연결 해제 완료: id = {}",clientId);
        });

        emitterMap.put(clientId, emitter);
        log.info("allRooms: {}",allRooms);
        try {
            emitter.send(SseEmitter.event()
                    .name("초기 방")
                    .data(allRooms)
                    .reconnectTime(RECONNECTION_TIMEOUT));
        } catch (IOException e){
            emitter.completeWithError(e);
        }

    return emitter;

    }

    private SseEmitter createEmitter() {
        return new SseEmitter(TIMEOUT);

    }

    public void notifyRoomUpdate(List<RoomRequest> allRooms){
        if (emitterMap.isEmpty()){
            return;
        }
        List<String> deadEmitters = new ArrayList<>();

        emitterMap.forEach((clientId,emitter)->{
            try {
                emitter.send(SseEmitter.event()
                        .name("방 업데이트")
                        .data(allRooms)
                        .reconnectTime(RECONNECTION_TIMEOUT));
            } catch (IOException e){
                deadEmitters.add(clientId);
            }
        });
        deadEmitters.forEach(emitterMap::remove);
    }

}
