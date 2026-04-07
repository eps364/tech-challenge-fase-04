# Antigravity Instructions - Tech Challenge Fase 04

Este arquivo define as diretrizes obrigatórias para a atuação do Antigravity neste repositório. Para facilitar o reuso e a organização, o contexto foi separado em arquivos específicos na pasta `.agent/context/`.

## Fontes de Contexto Obrigatórias

Sempre consulte os arquivos abaixo para garantir que todas as interações e gerações de código estejam alinhadas com as definições do projeto:

1.  **Regras de Negócio**: [.agent/context/business_rules.md](.agent/context/business_rules.md) - Domínio, fluxos de pedido/pagamento e eventos.
2.  **Arquitetura**: [.agent/context/architecture.md](.agent/context/architecture.md) - Estrutura de microsserviços, Clean Architecture e Banco de Dados.
3.  **Boas Práticas**: [.agent/context/best_practices.md](.agent/context/best_practices.md) - Padrões de código, naming, commits e testes.
4.  **Tecnologias**: [.agent/context/technologies.md](.agent/context/technologies.md) - Stack tecnológica detalhada.
5.  **Histórico e Mantenedores**: [.agent/context/tech_history.md](.agent/context/tech_history.md) - Contexto histórico das tecnologias utilizadas.
6.  **Sugestões e Evolução**: [.agent/context/suggestions.md](.agent/context/suggestions.md) - Próximos passos e melhorias sugeridas.
7.  **Detalhamento de RFs**: [.agent/feature/](.agent/feature/) - Detalhamento funcional de cada requisito (RF-01 a RF-06).

## Princípios Mandatórios

1.  **Rich Domain Model**: A lógica de negócio **deve** residir nas entidades de domínio e objetos de valor dentro do `core`. Evite serviços anêmicos que apenas passam dados.
2.  **Pure Java Core**: O pacote `core` deve ser **estritamente Java puro**. Nenhuma anotação de framework (JPA, Spring, Jakarta, Jackson) ou dependência de infraestrutura é permitida.
3.  **SOLID e DRY**: Aplique rigorosamente os princípios SOLID. Use interfaces (portas) para desacoplar o `core` de implementações externas (adaptadores).
4.  **Saga Pattern**: Fluxos complexos entre serviços devem ser orquestrados pelo `orchestrator-service` via RabbitMQ.

## Regra de Ouro
Toda sugestão de código deve preservar a rastreabilidade do status do pedido e a clareza arquitetural dos microsserviços. Não introduzir dependências que quebrem a execução via Docker Compose.

---
**Nota**: Em caso de conflito, as instruções específicas nos arquivos de contexto em `.agent/context/` têm prioridade sobre resumos gerais. Consulte também `.github/copilot-instructions.md` para diretrizes adicionais.

