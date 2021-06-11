# OlympusSpring

OlympusSpring is a backend for the [OlympusBlog](https://github.com/sentrionic/OlympusBlog) stack using [Spring](https://spring.io/).

## Stack
- Spring related packages for everything

## Getting started

0. Install Java and Maven
1. Clone this repository
2. Install Postgres and Redis.
3. Open the project in IntelliJ to get all the dependencies.
4. Rename `appsettings.properties.example` in `src/main/resources` to `appsettings.properties`
   and fill out the values. AWS is only required if you want file upload,
   GMail if you want to send reset emails.
5. Run `mvn spring-boot:run`.