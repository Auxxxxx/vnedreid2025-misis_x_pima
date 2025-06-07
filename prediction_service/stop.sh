#!/bin/bash

set -e

echo "⏹️  Stopping Prediction Service..."
echo "================================="

# Проверяем наличие Docker Compose
if ! command -v docker compose &> /dev/null; then
    echo "❌ ERROR: Docker Compose не установлен. Установите Docker Compose и попробуйте снова."
    exit 1
fi

# Проверяем, есть ли docker-compose.yml
if [ ! -f docker-compose.yml ]; then
    echo "❌ ERROR: docker-compose.yml не найден в текущей директории"
    exit 1
fi

# Останавливаем и удаляем контейнеры
echo "🔄 Stopping Docker Compose services..."
docker compose down

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ SUCCESS! All services stopped and containers removed"
    echo "=================================================="
    echo ""
    echo "📋 Additional cleanup commands:"
    echo "  - Remove volumes: docker compose down -v"
    echo "  - Remove images: docker compose down --rmi all"
    echo "  - View running containers: docker ps"
    echo "  - View all containers: docker ps -a"
    echo ""
    echo "🚀 To restart services: ./build.sh"
    echo ""
else
    echo "❌ Ошибка при остановке сервисов"
    exit 1
fi 