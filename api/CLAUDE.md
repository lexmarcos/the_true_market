# CS2 Skin Market Analysis API

## Project Overview

This is a RESTful API for analyzing Counter-Strike 2 (CS2) skin market data to determine whether a skin purchase is worthwhile based on multiple data points. The API consumes messages posted by bots via RabbitMQ for processing and analysis.

## Technical Stack

### Core Framework
- **Spring Boot**: 3.5.7
- **Java**: 17
- **Build Tool**: Gradle (Groovy)

### Currently Installed Dependencies
1. Spring for RabbitMQ — (MESSAGING)
2. Spring Data JPA — (SQL)
3. PostgreSQL Driver — (SQL)
4. Spring Web — (WEB)
5. Lombok — (DEVELOPER TOOLS)
6. Spring REST Docs — (TESTING)

> **Important**: Whenever a new library is installed, update this list immediately.

### Database
- **PostgreSQL**

## Architecture Requirements

### Clean Architecture
This project MUST follow Clean Architecture principles with clear separation of concerns:
- **Entities/Domain Layer**: Core business logic and domain models
- **Use Cases/Application Layer**: Business rules and application-specific logic
- **Interface Adapters Layer**: Controllers, presenters, and gateways
- **Frameworks & Drivers Layer**: External frameworks, databases, and message brokers

### SOLID Principles
Strictly adhere to SOLID principles:
- **S**ingle Responsibility Principle
- **O**pen/Closed Principle
- **L**iskov Substitution Principle
- **I**nterface Segregation Principle
- **D**ependency Inversion Principle

### RESTful API Standards
The API MUST rigorously follow REST architectural style principles:
- Proper use of HTTP methods (GET, POST, PUT, DELETE, PATCH)
- Resource-based URLs (nouns, not verbs)
- Stateless communication
- Proper HTTP status codes
- HATEOAS when applicable
- Content negotiation
- API versioning strategy

## Messaging Architecture

### RabbitMQ Integration
- The API consumes messages from bots via RabbitMQ
- Messages contain skin market data for analysis
- Implement proper message handlers and error handling
- Use dead letter queues for failed message processing

## Documentation Guidelines

### Markdown File Rules
- **DO NOT** create any `.md` files at the end of tasks
- **ONLY** edit `.md` files when explicitly instructed to do so
- This README should be updated only when specifically requested

## Development Guidelines

1. Follow Clean Architecture layer boundaries strictly
2. Implement dependency injection properly
3. Write testable code with clear interfaces
4. Use Lombok annotations to reduce boilerplate
5. Document API endpoints using Spring REST Docs
6. Implement proper exception handling and validation
7. Use DTOs for data transfer between layers
8. Keep domain entities pure and framework-agnostic

## Expected Functionality

The API should analyze CS2 skins based on:
- Current market price
- Historical price trends
- Market volatility
- Supply and demand metrics
- Rarity and condition factors
- Investment potential indicators
- Other relevant market data points

## Project Structure Recommendation
```
src/main/java/
├── domain/           # Entities and business logic
├── application/      # Use cases and application services
├── infrastructure/   # Framework implementations
│   ├── persistence/  # JPA repositories and entities
│   ├── messaging/    # RabbitMQ consumers and producers
│   └── web/         # REST controllers
└── config/          # Configuration classes
```

---

**Important**: Don't need to run the API after a modification

**Remember**: This is a production-grade API. Code quality, maintainability, and adherence to architectural principles are paramount.