# msg-pipeline-orchestrator-v2 — Sesión 08: SAM + SnapStart + CI/CD

## Cambios requeridos antes de deployar

Busca y reemplaza en los archivos indicados:

| Archivo | Placeholder | Tu valor |
|---|---|---|
| `samconfig.toml` | `629742034427` | Tu AWS Account ID |
| `samconfig.toml` | `us-east-1_XXXX` | Tu Cognito User Pool ID |

---

## Paso 1 — Compilar el JAR

```bash
# Generar JAR plano para Lambda
./gradlew clean lambdaJar

# Verificar que el JAR fue generado
ls -lh build/libs/msg-pipeline-orchestrator.jar

# Verificar que las clases estan en la raiz (no en BOOT-INF/)
jar tf build/libs/msg-pipeline-orchestrator.jar | grep LambdaHandler
# Debe mostrar: com/msgpipeline/api/LambdaHandler.class
```

## Paso 2 — Crear bucket S3 (si no existe)

```bash
# Reemplaza ACCOUNT_ID con tu ID real
aws s3 mb s3://msg-pipeline-artifacts-ACCOUNT_ID \
  --region us-east-1

# Habilitar versionado
aws s3api put-bucket-versioning \
  --bucket msg-pipeline-artifacts-ACCOUNT_ID \
  --versioning-configuration Status=Enabled
```

## Paso 3 — Subir JAR a S3

```bash
# Reemplaza ACCOUNT_ID con tu ID real
aws s3 cp build/libs/msg-pipeline-orchestrator.jar \
  s3://msg-pipeline-artifacts-ACCOUNT_ID/lambdas-v2/orchestrator/msg-pipeline-orchestrator-v2-lambda.jar
```

## Paso 4 — Deploy con SAM

```bash
# Instalar SAM CLI (si no lo tienes)
brew tap aws/tap
brew install aws-sam-cli
sam --version

# Primer deploy (interactivo — genera samconfig.toml)
sam build
sam deploy --guided

# Deploys siguientes (usa samconfig.toml)
sam build && sam deploy
```

## Paso 5 — Configurar GitHub Actions

En GitHub → Settings → Secrets and variables → Actions → New repository secret:

| Secret | Valor |
|---|---|
| `AWS_ACCESS_KEY_ID` | Access Key ID de msg-pipeline-dev-user |
| `AWS_SECRET_ACCESS_KEY` | Secret Access Key de msg-pipeline-dev-user |
| `AWS_REGION` | `us-east-1` |
| `S3_BUCKET` | `msg-pipeline-artifacts-ACCOUNT_ID` |
| `LAMBDA_FUNCTION_NAME` | `msg-pipeline-orchestrator-v2` |
| `LAMBDA_ALIAS` | `prod` |

## Handler Lambda

```
com.msgpipeline.api.LambdaHandler::handleRequest
```

## Variables de entorno en Lambda (prod)

| Variable | Valor |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `DYNAMODB_TABLE_NAME` | `msg-pipeline-messages` |
| `SQS_QUEUE_URL` | `https://sqs.us-east-1.amazonaws.com/ACCOUNT_ID/msg-pipeline-queue` |
| `COGNITO_JWK_SET_URI` | `https://cognito-idp.us-east-1.amazonaws.com/us-east-1_POOL_ID/.well-known/jwks.json` |
| `AWS_REGION_NAME` | `us-east-1` |

## Verificar SnapStart

```bash
aws lambda get-function-configuration \
  --function-name msg-pipeline-orchestrator-v2 \
  --query "SnapStart"
# Resultado: {"ApplyOn": "PublishedVersions", "OptimizationStatus": "On"}
```

## Rollback manual (si hay errores en canary)

```bash
# Reemplaza VERSION_ESTABLE con el numero de version anterior
aws lambda update-alias \
  --function-name msg-pipeline-orchestrator-v2 \
  --name prod \
  --function-version VERSION_ESTABLE \
  --routing-config '{}'
```
