package com.powervoice_oliveyoung.service;

import com.powervoice_oliveyoung.dto.MessageDto;
import com.powervoice_oliveyoung.dto.RequestDto;

public class MessageMapper {

    public RequestDto dataMapper(MessageDto messageDto) {
        return RequestDto.builder()
                .callId(messageDto.getCallInfo().getCallID())
                .event(messageDto.getEvent())
                .seq(messageDto.getSequenceID())
                .result(messageDto.getText())
                .txrx(messageDto.getCallInfo().getChannel())
                .build();
    }
}
