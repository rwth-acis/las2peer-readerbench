FROM openjdk:14-jdk-alpine

ENV LAS2PEER_PORT=9011
ENV DATABASE_NAME=LAS2PEERMON
ENV DATABASE_HOST=mysql-service.ma-zeufack
ENV DATABASE_PORT=3306
ENV DATABASE_USER=user
ENV DATABASE_PASSWORD=root
ENV RB_API_URL=http://rb-controller.ma-zeufack:32446
ENV L2P_ENDPOINT=http://137.226.232.75:32445/

RUN apk add --update bash mysql-client tzdata curl && rm -f /var/cache/apk/*
ENV TZ=Europe/Berlin

RUN addgroup -g 1000 -S las2peer && \
    adduser -u 1000 -S las2peer -G las2peer

COPY --chown=las2peer:las2peer . /src
WORKDIR /src

RUN mkdir reports
RUN chmod -R 777 reports
RUN chmod -R a+rwx /src
RUN chmod +x /src/docker-entrypoint.sh
# run the rest as unprivileged user
USER las2peer
#RUN gradle startscripts

EXPOSE $LAS2PEER_PORT
ENTRYPOINT ["/src/docker-entrypoint.sh"]