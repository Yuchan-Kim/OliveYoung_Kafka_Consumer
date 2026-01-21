package com.powervoice_oliveyoung.kafka;

import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.stereotype.Component;

@Component 
public class ShutdownFlag {
    private final AtomicBoolean shutdown = new AtomicBoolean(false);

    public boolean isRejecting(){
        return shutdown.get();
    }

    public void rejectNewMessages(){
        shutdown.set(true);
    }
    
}
