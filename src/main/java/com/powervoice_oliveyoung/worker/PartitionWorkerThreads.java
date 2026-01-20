package com.powervoice_oliveyoung.worker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.stereotype.Component;

import com.powervoice_oliveyoung.dto.KafkaWorker;
import com.powervoice_oliveyoung.dto.MessageDto;
import com.powervoice_oliveyoung.dto.RequestDto;
import com.powervoice_oliveyoung.queue.PartitionQueue;
import com.powervoice_oliveyoung.service.MessageMapper;
import com.powervoice_oliveyoung.service.RestApiClient;

import jakarta.annotation.PostConstruct;

@Slf4j
@Component
@RequiredArgsConstructor
public class PartitionWorkerThreads {

    private final PartitionQueue partitionQueue;
    private final RestApiClient restApiClient;
    private final MessageMapper messageMapper;

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
                KafkaWorker task = partitionQueue.take(partition);

                try {
                    // 1) Kafka message -> MessageDto
                    MessageDto messageDto = objectMapper.readValue(task.getMessage(), MessageDto.class);

                    // 2) MessageDto -> RequestDto
                    RequestDto requestDto = messageMapper.toRequestDto(messageDto);

                    // 3) call REST API -> post
                    restApiClient.post(requestDto);

                    // 4) 성공 시 ack
                    task.getAck().acknowledge();

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
