FROM eclipse-temurin:23-jdk AS build

RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:23-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

#ENTRYPOINT ["java", "-Djasypt.encryptor.password=${JASYPT_ENCRYPTOR_PASSWORD}", "-jar", "app.jar"]

ENTRYPOINT ["java", "-jar", "app.jar"]