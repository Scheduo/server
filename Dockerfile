FROM bellsoft/liberica-openjdk-alpine:17 AS builder

WORKDIR /app

COPY build.gradle settings.gradle gradle/ ./

RUN ./gradlew dependencies

COPY . .

RUN ./gradlew build -x test --no-daemon

FROM bellsoft/liberica-openjdk-alpine:17

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

ENTRYPOINT ["java","-jar","app.jar"]