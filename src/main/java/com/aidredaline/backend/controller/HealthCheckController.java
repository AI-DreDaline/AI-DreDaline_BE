package com.aidredaline.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "Health Check", description = "서버 상태 확인 API")
public class HealthCheckController {

    @GetMapping("/health")
    @Operation(
            summary = "서버 상태 확인",
            description = "서버가 정상적으로 작동하는지 확인합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "서버 정상 작동"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("timestamp", LocalDateTime.now());
        response.put("message", "AI-DreDaline API is running successfully! 🏃");
        response.put("version", "1.0.0");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/info")
    @Operation(
            summary = "API 정보",
            description = "API의 기본 정보를 반환합니다."
    )
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "AI-DreDaline Backend");
        response.put("description", "러닝 경로 생성 및 추적 API");
        response.put("version", "1.0.0");
        response.put("database", "PostgreSQL 16 with PostGIS");
        return ResponseEntity.ok(response);
    }
}