# Overview

This is a web app that can crawl a domain. It uses [Java](https://www.java.com/en/), [Gradle](https://gradle.org/), [Spring Boot](https://spring.io/projects/spring-boot), [Spring Cloud Stream](https://spring.io/projects/spring-cloud-stream), [Kafka](https://kafka.apache.org/), [Liquibase](https://www.liquibase.org/), [Docker](https://www.docker.com/) and [JSoup](https://jsoup.org/).


# Running the app

As a pre-requisite, docker needs to be installed.

Run below command in the project's root folder to start PostgreSql, Zookeeper and Kafka in docker:
```
docker-compose up -d
```

Then run the app using below command:
```
./gradlew bootRun
```

Once the start up completed, the app will be listening at http://localhost:8080/


# Testing the app
Import the postman collection under src/test/postman to [Postman](https://www.postman.com/).

- Send the `Start crawling` request to start the crawler process.
- Send the `Crawler Job Stats` request to check the progress of the crawling
- Send the `Crawler Pages` request to get a site map of the crawled domain
