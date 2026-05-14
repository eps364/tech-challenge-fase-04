# Setup OIDC pelo Console da AWS

Use este guia se voce nao conseguir usar o AWS CloudShell.

## O que voce cria manualmente

Crie manualmente apenas o bootstrap necessario para o GitHub Actions executar o Terraform:

| Item | Nome | Onde criar |
| --- | --- | --- |
| IAM OIDC provider | `token.actions.githubusercontent.com` | AWS Console, IAM |
| IAM role de deploy | `tech-challenge-fase-04-github-deploy` | AWS Console, IAM |
| Inline policy da role de deploy | `tech-challenge-fase-04-prod-deploy` | Dentro da role `tech-challenge-fase-04-github-deploy` |
| Bucket S3 do Terraform state | `tech-challenge-fase-04-prod-tfstate-510997984143` | AWS Console, S3 |
| GitHub variable | `TF_STATE_BUCKET` | GitHub Actions variables |
| GitHub secrets | `SES_FROM_EMAIL`, `REPORT_RECIPIENT_EMAIL`, `ADMIN_ALERT_EMAIL` | GitHub Actions secrets |

Nao crie manualmente as roles da aplicacao. O Terraform cria e atualiza estas roles e policies:

| Item Terraform | Nome criado em `prod` | Arquivo |
| --- | --- | --- |
| Role da Lambda avaliador | `tech-challenge-fase-04-prod-avaliador-role` | `infra/terraform/iam.tf` |
| Inline policy da Lambda avaliador | `tech-challenge-fase-04-prod-avaliador-policy` | `infra/terraform/iam.tf` |
| Role da Lambda reports generator | `tech-challenge-fase-04-prod-reports-generator-role` | `infra/terraform/iam.tf` |
| Inline policy da Lambda reports generator | `tech-challenge-fase-04-prod-reports-generator-policy` | `infra/terraform/iam.tf` |
| Role da Lambda email sender | `tech-challenge-fase-04-prod-email-sender-role` | `infra/terraform/iam.tf` |
| Inline policy da Lambda email sender | `tech-challenge-fase-04-prod-email-sender-policy` | `infra/terraform/iam.tf` |
| Role do EventBridge Scheduler | `tech-challenge-fase-04-prod-scheduler-role` | `infra/terraform/eventbridge.tf` |
| Inline policy do Scheduler | `tech-challenge-fase-04-prod-scheduler-invoke-lambda` | `infra/terraform/eventbridge.tf` |
| Permissao da Lambda para API Gateway | `AllowApiGatewayInvoke` | `infra/terraform/api-gateway.tf` |

A role manual `tech-challenge-fase-04-github-deploy` e a unica role que o workflow assume. As demais sao criadas pelo Terraform durante o deploy.

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
5. Cole a policy abaixo.
6. Nome da inline policy:

```text
tech-challenge-fase-04-prod-deploy
```

> Importante: nao cole no Console a policy do guia CloudShell mantendo placeholders como `${STATE_BUCKET}` ou `${AWS_ACCOUNT_ID}`. No CloudShell esses valores sao substituidos pelo shell; no Console eles ficam literais e a role nao ganha acesso ao bucket real, causando erro `403 Forbidden` no `terraform init`.

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "TerraformStateBucket",
      "Effect": "Allow",
      "Action": [
        "s3:GetBucketLocation",
        "s3:GetBucketVersioning",
        "s3:ListBucket"
      ],
      "Resource": "arn:aws:s3:::tech-challenge-fase-04-prod-tfstate-510997984143"
    },
    {
      "Sid": "TerraformStateObjects",
      "Effect": "Allow",
      "Action": [
        "s3:DeleteObject",
        "s3:GetObject",
        "s3:GetObjectVersion",
        "s3:PutObject"
      ],
      "Resource": "arn:aws:s3:::tech-challenge-fase-04-prod-tfstate-510997984143/tech-challenge-fase-04/prod/*"
    },
    {
      "Sid": "ReadCallerIdentity",
      "Effect": "Allow",
      "Action": "sts:GetCallerIdentity",
      "Resource": "*"
    },
    {
      "Sid": "ManageProjectResources",
      "Effect": "Allow",
      "Action": [
        "apigateway:*",
        "cloudwatch:DeleteAlarms",
        "cloudwatch:DeleteDashboards",
        "cloudwatch:DescribeAlarms",
        "cloudwatch:GetDashboard",
        "cloudwatch:ListDashboards",
        "cloudwatch:ListTagsForResource",
        "cloudwatch:PutDashboard",
        "cloudwatch:PutMetricAlarm",
        "cloudwatch:TagResource",
        "cloudwatch:UntagResource",
        "dynamodb:CreateTable",
        "dynamodb:DeleteTable",
        "dynamodb:DescribeContinuousBackups",
        "dynamodb:DescribeTable",
        "dynamodb:DescribeTimeToLive",
        "dynamodb:ListTables",
        "dynamodb:ListTagsOfResource",
        "dynamodb:TagResource",
        "dynamodb:UntagResource",
        "dynamodb:UpdateTable",
        "dynamodb:UpdateTimeToLive",
        "lambda:AddPermission",
        "lambda:CreateEventSourceMapping",
        "lambda:CreateFunction",
        "lambda:DeleteEventSourceMapping",
        "lambda:DeleteFunction",
        "lambda:GetEventSourceMapping",
        "lambda:GetFunction",
        "lambda:GetFunctionCodeSigningConfig",
        "lambda:GetPolicy",
        "lambda:ListEventSourceMappings",
        "lambda:ListTags",
        "lambda:ListVersionsByFunction",
        "lambda:RemovePermission",
        "lambda:TagResource",
        "lambda:UntagResource",
        "lambda:UpdateEventSourceMapping",
        "lambda:UpdateFunctionCode",
        "lambda:UpdateFunctionConfiguration",
        "logs:*",
        "scheduler:CreateSchedule",
        "scheduler:DeleteSchedule",
        "scheduler:GetSchedule",
        "scheduler:ListSchedules",
        "scheduler:ListTagsForResource",
        "scheduler:TagResource",
        "scheduler:UntagResource",
        "scheduler:UpdateSchedule",
        "ses:DeleteIdentity",
        "ses:GetIdentityDkimAttributes",
        "ses:GetIdentityVerificationAttributes",
        "ses:ListIdentities",
        "ses:ListTagsForResource",
        "ses:TagResource",
        "ses:UntagResource",
        "ses:VerifyEmailIdentity",
        "sns:CreateTopic",
        "sns:DeleteTopic",
        "sns:GetSubscriptionAttributes",
        "sns:GetTopicAttributes",
        "sns:ListSubscriptions",
        "sns:ListSubscriptionsByTopic",
        "sns:ListTagsForResource",
        "sns:SetTopicAttributes",
        "sns:Subscribe",
        "sns:TagResource",
        "sns:Unsubscribe",
        "sns:UntagResource",
        "sqs:CreateQueue",
        "sqs:DeleteQueue",
        "sqs:GetQueueAttributes",
        "sqs:GetQueueUrl",
        "sqs:ListQueues",
        "sqs:ListQueueTags",
        "sqs:SetQueueAttributes",
        "sqs:TagQueue",
        "sqs:UntagQueue"
      ],
      "Resource": "*"
    },
    {
      "Sid": "ManageProjectIamRoles",
      "Effect": "Allow",
      "Action": [
        "iam:CreateRole",
        "iam:DeleteRole",
        "iam:DeleteRolePolicy",
        "iam:GetRole",
        "iam:GetRolePolicy",
        "iam:ListAttachedRolePolicies",
        "iam:ListInstanceProfilesForRole",
        "iam:ListRolePolicies",
        "iam:PutRolePolicy",
        "iam:TagRole",
        "iam:UntagRole",
        "iam:UpdateAssumeRolePolicy"
      ],
      "Resource": "arn:aws:iam::510997984143:role/tech-challenge-fase-04-prod-*"
    },
    {
      "Sid": "PassProjectIamRoles",
      "Effect": "Allow",
      "Action": "iam:PassRole",
      "Resource": "arn:aws:iam::510997984143:role/tech-challenge-fase-04-prod-*",
      "Condition": {
        "StringEquals": {
          "iam:PassedToService": [
            "lambda.amazonaws.com",
            "scheduler.amazonaws.com"
          ]
        }
      }
    }
  ]
}
```

Se o workflow falhar com `Cannot read Terraform state object`, confira primeiro estes pontos:

- A role assumida no log precisa ser `arn:aws:iam::510997984143:role/tech-challenge-fase-04-github-deploy`.
- A inline policy precisa apontar para `arn:aws:s3:::tech-challenge-fase-04-prod-tfstate-510997984143` e para `arn:aws:s3:::tech-challenge-fase-04-prod-tfstate-510997984143/tech-challenge-fase-04/prod/*`.
- Se o bucket usa SSE-KMS em vez de SSE-S3, a role tambem precisa de permissoes na chave KMS usada pelo bucket.

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
