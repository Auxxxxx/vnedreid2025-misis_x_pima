package com.example.predictionservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

@Schema(description = "Ответ от YOLO API для анализа повреждений автомобиля")
public class YoloResponse {

    @JsonProperty("inference_id")
    @Schema(description = "ID инференса", example = "66e3a5f8-cc6e-4ffc-9dc6-ffd65ad6682e")
    private String inferenceId;

    @JsonProperty("time")
    @Schema(description = "Время выполнения анализа в секундах", example = "0.016142783999384847")
    private Double time;

    @JsonProperty("image")
    @Schema(description = "Информация об изображении")
    private ImageInfo image;

    @JsonProperty("predictions")
    @Schema(description = "Предсказания по типам повреждений")
    private Map<String, DamagePrediction> predictions;

    @JsonProperty("predicted_classes")
    @Schema(description = "Список предсказанных классов")
    private List<String> predictedClasses;

    // Конструкторы
    public YoloResponse() {}

    public YoloResponse(String inferenceId, Double time, ImageInfo image, 
                           Map<String, DamagePrediction> predictions, List<String> predictedClasses) {
        this.inferenceId = inferenceId;
        this.time = time;
        this.image = image;
        this.predictions = predictions;
        this.predictedClasses = predictedClasses;
    }

    // Getters и Setters
    public String getInferenceId() { return inferenceId; }
    public void setInferenceId(String inferenceId) { this.inferenceId = inferenceId; }

    public Double getTime() { return time; }
    public void setTime(Double time) { this.time = time; }

    public ImageInfo getImage() { return image; }
    public void setImage(ImageInfo image) { this.image = image; }

    public Map<String, DamagePrediction> getPredictions() { return predictions; }
    public void setPredictions(Map<String, DamagePrediction> predictions) { this.predictions = predictions; }

    public List<String> getPredictedClasses() { return predictedClasses; }
    public void setPredictedClasses(List<String> predictedClasses) { this.predictedClasses = predictedClasses; }

    @Schema(description = "Информация об изображении")
    public static class ImageInfo {
        @JsonProperty("width")
        @Schema(description = "Ширина изображения", example = "675")
        private Integer width;

        @JsonProperty("height")
        @Schema(description = "Высота изображения", example = "1200")
        private Integer height;

        public ImageInfo() {}

        public ImageInfo(Integer width, Integer height) {
            this.width = width;
            this.height = height;
        }

        public Integer getWidth() { return width; }
        public void setWidth(Integer width) { this.width = width; }

        public Integer getHeight() { return height; }
        public void setHeight(Integer height) { this.height = height; }
    }

    @Schema(description = "Предсказание для типа повреждения")
    public static class DamagePrediction {
        @JsonProperty("confidence")
        @Schema(description = "Уверенность в предсказании (0-1)", example = "0.25527042150497437")
        private Double confidence;

        @JsonProperty("class_id")
        @Schema(description = "ID класса", example = "5")
        private Integer classId;

        public DamagePrediction() {}

        public DamagePrediction(Double confidence, Integer classId) {
            this.confidence = confidence;
            this.classId = classId;
        }

        public Double getConfidence() { return confidence; }
        public void setConfidence(Double confidence) { this.confidence = confidence; }

        public Integer getClassId() { return classId; }
        public void setClassId(Integer classId) { this.classId = classId; }
    }
} 