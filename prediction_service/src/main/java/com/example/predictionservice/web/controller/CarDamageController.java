package com.example.predictionservice.web.controller;

import com.example.predictionservice.service.YoloService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/car-damage")
@Tag(name = "Car Damage Analysis", description = "AI-powered car damage detection and analysis using YOLO")
public class CarDamageController {

    private static final Logger logger = LoggerFactory.getLogger(CarDamageController.class);

    @Autowired
    private YoloService yoloService;

    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Анализировать повреждения автомобиля", 
        description = "Загружает изображение автомобиля и возвращает конкретный тип повреждения на русском языке. " +
                     "Возможные ответы: 'Машина в порядке' (если уверенность < 25%), 'Незначительная царапина', " +
                     "'Незначительная вмятина', 'Умеренная царапина', 'Умеренная вмятина', 'Умеренная поломка', " +
                     "'Серьезная царапина', 'Серьезная вмятина', 'Серьезная поломка'"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Анализ успешно выполнен. Возвращает конкретный тип повреждения: царапина, вмятина или поломка с указанием серьезности",
            content = @Content(
                mediaType = "text/plain",
                schema = @Schema(type = "string", example = "Серьезная поломка")
            )
        ),
        @ApiResponse(responseCode = "400", description = "Неверный запрос или недопустимый файл"),
        @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера или ошибка ML API")
    })
    public ResponseEntity<String> analyzeDamage(
        @Parameter(
            description = "Изображение автомобиля для анализа (JPG, PNG, поддерживаемые форматы)", 
            required = true
        )
        @RequestParam("file") MultipartFile imageFile
    ) {
        logger.info("Получен запрос на анализ повреждений автомобиля");

        try {
            // Валидация файла
            if (imageFile == null || imageFile.isEmpty()) {
                logger.warn("Получен пустой файл изображения");
                return ResponseEntity.badRequest()
                    .body("Файл изображения не может быть пустым");
            }

            // Проверка типа файла
            String contentType = imageFile.getContentType();
            if (contentType == null || (!contentType.startsWith("image/"))) {
                logger.warn("Получен файл неподдерживаемого типа: {}", contentType);
                return ResponseEntity.badRequest()
                    .body("Поддерживаются только изображения (JPG, PNG, etc.)");
            }

            // Проверка размера файла (максимум 10MB)
            long maxSize = 10 * 1024 * 1024; // 10MB
            if (imageFile.getSize() > maxSize) {
                logger.warn("Файл слишком большой: {} байт (максимум {} байт)", 
                           imageFile.getSize(), maxSize);
                return ResponseEntity.badRequest()
                    .body("Размер файла не должен превышать 10MB");
            }

            logger.info("Обрабатываю файл: {} (размер: {} байт, тип: {})", 
                       imageFile.getOriginalFilename(), imageFile.getSize(), contentType);

            // Выполняем анализ через YOLO
            String result = yoloService.analyzeDamage(imageFile);
            
            logger.info("Анализ повреждений успешно завершен для файла: {}, результат: {}", 
                       imageFile.getOriginalFilename(), result);
            
            return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body(result);

        } catch (IOException e) {
            logger.error("Ошибка при чтении файла изображения: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Ошибка при обработке изображения: " + e.getMessage());
        } catch (RuntimeException e) {
            logger.error("Ошибка при анализе повреждений: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Ошибка при анализе: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Неожиданная ошибка при анализе повреждений: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Внутренняя ошибка сервера");
        }
    }

    @GetMapping("/info")
    @Operation(
        summary = "Информация об API анализа повреждений", 
        description = "Возвращает информацию о возможностях API и поддерживаемых типах повреждений"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Информация получена успешно"),
        @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    public ResponseEntity<Map<String, Object>> getApiInfo() {
        logger.info("Запрос информации об API анализа повреждений");
        
        try {
            Map<String, Object> apiInfo = yoloService.getApiInfo();
            
            // Добавляем дополнительную информацию
            apiInfo.put("version", "1.0.0");
            apiInfo.put("description", "AI-powered car damage detection using YOLO");
            apiInfo.put("maxFileSize", "10MB");
            apiInfo.put("supportedFormats", new String[]{"JPG", "JPEG", "PNG", "BMP", "TIFF"});
            
            return ResponseEntity.ok(apiInfo);
            
        } catch (Exception e) {
            logger.error("Ошибка при получении информации об API: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Ошибка при получении информации об API"));
        }
    }

    @GetMapping("/damage-types")
    @Operation(
        summary = "Поддерживаемые типы повреждений", 
        description = "Возвращает список всех типов повреждений, которые может определить система"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Список типов повреждений получен успешно"),
        @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    public ResponseEntity<Map<String, Object>> getDamageTypes() {
        logger.info("Запрос списка поддерживаемых типов повреждений");
        
        try {
            Map<String, Object> damageTypes = new HashMap<>();
            
            Map<String, String> minorDamages = new HashMap<>();
            minorDamages.put("minor-dent", "Незначительная вмятина");
            minorDamages.put("minor-scratch", "Незначительная царапина");
            
            Map<String, String> moderateDamages = new HashMap<>();
            moderateDamages.put("moderate-broken", "Умеренное разрушение");
            moderateDamages.put("moderate-dent", "Умеренная вмятина");
            moderateDamages.put("moderate-scratch", "Умеренная царапина");
            
            Map<String, String> severeDamages = new HashMap<>();
            severeDamages.put("severe-broken", "Серьезное разрушение");
            severeDamages.put("severe-dent", "Серьезная вмятина");
            severeDamages.put("severe-scratch", "Серьезная царапина");
            
            damageTypes.put("minor", minorDamages);
            damageTypes.put("moderate", moderateDamages);
            damageTypes.put("severe", severeDamages);
            
            Map<String, Object> response = new HashMap<>();
            response.put("damageTypes", damageTypes);
            response.put("totalTypes", minorDamages.size() + moderateDamages.size() + severeDamages.size());
            response.put("severityLevels", new String[]{"minor", "moderate", "severe"});
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Ошибка при получении типов повреждений: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Ошибка при получении типов повреждений"));
        }
    }

    @GetMapping("/possible-responses")
    @Operation(
        summary = "Возможные ответы анализа", 
        description = "Возвращает все возможные ответы, которые может вернуть система анализа повреждений"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Список возможных ответов получен успешно"),
        @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    public ResponseEntity<Map<String, Object>> getPossibleResponses() {
        logger.info("Запрос списка возможных ответов анализа");
        
        try {
            Map<String, Object> response = new HashMap<>();
            
            // Получаем информацию от сервиса
            Map<String, Object> apiInfo = yoloService.getApiInfo();
            String[] possibleResponses = (String[]) apiInfo.get("possibleResponses");
            Double confidenceThreshold = (Double) apiInfo.get("confidenceThreshold");
            
            response.put("possibleResponses", possibleResponses);
            response.put("totalResponses", possibleResponses.length);
            response.put("confidenceThreshold", confidenceThreshold);
            response.put("thresholdDescription", String.format("Если уверенность ниже %.0f%%, возвращается 'Машина в порядке'", confidenceThreshold * 100));
            
            // Детальное описание каждого ответа
            Map<String, String> responseDescriptions = new HashMap<>();
            responseDescriptions.put("Машина в порядке", "Повреждения не обнаружены или уверенность ниже порогового значения (25%)");
            responseDescriptions.put("Незначительная царапина", "Обнаружена мелкая царапина на поверхности");
            responseDescriptions.put("Незначительная вмятина", "Обнаружена небольшая вмятина");
            responseDescriptions.put("Умеренная царапина", "Обнаружена заметная царапина, требующая внимания");
            responseDescriptions.put("Умеренная вмятина", "Обнаружена заметная вмятина, требующая ремонта");
            responseDescriptions.put("Умеренная поломка", "Обнаружены умеренные повреждения конструкции");
            responseDescriptions.put("Серьезная царапина", "Обнаружена глубокая царапина, требующая серьезного ремонта");
            responseDescriptions.put("Серьезная вмятина", "Обнаружена значительная вмятина, требующая серьезного ремонта");
            responseDescriptions.put("Серьезная поломка", "Обнаружены серьезные повреждения конструкции автомобиля");
            
            response.put("responseDescriptions", responseDescriptions);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Ошибка при получении возможных ответов: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Ошибка при получении возможных ответов"));
        }
    }

    /**
     * Создает стандартизированный ответ об ошибке
     */
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", true);
        error.put("message", message);
        error.put("timestamp", System.currentTimeMillis());
        return error;
    }
} 