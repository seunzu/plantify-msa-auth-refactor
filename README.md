# plantify-msa-auth-refactor

Local lab for comparing two authentication architectures in a Spring Boot MSA.

- Baseline: each resource service calls `auth-service /v1/auth/validate-token` on every authenticated request.
- Refactor target: resource services validate JWTs locally with JWKS-cached public keys.

## Services

- `auth-service`: copied from the original Plantify auth server `main` branch and simplified for local experiments.
- `common-auth-lib`: shared Spring Security Resource Server configuration for JWKS local validation. (`refactor/jwks-local-validation` only)
- `demo-resource-service`: small protected API. On `main` it calls `/v1/auth/validate-token` per request; on `refactor/jwks-local-validation` it validates JWTs locally via `common-auth-lib`.
- `experiments/k6`: load and failure-window scripts.
- `docs`: sequence diagrams and performance comparison notes.

## Local Run

```bash
docker compose up --build auth-service demo-resource-service
```

Issue a local experiment token:

```bash
curl -X POST http://localhost:8081/v1/auth/dev-token
```

In `refactor/jwks-local-validation`, the token is signed with RS256 and resource services validate it through:

```text
http://localhost:8081/.well-known/jwks.json
```

For this local lab, `auth-service` can generate an in-memory RSA key pair if no PEM key files are provided. Do not use that fallback in production. In a real environment, provide stable keys through Kubernetes Secret, KMS, Vault, or another secret manager.

Run the baseline load test:

```bash
docker compose --profile test run --rm k6 run /scripts/baseline-auth-call.js
```

Run the failure-window test and stop auth during the run:

```bash
docker compose --profile test run --rm k6 run /scripts/auth-failure-window.js
docker compose stop auth-service
```

## CI/CD

This repository currently has CI only. There is no CD target because this lab does not deploy to AWS/EKS yet.

## Docs

- [Architecture overview](docs/architecture-overview.md)
- [Package responsibilities](docs/package-responsibilities.md)
- [API response convention](docs/api-response-convention.md)
- [API spec](docs/api-spec.md)
- [Baseline sequence](docs/baseline-sequence.md)
- [Refactor target sequence](docs/refactor-target-sequence.md)
- [Load test report](docs/load-test-report.md)
