# 1-ci mərhələ: Maven ilə proqramı yığırıq
FROM maven:3.8.5-openjdk-17 AS build
COPY . .
RUN mvn clean package -DskipTests

# 2-ci mərhələ: Yalnız lazım olan JAR faylını işə salırıq
FROM openjdk:17.0.1-jdk-slim
COPY --from=build /target/shopify-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]