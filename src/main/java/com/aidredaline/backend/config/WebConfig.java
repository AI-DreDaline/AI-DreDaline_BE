package com.aidredaline.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * TTS 파일 서빙 설정
     * /tts/** 경로로 접근 시 프로젝트 루트의 tts 폴더에서 파일 제공
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/tts/**")
                .addResourceLocations("file:./tts/")  // 프로젝트 루트의 tts 폴더
                .setCachePeriod(3600)  // 1시간 캐싱
                .resourceChain(true);
    }

    /**
     * CORS 설정 (Frontend 연동용)
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(

                        "http://localhost:3000",  // React 개발 서버
                        "http://localhost:5001"   // flask 개발 서버
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}