package com.example.predictionservice.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Координаты bounding box для рисования")
public class BoundingBoxRequest {
    
    @Schema(description = "X координата левого верхнего угла", example = "100")
    private int x;
    
    @Schema(description = "Y координата левого верхнего угла", example = "50")
    private int y;
    
    @Schema(description = "Ширина бокса", example = "200")
    private int width;
    
    @Schema(description = "Высота бокса", example = "150")
    private int height;
    
    @Schema(description = "Цвет бокса в hex формате", example = "#FF0000", defaultValue = "#00FF00")
    private String color = "#00FF00";
    
    @Schema(description = "Подпись для бокса", example = "Test Box")
    private String label;
    
    @Schema(description = "Толщина линии", example = "3", defaultValue = "2")
    private int strokeWidth = 2;

    // Конструкторы
    public BoundingBoxRequest() {}

    public BoundingBoxRequest(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public BoundingBoxRequest(int x, int y, int width, int height, String color, String label) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.color = color;
        this.label = label;
    }

    // Getters и Setters
    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getStrokeWidth() {
        return strokeWidth;
    }

    public void setStrokeWidth(int strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    @Override
    public String toString() {
        return "BoundingBoxRequest{" +
                "x=" + x +
                ", y=" + y +
                ", width=" + width +
                ", height=" + height +
                ", color='" + color + '\'' +
                ", label='" + label + '\'' +
                ", strokeWidth=" + strokeWidth +
                '}';
    }
} 