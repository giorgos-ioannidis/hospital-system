# Demo στο Render: H2 στη μνήμη με τα demo δεδομένα (profile dev). Κάθε επανεκκίνηση ξεκινά καθαρά.
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN ./mvnw -q dependency:go-offline
COPY src src
RUN ./mvnw -q -DskipTests package && cp target/*.jar app.jar

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/app.jar app.jar
ENV TZ=Europe/Athens \
    JAVA_TOOL_OPTIONS="-Xmx256m -Xss512k -XX:+UseSerialGC -XX:MaxMetaspaceSize=160m -XX:ReservedCodeCacheSize=48m -XX:TieredStopAtLevel=1" \
    SPRING_PROFILES_ACTIVE=dev \
    SPRING_THYMELEAF_CACHE=true \
    SERVER_FORWARD_HEADERS_STRATEGY=framework
EXPOSE 8080
CMD ["sh", "-c", "java -Dserver.port=${PORT:-8080} -jar app.jar"]
