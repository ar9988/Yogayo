package com.red.yogaback.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class SseEmitterService {
    private final Map<String, SseEmitter> emitterMap = new ConcurrentHashMap<>();
    private static final long TIMEOUT = 60 * 1000;
    private static final long RECONNECTION_TIMEOUT = 1000L;

    public SseEmitter subscribe(String id) {
        SseEmitter emitter = createEmitter();

        emitter.onTimeout(()->{
            log.info("서버 타임 아웃 : id = {}", id);
            emitter.complete();
        });

        emitter.onError(e ->{
            log.info("SSE 서버 에러 발생 : id ={}, message ={}",id,e.getMessage());
            emitter.complete();
        });

        emitter.onCompletion(()->{
            if (emitterMap.remove(id) != null){
                log.info("SSE Emitter 캐시 삭제: id = {}",id);
            }
            log.info("SSE 연결 해제 완료: id = {}",id);
        });

        emitterMap.put(id, emitter);

        return emitter;
    }

    private SseEmitter createEmitter() {
        return new SseEmitter(TIMEOUT);

    }

}
