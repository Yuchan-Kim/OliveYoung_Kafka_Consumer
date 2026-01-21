package com.powervoice_oliveyoung.controller;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/test")
public class TestController {
    private final AtomicLong count = new AtomicLong(0);

    @PostMapping("/receive")
    public ResponseEntity<Void> receive(@RequestBody String body) {
        long n = count.incrementAndGet();
        log.warn("[TEST-REST] received count={}, body={}", n, body);
        return ResponseEntity.ok().build();
    }
    
}
