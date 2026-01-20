package com.powervoice_oliveyoung.queue;


import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import jakarta.annotation.PostConstruct;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.List;


import com.powervoice_oliveyoung.config.ConfigInfo;
import com.powervoice_oliveyoung.dto.KafkaWorker;


@Component
@RequiredArgsConstructor
public class PartitionQueue {

    private final ConfigInfo configInfo;
    private List<BlockingQueue<KafkaWorker>> queues;

    private int partitionCount;


    @PostConstruct
    public void init(){
        this.partitionCount = configInfo.getPartitions();
        int capPerPartition = configInfo.getMessageQueueSize();

        this.queues = new java.util.ArrayList<>(partitionCount);
        for (int i = 0; i < partitionCount; i++) {
            queues.add(new LinkedBlockingQueue<>(capPerPartition));
        }
    }

    //파티션 수 반환
    public int partitionCount(){
        return this.partitionCount;
    }

    //특정 파티션의 큐에 작업 추가 (타임아웃 포함)
    public boolean offer(int partition, KafkaWorker task, long timeoutMs) throws InterruptedException {
        return queues.get(partition).offer(task, timeoutMs, TimeUnit.MILLISECONDS);
    }

    //특정 파티션의 큐에 작업 추가
    public void put(int partition, KafkaWorker task) throws InterruptedException {
        queues.get(partition).put(task);
    }

    //특정 파티션의 큐에서 작업 가져오기
    public KafkaWorker take(int partition) throws InterruptedException {
        return queues.get(partition).take();
    }

    //모든 파티션의 큐가 비어있는지 확인
    public boolean isEmptyAll() {
        for (BlockingQueue<KafkaWorker> queue : queues) {
            if (!queue.isEmpty()) {
                return false;
            }
        }
        return true;
    }



    



}
