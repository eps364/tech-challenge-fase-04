# Tech Challenge - Fase 04

## Visão Geral
Plataforma de feedback para cursos online, hospedada em ambiente cloud, utilizando funções serverless para automação do recebimento de feedbacks, envio de notificações e geração de relatórios. O sistema permite que estudantes avaliem aulas e administradores acompanhem relatórios e alertas críticos.

## Objetivo
Desenvolver uma aplicação em nuvem, com funções serverless para:
- Receber feedbacks dos alunos
- Enviar notificações automáticas para itens críticos
- Gerar relatórios periódicos para análise

## Arquitetura da Solução
- Ambiente cloud configurado (ex: Azure, AWS, GCP)
- Funções serverless separadas por responsabilidade (ex: recebimento de feedback, envio de notificação, geração de relatório)
- Banco de dados gerenciado (armazenar feedbacks)
- Monitoramento e alertas
- Governança de acesso e segurança dos dados

## Endpoints
### POST /avaliacao
Recebe um feedback do aluno.

**Payload:**
```json
{
	"descricao": "string",
	"nota": 0
}
```
*nota: inteiro de 0 a 10*

## Dados para Notificação de Urgência
- Descrição
- Urgência
- Data de envio

## Dados para Relatório Semanal
- Descrição
- Urgência
- Data de envio
- Quantidade de avaliações por dia
- Quantidade de avaliações por urgência

## Critérios de Avaliação
- Explicação do modelo de cloud e componentes
- Funcionamento correto da aplicação
- Qualidade do código e documentação
- Arquitetura da solução, instruções de deploy, monitoramento e documentação das funções
- Configuração do ambiente cloud e funções serverless, com explicação das escolhas e segurança

## Artefatos de Entrega
- Repositório aberto com código-fonte
- Vídeo de demonstração do sistema, funções serverless e configurações

## Instruções de Deploy
1. Configure o ambiente cloud e recursos necessários (funções, banco, monitoramento)
2. Faça o deploy automatizado das funções serverless
3. Garanta as configurações de segurança e governança
4. Monitore a aplicação e valide notificações/relatórios

## Referências
- Ver docs/ para diagramas, exemplos de API e detalhes técnicos