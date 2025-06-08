package com.example.predictionservice.web.controller;

import com.example.predictionservice.service.ImageService;
import com.example.predictionservice.web.dto.BoundingBoxRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/image")
@Tag(name = "Image", description = "API для работы с изображениями")
public class ImageController {

    @Autowired
    private ImageService imageService;

    @PostMapping(value = "/draw-bbox", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Рисование bounding box на изображении",
        description = "Принимает изображение и координаты bounding box, рисует прямоугольник и возвращает обработанное изображение"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Bounding box успешно нарисован",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Неверные параметры или файл не предоставлен",
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
    public ResponseEntity<?> drawBoundingBox(
        @RequestParam("file") MultipartFile file,
        @RequestParam("bbox") String bboxJson
    ) {
        try {
            // Проверка наличия файла
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Файл не предоставлен"));
            }

            // Проверка формата файла
            if (!isValidImageFormat(file.getContentType())) {
                return ResponseEntity.badRequest().body(
                    createErrorResponse("Неподдерживаемый формат файла. Поддерживаются: PNG, JPEG")
                );
            }

            // Парсим параметры bounding box из JSON
            ObjectMapper mapper = new ObjectMapper();
            BoundingBoxRequest bboxRequest = mapper.readValue(bboxJson, BoundingBoxRequest.class);

            // Валидация координат
            if (bboxRequest.getWidth() <= 0 || bboxRequest.getHeight() <= 0) {
                return ResponseEntity.badRequest().body(
                    createErrorResponse("Ширина и высота бокса должны быть больше 0")
                );
            }

            // Рисуем bounding box
            String resultImage = imageService.drawSingleBoundingBox(
                file,
                bboxRequest.getX(),
                bboxRequest.getY(),
                bboxRequest.getWidth(),
                bboxRequest.getHeight(),
                bboxRequest.getColor(),
                bboxRequest.getLabel(),
                bboxRequest.getStrokeWidth()
            );

            // Формируем ответ
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Bounding box успешно нарисован");
            response.put("result_image", resultImage);
            response.put("bbox_coordinates", bboxRequest);
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            return ResponseEntity.status(500).body(
                createErrorResponse("Ошибка при чтении файла: " + e.getMessage())
            );
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                createErrorResponse("Ошибка при обработке изображения: " + e.getMessage())
            );
        }
    }

    @PostMapping(value = "/draw-bbox-simple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Простое рисование bounding box",
        description = "Принимает изображение и координаты в формате ML модели [x_min, y_min, x_max, y_max]"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Bounding box успешно нарисован",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Неверные параметры или файл не предоставлен"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Ошибка при обработке изображения"
        )
    })
    public ResponseEntity<?> drawBoundingBoxSimple(
        @RequestParam("file") MultipartFile file,
        @RequestParam("x_min") int xMin,
        @RequestParam("y_min") int yMin,
        @RequestParam("x_max") int xMax,
        @RequestParam("y_max") int yMax,
        @RequestParam(value = "color", defaultValue = "#00FF00") String color,
        @RequestParam(value = "label", required = false) String label,
        @RequestParam(value = "strokeWidth", defaultValue = "2") int strokeWidth
    ) {
        try {
            System.out.println("=== DEBUG: Начало обработки drawBoundingBoxSimple ===");
            System.out.println("File: " + file.getOriginalFilename() + ", size: " + file.getSize() + " bytes");
            System.out.println("Coordinates: x_min=" + xMin + ", y_min=" + yMin + ", x_max=" + xMax + ", y_max=" + yMax);
            System.out.println("Color: " + color + ", Label: " + label + ", StrokeWidth: " + strokeWidth);
            
            // Проверка наличия файла
            if (file.isEmpty()) {
                System.out.println("ERROR: Файл пустой");
                return ResponseEntity.badRequest().body(createErrorResponse("Файл не предоставлен"));
            }

            // Проверка формата файла
            if (!isValidImageFormat(file.getContentType())) {
                System.out.println("ERROR: Неподдерживаемый формат файла: " + file.getContentType());
                return ResponseEntity.badRequest().body(
                    createErrorResponse("Неподдерживаемый формат файла. Поддерживаются: PNG, JPEG")
                );
            }

            // Валидация координат
            if (xMax <= xMin || yMax <= yMin) {
                System.out.println("ERROR: Неверные координаты: xMax=" + xMax + " <= xMin=" + xMin + 
                                 " или yMax=" + yMax + " <= yMin=" + yMin);
                return ResponseEntity.badRequest().body(
                    createErrorResponse("Неверные координаты: x_max должен быть больше x_min, y_max больше y_min")
                );
            }

            // Конвертируем координаты в формат (x, y, width, height)
            int x = xMin;
            int y = yMin;
            int width = xMax - xMin;
            int height = yMax - yMin;
            
            System.out.println("Converted coordinates: x=" + x + ", y=" + y + ", width=" + width + ", height=" + height);

            // Рисуем bounding box
            String resultImage = imageService.drawSingleBoundingBox(
                file, x, y, width, height, color, label, strokeWidth
            );
            
            System.out.println("SUCCESS: Bounding box нарисован, результат длиной: " + 
                             (resultImage != null ? resultImage.length() : "null") + " символов");

            // Формируем ответ
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Bounding box успешно нарисован");
            response.put("result_image", resultImage);
            
            // Создаем координаты с HashMap для Java 8 совместимости
            Map<String, Object> coordinates = new HashMap<>();
            coordinates.put("x_min", xMin);
            coordinates.put("y_min", yMin);
            coordinates.put("x_max", xMax);
            coordinates.put("y_max", yMax);
            coordinates.put("x", x);
            coordinates.put("y", y);
            coordinates.put("width", width);
            coordinates.put("height", height);
            coordinates.put("color", color);
            coordinates.put("label", label != null ? label : "");
            coordinates.put("strokeWidth", strokeWidth);
            response.put("bbox_coordinates", coordinates);
            
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            System.out.println("ERROR: IOException: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(
                createErrorResponse("Ошибка при чтении файла: " + e.getMessage())
            );
        } catch (Exception e) {
            System.out.println("ERROR: Exception: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(
                createErrorResponse("Ошибка при обработке изображения: " + e.getMessage())
            );
        }
    }

    private boolean isValidImageFormat(String contentType) {
        return contentType != null && (
            contentType.equals("image/png") ||
            contentType.equals("image/jpeg") ||
            contentType.equals("image/jpg")
        );
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("error", message);
        error.put("timestamp", System.currentTimeMillis());
        return error;
    }
} 