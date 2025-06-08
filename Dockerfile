FROM gradle:8.1.1-jdk17 AS builder

WORKDIR /build

COPY build.gradle settings.gradle ./
COPY gradle gradle
RUN gradle dependencies || true

COPY src src
RUN gradle clean build -x test

FROM openjdk:17-jdk AS runner

WORKDIR /app

COPY --from=builder /build/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]