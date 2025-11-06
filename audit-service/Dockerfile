FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /app

COPY pom.xml .
COPY ./.mvn .mvn/
COPY mvnw .
COPY "mvnw.cmd" .
COPY ./src ./src

RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8084

ENTRYPOINT ["java", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", "-jar", "app.jar"]