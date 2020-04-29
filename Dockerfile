FROM openjdk:8

ENV DISPLAY :10

ARG JAR_FILE=target/auction-1.0-jar-with-dependencies.jar
ARG JAR_LIB_FILE=target/lib/

# copy target/auction-1.0.jar
COPY ${JAR_FILE} app.jar

# java -jar /usr/local/runme/app.jar
ENTRYPOINT ["java","-jar","app.jar"]