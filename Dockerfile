#Builder Image
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
#Copying project dependencies 
COPY pom.xml . 
#Force-downloading project dependencies
RUN mvn dependency:go-offline -B 
COPY src ./src 
#Building the final maven image
RUN mvn package -DskipTests -B 

#Runtime Image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
#Copying only the final jar file 
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
#Running app
ENTRYPOINT ["java","-jar","app.jar"]