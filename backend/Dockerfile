FROM maven:3-amazoncorretto-11 AS build

WORKDIR /app

COPY pom.xml .

RUN --mount=type=cache,target=/root/.m2 \
  ["mvn", "dependency:go-offline"]

COPY src src

RUN --mount=type=cache,target=/root/.m2 \
  ["mvn", "package", "-DskipTests"]



FROM amazoncorretto:11-alpine

COPY --from=build /app/target/*.jar /app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]