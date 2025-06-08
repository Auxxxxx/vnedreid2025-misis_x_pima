package com.example.predictionservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public class MLApiResponse {
    
    @JsonProperty("car_bbox")
    private List<Double> carBbox;
    
    @JsonProperty("damage_ratio")
    private double damageRatio;
    
    private Map<String, List<Detection>> detections;

    // Конструкторы
    public MLApiResponse() {}

    public MLApiResponse(List<Double> carBbox, double damageRatio, Map<String, List<Detection>> detections) {
        this.carBbox = carBbox;
        this.damageRatio = damageRatio;
        this.detections = detections;
    }

    // Getters и Setters
    public List<Double> getCarBbox() { return carBbox; }
    public void setCarBbox(List<Double> carBbox) { this.carBbox = carBbox; }

    public double getDamageRatio() { return damageRatio; }
    public void setDamageRatio(double damageRatio) { this.damageRatio = damageRatio; }

    public Map<String, List<Detection>> getDetections() { return detections; }
    public void setDetections(Map<String, List<Detection>> detections) { this.detections = detections; }

    // Внутренний класс для детекций
    public static class Detection {
        private BoundingBox bbox;
        private double confidence;

        public Detection() {}

        public Detection(BoundingBox bbox, double confidence) {
            this.bbox = bbox;
            this.confidence = confidence;
        }

        public BoundingBox getBbox() { return bbox; }
        public void setBbox(BoundingBox bbox) { this.bbox = bbox; }

        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }
    }

    // Внутренний класс для bounding box
    public static class BoundingBox {
        @JsonProperty("x_min")
        private int xMin;
        
        @JsonProperty("y_min")
        private int yMin;
        
        @JsonProperty("x_max")
        private int xMax;
        
        @JsonProperty("y_max")
        private int yMax;
        
        private int width;
        private int height;

        public BoundingBox() {}

        public BoundingBox(int xMin, int yMin, int xMax, int yMax, int width, int height) {
            this.xMin = xMin;
            this.yMin = yMin;
            this.xMax = xMax;
            this.yMax = yMax;
            this.width = width;
            this.height = height;
        }

        // Getters и Setters
        public int getXMin() { return xMin; }
        public void setXMin(int xMin) { this.xMin = xMin; }

        public int getYMin() { return yMin; }
        public void setYMin(int yMin) { this.yMin = yMin; }

        public int getXMax() { return xMax; }
        public void setXMax(int xMax) { this.xMax = xMax; }

        public int getYMax() { return yMax; }
        public void setYMax(int yMax) { this.yMax = yMax; }

        public int getWidth() { return width; }
        public void setWidth(int width) { this.width = width; }

        public int getHeight() { return height; }
        public void setHeight(int height) { this.height = height; }
    }
} 