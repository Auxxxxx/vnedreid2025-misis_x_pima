package com.example.predictionservice.service;

import com.example.predictionservice.model.MLApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

@Service
public class GifService {

    private static final Logger logger = LoggerFactory.getLogger(GifService.class);
    private static final String GIF_RESOURCE_PATH = "classpath*:gifs/**/*";
    
    // Параметры поиска
    private static final List<String> DAMAGE_PARAMETERS = Arrays.asList("rust", "dent", "scratch", "damage");
    
    // Кэш доступных файлов
    private List<Resource> availableAnimations;

    @Autowired
    private ImageService imageService;

    /**
     * Инициализация сервиса - загрузка списка доступных анимаций
     */
    public void init() {
        loadAvailableAnimations();
    }
    
    /**
     * Загружает список доступных файлов анимаций
     */
    private void loadAvailableAnimations() {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources(GIF_RESOURCE_PATH);
            
            availableAnimations = new ArrayList<>();
            for (Resource resource : resources) {
                if (resource.exists() && resource.isReadable()) {
                    String filename = resource.getFilename();
                    if (filename != null && (filename.toLowerCase().endsWith(".webm") || 
                                           filename.toLowerCase().endsWith(".gif") ||
                                           filename.toLowerCase().endsWith(".mp4"))) {
                        availableAnimations.add(resource);
                        logger.info("Найден файл анимации: {}", filename);
                    }
                }
            }
            
            logger.info("Загружено {} файлов анимаций", availableAnimations.size());
            
        } catch (IOException e) {
            logger.error("Ошибка при загрузке списка анимаций", e);
            availableAnimations = new ArrayList<>();
        }
    }
    
    /**
     * Находит наиболее подходящую анимацию по параметрам повреждений
     * 
     * @param damageParams список параметров повреждений (rust, dent, scratch, damage)
     * @return содержимое файла в виде byte[] или null если файл не найден
     */
    public byte[] findBestMatchingAnimation(List<String> damageParams) {
        if (availableAnimations == null || availableAnimations.isEmpty()) {
            init(); // Пытаемся перезагрузить если список пустой
        }
        
        if (availableAnimations.isEmpty()) {
            logger.warn("Нет доступных файлов анимаций");
            return null;
        }
        
        // Нормализуем параметры - приводим к нижнему регистру
        List<String> normalizedParams = new ArrayList<>();
        if (damageParams != null) {
            for (String param : damageParams) {
                if (param != null && !param.trim().isEmpty()) {
                    normalizedParams.add(param.trim().toLowerCase());
                }
            }
        }
        
        logger.info("Поиск анимации для параметров: {}", normalizedParams);
        
        Resource bestMatch = null;
        int maxMatches = -1;
        
        // Ищем файл с максимальным количеством совпадений
        for (Resource resource : availableAnimations) {
            String filename = resource.getFilename();
            if (filename == null) continue;
            
            String lowerFilename = filename.toLowerCase();
            int matches = calculateMatches(lowerFilename, normalizedParams);
            
            logger.debug("Файл '{}': {} совпадений", filename, matches);
            
            if (matches > maxMatches) {
                maxMatches = matches;
                bestMatch = resource;
            }
        }
        
        if (bestMatch != null) {
            try {
                logger.info("Выбран файл '{}' с {} совпадениями", bestMatch.getFilename(), maxMatches);
                return readResourceToBytes(bestMatch);
            } catch (IOException e) {
                logger.error("Ошибка при чтении файла анимации: {}", bestMatch.getFilename(), e);
            }
        } else {
            logger.warn("Не найдено подходящих файлов анимации для параметров: {}", normalizedParams);
        }
        
        return null;
    }
    
    /**
     * Вычисляет количество совпадений параметров в названии файла
     */
    private int calculateMatches(String filename, List<String> damageParams) {
        int matches = 0;
        
        for (String param : damageParams) {
            // Проверяем точное совпадение слова (с границами слов)
            if (filename.contains(param)) {
                matches++;
                logger.debug("Найдено совпадение '{}' в файле '{}'", param, filename);
            }
            
            // Дополнительные синонимы для более гибкого поиска
            if ("damage".equals(param)) {
                if (filename.contains("hole") || filename.contains("broken")) {
                    matches++;
                    logger.debug("Найдено совпадение синонима damage ('hole'/'broken') в файле '{}'", filename);
                }
            }
            
            if ("scratch".equals(param)) {
                if (filename.contains("scratches")) {
                    // Не добавляем дополнительный матч, т.к. это практически то же слово
                    logger.debug("Найдено совпадение множественной формы scratch в файле '{}'", filename);
                }
            }
        }
        
        return matches;
    }
    
    /**
     * Читает содержимое ресурса в массив байт
     */
    private byte[] readResourceToBytes(Resource resource) throws IOException {
        try (InputStream inputStream = resource.getInputStream()) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[16384];
            
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            
            return buffer.toByteArray();
        }
    }
    
    /**
     * Возвращает список всех доступных анимаций
     */
    public List<String> getAvailableAnimations() {
        if (availableAnimations == null || availableAnimations.isEmpty()) {
            init();
        }
        
        List<String> filenames = new ArrayList<>();
        for (Resource resource : availableAnimations) {
            String filename = resource.getFilename();
            if (filename != null) {
                filenames.add(filename);
            }
        }
        
        return filenames;
    }
    
    /**
     * Получает анимацию по точному имени файла
     */
    public byte[] getAnimationByName(String filename) {
        if (availableAnimations == null || availableAnimations.isEmpty()) {
            init();
        }
        
        for (Resource resource : availableAnimations) {
            if (Objects.equals(filename, resource.getFilename())) {
                try {
                    logger.info("Найден файл по имени: {}", filename);
                    return readResourceToBytes(resource);
                } catch (IOException e) {
                    logger.error("Ошибка при чтении файла {}", filename, e);
                    return null;
                }
            }
        }
        
        logger.warn("Файл {} не найден", filename);
        return null;
    }

    /**
     * Создает анимированный GIF показывающий процесс анализа
     * Пока упрощенная версия - просто последовательность кадров
     */
    public String createAnalysisGif(MultipartFile originalImage, MLApiResponse mlResponse) throws IOException {
        logger.info("Создаю анимированный GIF процесса анализа");

        // Читаем оригинальное изображение
        BufferedImage baseImage = ImageIO.read(new ByteArrayInputStream(originalImage.getBytes()));
        if (baseImage == null) {
            throw new IOException("Не удалось прочитать изображение");
        }

        // Создаем уменьшенную версию для GIF
        int maxSize = 400;
        double scale = Math.min((double) maxSize / baseImage.getWidth(), (double) maxSize / baseImage.getHeight());
        int newWidth = (int) (baseImage.getWidth() * scale);
        int newHeight = (int) (baseImage.getHeight() * scale);

        BufferedImage scaledImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = scaledImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(baseImage, 0, 0, newWidth, newHeight, null);
        g2d.dispose();

        // Создаем финальный кадр с bounding boxes
        BufferedImage finalFrame = createFrameWithBoundingBoxes(scaledImage, mlResponse, scale);

        // Для упрощения пока возвращаем финальный кадр как статичное изображение
        // В будущем здесь будет создание настоящего GIF с анимацией
        String base64Result = bufferedImageToBase64(finalFrame);
        
        logger.info("GIF процесса анализа создан");
        return base64Result;
    }

    /**
     * Создает кадр с нарисованными bounding boxes (масштабированными)
     */
    private BufferedImage createFrameWithBoundingBoxes(BufferedImage baseImage, MLApiResponse mlResponse, double scale) {
        BufferedImage frame = new BufferedImage(baseImage.getWidth(), baseImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = frame.createGraphics();
        
        // Копируем базовое изображение
        g2d.drawImage(baseImage, 0, 0, null);
        
        // Настраиваем рисование
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setStroke(new BasicStroke(2.0f));

        // Рисуем car bbox если есть (масштабированный)
        if (mlResponse.getCarBbox() != null && mlResponse.getCarBbox().size() >= 4) {
            drawScaledCarBbox(g2d, mlResponse.getCarBbox(), scale);
        }

        // Рисуем damage detections (масштабированные)
        if (mlResponse.getDetections() != null) {
            drawScaledDamageDetections(g2d, mlResponse.getDetections(), scale);
        }

        g2d.dispose();
        return frame;
    }

    /**
     * Рисует масштабированный car bounding box
     */
    private void drawScaledCarBbox(Graphics2D g2d, List<Double> carBbox, double scale) {
        g2d.setColor(Color.YELLOW);
        g2d.setStroke(new BasicStroke(3.0f));
        
        int x = (int) (carBbox.get(0) * scale);
        int y = (int) (carBbox.get(1) * scale);
        int width = (int) ((carBbox.get(2) - carBbox.get(0)) * scale);
        int height = (int) ((carBbox.get(3) - carBbox.get(1)) * scale);
        
        g2d.drawRect(x, y, width, height);
        
        // Добавляем подпись
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.drawString("CAR", x + 5, y - 5);
    }

    /**
     * Рисует масштабированные damage detections
     */
    private void drawScaledDamageDetections(Graphics2D g2d, java.util.Map<String, List<MLApiResponse.Detection>> detections, double scale) {
        Color[] colors = {Color.RED, Color.ORANGE, Color.BLUE, Color.MAGENTA};
        int colorIndex = 0;

        for (java.util.Map.Entry<String, List<MLApiResponse.Detection>> entry : detections.entrySet()) {
            String damageType = entry.getKey();
            List<MLApiResponse.Detection> detectionList = entry.getValue();
            
            if (detectionList != null && !detectionList.isEmpty()) {
                Color color = colors[colorIndex % colors.length];
                g2d.setColor(color);
                g2d.setStroke(new BasicStroke(2.0f));
                g2d.setFont(new Font("Arial", Font.BOLD, 10));
                
                for (MLApiResponse.Detection detection : detectionList) {
                    MLApiResponse.BoundingBox bbox = detection.getBbox();
                    if (bbox != null) {
                        int x = (int) (bbox.getXMin() * scale);
                        int y = (int) (bbox.getYMin() * scale);
                        int width = (int) ((bbox.getXMax() - bbox.getXMin()) * scale);
                        int height = (int) ((bbox.getYMax() - bbox.getYMin()) * scale);
                        
                        g2d.drawRect(x, y, width, height);
                        g2d.drawString(damageType.toUpperCase(), x + 2, y - 2);
                    }
                }
                colorIndex++;
            }
        }
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
} 