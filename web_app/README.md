# Web Application

## 🎯 Описание

Веб-приложение для определения повреждений автомобилей. Позволяет загружать фотографии автомобилей и получать детальный анализ повреждений с визуализацией результатов.

## 🛠 Технологический стек

- **Frontend Framework**: React 18
- **UI Library**: Material-UI (MUI)
- **State Management**: Redux Toolkit
- **HTTP Client**: Axios
- **Build Tool**: Vite
- **Type Checking**: TypeScript
- **Code Quality**: ESLint + Prettier
- **Testing**: Jest + React Testing Library

## 📁 Структура проекта

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

## 🚀 Быстрый старт

### Предварительные требования

- Node.js 18 или выше
- npm 9 или выше

### Установка

```bash
# Клонирование репозитория
git clone <repository-url>
cd web_app

# Установка зависимостей
npm install
```

### Разработка

```bash
# Запуск dev-сервера
npm run dev

# Сборка для продакшена
npm run build

# Запуск тестов
npm test
```

## 🌐 API Интеграция

Приложение интегрируется с двумя сервисами:

1. **Prediction Service** (http://localhost:8080)
   - Анализ изображений
   - Получение результатов детекции

2. **ML API** (http://localhost:8000)
   - Прямое взаимодействие с ML моделью
   - Получение raw-данных детекции

## 📱 Основные функции

- Загрузка изображений автомобилей
- Визуализация результатов анализа
- Отображение типов повреждений:
  - Вмятины
  - Ржавчина
  - Царапины
  - Деформации
- Анимированное отображение результатов
- История загруженных изображений

## 🎨 UI/UX Особенности

- Современный Material Design
- Адаптивный дизайн
- Темная/светлая тема
- Анимации для улучшения UX
- Drag & Drop загрузка файлов
- Интерактивная визуализация повреждений

## 🔧 Конфигурация

Основные настройки приложения находятся в файле `.env`:

```env
VITE_API_URL=http://localhost:8080
VITE_ML_API_URL=http://localhost:8000
VITE_MAX_FILE_SIZE=10485760  # 10MB в байтах
```

## 🧪 Тестирование

```bash
# Запуск unit-тестов
npm test

# Запуск e2e-тестов
npm run test:e2e

# Проверка типов
npm run type-check
```

## 📦 Сборка и деплой

```bash
# Сборка для продакшена
npm run build

# Предпросмотр собранного приложения
npm run preview
```

## 🤝 Вклад в разработку

1. Форкните репозиторий
2. Создайте ветку для ваших изменений
3. Внесите изменения
4. Создайте Pull Request

## 📄 Лицензия

MIT
