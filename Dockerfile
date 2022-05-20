RUN git clone "https://github.com/palaniappan1/COPAAL"

FROM maven:3.6.0-jdk-11-slim AS build
COPY /service/pom.xml .
RUN mvn -B -f pom.xml dependency:go-offline
COPY . /app
RUN ls -la

EXPOSE 3333
WORKDIR /COPAAL/service/src/main/java/org/dice_research/fc/run
ENTRYPOINT ["java","-jar","/app.jar"]
#CMD ["nc localhost 3333"]

