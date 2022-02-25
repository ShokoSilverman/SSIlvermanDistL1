FROM openjdk:11

ENV DB_HOST=csc380mysqllab1
ENV DB_PORT=3306
ENV DB_USER=root
ENV DB_PASSWORD=testtest1
ENV RABBIT_NAME=some-rabbit
ENV RABBIT_PORT=5672

COPY  target/SSIlvermanDistL1-0.0.1-SNAPSHOT.jar /usr/local/tomcat/webapps/RetroExchangeAPI.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/usr/local/tomcat/webapps/RetroExchangeAPI.jar"]