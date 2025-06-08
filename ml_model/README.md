Структура ответа:

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

короче потом на бэке вычислим подходящее соотношения для классов убитости и damage_ratio
если нет car_bbox, то значит, что фотка некорректная
у косяков есть различные ббоксы, для каждого ббокса есть уверенность модели в ответе
ВАЖНО: если уверенность низкая, то наверное рисовать не стоит
Возможно на бэке придётся пересчитывать damage_ratio

На самом деле модель сейчас очень слабая. В дальнейшем я могу добавить новую модель с LightAutoML.
Для этого придётся немного расширить проектик (впрочем вот бы он просто запустился в докере)

# ML_models

## Архитектура

- **ML_models/yolo_weights/yolo.pt** — веса YOLO модели для детекции повреждений на изображениях автомобилей.
- **web_app.py** — основной скрипт для запуска FastAPI приложения (или вспомогательный).
- **presentations/fastapi_app.py** — FastAPI-приложение, реализующее REST API для работы с моделью.
- **services/yolo_prediction.py** — сервис для инференса YOLO по изображениям.
- **settings/settings.py** — настройки приложения (пути, параметры модели и др.).
- **presentations/routers/damage_detection_router.py** — роутер FastAPI для обработки запросов на детекцию повреждений.
- **requirements.txt** — зависимости Python-проекта.

## Как запустить

1. Установите зависимости:
   ```bash
   pip install -r requirements.txt
   ```
2. Убедитесь, что файл весов YOLO (`ML_models/yolo_weights/yolo.pt`) находится на месте.
3. Запустите FastAPI приложение:
   ```bash
   uvicorn presentations.fastapi_app:app --host 0.0.0.0 --port 8000
   ```
4. После запуска API будет доступен по адресу:
   http://localhost:8000/docs (Swagger UI)