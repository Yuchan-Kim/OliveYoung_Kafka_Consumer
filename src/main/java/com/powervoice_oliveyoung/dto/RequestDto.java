package com.powervoice_oliveyoung.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RequestDto {

    private String callId;

    private boolean endpoint;

    private String event;

    private int seq;

    private String result;

    private String txrx;
}
