# Best Practices - Tech Challenge Fase 04

## Core Principles
- **SOLID**:
    - **S**: Use Cases must have a single responsibility.
    - **O**: Extend functionality through new Use Cases or Adapters, not modifying existing core logic.
    - **L**: Ensure Subtypes of Domain Entities/Value Objects can replace their base types.
    - **I**: Keep Port interfaces small and focused.
    - **D**: Depend on Ports (interfaces), not implemented Adapters.
- **DRY (Don't Repeat Yourself)**: Reuse validation logic and domain rules within the `core`. Shared mapping logic should be in base classes or static utilities.
- **KISS (Keep It Simple, Stupid)**: Avoid over-engineering. If a simple POJO/Record suffices, don't use complex patterns. Orchestrator use cases should be thin dispatchers.
- **YAGNI (You Ain't Gonna Need It)**: Do not implement features or abstract layers "for the future". Only implement what the current business rule requires.

## Development Standards
- **Naming**: 
    - Classes/Methods/Variables: English.
    - Packages: All lowercase, no underscores (e.g., `br.com.fiap.{service-name}.core`).
    - DTOs: Use `record` for application DTOs in `core.dto`. `Request`/`Response` naming convention.
- **Response Format**: 
    - Error handling follows **RFC 7807** (Problem Details) using `ProblemDetail` and `@RestControllerAdvice`.
    - **Validation Messages**: Must be in **English** and specify the field (e.g., "The price must be positive").

## Version Control
- **Conventional Commits**: Use `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`.
- **Language**: Commit messages in English.

## Documentation
- **API Collections**: Maintain Bruno collections in `docs/API`. 
    - Icons: Domain (`🔐`, `👤`, `🧾`, `💳`, etc.) + Permission (`🌐`, `🙋`, `👑`).
- **Diagrams**: Sequence diagrams in `docs/diagrams` must illustrate flows, events, and resilience points.

## Quality
- **Testing**:
    - Unit tests for all business logic changes (Core).
    - Integration tests for cross-service flows (Infra).
- **Performance**: Prevent changes that break Docker Compose execution or build (`./mvnw clean install`).
