# AGENTS.md

Spring Boot 4 (parent 4.1.0) / Spring Framework 6 Reactive WebClient on **Java 25** (enforced by the
maven-enforcer plugin). Single Maven module, package `guru.springframework.spring6reactivewebclient`.
It is a reactive web client (Thymeleaf UI + WebClient) that calls the `spring-6-reactive-mongo`
backend and is secured via the OAuth2 `spring-6-auth-server`.

## Build & test commands

- Full build: `./mvnw clean verify` — format checks, unit (`*Test`, surefire) + IT (`*IT`, failsafe)
  tests, Helm lint/template.
- Unit tests only: `./mvnw test`. Single test: `./mvnw test -Dtest=BeerWebControllerTest#methodName`.
- `./mvnw clean install` additionally builds the Docker image and packages the Helm chart into
  `target/helm/repo/`. Skip the Docker build with `-Dskip.docker.build=true`.
- `-Dskip.start.stop.springboot=true` skips the in-build app boot (spring-boot:start/stop).
- Start locally: `./mvnw spring-boot:run` (app on `:8087`, needs `compose.yaml` services).

After changing code, always verify: run the relevant Maven goal above and report its output
(evidence, not just "done").

## Sandbox build quirk (background)

This sandbox mounts the repo via filesystem passthrough, which blocks symlinks — Spotless's
`npm install` (prettier) would fail with `EPERM` unless npm skips bin links. The sandbox kit sets
`npm_config_bin_links=false` globally (`spec.yaml` → `environment.variables`), so no manual export
is needed here. On a normal host (Windows/CI) this does not apply either.

## Formatting is enforced (fails the `validate` phase)

- Java: Spring Java Format → fix with `./mvnw spring-javaformat:apply`.
- Everything else (pom.xml, `**/*.md`, json, `src/main/resources/application*.yaml`, `**/*.sh`):
  Spotless → fix with `./mvnw spotless:apply`.
- `AGENTS.md` and `CLAUDE.md` are excluded from the Spotless markdown check.
- Spotless flexmark also formats markdown, so any `.md` edit (except the two excluded files) must
  stay flexmark-clean; run `./mvnw spotless:apply` after editing markdown.

## External dependency gotcha

- The build resolves the auth-server snapshot (`maven.pkg.github.com`) and OCI Helm charts
  (`repo.repsy.io`, `registry-1.docker.io`) — CI needs the corresponding tokens/PATs
  (`server id github` in `settings.xml`).

## Test conventions

- Naming matters: `*Test` = unit (surefire), `*IT` = integration (failsafe). A `*Test` class will
  not run during `verify`'s failsafe phase and vice versa.
- ITs that need the running stack use `compose.yaml` (mongodb, auth-server, reactive-mongo, gateway).
- `BeerClientImplWithTestContainerIT` uses **Testcontainers with multiple containers**
  (`mongo:8.2.3`, `domboeckli/spring-6-auth-server:0.0.5-SNAPSHOT`,
  `domboeckli/spring-6-reactive-mongo:0.0.1-SNAPSHOT`, `domboeckli/spring-6-gateway:0.0.3-SNAPSHOT`);
  `verify` therefore needs **Docker**.
- A custom `TestClassOrderer` sorts test classes and `LocaleExtension` is auto-registered to force
  `Locale.US`. Do not add a global locale again.

## Architecture

- `client/` — `BeerClient` (reactive WebClient) against the `spring-6-reactive-mongo` backend
  (`webclient.reactive-mongo-url`); `web/` — Thymeleaf UI controllers (`/beers`).
- `config/health/` — health indicators for auth-server and reactive-mongo
  (`SECURITY_AUTH_SERVER_HEALTH_URL`, `webclient.reactive-mongo-url`).
- OAuth2 client credentials flow against the auth-server (provider `springauth`).
- The app runs on port **8087** (NodePort **30087**); auth-server on **9000/30900**;
  reactive-mongo on **8083/30083**; gateway on 8080 (no gateway in Kubernetes).

## Running locally

- Default profile auto-starts `compose.yaml` (mongodb + auth-server + reactive-mongo + gateway)
  via spring-boot-docker-compose. Auth-server must be reachable on `:9000`.
- Manual API testing: IntelliJ HTTP files in `restRequests/`.

## Deploy / CI

- Deployment is Helm-only: chart in `helm-charts/`, packaged to `target/helm/repo/`, release name =
  artifactId, namespace `spring-6-reactive-web-client`. Chart name is `<artifactId>-chart`
  (`spring-6-reactive-web-client-chart`). The README's raw `kubectl` flow references a `k8s/`
  source dir that no longer exists — ignore those sections.
- CI (`.github/workflows/`): `maven-build.yml` builds + deploys snapshots and triggers
  `deploy-and-test-cluster.yml`; `release.yml` runs `mvn release:prepare release:perform` on
  main/master only (version must be `-SNAPSHOT`); SonarCloud analysis runs in the `analyze` job.
- Dependency updates are managed via `.github/renovate.json`; validate changes with
  `renovate-config-validator .github/renovate.json`.
