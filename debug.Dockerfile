FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B

FROM mcr.microsoft.com/playwright/java:v1.58.0-jammy

WORKDIR /app
ARG JAVA_OPTS 
ARG JAVA_DEBUG_OPTS

ENV PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1
ENV PLAYWRIGHT_BROWSERS_PATH=/ms-playwright

COPY exemplos ./exemplos

COPY --from=builder /app/target/ .

ENV JAVA_OPTS=${JAVA_OPTS}
ENV JAVA_DEBUG_OPTS=${JAVA_DEBUG_OPTS}
ENV SCRAPING_BROWSER_POOL_SIZE=3
ENV SCRAPING_BROWSER_HEADLESS=true

EXPOSE 5005 
EXPOSE 8181

CMD [ "sh", "-c", "java $JAVA_OPTS $JAVA_DEBUG_OPTS -jar poupa-compra-scraping-1.0.0.jar" ]
