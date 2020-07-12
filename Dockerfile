FROM adoptopenjdk/openjdk11:jdk-11.0.7_10-alpine-slim
MAINTAINER Selvedin Dokara <selvedin.dokara@gmail.com>

WORKDIR /opt/app

ADD target/app.jar .
ADD start.sh .
ENTRYPOINT ["./start.sh"]
