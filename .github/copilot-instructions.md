# Instruções para o Copilot - Tech Challenge Fase 04

Este arquivo define como o Copilot deve atuar neste repositório. Ele é dividido em seções que cobrem desde o contexto do desafio até regras específicas de codificação e arquitetura. O objetivo é garantir que o Copilot gere código alinhado com as expectativas do projeto, seguindo as melhores práticas e respeitando as regras de negócio definidas.

## 1) Contexto do desafio

- Tema do desafio: **Qualidade dos cursos on-line**.
- Foco da solução: coletar avaliações/feedbacks de alunos, identificar urgências e apoiar acompanhamento por relatórios.
- Contexto de execução: ambiente cloud com recursos/custos limitados, priorizando simplicidade, rastreabilidade e documentação clara.
- Referência principal: documento do desafio em `docs/10ADJT - Fase 4 - Tech Challenge.pdf`.
- Fontes complementares obrigatórias:
	- `README.md`
	- `docs/document.md`
	- `docs/API/README.md`
	- `docs/API/exemplos.md`
	- `docs/diagrams/architecture.mmd`
	- `docs/diagrams/flows.mmd`
	- `.agent/instructions.md`
	- `.agent/context/*`
	- `.agent/rules/*`

## 2) Objetivo da fase

- Estruturar uma aplicação cloud com funções serverless para:
	- receber feedbacks;
	- acionar notificações para situações urgentes;
	- gerar relatórios periódicos para análise.
- Garantir material de entrega completo: documentação técnica, evidências de funcionamento e clareza arquitetural.

## 3) Requisitos de entrega e avaliação

- O Copilot deve priorizar saídas que facilitem a avaliação do challenge:
	- explicação objetiva da arquitetura e escolhas técnicas;
	- documentação de endpoints e fluxos;
	- instruções reprodutíveis de execução/deploy;
	- consistência entre README, documentação API e diagramas;
	- foco em qualidade de código e segurança.
- Sempre que houver ambiguidade, manter aderência ao enunciado do PDF e registrar suposições explicitamente.

## 4) Requisitos Funcionais (RF)

- RF-01: receber avaliação via endpoint HTTP (`POST /avaliacao`) contendo descrição e nota.
- RF-02: validar dados de entrada (tipos, faixas válidas e campos obrigatórios).
- RF-03: persistir feedbacks em base de dados.
- RF-04: classificar urgência e acionar notificação para casos críticos.
- RF-05: gerar relatório semanal consolidando volume por dia e por urgência.
- RF-06: disponibilizar exemplos de request/response atualizados em `docs/API`.

## 5) Requisitos Não Funcionais (RNF)

- RNF-01: segurança por padrão (segredos fora do código, menor privilégio, governança de acesso).
- RNF-02: observabilidade mínima (logs, métricas e alertas para falhas críticas).
- RNF-03: documentação versionada e coerente com o estado do repositório.
- RNF-04: organização arquitetural com separação de responsabilidades (core x infra quando aplicável).
- RNF-05: clareza e manutenibilidade (nomenclatura consistente, mensagens de validação claras, baixo acoplamento).

## 6) Diretrizes específicas deste repositório

- Estado atual do repositório: foco em **documentação e instruções** (sem código-fonte de aplicação versionado neste momento).
- Ao gerar conteúdo técnico, o Copilot deve:
	- evitar inventar estrutura de código inexistente;
	- propor incrementos compatíveis com os arquivos já presentes;
	- manter coerência entre termos de domínio:
		- avaliação/feedback;
		- urgência/alerta;
		- relatório semanal.
- A pasta `.agent` contém regras mandatórias de arquitetura e desenvolvimento e deve ser consultada antes de qualquer sugestão de implementação.
- Manter os diagramas em `docs/diagrams` sincronizados com os fluxos descritos em `docs/API` e `docs/document.md`.

## 7) Testes e qualidade

- Ao sugerir testes, cobrir no mínimo:
	- validação do payload de `POST /avaliacao`;
	- cenários de urgência e disparo de notificação;
	- consolidação do relatório semanal.
- Em mudanças de documentação:
	- verificar consistência de exemplos JSON entre `docs/API/README.md` e `docs/API/exemplos.md`;
	- garantir que exemplos reflitam o fluxo descrito nos diagramas.
- Seguir princípios de qualidade já registrados em `.agent/context/best_practices.md`.

## 8) Documentação obrigatória

- Sempre preservar e evoluir estes artefatos:
	- `README.md` (visão geral e objetivo);
	- `docs/document.md` (documento principal de entrega);
	- `docs/API/README.md` e `docs/API/exemplos.md` (contratos e exemplos);
	- `docs/diagrams/architecture.mmd` e `docs/diagrams/flows.mmd` (arquitetura e fluxos);
	- referência oficial do desafio em `docs/10ADJT - Fase 4 - Tech Challenge.pdf`.
- Ao atualizar um artefato de documentação, revisar impactos nos demais arquivos listados acima.

## 9) Diretrizes de commits

- Usar Conventional Commits em inglês: `feat`, `fix`, `docs`, `refactor`, `test`, `chore`.
- Mensagens de commit objetivas, descrevendo claramente o que foi alterado e por quê.
- Evitar commits que misturem mudanças sem relação (ex.: API + diagrama + estilo sem vínculo funcional).

## 10) Regra de ouro para o Copilot

- Toda sugestão deve maximizar **clareza arquitetural**, **consistência documental** e **rastreabilidade funcional** com o enunciado do challenge.
- Não quebrar alinhamento entre fontes oficiais (PDF), README, documentação API e diagramas.
- Em caso de lacunas no enunciado, explicitar hipóteses e manter a solução simples, segura e avaliável.

## 11) Contexto adicional obrigatório

- O Copilot deve considerar também a pasta `.agent` como fonte de contexto e instruções do repositório.
- Em caso de conflito entre instruções, aplicar a regra mais específica para o módulo/arquivo alvo.
