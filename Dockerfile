FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B

FROM mcr.microsoft.com/playwright/java:v1.58.0-jammy

RUN apt-get update && apt-get install -y --no-install-recommends \
    xvfb \
    x11vnc \
    novnc \
    websockify \
    fluxbox \
    wget \
    ca-certificates \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=builder /app/target/ .
    
COPY entrypoint.sh /usr/local/bin/entrypoint.sh
RUN chmod +x /usr/local/bin/entrypoint.sh


ENV PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1
ENV PLAYWRIGHT_BROWSERS_PATH=/ms-playwright
ENV SCRAPING_BROWSER_POOL_SIZE=3
ENV SCRAPING_BROWSER_HEADLESS=false
ENV SCRAPING_DEBUG_UI=true
ENV DEBIAN_FRONTEND=noninteractive


ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]
