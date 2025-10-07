# Book Collection API (Java / Spring Boot)

[![Java CI with Maven](https://github.com/methods/N_BookAPIV.3/actions/workflows/build-and-test.yml/badge.svg)](https://github.com/methods/N_BookAPIV.3/actions/workflows/build-and-test.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

A RESTful API for managing a collection of books and their reservations, built with Java and the Spring Boot framework. This project serves as a follow-up to a similar API built in Python/Flask, with a focus on applying enterprise-grade architectural patterns from the ground up.

This project was developed as part of a technical mentorship program, emphasizing Test-Driven Development (TDD), API-first design, and modern software engineering principles.

## Features

-   **Full CRUD for Resources**: Complete Create, Read, Update, and Delete operations for `Book` and `Reservation` resources.
-   **Authentication**: Secure, session-based authentication using Google OAuth2/OIDC.
-   **Authorization**: Fine-grained access control with both role-based (`ROLE_USER`, `ROLE_ADMIN`) and resource-ownership rules.
-   **Pagination**: Paginated responses for collection endpoints (e.g., `GET /books`).
-   **HATEOAS**: API responses include hypermedia links to related resources, making the API discoverable.
-   **Comprehensive Testing**: A full suite of unit and integration tests to ensure code quality and correctness.
-   **CI/CD**: Automated builds and testing via a GitHub Actions workflow.

## Architectural Principles

This project was built with a "foundation-first" philosophy, emphasizing a clean and scalable architecture.

-   **Layered Architecture**: A strict separation of concerns between the Controller (web), Service (business logic), and Repository (data access) layers.
-   **API-First Design**: The API contract is defined in an `openapi.yml` specification. Java interfaces and DTOs are generated from this contract, ensuring the implementation never drifts from the spec.
-   **Test-Driven Development (TDD)**: Every feature was developed by first writing a failing test that specifies the required behavior, followed by the minimum code to make it pass.
-   **Dependency Injection (DI)**: Leverages Spring's IoC container to manage components ("Beans") and their dependencies, promoting loose coupling and high testability.
-   **Secure by Default**: Utilizes Spring Security to create a secure application, with explicit rules for authentication and authorization.

## API Endpoints

| Method | Path                                                  | Description                              | Authorization Required      |
| :----- | :---------------------------------------------------- | :--------------------------------------- | :-------------------------- |
| `POST` | `/books`                                              | Create a new book.                       | `Admin`                     |
| `GET`  | `/books`                                              | Get a paginated list of all books.       | `Authenticated User`        |
| `GET`  | `/books/{id}`                                         | Get a single book by its ID.             | `Authenticated User`        |
| `PUT`  | `/books/{id}`                                         | Update an existing book.                 | `Admin`                     |
| `DELETE`| `/books/{id}`                                         | Delete a book.                           | `Admin`                     |
| `POST` | `/books/{bookId}/reservations`                        | Create a reservation for a book.         | `Authenticated User`        |
| `GET`  | `/reservations`                                       | List reservations. (User sees own, Admin sees all). | `Authenticated User`        |
| `GET`  | `/books/{bookId}/reservations/{reservationId}`        | Get a specific reservation.              | `Owner` or `Admin`          |
| `DELETE`| `/books/{bookId}/reservations/{reservationId}`        | Cancel a reservation.                    | `Owner` or `Admin`          |

## Getting Started

### Prerequisites

-   **Java JDK 21** or later.
-   **Apache Maven** 3.8 or later.
-   **Docker** (with Docker Desktop or a compatible runtime like Colima) for running a local MongoDB instance.

### Configuration

1.  **Google OAuth2 Credentials**: This project uses Google for authentication. You must create your own OAuth 2.0 Client ID in the Google Cloud Console.
    -   Set **Authorized JavaScript origins** to `http://localhost:8080`.
    -   Set **Authorized redirect URIs** to `http://localhost:8080/login/oauth2/code/google`.

2.  **Local Properties File**: The application loads secrets from an uncommitted local properties file.
    -   Create a file at `src/main/resources/application-local.properties`.
    -   **This file is already in `.gitignore` and will not be committed.**
    -   Add your Google credentials to this file:
        ```properties
        # src/main/resources/application-local.properties
        spring.security.oauth2.client.registration.google.client-id=YOUR_GOOGLE_CLIENT_ID
        spring.security.oauth2.client.registration.google.client-secret=YOUR_GOOGLE_CLIENT_SECRET
        ```

### Running the Application

1.  **Start MongoDB**: Make sure your Docker daemon is running and start a MongoDB container.
    ```bash
    docker run -d -p 27017:27017 --name mongo-dev mongo
    ```

2.  **Run from your IDE**:
    -   Open the project in IntelliJ IDEA.
    -   Navigate to `src/main/java/com/bookapi/book_api/BookApiApplication.java`.
    -   Click the green "play" button to run the application.

3.  **Run from the Command Line**:
    ```bash
    mvn spring-boot:run
    ```

The application will be available at `http://localhost:8080`.

### Running the Tests

To run the complete suite of unit and integration tests:
```bash
mvn clean verify
```
## Technology Stack

| Category          | Technologies                                                                 |
|-------------------|------------------------------------------------------------------------------|
| **Framework**     | Spring Boot 3                                                                |
| **Security**      | Spring Security (OAuth2 Client, Method Security)                            |
| **Database**      | Spring Data MongoDB                                                          |
| **API Documentation** | OpenAPI 3 / SpringDoc                                                     |
| **Hypermedia**    | Spring HATEOAS                                                               |
| **Testing**       | JUnit 5, Mockito, AssertJ                                                    |
| **Build Tool**    | Apache Maven                                                                |
| **Utilities**     | Lombok                                                                       |
