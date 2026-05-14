# Deploy via GitHub Actions

Este projeto faz deploy automatico para `prod` quando houver push na branch `main`.

## Configuracao escolhida

- Repositorio: `eps364/tech-challenge-fase-04`
- Branch de deploy: `main`
- Conta AWS: `510997984143`
- Regiao AWS: `us-east-1`
- Ambiente Terraform: `prod`
- Bucket sugerido para state: `tech-challenge-fase-04-prod-tfstate-510997984143`
- IAM role sugerida: `tech-challenge-fase-04-github-deploy`

## GitHub Secrets e Variables

Em `Settings > Secrets and variables > Actions`, crie:

Secrets:

- `SES_FROM_EMAIL`: e-mail verificado no SES para envio
- `REPORT_RECIPIENT_EMAIL`: e-mail que recebe os relatorios
- `ADMIN_ALERT_EMAIL`: e-mail que recebe alertas

Variables:

- `TF_STATE_BUCKET`: `tech-challenge-fase-04-prod-tfstate-510997984143`

## Protecao de acesso

A role abaixo so pode ser assumida por workflows deste repositorio na branch `main`, via OIDC. Isso evita guardar `AWS_ACCESS_KEY_ID` e `AWS_SECRET_ACCESS_KEY` no GitHub.

Como o deploy para producao e automatico, proteja a branch `main` no GitHub:

- Exija pull request antes de merge.
- Exija que os checks passem antes do merge.
- Restrinja quem pode fazer push direto na `main`.
- Nao compartilhe o arquivo `terraform.tfstate`; ele pode conter dados sensiveis.

Observacao: a workflow `deploy-dev.yml` existente ainda usa access keys de longa duracao. Para manter o mesmo modelo seguro em `dev`, crie outra role OIDC e remova esses secrets quando nao forem mais necessarios.

## Bootstrap unico na AWS

Execute os comandos abaixo uma vez no AWS CloudShell, autenticado na conta `510997984143`, na regiao `us-east-1`.

```bash
export AWS_REGION="us-east-1"
export AWS_ACCOUNT_ID="510997984143"
export GITHUB_REPO="eps364/tech-challenge-fase-04"
export GITHUB_BRANCH="main"
export STATE_BUCKET="tech-challenge-fase-04-prod-tfstate-510997984143"
export ROLE_NAME="tech-challenge-fase-04-github-deploy"
export OIDC_PROVIDER_ARN="arn:aws:iam::${AWS_ACCOUNT_ID}:oidc-provider/token.actions.githubusercontent.com"
```

Crie o bucket do Terraform state:

```bash
if ! aws s3api head-bucket --bucket "${STATE_BUCKET}" 2>/dev/null; then
  aws s3api create-bucket \
    --bucket "${STATE_BUCKET}" \
    --region "${AWS_REGION}"
fi

aws s3api put-public-access-block \
  --bucket "${STATE_BUCKET}" \
  --public-access-block-configuration BlockPublicAcls=true,IgnorePublicAcls=true,BlockPublicPolicy=true,RestrictPublicBuckets=true

aws s3api put-bucket-encryption \
  --bucket "${STATE_BUCKET}" \
  --server-side-encryption-configuration '{"Rules":[{"ApplyServerSideEncryptionByDefault":{"SSEAlgorithm":"AES256"}}]}'

aws s3api put-bucket-versioning \
  --bucket "${STATE_BUCKET}" \
  --versioning-configuration Status=Enabled
```

Crie o OIDC provider do GitHub:

```bash
if ! aws iam get-open-id-connect-provider \
  --open-id-connect-provider-arn "${OIDC_PROVIDER_ARN}" >/dev/null 2>&1; then
  aws iam create-open-id-connect-provider \
    --url https://token.actions.githubusercontent.com \
    --client-id-list sts.amazonaws.com
fi
```

Crie a trust policy da role:

```bash
cat > /tmp/github-actions-trust-policy.json <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Federated": "${OIDC_PROVIDER_ARN}"
      },
      "Action": "sts:AssumeRoleWithWebIdentity",
      "Condition": {
        "StringEquals": {
          "token.actions.githubusercontent.com:aud": "sts.amazonaws.com",
          "token.actions.githubusercontent.com:sub": "repo:${GITHUB_REPO}:ref:refs/heads/${GITHUB_BRANCH}"
        }
      }
    }
  ]
}
EOF
```

Crie ou atualize a role:

```bash
if ! aws iam get-role --role-name "${ROLE_NAME}" >/dev/null 2>&1; then
  aws iam create-role \
    --role-name "${ROLE_NAME}" \
    --assume-role-policy-document file:///tmp/github-actions-trust-policy.json
else
  aws iam update-assume-role-policy \
    --role-name "${ROLE_NAME}" \
    --policy-document file:///tmp/github-actions-trust-policy.json
fi
```

Crie a policy de deploy:

Se voce for configurar pelo Console da AWS em vez do CloudShell, use o JSON ja preenchido em `docs/aws-console-oidc-setup.md`. Os placeholders `${STATE_BUCKET}` e `${AWS_ACCOUNT_ID}` abaixo dependem da substituicao feita pelo shell.

```bash
cat > /tmp/github-actions-deploy-policy.json <<EOF
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
      "Resource": "arn:aws:s3:::${STATE_BUCKET}"
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
      "Resource": "arn:aws:s3:::${STATE_BUCKET}/tech-challenge-fase-04/prod/*"
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
      "Resource": "arn:aws:iam::${AWS_ACCOUNT_ID}:role/tech-challenge-fase-04-prod-*"
    },
    {
      "Sid": "PassProjectIamRoles",
      "Effect": "Allow",
      "Action": "iam:PassRole",
      "Resource": "arn:aws:iam::${AWS_ACCOUNT_ID}:role/tech-challenge-fase-04-prod-*",
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
EOF

aws iam put-role-policy \
  --role-name "${ROLE_NAME}" \
  --policy-name "tech-challenge-fase-04-prod-deploy" \
  --policy-document file:///tmp/github-actions-deploy-policy.json
```

## SES

O Terraform cria a identidade de e-mail no SES, mas a AWS envia uma mensagem de verificacao para esse endereco. Abra o e-mail e confirme o link antes de testar o envio.

Se sua conta SES ainda estiver em sandbox, remetente e destinatarios precisam estar verificados na mesma regiao. Para enviar para qualquer destinatario, solicite acesso de producao no SES.
