package com.example.predictionservice.web.controller;

import com.example.predictionservice.service.PingService;
import com.example.predictionservice.web.dto.PingResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "Ping", description = "Ping API for service health check")
public class PingController {

    private final PingService pingService;

    @Autowired
    public PingController(PingService pingService) {
        this.pingService = pingService;
    }

    @GetMapping("/ping")
    @Operation(summary = "Ping endpoint", description = "Check if service is running")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Service is running"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<PingResponse> ping() {
        PingResponse response = pingService.ping();
        return ResponseEntity.ok(response);
    }
} 