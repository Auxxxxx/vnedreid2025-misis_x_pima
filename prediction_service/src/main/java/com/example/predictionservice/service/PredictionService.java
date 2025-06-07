package com.example.predictionservice.service;

import com.example.predictionservice.model.PredictionResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.Random;

@Service
public class PredictionService {

    private final Random random = new Random();

    // Мокованные base64 данные для примера (маленькое изображение 1x1 пиксель)
    private static final String MOCK_IMAGE_BASE64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChAFIdni/6wAAAABJRU5ErkJggg==";
    
    // Мокованный GIF (анимированный GIF 1x1 пиксель)
    private static final String MOCK_GIF_BASE64 = "R0lGODdhAQABAIAAAP///wAAACwAAAAAAQABAAACAkQBADs=";

    public PredictionResponse analyzeImage(MultipartFile file) {
        // Симуляция обработки
        try {
            Thread.sleep(500); // Имитация времени обработки
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Генерируем случайные результаты анализа
        int category = random.nextInt(5) + 1; // 1-5
        boolean dent = random.nextBoolean();
        boolean rust = random.nextBoolean();
        boolean scratch = random.nextBoolean();
        boolean deformation = random.nextBoolean();
        int percentage = random.nextInt(41) + 60; // 60-100%

        // Создаем мокованные результаты изображений
        String processedImage = generateMockProcessedImage();
        String resultGif = generateMockGif();

        return new PredictionResponse(
            category,
            dent,
            rust,
            scratch,
            deformation,
            processedImage,
            resultGif,
            percentage
        );
    }

    private String generateMockProcessedImage() {
        // В реальности здесь была бы обработка изображения
        // Для демо возвращаем мокованное изображение
        return MOCK_IMAGE_BASE64;
    }

    private String generateMockGif() {
        // В реальности здесь была бы генерация анимированного GIF
        // Для демо возвращаем мокованный GIF
        return MOCK_GIF_BASE64;
    }

    public boolean isValidImageFormat(String contentType) {
        return contentType != null && (
            contentType.equals("image/png") ||
            contentType.equals("image/jpeg") ||
            contentType.equals("image/jpg")
        );
    }
} 