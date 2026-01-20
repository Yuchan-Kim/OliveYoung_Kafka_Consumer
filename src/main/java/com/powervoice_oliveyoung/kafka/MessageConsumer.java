package com.powervoice_oliveyoung.kafka;

import com.powervoice_oliveyoung.config.ConfigInfo;
import com.powervoice_oliveyoung.dto.KafkaWorkerDto;
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
    private final FlowControlManager flow;
    private final ConfigInfo configInfo;


    @KafkaListener(topics = "${spring.kafka.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(ConsumerRecord<String,String> record, Acknowledgment ack){
        int partitionNumber = record.partition();
        long offset = record.offset();
        KafkaWorkerDto worker = KafkaWorkerDto.of(partitionNumber, record.offset(), record.value(), ack);
        
        try{
            boolean ok = partitionQueue.offer(partitionNumber, worker,50);

            if(!ok){
                int total = partitionQueue.totalsize();
                log.warn("[KafkaListener] Partition {} queue full. Current total queue size: {}", partitionNumber, partitionQueue.totalsize());

                if(total >= configInfo.getFlowControlHigh()){
                    flow.pauseAll("Queue-full total = " + total);
                }
                return; //ack 하지 않고 반환
            }
        }catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            return; // ack 안 함
        } catch (Exception e) {
            log.error("[KafkaListener] Error processing message: {}", e.getMessage());
            // ack 안 함
        }
    }

    


}
