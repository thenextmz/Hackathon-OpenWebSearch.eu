FROM debian:bullseye as jre_run

WORKDIR /app

RUN apt-get update -y && \
    apt-get install -y g++ && \
    apt-get install -y make && \
    apt-get install -y build-essential && \
    apt-get install -y openjdk-17-jre

COPY resources /app/resources
COPY lucene-ciff /app/lucene-ciff
COPY scripts /app/scripts
COPY search-service /app/search-service

RUN mkdir /app/lucene

WORKDIR /app/scripts
RUN ./build.sh

WORKDIR /app/search-service
ENTRYPOINT ["java", "-jar", "core/target/service.jar"]
CMD ["--lucene-dir-path", "/app/lucene/", "--parquet-dir-path", "/app/resources/"]