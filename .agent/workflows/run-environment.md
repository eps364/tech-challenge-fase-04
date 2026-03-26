---
description: Como rodar o ambiente completo do projeto com Docker Compose
---

# Fluxo de Execução do Ambiente Completo

Este workflow descreve os passos para construir e iniciar o ecossistema de microsserviços do Tech Challenge Fase 03.

## Pré-requisitos
- Docker Desktop ou Docker Engine com Compose habilitado.
- Java 17+ instalado (para build Maven opcionalmente).

---

## 🐳 Modo Produção (build completo)

Gera imagem Docker otimizada (JRE slim) para cada serviço.

1. **Subir todos os serviços:**
```bash
docker compose up -d --build
```

2. **Verificar o status dos serviços:**
```bash
docker compose ps
```

3. **Parar tudo e limpar volumes:**
```bash
docker compose down -v
```

---

## 🔥 Modo Dev (hot reload)

Usa `maven:3.9.9-eclipse-temurin-21` com `spring-boot:run` e `spring-boot-devtools`. Qualquer alteração em `.java` ou `resources` reinicia automaticamente o contexto do serviço (~3-5s), sem rebuild de imagem Docker.

1. **Subir o ambiente dev (infraestrutura + serviços com hot reload):**
// turbo
```bash
docker compose -f compose.yml -f compose.dev.yml up -d
```

2. **Verificar logs de um serviço (confirmar LiveReload ativo):**
```bash
docker compose -f compose.yml -f compose.dev.yml logs -f auth-service
```
> Procure por: `LiveReload server is running on port 35729`

3. **Rebuild de um serviço específico (se necessário):**
```bash
docker compose -f compose.yml -f compose.dev.yml restart auth-service
```

4. **Parar o ambiente dev:**
```bash
docker compose -f compose.yml -f compose.dev.yml down
```
