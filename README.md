# spring-6-reactive-web-client
Welcome to the Reactive Programming with Spring Framework project! This project is a practical exploration of reactive programming using Spring Framework 5,
designed to help you understand and implement reactive systems. Here's a quick guide to get you started:

## Project Purpose
The main goal of this project is to demonstrate how to build reactive applications using Spring Framework. It depends on a backend service (project spring-6-reactive-mongo)
that interacts with MongoDB, showcasing how to handle asynchronous data streams effectively.

## Getting Started
* Backend Project spring-6-reactive-mongo Started listening on port 8083. The Backend is interacting with the MondoDB (either native running on mongodb://localhost:27017 or as Docker TestContainer
  which does not require installed MongoDB instance
* Docker Desktop: Required for running TestContainers, which are used for testing in a Docker environment.

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

## Web Interface

This application includes a web interface that allows users to interact with the beer data through a browser. The web interface provides the following features:

- View a paginated list of beers
- Navigate through pages of beer listings
- View details of individual beers

To access the web interface, start the application and navigate to: 
- http://localhost:8087/beers
- http://localhost:30087/beers

