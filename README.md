# Building URL Shortener API using Java Spring-Boot & GridDB

**Prerequisites**:

- [Java OpenJDK 17](https://bit.ly/openjdk1706)
- [Docker 23.0.1](https://docs.docker.com/engine/install/)

## Technology Stack
Back-end: Spring Boot, Maven\
Database: GridDB 5.1.0


## Run Application
Build the docker image: 
```shell
docker compose build
```

Run the app with Docker Compose:

```shell
docker compose up
```


#### The following ports are exposed:
* The application runs on port 8080.
* Port 35729 allows the livereload plugin to listen to changes
* Port 5005 makes the debugger available to the IDE

#### Reference Documentation
* [Being able to automatically build and deploy changes to without having to manually build or restart the application](https://medium.com/trantor-inc/developing-spring-boot-applications-in-docker-locally-4ec922f4cb45)
* [livereload using spring-boot-devtools](https://docs.spring.io/spring-boot/docs/current/reference/html/using.html#using.devtools)

##### tags: `url-shortener` `shortener` `griddb` `spring-boot` `json-api` `docker` `live-reload` `inotify-tools`