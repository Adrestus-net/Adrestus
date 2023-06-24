FROM ubuntu:latest
WORKDIR /app
RUN apt-get update \
 && apt-get install -y sudo

RUN adduser --disabled-password --gecos '' docker
RUN adduser docker sudo
RUN echo '%sudo ALL=(ALL) NOPASSWD:ALL' >> /etc/sudoers
RUN sudo apt-get update
#################################################################################
FROM maven:3.8.6-openjdk-11 as maven_build
WORKDIR /app
#copy pom
#RUN cd ./module2 && mvn clean install && cd ..
COPY ./ .
RUN ls
#COPY /module3/pom.xml .
#resolve maven
RUN mvn clean install -Dmaven.test.skip -Dmaven.main.skip -Dspring-boot.repackage.skip
#RUN cd module3 && mvn clean package shade:shade-Dmaven.test.skip -Dmaven.main.skip -Dspring-boot.repackage.skip
#copy source
COPY adrestus-protocol/src /adrestus-protocol/src
# build the app (no dependency download here)
RUN mvn clean package -Dmaven.test.skip
#COPY target/original-docker2-1.0-SNAPSHOT.jar .

########JRE run stage########
FROM openjdk:11.0-jre
WORKDIR /app

#copy built app layer by layer
ARG DOCKER_TARGET=/app/adrestus-protocol/target/
COPY --from=maven_build ${DOCKER_TARGET}  /app/
#COPY --from=maven_build ${DOCKER_PACKAGING_DIR}/META-INF /app/META-INF

RUN ls
RUN cd /app && ls
#ENTRYPOINT java -jar ${JAR_NAME}
ENTRYPOINT ["java", "-jar", "adrestus-protocol-1.0-SNAPSHOT-jar-with-dependencies.jar"]
