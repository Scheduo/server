#FROM gradle:8.1.1-jdk17 AS builder
#
#WORKDIR /build
#
#COPY build.gradle settings.gradle ./
#COPY gradle gradle
#RUN gradle dependencies || true
#
#COPY src src
#RUN gradle clean build -x test --no-daemon --stacktrace
#
#FROM openjdk:17-jdk AS runner
#
#WORKDIR /app
#
#COPY --from=builder /build/build/libs/*.jar app.jar
#
#ENTRYPOINT ["java", "-jar", "app.jar"]

FROM bellsoft/liberica-openjdk-alpine:17 AS builder

WORKDIR /app

COPY . .

RUN ./gradlew clean build -x test


# Run stage

FROM bellsoft/liberica-openjdk-alpine:17

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

ENTRYPOINT ["java","-jar","app.jar"]