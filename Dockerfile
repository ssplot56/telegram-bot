FROM openjdk:21-ea-1-jdk-slim

WORKDIR /app

# Copy the JAR file of your Spring Boot application to the /app directory
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

# Install H2 database using apt-get, including the unzip package
RUN apt-get update && apt-get install -y curl unzip \
    && curl -o h2.zip http://www.h2database.com/h2-2019-10-14.zip \
    && unzip h2.zip \
    && rm h2.zip \
    && apt-get remove -y curl unzip \
    && apt-get autoremove -y \
    && rm -rf /var/lib/apt/lists/*

# Set the entry point to run the application and H2 database
ENTRYPOINT ["java", "-jar", "/app/app.jar", "&", "sh", "/app/h2/bin/h2.sh", "-webAllowOthers", "-tcpAllowOthers"]
