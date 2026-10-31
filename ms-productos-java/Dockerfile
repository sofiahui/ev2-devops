#  ---------- Etapa 1: compilar ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
# Copiamos primero el pom para aprovechar la cache de dependencias
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B package -DskipTests

# ---------- Etapa 2: imagen final liviana ----------
FROM eclipse-temurin:24-jre-alpine
WORKDIR /app
# Usuario sin privilegios (seguridad)
RUN addgroup -S app && adduser -S app -G app
COPY --from=build /app/target/ms-productos.jar app.jar
USER app
EXPOSE 8081
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"
HEALTHCHECK --interval=10s --timeout=3s --start-period=40s --retries=5 \
  CMD wget -qO- http://localhost:8081/actuator/health || exit 1
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]