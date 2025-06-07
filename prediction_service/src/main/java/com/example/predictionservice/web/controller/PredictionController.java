package com.example.predictionservice.web.controller;

import com.example.predictionservice.model.PredictionResponse;
import com.example.predictionservice.service.PredictionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "Prediction", description = "API для анализа повреждений на изображениях")
public class PredictionController {

    @Autowired
    private PredictionService predictionService;

    @PostMapping(value = "/submit-photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Анализ изображения на предмет повреждений",
        description = "Загружает изображение (PNG/JPEG) и возвращает результат анализа повреждений"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Анализ выполнен успешно",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PredictionResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Неверный формат файла или файл не предоставлен",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Ошибка при обработке изображения",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class)
            )
        )
    })
    public ResponseEntity<?> submitPhoto(
        @RequestParam("file") MultipartFile file
    ) {
        try {
            // Проверка наличия файла
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Файл не предоставлен"));
            }

            // Проверка формата файла
            if (!predictionService.isValidImageFormat(file.getContentType())) {
                return ResponseEntity.badRequest().body(
                    createErrorResponse("Неподдерживаемый формат файла. Поддерживаются: PNG, JPEG")
                );
            }

            // Проверка размера файла (максимум 10MB)
            if (file.getSize() > 10 * 1024 * 1024) {
                return ResponseEntity.badRequest().body(
                    createErrorResponse("Размер файла превышает 10MB")
                );
            }

            // Анализ изображения
            PredictionResponse result = predictionService.analyzeImage(file);
            
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                createErrorResponse("Ошибка при обработке изображения: " + e.getMessage())
            );
        }
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", message);
        error.put("timestamp", System.currentTimeMillis());
        return error;
    }
} 