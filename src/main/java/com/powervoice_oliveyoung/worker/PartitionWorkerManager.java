package com.powervoice_oliveyoung.worker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;

import com.powervoice_oliveyoung.config.ConfigInfo;
import com.powervoice_oliveyoung.dto.KafkaWorkerDto;
import com.powervoice_oliveyoung.dto.MessageDto;
import com.powervoice_oliveyoung.dto.RequestDto;
import com.powervoice_oliveyoung.kafka.FlowControlManager;
import com.powervoice_oliveyoung.kafka.ShutdownFlag;
import com.powervoice_oliveyoung.queue.PartitionQueue;
import com.powervoice_oliveyoung.service.MessageMapper;
import com.powervoice_oliveyoung.service.RestApiClient;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Slf4j
@Component
@RequiredArgsConstructor
public class PartitionWorkerManager {

    private final PartitionQueue partitionQueue;
    private final RestApiClient restApiClient;
    private final MessageMapper messageMapper;
    private final FlowControlManager flow;
    private final ConfigInfo configInfo;
    private final ShutdownFlag shutdownFlag;


    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AtomicInteger inProgress = new AtomicInteger(0);

    private ExecutorService executorService;

    @PostConstruct
    public void processStart() {
        int partitionCount = partitionQueue.partitionCount();
        executorService = Executors.newFixedThreadPool(partitionCount);

        for (int i = 0; i < partitionCount; i++){
            final int partitionNumber = i;
            executorService.submit(() -> {
                loop(partitionNumber);
            });
        }


    }

    private void loop(int partition) {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                KafkaWorkerDto task = partitionQueue.take(partition);

                if (task.isPoison()) {
                    break; // graceful exit
                }
                
                inProgress.incrementAndGet(); 

                try {
                    // 1) Kafka message -> MessageDto
                    MessageDto messageDto = objectMapper.readValue(task.getMessage(), MessageDto.class);

                    // 2) MessageDto -> RequestDto
                    RequestDto requestDto = messageMapper.toRequestDto(messageDto);

                    // 3) call REST API -> post
                    restApiClient.post(requestDto);

                    // 4) 성공 시 ack
                    task.getAck().acknowledge();

                    // 처리 후 큐가 충분히 줄었으면 resume
                    int total = partitionQueue.totalsize();
                    if (flow.isPaused() && total <= configInfo.getFlowControlLow()) {
                        flow.resumeAll("queue-drained total=" + total);
                    }

                } catch (Exception e) {
                    // 실패 시 ack 하지 않음 -> 재처리
                    log.error("[Worker] process fail. partition={}, offset={}", task.getPartition(), task.getOffset(), e);
                } finally {
                    inProgress.decrementAndGet();
                }

            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        log.info("[Worker] exit. partition={}", partition);
    }

    @PreDestroy
    public void shutdownGracefully() {
        log.warn("[Shutdown] start");

        // 1) 신규 유입 차단s
        shutdownFlag.rejectNewMessages();

        // 2) Kafka polling 중지(입구 닫기)
        flow.pauseAll("shutdown");

        // 3) drain: 큐 empty + inFlight=0 될 때까지 대기
        while (true) {
            int total = partitionQueue.totalsize();
            int processing = inProgress.get();

            log.warn("[Shutdown] draining... totalQueued={}, inProgress={}", total, processing);

            if (total == 0 && processing == 0) break;

            try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        }

        // ack commit 처리 여유를 아주 짧게 줌 (async-acks 쓰는 경우)
        try { Thread.sleep(200); } catch (InterruptedException ignored) {}

        

        // 4) 워커 종료 신호(블록 해제)
        try {
            partitionQueue.addPoisonToAll();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 5) executor 종료 대기
        if (executorService != null) {
            executorService.shutdown();
            while (!executorService.isTerminated()) {
                try { Thread.sleep(300); } catch (InterruptedException ignored) {}
            }
        }

        // 6) 이제 stop: 그룹 이탈/파티션 반납
        flow.stopAll("shutdown-finalize");

        log.warn("[Shutdown] complete");
    }

}
