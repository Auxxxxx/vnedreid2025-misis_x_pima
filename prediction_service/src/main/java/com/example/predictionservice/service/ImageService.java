package com.example.predictionservice.service;

import com.example.predictionservice.model.MLApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class ImageService {

    private static final Logger logger = LoggerFactory.getLogger(ImageService.class);

    // Цвета для разных типов повреждений
    private static final Map<String, Color> DAMAGE_COLORS;
    
    static {
        Map<String, Color> colors = new HashMap<>();
        colors.put("rust", Color.ORANGE);
        colors.put("dent", Color.BLUE);
        colors.put("scratch", Color.RED);
        colors.put("deformation", Color.MAGENTA);
        colors.put("default", Color.GREEN);
        DAMAGE_COLORS = Collections.unmodifiableMap(colors);
    }

    /**
     * Рисует bounding boxes на изображении на основе результатов ML API
     */
    public String drawBoundingBoxes(MultipartFile originalImage, MLApiResponse mlResponse) throws IOException {
        logger.info("=== НАЧАЛО ОТРИСОВКИ BOUNDING BOXES ===");
        logger.info("Имя файла: {}, размер: {} байт", originalImage.getOriginalFilename(), originalImage.getSize());
        logger.info("ML Response: carBbox={}, damageRatio={}", mlResponse.getCarBbox(), mlResponse.getDamageRatio());
        
        try {
            // Читаем оригинальное изображение
            logger.info("Шаг 1: Чтение оригинального изображения...");
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(originalImage.getBytes()));
            if (image == null) {
                logger.error("ОШИБКА: Не удалось прочитать изображение - ImageIO.read вернул null");
                throw new IOException("Не удалось прочитать изображение");
            }
            logger.info("Изображение успешно загружено: {}x{}, тип: {}", 
                       image.getWidth(), image.getHeight(), image.getType());

            // Создаем копию для рисования
            logger.info("Шаг 2: Создание копии изображения для рисования...");
            BufferedImage resultImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = resultImage.createGraphics();
            
            // Копируем оригинальное изображение
            logger.info("Шаг 3: Копирование оригинального изображения...");
            g2d.drawImage(image, 0, 0, null);
            
            // Настраиваем качество рисования
            logger.info("Шаг 4: Настройка качества рисования...");
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Рисуем car bounding box если есть
            if (mlResponse.getCarBbox() != null && mlResponse.getCarBbox().size() >= 4) {
                logger.info("Шаг 5: Отрисовка car bounding box...");
                logger.info("Car bbox координаты: {}", mlResponse.getCarBbox());
                try {
                    drawCarBoundingBox(g2d, mlResponse.getCarBbox());
                    logger.info("Car bounding box успешно нарисован");
                } catch (Exception e) {
                    logger.error("ОШИБКА при отрисовке car bounding box: {}", e.getMessage(), e);
                    throw e;
                }
            } else {
                logger.warn("Car bounding box отсутствует или имеет неверный формат: {}", mlResponse.getCarBbox());
            }

            // Рисуем damage detections если есть
            if (mlResponse.getDetections() != null && !mlResponse.getDetections().isEmpty()) {
                logger.info("Шаг 6: Отрисовка damage detections...");
                logger.info("Detections: {}", mlResponse.getDetections());
                try {
                    drawDamageDetections(g2d, mlResponse.getDetections());
                    logger.info("Damage detections успешно нарисованы");
                } catch (Exception e) {
                    logger.error("ОШИБКА при отрисовке damage detections: {}", e.getMessage(), e);
                    throw e;
                }
            } else {
                logger.info("Damage detections отсутствуют: {}", mlResponse.getDetections());
            }

            // Добавляем информацию о damage ratio
            logger.info("Шаг 7: Добавление информации о damage ratio...");
            try {
                drawDamageInfo(g2d, mlResponse.getDamageRatio(), image.getWidth(), image.getHeight());
                logger.info("Информация о damage ratio успешно добавлена");
            } catch (Exception e) {
                logger.error("ОШИБКА при добавлении информации о damage ratio: {}", e.getMessage(), e);
                throw e;
            }

            g2d.dispose();
            logger.info("Шаг 8: Graphics2D освобожден");

            // Конвертируем в base64
            logger.info("Шаг 9: Конвертация в base64...");
            String base64Result = bufferedImageToBase64(resultImage);
            logger.info("Конвертация в base64 завершена, длина строки: {} символов", base64Result.length());

            logger.info("=== ОТРИСОВКА BOUNDING BOXES ЗАВЕРШЕНА УСПЕШНО ===");
            return base64Result;
            
        } catch (Exception e) {
            logger.error("=== КРИТИЧЕСКАЯ ОШИБКА ПРИ ОТРИСОВКЕ BOUNDING BOXES ===");
            logger.error("Тип ошибки: {}", e.getClass().getSimpleName());
            logger.error("Сообщение ошибки: {}", e.getMessage());
            logger.error("Stack trace:", e);
            throw e;
        }
    }

    /**
     * Рисует основной bounding box автомобиля
     */
    private void drawCarBoundingBox(Graphics2D g2d, List<Double> carBbox) {
        logger.info("  -> Начинаю отрисовку car bounding box");
        try {
            logger.info("  -> Установка цвета и стиля линии...");
            g2d.setColor(Color.YELLOW);
            g2d.setStroke(new BasicStroke(4.0f)); // Более толстая линия для car bbox
            
            logger.info("  -> Извлечение координат из carBbox: {}", carBbox);
            Double x1 = carBbox.get(0);
            Double y1 = carBbox.get(1);
            Double x2 = carBbox.get(2);
            Double y2 = carBbox.get(3);
            
            logger.info("  -> Координаты: x1={}, y1={}, x2={}, y2={}", x1, y1, x2, y2);
            
            int x = x1.intValue();
            int y = y1.intValue();
            int width = x2.intValue() - x;
            int height = y2.intValue() - y;
            
            logger.info("  -> Вычисленные размеры: x={}, y={}, width={}, height={}", x, y, width, height);
            
            if (width <= 0 || height <= 0) {
                logger.warn("  -> ПРЕДУПРЕЖДЕНИЕ: Некорректные размеры бокса: width={}, height={}", width, height);
                return;
            }
            
            logger.info("  -> Рисование прямоугольника...");
            g2d.drawRect(x, y, width, height);
            
            // Добавляем подпись
            logger.info("  -> Добавление подписи...");
            g2d.setFont(new Font("Arial", Font.BOLD, 16));
            g2d.fillRect(x, y - 25, 60, 25);
            g2d.setColor(Color.BLACK);
            g2d.drawString("CAR", x + 5, y - 8);
            
            logger.info("  -> Car bounding box успешно нарисован: x={}, y={}, width={}, height={}", x, y, width, height);
        } catch (Exception e) {
            logger.error("  -> ОШИБКА в drawCarBoundingBox: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Рисует bounding boxes для обнаруженных повреждений
     */
    private void drawDamageDetections(Graphics2D g2d, Map<String, List<MLApiResponse.Detection>> detections) {
        logger.info("  -> Начинаю отрисовку damage detections");
        logger.info("  -> Количество типов повреждений: {}", detections.size());
        
        try {
            for (Map.Entry<String, List<MLApiResponse.Detection>> entry : detections.entrySet()) {
                String damageType = entry.getKey();
                List<MLApiResponse.Detection> detectionList = entry.getValue();
                
                logger.info("  -> Обработка типа повреждения: '{}', количество обнаружений: {}", 
                           damageType, detectionList != null ? detectionList.size() : 0);
                
                if (detectionList != null && !detectionList.isEmpty()) {
                    Color color = DAMAGE_COLORS.getOrDefault(damageType, DAMAGE_COLORS.get("default"));
                    logger.info("  -> Выбран цвет для '{}': {}", damageType, color);
                    drawDetectionBoxes(g2d, detectionList, damageType, color);
                } else {
                    logger.info("  -> Пропускаю '{}' - пустой список обнаружений", damageType);
                }
            }
            logger.info("  -> Все damage detections обработаны");
        } catch (Exception e) {
            logger.error("  -> ОШИБКА в drawDamageDetections: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Рисует боксы для конкретного типа повреждений
     */
    private void drawDetectionBoxes(Graphics2D g2d, List<MLApiResponse.Detection> detections, String damageType, Color color) {
        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(3.0f));
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        
        for (int i = 0; i < detections.size(); i++) {
            MLApiResponse.Detection detection = detections.get(i);
            MLApiResponse.BoundingBox bbox = detection.getBbox();
            
            if (bbox != null) {
                int x = bbox.getXMin();
                int y = bbox.getYMin();
                int width = bbox.getXMax() - x;
                int height = bbox.getYMax() - y;
                
                // Рисуем прямоугольник
                g2d.drawRect(x, y, width, height);
                
                // Рисуем подпись с типом повреждения и confidence
                String label = String.format("%s %.2f", damageType.toUpperCase(), detection.getConfidence());
                FontMetrics fm = g2d.getFontMetrics();
                int labelWidth = fm.stringWidth(label);
                int labelHeight = fm.getHeight();
                
                // Фон для подписи
                g2d.fillRect(x, y - labelHeight, labelWidth + 10, labelHeight);
                g2d.setColor(Color.WHITE);
                g2d.drawString(label, x + 5, y - 5);
                g2d.setColor(color); // Возвращаем цвет для следующих боксов
                
                logger.debug("Нарисован {} бокс: x={}, y={}, width={}, height={}, confidence={}", 
                           damageType, x, y, width, height, detection.getConfidence());
            }
        }
    }

    /**
     * Добавляет информацию о damage_ratio на изображение
     */
    private void drawDamageInfo(Graphics2D g2d, double damageRatio, int imageWidth, int imageHeight) {
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        
        String damageText = String.format("Damage Ratio: %.1f%%", damageRatio * 100);
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(damageText);
        
        // Позиционируем текст в правом верхнем углу
        int x = imageWidth - textWidth - 20;
        int y = 30;
        
        // Фон для текста
        g2d.setColor(new Color(255, 255, 255, 200)); // Полупрозрачный белый
        g2d.fillRect(x - 10, y - 20, textWidth + 20, 30);
        
        // Текст
        g2d.setColor(Color.BLACK);
        g2d.drawString(damageText, x, y);
        
        logger.debug("Добавлена информация о damage ratio: {}", damageRatio);
    }

    /**
     * Конвертирует BufferedImage в base64 строку
     */
    private String bufferedImageToBase64(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.getEncoder().encodeToString(imageBytes);
    }

    /**
     * Создает миниатюрную версию изображения для GIF
     */
    public String createThumbnail(MultipartFile originalImage, int maxWidth, int maxHeight) throws IOException {
        logger.info("Создаю миниатюру изображения");

        BufferedImage original = ImageIO.read(new ByteArrayInputStream(originalImage.getBytes()));
        if (original == null) {
            throw new IOException("Не удалось прочитать изображение");
        }

        // Вычисляем новые размеры с сохранением пропорций
        double scaleX = (double) maxWidth / original.getWidth();
        double scaleY = (double) maxHeight / original.getHeight();
        double scale = Math.min(scaleX, scaleY);

        int newWidth = (int) (original.getWidth() * scale);
        int newHeight = (int) (original.getHeight() * scale);

        // Создаем миниатюру
        BufferedImage thumbnail = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = thumbnail.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(original, 0, 0, newWidth, newHeight, null);
        g2d.dispose();

        String base64Result = bufferedImageToBase64(thumbnail);
        
        logger.info("Миниатюра создана: {}x{} -> {}x{}", 
                   original.getWidth(), original.getHeight(), newWidth, newHeight);
        
        return base64Result;
    }

    /**
     * Рисует один bounding box на изображении
     */
    public String drawSingleBoundingBox(MultipartFile originalImage, int x, int y, int width, int height, 
                                       String colorHex, String label, int strokeWidth) throws IOException {
        logger.info("Начинаю рисование одного bounding box на изображении");
        System.out.println("=== ImageService.drawSingleBoundingBox ===");
        System.out.println("Input parameters: x=" + x + ", y=" + y + ", width=" + width + ", height=" + height);
        System.out.println("Color: " + colorHex + ", Label: " + label + ", StrokeWidth: " + strokeWidth);
        System.out.println("Original image: " + originalImage.getOriginalFilename() + ", size: " + originalImage.getSize() + " bytes");

        try {
            // Читаем оригинальное изображение
            System.out.println("Reading original image...");
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(originalImage.getBytes()));
            if (image == null) {
                System.out.println("ERROR: Не удалось прочитать изображение - image is null");
                throw new IOException("Не удалось прочитать изображение");
            }
            
            System.out.println("Original image loaded successfully: " + image.getWidth() + "x" + image.getHeight());

            // Создаем копию для рисования
            System.out.println("Creating result image...");
            BufferedImage resultImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = resultImage.createGraphics();
            
            // Копируем оригинальное изображение
            System.out.println("Copying original image to result...");
            g2d.drawImage(image, 0, 0, null);
            
            // Настраиваем качество рисования
            System.out.println("Setting up graphics...");
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setStroke(new BasicStroke((float) strokeWidth));

            // Парсим цвет из hex строки
            System.out.println("Parsing color: " + colorHex);
            Color boxColor = parseHexColor(colorHex);
            g2d.setColor(boxColor);
            System.out.println("Color parsed successfully: " + boxColor);

            // Проверяем границы изображения
            System.out.println("Checking bounds...");
            int safeX = Math.max(0, Math.min(x, image.getWidth() - 1));
            int safeY = Math.max(0, Math.min(y, image.getHeight() - 1));
            int safeWidth = Math.max(1, Math.min(width, image.getWidth() - safeX));
            int safeHeight = Math.max(1, Math.min(height, image.getHeight() - safeY));
            
            System.out.println("Safe coordinates: x=" + safeX + ", y=" + safeY + ", width=" + safeWidth + ", height=" + safeHeight);

            // Рисуем прямоугольник
            System.out.println("Drawing rectangle...");
            g2d.drawRect(safeX, safeY, safeWidth, safeHeight);
            System.out.println("Rectangle drawn successfully");

            // Рисуем подпись если она есть
            if (label != null && !label.trim().isEmpty()) {
                System.out.println("Drawing label: " + label);
                drawLabel(g2d, label, safeX, safeY, boxColor);
                System.out.println("Label drawn successfully");
            } else {
                System.out.println("No label to draw");
            }

            g2d.dispose();
            System.out.println("Graphics disposed");

            // Конвертируем в base64
            System.out.println("Converting to base64...");
            String base64Result = bufferedImageToBase64(resultImage);
            System.out.println("Base64 conversion successful, length: " + (base64Result != null ? base64Result.length() : "null"));
            
            logger.info("Bounding box успешно нарисован: x={}, y={}, width={}, height={}, color={}, label={}", 
                       safeX, safeY, safeWidth, safeHeight, colorHex, label);
            
            System.out.println("=== ImageService.drawSingleBoundingBox SUCCESS ===");
            return base64Result;
            
        } catch (Exception e) {
            System.out.println("ERROR in ImageService.drawSingleBoundingBox: " + e.getMessage());
            e.printStackTrace();
            logger.error("Ошибка при рисовании bounding box", e);
            throw e;
        }
    }

    /**
     * Парсит hex цвет в Color объект
     */
    private Color parseHexColor(String hexColor) {
        try {
            // Убираем # если есть
            String hex = hexColor.startsWith("#") ? hexColor.substring(1) : hexColor;
            
            // Проверяем длину
            if (hex.length() == 6) {
                int r = Integer.parseInt(hex.substring(0, 2), 16);
                int g = Integer.parseInt(hex.substring(2, 4), 16);
                int b = Integer.parseInt(hex.substring(4, 6), 16);
                return new Color(r, g, b);
            } else if (hex.length() == 3) {
                // Короткий формат #RGB -> #RRGGBB
                int r = Integer.parseInt(hex.substring(0, 1) + hex.substring(0, 1), 16);
                int g = Integer.parseInt(hex.substring(1, 2) + hex.substring(1, 2), 16);
                int b = Integer.parseInt(hex.substring(2, 3) + hex.substring(2, 3), 16);
                return new Color(r, g, b);
            }
        } catch (NumberFormatException e) {
            logger.warn("Не удалось распарсить цвет: {}, использую зеленый по умолчанию", hexColor);
        }
        
        // По умолчанию зеленый
        return Color.GREEN;
    }

    /**
     * Рисует подпись для бокса
     */
    private void drawLabel(Graphics2D g2d, String label, int x, int y, Color boxColor) {
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        FontMetrics fm = g2d.getFontMetrics();
        int labelWidth = fm.stringWidth(label);
        int labelHeight = fm.getHeight();
        
        // Позиция для подписи (над боксом)
        int labelX = x;
        int labelY = y - 5;
        
        // Если подпись выходит за верхний край, рисуем внутри бокса
        if (labelY - labelHeight < 0) {
            labelY = y + labelHeight + 5;
        }
        
        // Фон для подписи (полупрозрачный)
        g2d.setColor(new Color(boxColor.getRed(), boxColor.getGreen(), boxColor.getBlue(), 180));
        g2d.fillRect(labelX, labelY - labelHeight, labelWidth + 10, labelHeight + 5);
        
        // Текст подписи
        g2d.setColor(Color.WHITE);
        g2d.drawString(label, labelX + 5, labelY - 3);
        
        // Возвращаем цвет бокса
        g2d.setColor(boxColor);
    }
} 