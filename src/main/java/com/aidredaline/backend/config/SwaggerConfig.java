package com.aidredaline.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AI-DreDaline API")
                        .description("러닝 경로 생성 및 추적 API\n\n" +
                                "### 주요 기능\n" +
                                "- 템플릿 기반 경로 생성\n" +
                                "- 실시간 GPS 추적\n" +
                                "- 러닝 기록 분석 및 통계")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("AI-DreDaline Team")
                                .url("https://github.com/AI-DreDaline/AI-DreDaline_BE")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development Server"),
                        new Server()
                                .url("https://api.aidredaline.com")
                                .description("Production Server (준비 중)")
                ));
    }
}