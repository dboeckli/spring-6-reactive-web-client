# spring-6-reactive-web-client
Welcome to the Reactive Programming with Spring Framework project! This project is a practical exploration of reactive programming using Spring Framework 5, 
designed to help you understand and implement reactive systems. Here's a quick guide to get you started:

## Project Purpose
The main goal of this project is to demonstrate how to build reactive applications using Spring Framework. It depends on a backend service (project spring-6-reactive-mongo)
that interacts with MongoDB, showcasing how to handle asynchronous data streams effectively.

## Getting Started
To start contributing, ensure you have the following set up:
* Spring Framework 6: The backbone of our reactive application.
* Backend Project spring-6-reactive-mongo Started listening on port 8080. The Backend is interacting with the MondoDB (either native running on mongodb://localhost:27017 or as Docker TestContainer 
which does not require installed MongoDB instance
* Docker Desktop: Required for running TestContainers, which are used for testing in a Docker environment.

Remark: The unit tests are disabled because they require that the backend is up and running. Locally those tests requires that you have started the backend. This requirement
is currently not possible in the github pipeline and the build would fail there.

```plaintext
+---------+               +----------------+               +--------------------+
| Client  |               | Gateway Server |               | Authentication     |
| (makes  |  -----------> | (Port 8080)    |  -----------> | Server (Port 9000) |
| request)|  <----------- |                |  <----------- | (returns token)    |
+---------+               +----------------+               +--------------------+
                                |   ^  
                                |   |
                                v   |
                           +-------------------+               
                           | Reactive-Mongo    |
                           | (Port 8083)       |
                           | (Executes         |
                           | query and         |
                           | creates           |
                           | response)         |
                           +-------------------+
```

## Spring Framework Guru Course
This repository has examples from my course [Reactive Programming with Spring Framework 5](https://www.udemy.com/reactive-programming-with-spring-framework-5/?couponCode=GITHUB_REPO_SF5B2G)

## All Spring Framework Guru Courses
### Spring Framework 6
* [Spring Framework 6 - Beginner to Guru](https://www.udemy.com/course/spring-framework-6-beginner-to-guru/?referralCode=2BD0B7B7B6B511D699A9)
* [Spring AI: Beginner to Guru](https://www.udemy.com/course/spring-ai-beginner-to-guru/?referralCode=EF8DB31C723FFC8E2751)
* [Hibernate and Spring Data JPA: Beginner to Guru](https://www.udemy.com/course/hibernate-and-spring-data-jpa-beginner-to-guru/?referralCode=251C4C865302C7B1BB8F)
* [API First Engineering with Spring Boot](https://www.udemy.com/course/api-first-engineering-with-spring-boot/?referralCode=C6DAEE7338215A2CF276)
* [Introduction to Kafka with Spring Boot](https://www.udemy.com/course/introduction-to-kafka-with-spring-boot/?referralCode=15118530CA63AD1AF16D)
* [Spring Security: Beginner to Guru](https://www.udemy.com/course/spring-security-core-beginner-to-guru/?referralCode=306F288EB78688C0F3BC)
