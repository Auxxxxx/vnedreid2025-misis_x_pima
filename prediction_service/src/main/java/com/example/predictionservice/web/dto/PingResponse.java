package com.example.predictionservice.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ping response")
public class PingResponse {
    
    @Schema(description = "Response message", example = "pong")
    private String message;
    
    @Schema(description = "Timestamp of response", example = "1640995200000")
    private long timestamp;
    
    @Schema(description = "Service status", example = "UP")
    private String status;

    public PingResponse() {
    }

    public PingResponse(String message, long timestamp, String status) {
        this.message = message;
        this.timestamp = timestamp;
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
} 