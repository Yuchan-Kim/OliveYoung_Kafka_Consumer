package com.powervoice_oliveyoung.dto;

import org.springframework.kafka.support.Acknowledgment;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class KafkaWorkerDto {
    private final int partition;
    private final long offset;
    private final String message;
    private final Acknowledgment ack;
    
    public static KafkaWorkerDto of(int partition, long offset, String message, Acknowledgment ack) {
        return new KafkaWorkerDto(partition, offset, message, ack);
    }
}
