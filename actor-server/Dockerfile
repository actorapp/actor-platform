FROM openjdk:8u92-jre-alpine
MAINTAINER Actor LLC <oss@actor.im>
RUN apk --update add bash openssl apr
ADD target/docker/stage/var /var
ADD templates /var/lib/actor/templates
ENTRYPOINT bin/actor
WORKDIR /var/lib/actor
EXPOSE 9070 9080 9090
