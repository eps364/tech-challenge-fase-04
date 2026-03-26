# Regra: Clean Architecture (Core/Infra) - Tech Challenge Fase 04

Neste projeto de microsserviços, a arquitetura deve ser dividida rigorosamente entre **CORE** e **INFRA**.

## 1. Módulo Core
O `core` deve ser o coração da aplicação, livre de frameworks externos (sempre que possível) e focado em regras de negócio.
- **`core.domain`**: Contém as entidades, agregados, objetos de valor (VOs) e exceções de domínio.
- **`core.usecase`**: Contém os casos de uso (Lógica de Orquestração).
- **`core.gateway`**: Interfaces (Portas de Saída) que definem como o sistema interage com o externo
- **`core.dto`**: DTOs de aplicação (preferencialmente `record`) usados para entrada/saída dos casos de uso.
- **Regra de Ouro**: Nenhuma classe de `infra` (frameworks, bibliotecas externas, JDBC, JPA, Spring annotations, Jackson) pode ser usada dentro do `core`. O `core` deve possuir apenas **Java puro**. Isso garante que a regra de negócio seja testável e protegida contra mudanças tecnológicas.

## 2. Módulo Infra
O `infra` contém as implementações técnicas e integrações.
- **`infra.persistence`**: Implementações de adaptadores para bancos de dados (JPA, Mongo, etc).
- **`infra.messaging`**: Implementações de produtores/consumidores Kafka.
- **`infra.gateway`**: Implementações reais das portas definidas no Core (consumo de APIs externas).
- **`infra.controller`**: Adaptadores de entrada (RESTEasy, Controllers Spring, Webflux).

## 3. Diretrizes de Injeção de Dependência
- O `core` não deve conhecer classes do `infra`.
- O `infra` conhece as interfaces do `core.gateway` para implementá-las.
- Configurações de Beans do Spring devem preferivelmente ficar em pacotes específicos de configuração no `infra` para injetar implementações `infra` nas portas do `core`.
