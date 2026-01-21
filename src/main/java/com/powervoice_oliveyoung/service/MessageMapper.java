package com.powervoice_oliveyoung.service;

import org.springframework.stereotype.Service;

import com.powervoice_oliveyoung.dto.MessageDto;
import com.powervoice_oliveyoung.dto.RequestDto;

@Service
public class MessageMapper {

    public RequestDto toRequestDto(MessageDto messageDto) {
        return RequestDto.builder()
                .CALLID(messageDto.getCallInfo().getCallID())
                .EVENT(messageDto.getEvent())
                .SEQ(messageDto.getSequenceID())
                .RESULT(messageDto.getText())
                .TXRX(messageDto.getCallInfo().getChannel())
                .build();
    }
}
