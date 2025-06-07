package com.example.predictionservice.service;

import com.example.predictionservice.model.ServiceStatus;
import com.example.predictionservice.web.dto.PingResponse;
import org.springframework.stereotype.Service;

@Service
public class PingService {
    
    private static final String VERSION = "1.0.0";
    
    public PingResponse ping() {
        ServiceStatus status = getServiceStatus();
        return new PingResponse(
            "pong", 
            status.getTimestamp(), 
            status.getStatus()
        );
    }
    
    private ServiceStatus getServiceStatus() {
        return new ServiceStatus("UP", System.currentTimeMillis(), VERSION);
    }
} 