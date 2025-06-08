package com.example.predictionservice.service;

import com.example.predictionservice.config.MLApiConfig;
import com.example.predictionservice.model.MLApiResponse;
import com.example.predictionservice.model.PredictionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
public class PredictionService {

    private static final Logger logger = LoggerFactory.getLogger(PredictionService.class);

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private MLApiConfig mlApiConfig;

    @Autowired
    private ImageService imageService;
    
    @Autowired
    private GifService gifService;

    // Мокованные base64 данные для обработанных изображений (заглушки)
    private static final String MOCK_IMAGE_BASE64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChAFIdni/6wAAAABJRU5ErkJggg==";
    private static final String MOCK_GIF_BASE64 = "R0lGODdhAQABAIAAAP///wAAACwAAAAAAQABAAACAkQBADs=";

    public PredictionResponse analyzeImage(MultipartFile file) throws IOException {
        try {
            logger.info("Отправляю запрос к ML API: {}", mlApiConfig.getMlApiUrl());
            
            // Подготавливаем multipart запрос для ML API
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            // Создаем multipart body
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            });

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // Отправляем запрос к ML API
            ResponseEntity<MLApiResponse> response = restTemplate.postForEntity(
                mlApiConfig.getMlApiUrl(),
                requestEntity,
                MLApiResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                logger.info("Получен успешный ответ от ML API");
                return convertToResponse(response.getBody(), file);
            } else {
                logger.error("Получен неуспешный ответ от ML API: {}", response.getStatusCode());
                throw new RuntimeException("ML API вернул неуспешный статус: " + response.getStatusCode());
            }

        } catch (RestClientException e) {
            logger.error("Ошибка при обращении к ML API: {}", e.getMessage(), e);
            throw new RuntimeException("Ошибка при обращении к ML API: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Неожиданная ошибка при анализе изображения: {}", e.getMessage(), e);
            throw new RuntimeException("Ошибка при анализе изображения: " + e.getMessage(), e);
        }
    }

    private PredictionResponse convertToResponse(MLApiResponse mlResponse, MultipartFile originalFile) {
        // Определяем категорию на основе damage_ratio
        int category = calculateCategory(mlResponse.getDamageRatio());
        
        // Проверяем наличие различных типов повреждений
        Map<String, List<MLApiResponse.Detection>> detections = mlResponse.getDetections();
        
        boolean hasDent = hasDetection(detections, "dent");
        boolean hasRust = hasDetection(detections, "rust");
        boolean hasScratch = hasDetection(detections, "scratch");
        boolean hasDeformation = hasDetection(detections, "deformation");
        
        // Рассчитываем процент уверенности на основе damage_ratio
        int percentage = (int) Math.round(mlResponse.getDamageRatio() * 100);
        
        // Генерируем обработанные изображения
        String processedImage = generateProcessedImage(mlResponse, originalFile);
        String resultGif = generateResultGif(mlResponse, originalFile, hasDent, hasRust, hasScratch, hasDeformation);

        logger.info("Конвертация ML ответа: category={}, dent={}, rust={}, scratch={}, deformation={}, percentage={}%",
                   category, hasDent, hasRust, hasScratch, hasDeformation, percentage);

        return new PredictionResponse(
            category,
            hasDent,
            hasRust,
            hasScratch,
            hasDeformation,
            processedImage,
            resultGif,
            percentage
        );
    }

    private int calculateCategory(double damageRatio) {
        // Категории повреждений на основе damage_ratio:
        // 1 - минимальные повреждения (0-0.2)
        // 2 - легкие повреждения (0.2-0.4)
        // 3 - умеренные повреждения (0.4-0.6)
        // 4 - серьезные повреждения (0.6-0.8)
        // 5 - критические повреждения (0.8-1.0)
        
        if (damageRatio <= 0.2) return 1;
        else if (damageRatio <= 0.4) return 2;
        else if (damageRatio <= 0.6) return 3;
        else if (damageRatio <= 0.8) return 4;
        else return 5;
    }

    private boolean hasDetection(Map<String, List<MLApiResponse.Detection>> detections, String type) {
        if (detections == null || !detections.containsKey(type)) {
            return false;
        }
        
        List<MLApiResponse.Detection> typeDetections = detections.get(type);
        return typeDetections != null && !typeDetections.isEmpty();
    }

    private String generateProcessedImage(MLApiResponse mlResponse, MultipartFile originalFile) {
        try {
            // Используем ImageService для рисования bounding boxes
            return imageService.drawBoundingBoxes(originalFile, mlResponse);
        } catch (IOException e) {
            logger.error("Ошибка при создании обработанного изображения: {}", e.getMessage(), e);
            // Возвращаем мок в случае ошибки
            return MOCK_IMAGE_BASE64;
        }
    }

    private String generateResultGif(MLApiResponse mlResponse, MultipartFile originalFile, 
                                   boolean hasDent, boolean hasRust, boolean hasScratch, boolean hasDeformation) {
        try {
            // Сначала пытаемся найти подходящую анимацию по параметрам повреждений
            List<String> damageParams = new ArrayList<>();
            
            if (hasRust) damageParams.add("rust");
            if (hasDent) damageParams.add("dent");
            if (hasScratch) damageParams.add("scratch");
            if (hasDeformation) damageParams.add("damage");
            
            logger.info("Поиск анимации для параметров: {}", damageParams);
            
            // Пытаемся найти подходящую анимацию
            byte[] animationData = gifService.findBestMatchingAnimation(damageParams);
            
            if (animationData != null) {
                // Конвертируем в base64
                String base64Animation = Base64.getEncoder().encodeToString(animationData);
                logger.info("Найдена подходящая анимация размером {} байт", animationData.length);
                return base64Animation;
            } else {
                logger.warn("Не найдена подходящая анимация, создаем GIF процесса анализа");
                // Если анимация не найдена, создаем GIF процесса анализа
                return gifService.createAnalysisGif(originalFile, mlResponse);
            }
            
        } catch (Exception e) {
            logger.error("Ошибка при создании GIF: {}", e.getMessage(), e);
            // Возвращаем мок в случае ошибки
            return MOCK_GIF_BASE64;
        }
    }

    public boolean isValidImageFormat(String contentType) {
        return contentType != null && (
            contentType.equals("image/png") ||
            contentType.equals("image/jpeg") ||
            contentType.equals("image/jpg")
        );
    }
} 