# URL - Shortener

A spring-boot service for shortening URLs

## How it works
> This service takes a long URL and generates a shortened version that redirects to the original URL. It uses Spring Boot and stores the URL mappings in a postgres database.

## Getting started & Running this project
> ### Requirements
> - Java 21
> - Gradle

### Steps
> - Clone this repository
> - Create the `.env` file in your project root based on the `.env.example` provided. Or replace database environment variables directly in application.properties 
> - Run the Command `gradle bootRun` to start the server
> - It should be accessible on `http://localhost:{SERVER_PORT}`

## Developers
- [Martial Mutabaruka](https://github.com/katros1)
- [Patrick Nshimiyimana](https://github.com/Patricknshimiyimana)
