package com.example.predictionservice.web.controller;

import com.example.predictionservice.service.GifService;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/animation")
@Tag(name = "Animation", description = "API для работы с анимациями повреждений")
public class AnimationController {

    private static final Logger logger = LoggerFactory.getLogger(AnimationController.class);

    @Autowired
    private GifService gifService;

    @GetMapping("/find")
    @Operation(
        summary = "Поиск анимации по параметрам повреждений",
        description = "Находит наиболее подходящую анимацию по списку параметров повреждений. " +
                     "Поддерживаемые параметры: rust, dent, scratch, damage. " +
                     "Чем больше совпадений в названии файла, тем выше приоритет."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Анимация найдена и возвращена",
            content = @Content(mediaType = "video/webm")
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Подходящая анимация не найдена",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Ошибка при получении анимации"
        )
    })
    public ResponseEntity<?> findAnimation(
        @Parameter(description = "Список параметров повреждений (rust, dent, scratch, damage)", 
                  example = "rust,dent")
        @RequestParam(required = false) List<String> params
    ) {
        try {
            logger.info("Запрос анимации для параметров: {}", params);
            
            // Инициализируем сервис если нужно
            gifService.init();
            
            byte[] animationData = gifService.findBestMatchingAnimation(params);
            
            if (animationData != null) {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType("video/webm"));
                headers.setContentLength(animationData.length);
                headers.set("Content-Disposition", "inline; filename=\"damage_animation.webm\"");
                
                logger.info("Возвращаю анимацию размером {} байт", animationData.length);
                return new ResponseEntity<>(animationData, headers, HttpStatus.OK);
            } else {
                Map<String, Object> errorResponse = createErrorResponse(
                    "Не найдена подходящая анимация для параметров: " + params,
                    "NOT_FOUND"
                );
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
        } catch (Exception e) {
            logger.error("Ошибка при поиске анимации", e);
            Map<String, Object> errorResponse = createErrorResponse(
                "Ошибка при поиске анимации: " + e.getMessage(),
                "INTERNAL_ERROR"
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/list")
    @Operation(
        summary = "Получить список всех доступных анимаций",
        description = "Возвращает список имен всех доступных файлов анимаций"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Список анимаций получен успешно",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = Map.class)
        )
    )
    public ResponseEntity<Map<String, Object>> listAnimations() {
        try {
            logger.info("Запрос списка доступных анимаций");
            
            // Инициализируем сервис если нужно
            gifService.init();
            
            List<String> animations = gifService.getAvailableAnimations();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("animations", animations);
            response.put("count", animations.size());
            response.put("timestamp", System.currentTimeMillis());
            
            logger.info("Найдено {} анимаций", animations.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Ошибка при получении списка анимаций", e);
            Map<String, Object> errorResponse = createErrorResponse(
                "Ошибка при получении списка анимаций: " + e.getMessage(),
                "INTERNAL_ERROR"
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/by-name/{filename}")
    @Operation(
        summary = "Получить анимацию по имени файла",
        description = "Возвращает анимацию по точному имени файла"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Анимация найдена и возвращена",
            content = @Content(mediaType = "video/webm")
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Файл не найден"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Ошибка при получении файла"
        )
    })
    public ResponseEntity<?> getAnimationByName(
        @Parameter(description = "Имя файла анимации", example = "rust_dent_scratches.webm")
        @PathVariable String filename
    ) {
        try {
            logger.info("Запрос анимации по имени: {}", filename);
            
            byte[] animationData = gifService.getAnimationByName(filename);
            
            if (animationData != null) {
                HttpHeaders headers = new HttpHeaders();
                
                // Определяем content type по расширению файла
                String contentType = "video/webm";
                if (filename.toLowerCase().endsWith(".gif")) {
                    contentType = "image/gif";
                } else if (filename.toLowerCase().endsWith(".mp4")) {
                    contentType = "video/mp4";
                }
                
                headers.setContentType(MediaType.parseMediaType(contentType));
                headers.setContentLength(animationData.length);
                headers.set("Content-Disposition", "inline; filename=\"" + filename + "\"");
                
                logger.info("Возвращаю файл {} размером {} байт", filename, animationData.length);
                return new ResponseEntity<>(animationData, headers, HttpStatus.OK);
            } else {
                Map<String, Object> errorResponse = createErrorResponse(
                    "Файл не найден: " + filename,
                    "NOT_FOUND"
                );
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
        } catch (Exception e) {
            logger.error("Ошибка при получении файла: {}", filename, e);
            Map<String, Object> errorResponse = createErrorResponse(
                "Ошибка при получении файла: " + e.getMessage(),
                "INTERNAL_ERROR"
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/find-by-params")
    @Operation(
        summary = "Поиск анимации по JSON параметрам",
        description = "Находит анимацию по JSON объекту с булевыми параметрами повреждений"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Анимация найдена и возвращена",
            content = @Content(mediaType = "video/webm")
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Подходящая анимация не найдена"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Неверные параметры запроса"
        )
    })
    public ResponseEntity<?> findAnimationByParams(
        @RequestBody Map<String, Object> damageParams
    ) {
        try {
            logger.info("Запрос анимации по JSON параметрам: {}", damageParams);
            
            // Преобразуем JSON параметры в список активных повреждений
            List<String> activeParams = new ArrayList<>();
            
            for (String param : Arrays.asList("rust", "dent", "scratch", "damage")) {
                Object value = damageParams.get(param);
                if (value != null && (Boolean.TRUE.equals(value) || "true".equalsIgnoreCase(value.toString()))) {
                    activeParams.add(param);
                }
            }
            
            logger.info("Активные параметры: {}", activeParams);
            
            byte[] animationData = gifService.findBestMatchingAnimation(activeParams);
            
            if (animationData != null) {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType("video/webm"));
                headers.setContentLength(animationData.length);
                headers.set("Content-Disposition", "inline; filename=\"damage_animation.webm\"");
                
                logger.info("Возвращаю анимацию размером {} байт", animationData.length);
                return new ResponseEntity<>(animationData, headers, HttpStatus.OK);
            } else {
                Map<String, Object> errorResponse = createErrorResponse(
                    "Не найдена подходящая анимация для параметров: " + activeParams,
                    "NOT_FOUND"
                );
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
        } catch (Exception e) {
            logger.error("Ошибка при поиске анимации по JSON параметрам", e);
            Map<String, Object> errorResponse = createErrorResponse(
                "Ошибка при обработке запроса: " + e.getMessage(),
                "INTERNAL_ERROR"
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Создает стандартный объект ошибки
     */
    private Map<String, Object> createErrorResponse(String message, String errorCode) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("error", errorCode);
        error.put("message", message);
        error.put("timestamp", System.currentTimeMillis());
        return error;
    }
} 