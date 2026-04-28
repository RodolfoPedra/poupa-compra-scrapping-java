FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B

FROM azul/zulu-openjdk-debian:21

WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends \
    chromium \
    fonts-freefont-ttf \
    libnss3 \
    libfreetype6 \
    libharfbuzz0b \
    ca-certificates \
    iputils-ping \
    dbus \
    wget \
    && rm -rf /var/lib/apt/lists/*

ENV PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1

COPY --from=builder /app/target/ .

ENV SCRAPING_BROWSER_POOL_SIZE=3
ENV SCRAPING_BROWSER_HEADLESS=true
ENV SCRAPING_BROWSER_EXECUTABLE_PATH=/usr/bin/chromium
ENV URL_POUPA_COMPRA_API=http://localhost:8182

EXPOSE 5005
EXPOSE 8181

CMD [ "sh", "-c", "java -jar poupa-compra-scraping-1.0.0.jar" ]
