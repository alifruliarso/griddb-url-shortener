# FROM eclipse-temurin:11
FROM eclipse-temurin:17.0.6_10-jdk-jammy
RUN apt-get update && apt-get -y upgrade
RUN apt-get install -y inotify-tools dos2unix
ENV HOME=/app
RUN mkdir -p $HOME
WORKDIR $HOME