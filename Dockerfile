# Етап 1: Збірка Backend
FROM eclipse-temurin:23-jdk AS backend-build

RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

WORKDIR /backend

COPY backend/pom.xml .
RUN mvn dependency:go-offline -B

COPY backend/src ./src
RUN mvn clean package -DskipTests

# Етап 2: Збірка Frontend
FROM node:24-alpine AS frontend-build
WORKDIR /frontend

RUN apk update && apk upgrade && apk add --no-cache libc6-compat

COPY frontend/package.json frontend/package-lock.json* ./
RUN npm ci

COPY frontend/ .
RUN npm run build

# Етап 3: Фінальний образ
FROM eclipse-temurin:23-jre AS runner

# Встановлюємо Node.js для запуску frontend
RUN apt-get update && \
    apt-get install -y curl && \
    curl -fsSL https://deb.nodesource.com/setup_24.x | bash - && \
    apt-get install -y nodejs && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Копіюємо backend JAR
COPY --from=backend-build /backend/target/*.jar backend.jar

# Копіюємо frontend build
COPY --from=frontend-build /frontend/.next ./.next
COPY --from=frontend-build /frontend/public ./public
COPY --from=frontend-build /frontend/node_modules ./node_modules
COPY --from=frontend-build /frontend/package.json ./package.json

# Створюємо startup script
RUN echo '#!/bin/bash\n\
java -jar backend.jar &\n\
BACKEND_PID=$!\n\
npm start &\n\
FRONTEND_PID=$!\n\
\n\
trap "kill $BACKEND_PID $FRONTEND_PID; exit" SIGTERM SIGINT\n\
\n\
wait $BACKEND_PID $FRONTEND_PID' > /app/start.sh && \
    chmod +x /app/start.sh

EXPOSE 3000 8080

ENV NODE_ENV=production
ENV PORT=3000
ENV HOSTNAME="0.0.0.0"

CMD ["/app/start.sh"]