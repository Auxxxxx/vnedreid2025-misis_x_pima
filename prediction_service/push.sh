#!/bin/bash

set -e

echo "🚀 Pushing Prediction Service to DockerHub..."
echo "============================================="

# Загружаем переменные среды
if [ -f .env ]; then
    source .env
    echo "✅ Loaded environment variables from .env"
else
    echo "⚠️  No .env file found, using default image tag"
    export PREDICTION_SERVICE_IMAGE="auxxxxx/vnedreid2025-prediction_service:latest"
fi

IMAGE_TAG="$PREDICTION_SERVICE_IMAGE"
echo "📌 Pushing image: $IMAGE_TAG"

# Проверяем наличие Docker
if ! command -v docker &> /dev/null; then
    echo "❌ ERROR: Docker не установлен. Установите Docker и попробуйте снова."
    exit 1
fi

# Проверяем, что образ существует локально
if ! docker image inspect "$IMAGE_TAG" &> /dev/null; then
    echo "❌ ERROR: Образ $IMAGE_TAG не найден локально."
    echo "💡 Запустите ./build.sh для сборки образа"
    exit 1
fi

# Проверяем, что пользователь авторизован в Docker Hub
echo "🔐 Checking Docker Hub authentication..."
if ! docker info | grep -q "Username:"; then
    echo "⚠️  You are not logged in to Docker Hub"
    echo "💡 Please run: docker login"
    read -p "Do you want to login now? (y/n): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        docker login
    else
        echo "❌ Cannot push without Docker Hub authentication"
        exit 1
    fi
fi

# Пушим образ
echo "🐳 Pushing Docker image to DockerHub..."
docker push "$IMAGE_TAG"

if [ $? -eq 0 ]; then
    echo ""
    echo "🎉 SUCCESS! Image pushed to DockerHub!"
    echo "=================================="
    echo "📦 Image: $IMAGE_TAG"
    echo "🌐 DockerHub: https://hub.docker.com/r/auxxxxx/vnedreid2025-prediction_service"
    echo ""
    echo "💡 To pull this image on another machine:"
    echo "   docker pull $IMAGE_TAG"
    echo ""
else
    echo "❌ Ошибка при отправке образа в DockerHub"
    exit 1
fi 