# Coding Rules - Tech Challenge Fase 04

Estas regras devem ser seguidas rigorosamente pelo Antigravity ao gerar ou modificar código.

## Naming Conventions

### Use Cases
- **Pattern**: `<Action><Entity>UseCase.java`
- **Example**: `CreateOrderUseCase`, `ProcessPaymentUseCase`.
- **Method**: O método principal deve ser `execute()`.

### Ports (Interfaces no Core)
- **Pattern**: `<Entity><Resource>Port.java`
- **Example**: `OrderRepositoryPort`, `PaymentGatewayPort`.

### Adapters (Implementações na Infra)
- **Pattern**: `<Resource>Adapter.java` ou `<Resource>Impl.java` (se for Gateway).
- **Example**: `OrderRepositoryAdapter`, `KeycloakGatewayImpl`.

### DTOs
- **Pattern**: `<Entity>Request.java` e `<Entity>Response.java`.
- **Format**: Use Java `record` sempre que possível.

## Project Structure (Clean Architecture)

Ao criar um novo recurso ou microsserviço, siga a estrutura:

```
src/main/java/br/com/fiap/<service>/
├── core/
│   ├── domain/
│   │   ├── entity/      <-- Rich entities (POJOs)
│   │   └── valueobject/ <-- Immutable objects
│   ├── usecase/         <-- Business logic orchestration
│   ├── gateway/         <-- Interfaces (Ports)
│   └── dto/             <-- Records for application communication
└── infra/
    ├── entity/          <-- JPA Entities (@Entity)
    ├── repository/      <-- Spring Data Interfaces
    ├── gateway/         <-- Implementations of Core Ports (Adapters)
    └── web/
        └── controller/  <-- Spring RestControllers
```

## Validation Rules
1. **No Frameworks in Core**: Nunca importe `org.springframework.*`, `jakarta.persistence.*`, ou `com.fasterxml.jackson.*` no pacote `core`.
2. **Rich Domain**: Se houver lógica de validação (ex: "pedido não pode ser vazio"), ela deve estar na `Entity` de domínio, não no `UseCase`.
3. **Field Validation Messages**: Todo erro de validação deve ser em **Inglês** e indicar claramente qual campo está com problema.
    - **Example**: "The name cannot be empty or null", "The quantity must be greater than zero".
4. **Manual Mapping**: O mapeamento de `Domain` -> `Infra Entity` e vice-versa deve ser feito manualmente no `Adapter`.
