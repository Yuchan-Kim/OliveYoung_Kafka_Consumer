package com.powervoice_oliveyoung.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class MessageDto {
    private boolean endpoint;
    private String event;
    private int sequenceID;
    private String text;
    private CallInfo callInfo;

    @Data
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CallInfo {
        private String callID;
        private String channel;
        //private String ext; --> 추가 예정 여부 확인 필요
    }
}
