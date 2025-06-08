package com.example.predictionservice.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

@Schema(description = "Результат анализа повреждений автомобиля")
public class CarDamageAnalysisResponse {

    @Schema(description = "ID анализа", example = "66e3a5f8-cc6e-4ffc-9dc6-ffd65ad6682e")
    private String analysisId;

    @Schema(description = "Время выполнения анализа в секундах", example = "0.016")
    private Double processingTime;

    @Schema(description = "Ширина изображения", example = "675")
    private Integer imageWidth;

    @Schema(description = "Высота изображения", example = "1200")
    private Integer imageHeight;

    @Schema(description = "Детали повреждений с уровнями уверенности")
    private Map<String, DamageDetails> damageAnalysis;

    @Schema(description = "Наиболее вероятный тип повреждения", example = "severe-broken")
    private String mostLikelyDamage;

    @Schema(description = "Максимальная уверенность", example = "0.255")
    private Double maxConfidence;

    @Schema(description = "Общая оценка состояния автомобиля", example = "SEVERE_DAMAGE")
    private String overallCondition;

    // Конструкторы
    public CarDamageAnalysisResponse() {}

    public CarDamageAnalysisResponse(String analysisId, Double processingTime, Integer imageWidth, Integer imageHeight,
                                   Map<String, DamageDetails> damageAnalysis, String mostLikelyDamage, 
                                   Double maxConfidence, String overallCondition) {
        this.analysisId = analysisId;
        this.processingTime = processingTime;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.damageAnalysis = damageAnalysis;
        this.mostLikelyDamage = mostLikelyDamage;
        this.maxConfidence = maxConfidence;
        this.overallCondition = overallCondition;
    }

    // Getters и Setters
    public String getAnalysisId() { return analysisId; }
    public void setAnalysisId(String analysisId) { this.analysisId = analysisId; }

    public Double getProcessingTime() { return processingTime; }
    public void setProcessingTime(Double processingTime) { this.processingTime = processingTime; }

    public Integer getImageWidth() { return imageWidth; }
    public void setImageWidth(Integer imageWidth) { this.imageWidth = imageWidth; }

    public Integer getImageHeight() { return imageHeight; }
    public void setImageHeight(Integer imageHeight) { this.imageHeight = imageHeight; }

    public Map<String, DamageDetails> getDamageAnalysis() { return damageAnalysis; }
    public void setDamageAnalysis(Map<String, DamageDetails> damageAnalysis) { this.damageAnalysis = damageAnalysis; }

    public String getMostLikelyDamage() { return mostLikelyDamage; }
    public void setMostLikelyDamage(String mostLikelyDamage) { this.mostLikelyDamage = mostLikelyDamage; }

    public Double getMaxConfidence() { return maxConfidence; }
    public void setMaxConfidence(Double maxConfidence) { this.maxConfidence = maxConfidence; }

    public String getOverallCondition() { return overallCondition; }
    public void setOverallCondition(String overallCondition) { this.overallCondition = overallCondition; }

    @Schema(description = "Детали повреждения")
    public static class DamageDetails {
        
        @Schema(description = "Уверенность в предсказании (0-1)", example = "0.255")
        private Double confidence;

        @Schema(description = "ID класса", example = "5")
        private Integer classId;

        @Schema(description = "Категория серьезности", example = "SEVERE")
        private String severity;

        @Schema(description = "Тип повреждения", example = "BROKEN")
        private String damageType;

        @Schema(description = "Процент уверенности", example = "25.5%")
        private String confidencePercentage;

        public DamageDetails() {}

        public DamageDetails(Double confidence, Integer classId, String severity, String damageType) {
            this.confidence = confidence;
            this.classId = classId;
            this.severity = severity;
            this.damageType = damageType;
            this.confidencePercentage = String.format("%.1f%%", confidence * 100);
        }

        public Double getConfidence() { return confidence; }
        public void setConfidence(Double confidence) { 
            this.confidence = confidence;
            this.confidencePercentage = String.format("%.1f%%", confidence * 100);
        }

        public Integer getClassId() { return classId; }
        public void setClassId(Integer classId) { this.classId = classId; }

        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }

        public String getDamageType() { return damageType; }
        public void setDamageType(String damageType) { this.damageType = damageType; }

        public String getConfidencePercentage() { return confidencePercentage; }
        public void setConfidencePercentage(String confidencePercentage) { this.confidencePercentage = confidencePercentage; }
    }
} 