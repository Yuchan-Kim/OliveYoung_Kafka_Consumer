package com.powervoice_oliveyoung.kafka;

import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.kafka.config.KafkaListenerEndpoint;
import org.springframework.kafka.config.KafkaListenerEndpointRegistrar;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class FlowControlManager {

    private final KafkaListenerEndpointRegistry registry;
    private final AtomicBoolean paused = new AtomicBoolean(false);

    //현재 일시정지 상태인지 확인
    public boolean isPaused() {
        return paused.get();
    }

    //모든 리스너 컨테이너 중지
    public void pauseAll(String reason) {
        if (!paused.compareAndSet(false,true)) {
            return;
        }

        int n = 0;
        for (MessageListenerContainer c : registry.getListenerContainers()){
            try{
                if(c.isRunning()) {
                    c.pause();
                    n++;
                }
            }catch (Exception e) {
                log.error("[FlowControlManager] Error pausing listener container: {}", c.getListenerId(), e);
            }
        }
        log.warn("[FlowControlManager] PAUSED all containers. count={}, reason={}", n, reason);
    }

    //모든 리스너 컨테이너 재개
    public void resumeAll(String reason) {
        if(!paused.compareAndSet(true,false)){
            return;
        }
        int n = 0;
        for (MessageListenerContainer c : registry.getListenerContainers()){
            try{
                if(c.isRunning()) {
                    c.resume();
                    n++;
                }
            }catch (Exception e) {
        log.warn("[FlowControlManager] RESUMED all containers. count={}, reason={}", n, reason);
            }
        }
    }
    
}
