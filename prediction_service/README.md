# Prediction Service

Spring Boot сервис для предсказаний с интеграцией Docker и автоматической сборкой.

## 🚀 Быстрый старт

```bash
# Клонируйте репозиторий
git clone <your-repo-url>
cd prediction_service

# Запустите сборку и развертывание
./build.sh
```

После успешного запуска сервис будет доступен:
- 🌐 **API:** http://localhost:8080/prediction/swagger-ui.html
- 📋 **OpenAPI JSON:** http://localhost:8080/prediction/v3/api-docs
- 🔍 **Ping:** http://localhost:8080/prediction/api/ping
- 📊 **Health:** http://localhost:8080/prediction/actuator/health

## 📁 Структура проекта

```
prediction_service/
├── src/main/java/com/example/predictionservice/
│   ├── PredictionServiceApplication.java     # Главный класс приложения
│   ├── web/                                  # REST контроллеры
│   ├── service/                              # Бизнес-логика
│   ├── model/                                # Модели данных
│   ├── exceptions/                           # Обработка исключений
│   └── config/                               # Конфигурация
├── src/main/resources/
│   └── application.yml                       # Конфигурация Spring Boot
├── docker-compose.yml                        # Docker Compose конфигурация
├── Dockerfile                                # Docker образ
├── .env.example                              # Пример переменных среды
├── build.sh                                  # Скрипт сборки и запуска
├── push.sh                                   # Скрипт публикации в DockerHub
└── stop.sh                                   # Скрипт остановки
```

## Запуск приложения

### Быстрый старт с Docker Compose (рекомендуется)

Самый простой способ запуска:

```bash
./build.sh
```

Этот скрипт автоматически:
- Собирает JAR файл
- Создает Docker образ
- Настраивает переменные среды
- Запускает сервис через Docker Compose

### Управление сервисом

**Остановка сервиса:**
```bash
./stop.sh
```

**Отправка образа в DockerHub:**
```bash
./push.sh
```

### Ручной запуск

#### Локальный запуск

1. Убедитесь, что у вас установлен Java 8 и Maven

2. Соберите проект (по умолчанию без тестов):
   ```bash
   mvn clean package
   ```

3. Или соберите проект с запуском тестов:
   ```bash
   mvn clean package -Pwith-tests
   ```

4. Запустите приложение:
   ```bash
   java -jar target/prediction-service-1.0.0.jar
   ```

#### Docker Compose запуск

1. Соберите JAR файл для Docker (без тестов):
   ```bash
   mvn clean package -Pdocker-build
   ```

2. Соберите Docker образ:
   ```bash
   docker build -t auxxxxx/vnedreid2025-prediction_service:latest .
   ```

3. Запустите через Docker Compose:
   ```bash
   docker compose up -d
   ```

### Варианты сборки

- **Сборка без тестов (по умолчанию):**
  ```bash
  mvn clean package
  ```

- **Сборка с тестами:**
  ```bash
  mvn clean package -Pwith-tests
  ```
  или
  ```bash
  mvn clean package -Dmaven.test.skip=false
  ```

- **Только компиляция без упаковки:**
  ```bash
  mvn clean compile
  ```

### Автоматическая сборка

Для упрощения сборки можно использовать готовые скрипты:

**Основные скрипты:**
- `./build.sh` - Полная сборка и запуск через Docker Compose
- `./push.sh` - Отправка образа в DockerHub
- `./stop.sh` - Остановка всех сервисов

**Windows:**
```cmd
build.bat
```

Скрипты автоматически:
- Проверяют наличие Java и Maven
- Собирают проект без тестов
- Выводят информацию о результате сборки

## API Endpoints

### Ping Endpoint
- **GET** `/api/ping` - Проверка работоспособности сервиса

Пример ответа:
```json
{
  "message": "pong",
  "timestamp": 1640995200000,
  "status": "UP"
}
```

### Prediction Endpoint
- **POST** `/api/submit-photo` - Анализ изображения на предмет повреждений

**Параметры запроса:**
- `file` (multipart/form-data) - Изображение в формате PNG или JPEG (максимум 10MB)

**Пример ответа:**
```json
{
  "category": 3,
  "dent": true,
  "rust": false,
  "scratch": true,
  "deformation": false,
  "res_photo": "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChAFIdni/6wAAAABJRU5ErkJggg==",
  "res_gif": "R0lGODdhAQABAIAAAP///wAAACwAAAAAAQABAAACAkQBADs=",
  "percentage": 87
}
```

**Поля ответа:**
- `category` - Категория повреждения (1-5)
- `dent` - Наличие вмятины (boolean)
- `rust` - Наличие ржавчины (boolean)
- `scratch` - Наличие царапины (boolean)
- `deformation` - Наличие деформации (boolean)
- `res_photo` - Обработанное изображение в формате base64
- `res_gif` - Анимированный GIF результата в формате base64
- `percentage` - Процент уверенности (0-100)

**Тестирование с curl:**
```bash
curl -X POST http://localhost:8080/prediction/api/submit-photo \
  -F "file=@/path/to/image.jpg" \
  -H "Content-Type: multipart/form-data"
```

## Конфигурация ML API

Сервис интегрирован с ML API для анализа изображений. Конфигурация осуществляется через переменные среды:

| Переменная | Описание | Значение по умолчанию |
|------------|----------|----------------------|
| `ML_API_HOST` | Хост ML API сервиса | `localhost` |
| `ML_API_PORT` | Порт ML API сервиса | `8000` |
| `ML_API_ENDPOINT` | Endpoint ML API | `/predict` |
| `ML_API_TIMEOUT` | Таймаут запросов (мс) | `30000` |

**Пример ML API ответа:**
```json
{
  "car_bbox": [100, 150, 200, 250],
  "damage_ratio": 0.15,
  "detections": {
    "rust": [
      {
        "bbox": {
          "x_min": 100,
          "y_min": 150,
          "x_max": 120,
          "y_max": 170,
          "width": 20,
          "height": 20
        },
        "confidence": 0.95
      }
    ],
    "dent": [],
    "scratch": []
  }
}
```

### Запуск ML API

Убедитесь, что ML API запущен на указанном хосте и порту перед использованием сервиса предсказаний.

**Пример запуска ML API (если у вас есть соответствующий сервис):**
```bash
# Запуск ML API на localhost:8000
python ml_api_server.py --host localhost --port 8000
```

## Документация API

После запуска приложения доступна документация Swagger UI:
- Swagger UI: http://localhost:8080/prediction/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/prediction/v3/api-docs

## Проверка работоспособности

Проверьте, что сервис запущен:
```bash
curl http://localhost:8080/prediction/api/ping
```

Проверьте health endpoint:
```bash
curl http://localhost:8080/prediction/actuator/health
```

## Тестирование

Запуск тестов:
```bash
mvn test
```