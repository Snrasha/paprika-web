FROM openjdk:8-jre-alpine

MAINTAINER Guillaume Willefert
WORKDIR /dock
ADD Paprika-web.jar /dock
ADD Dockerfile /dock
