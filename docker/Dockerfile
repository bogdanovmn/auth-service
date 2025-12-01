FROM openjdk:26-ea-slim
WORKDIR /app
RUN mkdir -p /app/logs
RUN mkdir -p /app/jwt
COPY auth-service-*.jar /app/app.jar
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
