FROM openjdk:26-ea-slim
WORKDIR /app
RUN mkdir -p /app/logs
COPY auth-service-*.jar /app/app.jar
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
