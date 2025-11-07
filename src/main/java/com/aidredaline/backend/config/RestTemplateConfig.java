package com.aidredaline.backend.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate 설정
 * HTTP 클라이언트 설정
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();

        //무한대기 방지를 위한 설정 - 연결 타임아웃: 10초
        factory.setConnectTimeout(10000);

        //무한대기 방지를 위한 설정 - 읽기 타임아웃: 30초
        factory.setReadTimeout(30000);

        return new RestTemplate(factory);
    }
}