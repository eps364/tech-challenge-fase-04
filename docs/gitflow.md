# GitFlow do projeto

Este repositorio adota um fluxo com duas branches fixas:

- `main`: branch de producao, sempre refletindo o que esta aprovado para release.
- `develop`: branch de integracao, onde novas implementacoes entram primeiro.

## Regras

- `main` nao aceita commit direto.
- `develop` nao aceita commit direto.
- Toda nova implementacao deve nascer a partir de `develop`.
- Toda implementacao deve voltar para `develop` por meio de Pull Request.
- A promocao para producao acontece por Pull Request de `develop` para `main`.

## Fluxo esperado

1. Atualize sua branch local `develop`.
2. Crie uma branch de trabalho a partir de `develop`.
3. Desenvolva na branch de trabalho.
4. Abra um Pull Request da branch de trabalho para `develop`.
5. Depois da aprovacao e merge em `develop`, abra um Pull Request de `develop` para `main`.

Exemplo:

```bash
git checkout develop
git pull origin develop
git checkout -b feature/nova-funcionalidade
```

## Convencao de nomes

Padrao recomendado para branches de trabalho:

- `feature/<descricao-curta>`
- `fix/<descricao-curta>`
- `chore/<descricao-curta>`

## Regras de PR aplicadas no repositorio

Este repositorio possui uma automacao para validar o fluxo abaixo:

- PR para `develop`: permitido apenas a partir de branch de trabalho.
- PR para `main`: permitido apenas a partir de `develop`.

## Protecao de branches no GitHub

Para que o fluxo fique realmente bloqueado no remoto, configure protecao para `main` e `develop` com estas regras:

- exigir Pull Request antes do merge;
- exigir pelo menos 1 aprovacao;
- exigir que as conversas estejam resolvidas;
- exigir que os checks `CI` e `GitFlow Guard` passem;
- bloquear pushes diretos.

Se quiser, depois podemos automatizar tambem a configuracao dessas protecoes no GitHub.
