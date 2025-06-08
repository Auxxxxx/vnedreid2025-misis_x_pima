package com.example.predictionservice.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Результат анализа изображения")
public class PredictionResponse {

    @Schema(description = "Категория повреждения", example = "3", minimum = "1", maximum = "5")
    private int category;

    @Schema(description = "Наличие вмятины", example = "true")
    private boolean dent;

    @Schema(description = "Наличие ржавчины", example = "false")
    private boolean rust;

    @Schema(description = "Наличие царапины", example = "true")
    private boolean scratch;

    @Schema(description = "Наличие деформации", example = "false")
    private boolean deformation;

    @Schema(description = "Обработанное изображение в формате base64")
    private String res_photo;

    @Schema(description = "Анимированный GIF результата в формате base64")
    private String res_gif;

    @Schema(description = "Процент уверенности", example = "87", minimum = "0", maximum = "100")
    private int percentage;

    // Конструкторы
    public PredictionResponse() {}

    public PredictionResponse(int category, boolean dent, boolean rust, boolean scratch, 
                            boolean deformation, String res_photo, String res_gif, int percentage) {
        this.category = category;
        this.dent = dent;
        this.rust = rust;
        this.scratch = scratch;
        this.deformation = deformation;
        this.res_photo = res_photo;
        this.res_gif = res_gif;
        this.percentage = percentage;
    }

    // Getters и Setters
    public int getCategory() { return category; }
    public void setCategory(int category) { this.category = category; }

    public boolean isDent() { return dent; }
    public void setDent(boolean dent) { this.dent = dent; }

    public boolean isRust() { return rust; }
    public void setRust(boolean rust) { this.rust = rust; }

    public boolean isScratch() { return scratch; }
    public void setScratch(boolean scratch) { this.scratch = scratch; }

    public boolean isDeformation() { return deformation; }
    public void setDeformation(boolean deformation) { this.deformation = deformation; }

    public String getRes_photo() { return res_photo; }
    public void setRes_photo(String res_photo) { this.res_photo = res_photo; }

    public String getRes_gif() { return res_gif; }
    public void setRes_gif(String res_gif) { this.res_gif = res_gif; }

    public int getPercentage() { return percentage; }
    public void setPercentage(int percentage) { this.percentage = percentage; }
} 