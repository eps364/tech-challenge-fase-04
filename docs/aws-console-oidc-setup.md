# Setup OIDC pelo Console da AWS

Use este guia se voce nao conseguir usar o AWS CloudShell.

## 1. Criar o Identity Provider do GitHub

1. Acesse o Console da AWS na conta `510997984143`.
2. Abra `IAM`.
3. No menu lateral, acesse `Identity providers`.
4. Clique em `Add provider`.
5. Em `Provider type`, escolha `OpenID Connect`.
6. Em `Provider URL`, informe:

```text
https://token.actions.githubusercontent.com
```

7. Em `Audience`, informe:

```text
sts.amazonaws.com
```

8. Confirme em `Add provider`.

## 2. Criar a role do GitHub Actions

1. Ainda no IAM, acesse `Roles`.
2. Clique em `Create role`.
3. Em `Trusted entity type`, escolha `Web identity`.
4. Em `Identity provider`, selecione:

```text
token.actions.githubusercontent.com
```

5. Em `Audience`, selecione:

```text
sts.amazonaws.com
```

6. Se a tela pedir organizacao/repositorio/branch, use:

```text
Organization: eps364
Repository: tech-challenge-fase-04
Branch: main
```

7. Avance. Se a AWS pedir uma policy agora, voce pode criar sem policy e adicionar a inline policy no proximo passo.
8. Nome da role:

```text
tech-challenge-fase-04-github-deploy
```

9. Crie a role.

## 3. Conferir ou ajustar a trust policy

Abra a role `tech-challenge-fase-04-github-deploy`, entre na aba `Trust relationships` e clique em `Edit trust policy`.

Use esta policy:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Federated": "arn:aws:iam::510997984143:oidc-provider/token.actions.githubusercontent.com"
      },
      "Action": "sts:AssumeRoleWithWebIdentity",
      "Condition": {
        "StringEquals": {
          "token.actions.githubusercontent.com:aud": "sts.amazonaws.com",
          "token.actions.githubusercontent.com:sub": "repo:eps364/tech-challenge-fase-04:ref:refs/heads/main"
        }
      }
    }
  ]
}
```

## 4. Criar a policy de permissoes da role

Abra a role `tech-challenge-fase-04-github-deploy`.

1. Aba `Permissions`.
2. Clique em `Add permissions`.
3. Escolha `Create inline policy`.
4. Escolha a aba `JSON`.
5. Cole a policy que esta no arquivo `docs/github-actions-deploy.md`, na secao `Crie a policy de deploy`.
6. Nome da inline policy:

```text
tech-challenge-fase-04-prod-deploy
```

## 5. Criar o bucket do Terraform state pelo Console

1. Abra `S3`.
2. Clique em `Create bucket`.
3. Nome:

```text
tech-challenge-fase-04-prod-tfstate-510997984143
```

4. Regiao:

```text
US East (N. Virginia) us-east-1
```

5. Deixe `Block all public access` ligado.
6. Ative `Bucket Versioning`.
7. Em criptografia, use `Server-side encryption with Amazon S3 managed keys (SSE-S3)`.
8. Crie o bucket.

## 6. Conferir GitHub

No GitHub, confira:

Variable:

```text
TF_STATE_BUCKET=tech-challenge-fase-04-prod-tfstate-510997984143
```

Secrets:

```text
SES_FROM_EMAIL=luizsaraiva50@gmail.com
REPORT_RECIPIENT_EMAIL=luizsaraiva50@gmail.com
ADMIN_ALERT_EMAIL=luizsaraiva50@gmail.com
```

Depois reexecute o workflow.
