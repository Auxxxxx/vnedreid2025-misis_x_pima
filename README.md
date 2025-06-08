# vnedreid2025-misis_x_pima
Решение команды МИСИС х ПИМА на хакатоне Внедрейд 2025. Трек компании Авито "Определение битости машин"

## 📁 Структура проекта

```
.
├── prediction_service/           # Сервис предсказаний на Spring Boot
│   ├── src/                     # Исходный код
│   │   ├── main/
│   │   │   ├── java/           # Java исходники
│   │   │   └── resources/      # Ресурсы приложения
│   │   └── test/               # Тесты
│   ├── test-images/            # Тестовые изображения
│   ├── docker-compose.yml      # Конфигурация Docker Compose
│   ├── Dockerfile              # Конфигурация Docker образа
│   ├── pom.xml                 # Maven конфигурация
│   ├── build.sh               # Скрипт сборки
│   ├── push.sh                # Скрипт публикации
│   └── stop.sh                # Скрипт остановки
├── web_app/                    # Веб-приложение на React
│   ├── src/                   # Исходный код React
│   ├── public/                # Статические файлы
│   └── package.json           # Зависимости npm
└── ml_model/                  # Модель машинного обучения
    ├── ML_models/            # Веса и конфигурация моделей
    ├── services/             # Сервисы для работы с ML
    └── presentations/        # FastAPI приложение
```

## 🚀 Быстрый старт

### Предварительные требования

- Java 8 или выше
- Maven
- Docker и Docker Compose
- Node.js 18 или выше
- npm 9 или выше
- Python 3.8 или выше
- Git

### Запуск всех компонентов

1. **Prediction Service**
```bash
cd prediction_service
./build.sh
```

2. **ML Model**
```bash
cd ml_model
pip install -r requirements.txt
uvicorn presentations.fastapi_app:app --host 0.0.0.0 --port 8000
```

3. **Web Application**
```bash
cd web_app
npm install
npm run dev
```

## 📝 Описание компонентов

### Prediction Service

Spring Boot сервис для анализа изображений автомобилей на предмет повреждений.

#### Основные функции:
- REST API для загрузки и анализа изображений
- Интеграция с ML моделью
- Docker контейнеризация
- Swagger документация

#### API Endpoints:
- **POST** `/api/submit-photo` - Анализ изображения
  - Принимает: изображение (PNG/JPEG, до 10MB)
  - Возвращает: категорию повреждения, типы повреждений, обработанное изображение

- **GET** `/api/ping` - Проверка работоспособности

#### Управление сервисом:
- Запуск: `./build.sh`
- Остановка: `./stop.sh`
- Публикация: `./push.sh`

### Web Application

Веб-приложение для определения повреждений автомобилей. Позволяет загружать фотографии автомобилей и получать детальный анализ повреждений с визуализацией результатов.

#### Технологический стек:
- **Frontend Framework**: React 18
- **UI Library**: Material-UI (MUI)
- **State Management**: Redux Toolkit
- **HTTP Client**: Axios
- **Build Tool**: Vite
- **Type Checking**: TypeScript
- **Code Quality**: ESLint + Prettier
- **Testing**: Jest + React Testing Library

#### Структура проекта:
```
web_app/
├── src/
│   ├── components/        # React компоненты
│   ├── pages/            # Страницы приложения
│   ├── services/         # API сервисы
│   ├── store/            # Redux store
│   ├── types/            # TypeScript типы
│   ├── utils/            # Вспомогательные функции
│   └── App.tsx           # Корневой компонент
├── public/               # Статические файлы
├── tests/                # Тесты
├── .eslintrc.js         # Конфигурация ESLint
├── .prettierrc          # Конфигурация Prettier
├── package.json          # Зависимости
├── tsconfig.json         # Конфигурация TypeScript
└── vite.config.ts        # Конфигурация Vite
```

#### Основные функции:
- Загрузка изображений автомобилей
- Визуализация результатов анализа
- Отображение типов повреждений:
  - Вмятины
  - Ржавчина
  - Царапины
  - Деформации
- Анимированное отображение результатов
- История загруженных изображений

#### UI/UX Особенности:
- Современный Material Design
- Адаптивный дизайн
- Темная/светлая тема
- Анимации для улучшения UX
- Drag & Drop загрузка файлов
- Интерактивная визуализация повреждений

### ML Model

Модель машинного обучения для определения повреждений автомобилей.

#### Архитектура:
- **ML_models/yolo_weights/yolo.pt** — веса YOLO модели для детекции повреждений на изображениях автомобилей
- **web_app.py** — основной скрипт для запуска FastAPI приложения
- **presentations/fastapi_app.py** — FastAPI-приложение, реализующее REST API
- **services/yolo_prediction.py** — сервис для инференса YOLO по изображениям
- **settings/settings.py** — настройки приложения
- **presentations/routers/damage_detection_router.py** — роутер FastAPI
- **requirements.txt** — зависимости Python-проекта

#### Формат ответа ML API:
```json
{
  "car_bbox": [x_min, y_min, x_max, y_max],
  "damage_ratio": 0.15,
  "detections": {
    "rust": [
      {
        "bbox": {"x_min": 100, "y_min": 150, "x_max": 120, "y_max": 170, "width": 20, "height": 20},
        "confidence": 0.95
      }
    ],
    "dent": [...],
    "scratch": [...]
  }
}
```

**Примечания по ML API:**
- Если нет car_bbox, значит фотография некорректная
- У каждого повреждения есть свой bbox и уверенность модели
- При низкой уверенности повреждение не отображается
- На бэкенде может потребоваться пересчет damage_ratio

**Текущее состояние модели:**
- Модель находится в начальной стадии разработки
- В будущем планируется добавление новой модели с LightAutoML
- Требуется доработка для корректной работы в Docker

## 🔧 Конфигурация

### ML API настройки

Сервис предсказаний интегрирован с ML API. Настройка через переменные среды:

| Переменная | Описание | Значение по умолчанию |
|------------|----------|----------------------|
| `ML_API_HOST` | Хост ML API | `localhost` |
| `ML_API_PORT` | Порт ML API | `8000` |
| `ML_API_ENDPOINT` | Endpoint ML API | `/predict` |
| `ML_API_TIMEOUT` | Таймаут запросов (мс) | `30000` |

### Web App настройки

Основные настройки приложения находятся в файле `.env`:

```env
VITE_API_URL=http://localhost:8080
VITE_ML_API_URL=http://localhost:8000
VITE_MAX_FILE_SIZE=10485760  # 10MB в байтах
```

## 📚 Документация

- Prediction Service Swagger UI: http://localhost:8080/prediction/swagger-ui.html
- ML API Swagger UI: http://localhost:8000/docs
- Web App: http://localhost:5173

## 🧪 Тестирование

### Prediction Service
```bash
mvn clean package -Pwith-tests
```

### Web App
```bash
# Запуск unit-тестов
npm test

# Запуск e2e-тестов
npm run test:e2e

# Проверка типов
npm run type-check
```

## 🤝 Вклад в проект

1. Форкните репозиторий
2. Создайте ветку для ваших изменений
3. Внесите изменения
4. Создайте Pull Request

## 📄 Лицензия

MIT
