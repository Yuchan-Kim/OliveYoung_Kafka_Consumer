package com.powervoice_oliveyoung.service;

import com.powervoice_oliveyoung.config.ConfigInfo;
import com.powervoice_oliveyoung.dto.RequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class RestApiClient {
    private final WebClient webClient;
    private final ConfigInfo configInfo;

    public void post(RequestDto requestDto) {
        webClient.post()
                .uri(configInfo.getOliveRestApiUrl())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDto)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }





}
