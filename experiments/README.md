# Experiments

This folder keeps load and failure-injection scripts outside application code.

## Baseline: service calls auth on every request

```bash
docker compose up --build auth-service demo-resource-service
docker compose --profile test run --rm k6 run /scripts/baseline-auth-call.js
```

## Auth failure window

Run the test, then stop only the auth service in another terminal.

```bash
docker compose --profile test run --rm k6 run /scripts/auth-failure-window.js
docker compose stop auth-service
```

In the baseline architecture, protected resource requests should start failing because the resource service calls auth for every token validation.
