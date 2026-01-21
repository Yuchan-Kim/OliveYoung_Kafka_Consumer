package com.powervoice_oliveyoung.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RequestDto {

    private String CALLID;

    private boolean ENDPOINT;

    private String EVENT;

    private int SEQ;

    private String RESULT;

    private String TXRX;
}
