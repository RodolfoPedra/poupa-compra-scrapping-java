FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B

FROM azul/zulu-openjdk-debian:21

WORKDIR /app
ARG JAVA_OPTS 
ARG JAVA_DEBUG_OPTS
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

ENV JAVA_OPTS=${JAVA_OPTS}
ENV JAVA_DEBUG_OPTS=${JAVA_DEBUG_OPTS}

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8181/hello || exit 1

EXPOSE 5005 
EXPOSE 8181

CMD [ "sh", "-c", "java $JAVA_OPTS $JAVA_DEBUG_OPTS -jar poupa-compra-scraping-1.0.0.jar" ]
