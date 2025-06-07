FROM openjdk:8-jdk-alpine

# Создаем рабочую директорию
WORKDIR /app

# Копируем JAR файл
COPY target/prediction-service-1.0.0.jar app.jar

# Открываем порт
EXPOSE 8080

# Запускаем приложение
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"] 