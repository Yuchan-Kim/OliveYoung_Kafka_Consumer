package com.powervoice_oliveyoung.queue;

import com.powervoice_oliveyoung.config.ConfigInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessageQueue {

    private final ConfigInfo configInfo;

}
