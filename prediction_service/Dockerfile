FROM openjdk:8-jdk-alpine

# Устанавливаем шрифты и необходимые пакеты для AWT/Graphics
RUN apk add --no-cache fontconfig ttf-dejavu

# Создаем рабочую директорию
WORKDIR /app

# Копируем JAR файл
COPY target/prediction-service-1.0.0.jar app.jar

# Открываем порт
EXPOSE 8080

# Запускаем приложение с headless режимом для AWT
ENTRYPOINT ["java", "-Djava.awt.headless=true", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"] 