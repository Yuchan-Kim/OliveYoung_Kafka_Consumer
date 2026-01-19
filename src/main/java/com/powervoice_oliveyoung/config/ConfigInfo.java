package com.powervoice_oliveyoung.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class ConfigInfo {

    @Value("${oliveyoung.restApi.url}")
    private String oliveRestApiUrl;

}
