# Etapa 1: compila o jar com o JDK
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Baixa as dependências antes de copiar o código, para aproveitar o cache do Docker
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN sh ./mvnw -B -q dependency:go-offline

COPY src/ src/
RUN sh ./mvnw -B -q package -DskipTests

# Etapa 2: imagem final só com o JRE e o jar
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
