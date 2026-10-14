FROM maven:3.9-eclipse-temurin-11 AS builder

WORKDIR /app

COPY settings.xml /root/.m2/settings.xml
COPY pom.xml .

RUN mvn -B -e org.apache.maven.plugins:maven-dependency-plugin:3.6.1:go-offline

COPY src ./src

RUN mvn -B -e package -DskipTests

FROM eclipse-temurin:11-jre-alpine

RUN apk add --no-cache curl tzdata

WORKDIR /app

COPY --from=builder /app/target/ticket-system-1.0.0.jar app.jar

RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "-Dfile.encoding=UTF-8", "app.jar"]