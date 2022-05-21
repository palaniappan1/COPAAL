FROM openjdk:8-jdk-alpine
RUN apk add --no-cache curl tar bash procps
RUN apk update && apk add git
RUN git clone "https://github.com/palaniappan1/COPAAL"

FROM maven:3.6.0-jdk-11-slim AS build
COPY /service/pom.xml .
RUN mvn -B -f pom.xml dependency:go-offline
COPY . /app
COPY service/target/corraborative-2.2.2.jar /app
EXPOSE 3333
ENTRYPOINT ["java","-jar","/app/corraborative-2.2.2.jar"]
