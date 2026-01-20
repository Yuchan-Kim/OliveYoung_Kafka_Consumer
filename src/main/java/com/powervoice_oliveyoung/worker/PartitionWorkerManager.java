package com.powervoice_oliveyoung.worker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.stereotype.Component;

import com.powervoice_oliveyoung.config.ConfigInfo;
import com.powervoice_oliveyoung.dto.KafkaWorkerDto;
import com.powervoice_oliveyoung.dto.MessageDto;
import com.powervoice_oliveyoung.dto.RequestDto;
import com.powervoice_oliveyoung.kafka.FlowControlManager;
import com.powervoice_oliveyoung.queue.PartitionQueue;
import com.powervoice_oliveyoung.service.MessageMapper;
import com.powervoice_oliveyoung.service.RestApiClient;

import jakarta.annotation.PostConstruct;

@Slf4j
@Component
@RequiredArgsConstructor
public class PartitionWorkerManager {

    private final PartitionQueue partitionQueue;
    private final RestApiClient restApiClient;
    private final MessageMapper messageMapper;
    private final FlowControlManager flow;
    private final ConfigInfo configInfo;

    private final ObjectMapper objectMapper = new ObjectMapper();

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
                }

            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        log.info("[Worker] exit. partition={}", partition);
    }

}
