# Maven Build Stage
FROM maven:3.6-openjdk-17-slim as maven_build

WORKDIR /app
COPY . .

# build the app and download dependencies only when these are new (thanks to the cache)
RUN --mount=type=cache,target=/root/.m2  mvn clean package -Dmaven.test.skip

# JRE Run Stage
FROM debian:bullseye as jre_run
WORKDIR /app

RUN apt-get update -y && \
    apt-get install -y g++ && \
    apt-get install -y make && \
    apt-get install -y build-essential && \
    apt-get install -y openjdk-17-jre

# copy all files including the config file(s)
WORKDIR /app
COPY . .

# copy the target directory to run stage
WORKDIR /app
COPY --from=maven_build /app/core/target target

# run the built JAR file
WORKDIR /app
ENTRYPOINT ["java", "-jar", "target/service.jar"]