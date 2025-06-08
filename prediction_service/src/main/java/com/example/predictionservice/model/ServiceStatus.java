package com.example.predictionservice.model;

public class ServiceStatus {
    
    private String status;
    private long timestamp;
    private String version;

    public ServiceStatus() {
    }

    public ServiceStatus(String status, long timestamp, String version) {
        this.status = status;
        this.timestamp = timestamp;
        this.version = version;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
} 