package com.example.predictionservice.service;

import com.example.predictionservice.config.YoloConfig;
import com.example.predictionservice.model.YoloResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class YoloService {

    private static final Logger logger = LoggerFactory.getLogger(YoloService.class);
    private static final double CONFIDENCE_THRESHOLD = 0.25; // 25%

    private final YoloConfig yoloConfig;
    private final RestTemplate restTemplate;

    @Autowired
    public YoloService(YoloConfig yoloConfig, RestTemplate restTemplate) {
        this.yoloConfig = yoloConfig;
        this.restTemplate = restTemplate;
    }

    /**
     * Анализирует изображение автомобиля и возвращает состояние на русском языке
     */
    public String analyzeDamage(MultipartFile imageFile) throws IOException {
        logger.info("Начинаю анализ повреждений автомобиля через YOLO API");

        try {
            // Конвертируем изображение в base64
            byte[] imageBytes = imageFile.getBytes();
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            
            logger.info("Изображение конвертировано в base64, размер: {} байт", imageBytes.length);

            // Подготавливаем запрос к YOLO
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            // Создаем тело запроса (base64 строка)
            HttpEntity<String> requestEntity = new HttpEntity<>(base64Image, headers);

            // Формируем URL с API ключом
            String urlWithApiKey = yoloConfig.getApi().getUrl() + "?api_key=" + yoloConfig.getApi().getKey();
            
            logger.info("Отправляю запрос к YOLO API: {}", urlWithApiKey);

            // Отправляем POST запрос
            ResponseEntity<YoloResponse> response = restTemplate.postForEntity(
                urlWithApiKey,
                requestEntity,
                YoloResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                logger.info("Получен успешный ответ от YOLO API");
                return determineCarCondition(response.getBody());
            } else {
                logger.error("Получен неуспешный ответ от YOLO API: {}", response.getStatusCode());
                throw new RuntimeException("YOLO API вернул неуспешный статус: " + response.getStatusCode());
            }

        } catch (RestClientException e) {
            logger.error("Ошибка при обращении к YOLO API: {}", e.getMessage(), e);
            throw new RuntimeException("Ошибка при обращении к YOLO API: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Неожиданная ошибка при анализе повреждений: {}", e.getMessage(), e);
            throw new RuntimeException("Ошибка при анализе повреждений: " + e.getMessage(), e);
        }
    }

    /**
     * Определяет состояние автомобиля на основе анализа YOLO
     */
    private String determineCarCondition(YoloResponse yoloResponse) {
        logger.info("Определяю состояние автомобиля на основе ответа YOLO");
        logger.debug("Полученный ответ YOLO: inference_id={}, time={}", 
                    yoloResponse.getInferenceId(), yoloResponse.getTime());

        String mostLikelyDamage = null;
        Double maxConfidence = 0.0;

        // Находим повреждение с максимальной уверенностью
        if (yoloResponse.getPredictions() != null && !yoloResponse.getPredictions().isEmpty()) {
            logger.info("Анализирую {} предсказаний от YOLO", yoloResponse.getPredictions().size());
            
            int predictionCount = 0;
            for (Map.Entry<String, YoloResponse.DamagePrediction> entry : yoloResponse.getPredictions().entrySet()) {
                predictionCount++;
                String damageKey = entry.getKey();
                YoloResponse.DamagePrediction prediction = entry.getValue();
                
                logger.debug("Предсказание #{}: ключ='{}', prediction={}", predictionCount, damageKey, prediction);
                
                if (prediction != null && prediction.getConfidence() != null) {
                    Double confidence = prediction.getConfidence();
                    logger.info("Обнаружено повреждение '{}' с уверенностью {:.3f} ({}%)", 
                               damageKey, confidence, String.format("%.1f", confidence * 100));
                    
                    if (confidence > maxConfidence) {
                        logger.info("Новый максимум уверенности: {:.3f} -> {:.3f}, повреждение: '{}' -> '{}'", 
                                   maxConfidence, confidence, mostLikelyDamage, damageKey);
                        maxConfidence = confidence;
                        mostLikelyDamage = damageKey;
                    } else {
                        logger.debug("Уверенность {:.3f} меньше текущего максимума {:.3f}", confidence, maxConfidence);
                    }
                } else {
                    logger.warn("Предсказание '{}' имеет некорректные данные: prediction={}", damageKey, prediction);
                }
            }
            
            logger.info("Анализ завершен. Всего предсказаний: {}", predictionCount);
        } else {
            logger.warn("Предсказания отсутствуют или пусты в ответе YOLO");
        }

        logger.info("Итоговые результаты анализа:");
        logger.info("  - Максимальная уверенность: {:.3f} ({}%)", maxConfidence, String.format("%.1f", maxConfidence * 100));
        logger.info("  - Наиболее вероятное повреждение: '{}'", mostLikelyDamage);
        logger.info("  - Пороговое значение: {:.3f} ({}%)", CONFIDENCE_THRESHOLD, String.format("%.0f", CONFIDENCE_THRESHOLD * 100));

        // Применяем трешхолд 25%
        if (maxConfidence < CONFIDENCE_THRESHOLD) {
            logger.info("РЕШЕНИЕ: Уверенность {:.3f} ниже порога {:.3f} ({}%), возвращаю 'Машина в порядке'", 
                       maxConfidence, CONFIDENCE_THRESHOLD, String.format("%.0f", CONFIDENCE_THRESHOLD * 100));
            return "Машина в порядке";
        }

        logger.info("Уверенность выше порога, определяю конкретный тип повреждения...");

        // Определяем конкретный тип повреждения
        if (mostLikelyDamage != null) {
            logger.info("Анализирую тип повреждения: '{}'", mostLikelyDamage);
            
            String result = translateDamageType(mostLikelyDamage);
            logger.info("РЕШЕНИЕ: {} (тип: '{}', уверенность: {:.3f})", result, mostLikelyDamage, maxConfidence);
            return result;
        } else {
            logger.warn("Тип повреждения не определен (mostLikelyDamage=null)");
            return "Неопределенное повреждение";
        }
    }

    /**
     * Переводит тип повреждения из английского в русский с указанием конкретного типа
     */
    private String translateDamageType(String damageType) {
        if (damageType == null) {
            return "Неопределенное повреждение";
        }
        
        logger.debug("Перевожу тип повреждения: '{}'", damageType);
        
        // Парсим тип повреждения
        String[] parts = damageType.split("-");
        if (parts.length != 2) {
            logger.warn("Неожиданный формат типа повреждения: '{}', ожидался формат 'severity-type'", damageType);
            return "Неопределенное повреждение";
        }
        
        String severity = parts[0].toLowerCase();
        String type = parts[1].toLowerCase();
        
        logger.debug("Серьезность: '{}', тип: '{}'", severity, type);
        
        // Переводим серьезность
        String severityRu;
        switch (severity) {
            case "minor":
                severityRu = "Незначительная";
                break;
            case "moderate":
                severityRu = "Умеренная";
                break;
            case "severe":
                severityRu = "Серьезная";
                break;
            default:
                logger.warn("Неизвестная серьезность: '{}'", severity);
                severityRu = "Неопределенная";
                break;
        }
        
        // Переводим тип повреждения
        String typeRu;
        switch (type) {
            case "scratch":
                typeRu = "царапина";
                break;
            case "dent":
                typeRu = "вмятина";
                break;
            case "broken":
                typeRu = "поломка";
                break;
            default:
                logger.warn("Неизвестный тип повреждения: '{}'", type);
                typeRu = "повреждение";
                break;
        }
        
        String result = severityRu + " " + typeRu;
        logger.debug("Результат перевода: '{}'", result);
        return result;
    }

    /**
     * Получает информацию о возможностях API
     */
    public Map<String, Object> getApiInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("apiUrl", yoloConfig.getApi().getUrl());
        info.put("hasApiKey", yoloConfig.getApi().getKey() != null && !yoloConfig.getApi().getKey().isEmpty());
        info.put("confidenceThreshold", CONFIDENCE_THRESHOLD);
        info.put("supportedDamageTypes", new String[]{
            "minor-dent", "minor-scratch", 
            "moderate-broken", "moderate-dent", "moderate-scratch",
            "severe-broken", "severe-dent", "severe-scratch"
        });
        info.put("possibleResponses", new String[]{
            "Машина в порядке",
            "Незначительная царапина", "Незначительная вмятина",
            "Умеренная царапина", "Умеренная вмятина", "Умеренная поломка",
            "Серьезная царапина", "Серьезная вмятина", "Серьезная поломка"
        });
        return info;
    }
} 