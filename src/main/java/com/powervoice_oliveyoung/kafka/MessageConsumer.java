package com.powervoice_oliveyoung.kafka;

import com.powervoice_oliveyoung.dto.KafkaWorker;
import com.powervoice_oliveyoung.queue.PartitionQueue;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageConsumer {

    private final PartitionQueue partitionQueue;


    @KafkaListener(topics = "${spring.kafka.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(ConsumerRecord<String,String> record, Acknowledgment ack){
        try{
            int partitionNumber = record.partition();
            KafkaWorker worker = KafkaWorker.of(partitionNumber, record.offset(), record.value(), ack);
            partitionQueue.offer(partitionNumber, worker,50);
        }catch(Exception e){
            log.error("[KafkaListener] Error processing message: {}", record.value(), e);
        }
    }

    


}
