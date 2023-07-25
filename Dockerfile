FROM eclipse-temurin:17-jdk-alpine
RUN mkdir /opt/app
COPY build/libs/*.jar /opt/app.jar
ENTRYPOINT ["java","-jar","/opt/app.jar"]