# Card

Card service: issuing and managing bank cards (card types, categories, profiles, fees, number patterns, card requests, PIN/CVV2/OTP secrets, access-file export).

The code lives in [`card-service/`](card-service).

## Stack

- Java 21, Spring Boot 3.5
- Maven multi-module build
- Oracle (Flyway migrations), Redis (PAN cache, sessions), HashiCorp Vault (secret encryption)
- Hibernate Envers for auditing
- Docker / Docker Compose, Kubernetes (Kustomize)

## Modules

| Module | Responsibility |
|---|---|
| `card-domain/card-domain-core` | Entities, value objects, enums, domain events, domain service |
| `card-domain/card-application-service` | Ports, commands/queries, handlers, mappers, service implementations |
| `card-infrastructure` | JPA entities and repositories, adapters, Redis, Vault, SSM / Party / credit clients |
| `card-application` | REST controllers and exception handling |
| `card-container` | Spring Boot bootstrap, configuration, `application*.yml`, Flyway migrations, Dockerfile |

The domain modules do not depend on infrastructure (hexagonal architecture, checked by `ArchitectureTest`).

## Features (REST, base path `/api/v1`)

| Path | Feature |
|---|---|
| `/banks` | Banks and BIN codes |
| `/card-category`, `/card-type`, `/card-profile` | Card catalogue |
| `/fee-profile` | Fee profiles |
| `/card-number-pattern` | Card number patterns (Luhn generation) |
| `/card` | Card lifecycle (activate, deactivate, block) and secrets (generate / validate PIN, CVV2, OTP) |
| `/card-request` | Issue, renew and replacement requests, batch processing, access-file generation |
| `/status`, `/reason` | Statuses and action reasons |
| `/card/enums` | Enum lookups |

## Build and run

```bash
cd card-service
mvn clean verify
```

`uaa-client` is resolved from the company Nexus; use `ci/maven-settings.xml` as a template for the credentials.

Run the whole stack locally (Oracle, Redis, Vault and the service):

```bash
cd card-service
cp .env.example .env   # then adjust the values
docker compose up --build
```

The service listens on port `8007`.

## Configuration

Secrets are never stored in the repository. Set them through environment variables (see `card-service/.env.example`), for example `VAULT_TOKEN`, `CARD_DB_PASSWORD`, `REDIS_PASSWORD`. External systems are configured with `SSM_BASE_URL`, `PARTY_BASE_URL` and `UAA_BASE_URL`.

Profiles: `development`, `staging`, `production` (see `card-container/src/main/resources`).

## Deployment

Kubernetes manifests are in `card-service/k8s` (base plus `staging` and `production` overlays); see [`card-service/k8s/README.md`](card-service/k8s/README.md). CI is defined in `card-service/.gitlab-ci.yml`.
