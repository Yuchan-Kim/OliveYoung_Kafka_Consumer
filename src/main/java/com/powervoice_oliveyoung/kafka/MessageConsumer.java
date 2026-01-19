package com.powervoice_oliveyoung.kafka;

import com.powervoice_oliveyoung.config.ConfigInfo;
import com.powervoice_oliveyoung.dto.MessageDto;
import com.powervoice_oliveyoung.dto.RequestDto;
import com.powervoice_oliveyoung.service.MessageMapper;
import com.powervoice_oliveyoung.service.RestApiClient;
import lombok.RequiredArgsConstructor;
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


    @KafkaListener(topics = "${spring.kafka.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(String message, Acknowledgment ack){
        try{
            //1. Broker에서 Consume한 메세지 파싱
            log.debug("[KafkaListener] Consumed message: {}", message);
            MessageDto messageDto = objectMapper.readValue(message, MessageDto.class);

            //2. REST API 호출
            RequestDto requestDto = messageMapper.dataMapper(messageDto);
            restApiClient.post(requestDto);

            //3. Ack
            ack.acknowledge();

        }catch(Exception e){
            log.error("[KafkaListener] Error processing message: {}", message, e);
        }
    }

}
