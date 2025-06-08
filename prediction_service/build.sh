#!/bin/bash

# Цвета для вывода
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Функция для вывода сообщений
print_message() {
    echo -e "${BLUE}🚀 $1${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_message "Building Prediction Service..."
echo "================================="

# Устанавливаем переменные среды
PREDICTION_SERVICE_IMAGE="auxxxxx/vnedreid2025-prediction_service:latest"

echo "📌 Image tag: $PREDICTION_SERVICE_IMAGE"

# Записываем переменные в .env файл
echo "PREDICTION_SERVICE_IMAGE=\"$PREDICTION_SERVICE_IMAGE\"" > .env
echo "CONTEXT_PATH=\"/prediction\"" >> .env

# ML API Configuration
echo "ML_API_HOST=109.73.196.162" >> .env
echo "ML_API_PORT=80" >> .env
echo "ML_API_ENDPOINT=/model/damage-detection/analyze" >> .env
echo "ML_API_TIMEOUT=30000" >> .env

print_success "Environment variable set locally: PREDICTION_SERVICE_IMAGE=$PREDICTION_SERVICE_IMAGE"
print_success "Context path set locally: CONTEXT_PATH=/prediction"
print_success "ML API configuration set locally"

print_message "Building JAR file..."
mvn clean package -DskipTests

print_message "Building Docker image: $PREDICTION_SERVICE_IMAGE"
docker build -t $PREDICTION_SERVICE_IMAGE .

print_success "Docker image built successfully: $PREDICTION_SERVICE_IMAGE"

print_message "Stopping existing containers..."
docker compose down

print_message "Starting services with Docker Compose..."
docker compose up -d

print_success "SUCCESS! Prediction Service is running!"
echo "=================================="
echo -e "${GREEN}🌐 Service URL: http://localhost:8080/prediction${NC}"
echo -e "${GREEN}📊 Health check: http://localhost:8080/prediction/actuator/health${NC}"
echo -e "${GREEN}🔍 API docs: http://localhost:8080/prediction/swagger-ui.html${NC}"
echo -e "${GREEN}📡 Ping endpoint: http://localhost:8080/prediction/api/ping${NC}"
echo ""
echo -e "${BLUE}📋 Useful commands:${NC}"
echo "  - View logs: docker compose logs -f"
echo "  - Stop service: docker compose down"
echo "  - Restart: docker compose restart" 