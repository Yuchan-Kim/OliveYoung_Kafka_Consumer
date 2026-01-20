package com.powervoice_oliveyoung.kafka;

import com.powervoice_oliveyoung.config.ConfigInfo;
import com.powervoice_oliveyoung.dto.KafkaWorker;
import com.powervoice_oliveyoung.dto.MessageDto;
import com.powervoice_oliveyoung.dto.RequestDto;
import com.powervoice_oliveyoung.queue.PartitionQueue;
import com.powervoice_oliveyoung.service.MessageMapper;
import com.powervoice_oliveyoung.service.RestApiClient;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageConsumer {

    private final ConfigInfo configInfo;
    private final RestApiClient restApiClient;
    private final MessageMapper messageMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PartitionQueue partitionQueue;


    @KafkaListener(topics = "${spring.kafka.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(ConsumerRecord<String,String> record, Acknowledgment ack){
        try{
            int partitionNumber = record.partition();
            KafkaWorker worker = KafkaWorker.of(partitionNumber, record.offset(), record.value(), ack);
            partitionQueue.put(partitionNumber, worker);
        }catch(Exception e){
            log.error("[KafkaListener] Error processing message: {}", record.value(), e);
        }
    }

    


}
