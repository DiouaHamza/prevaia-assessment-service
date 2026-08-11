# service-template

Starting point for a new microservice. Not part of any build itself — copy this
folder to bootstrap a new service.

## Usage

1. Copy this directory to `<repo-root>/<new-service>/` (it must stay a sibling of
   `libs/` for the relative `includeBuild('../libs/service-commons')` path in
   `settings.gradle` to resolve).
2. Rename:
   - `rootProject.name` in `settings.gradle`
   - `group`/artifact expectations in `build.gradle` if needed
   - the `com.preveaia.service_template` package and `ServiceTemplateApplication` class
   - `spring.application.name` and `server.port` in `application.yaml`
   - the jar name in `Dockerfile`
3. Add whatever starters the new service needs (data-jpa, amqp, etc.) to `build.gradle`.
4. If the service needs to validate JWTs, inject `com.preveaia.commons.jwt.JwtValidator`
   (construct it from your service's `emaia.security.jwt.secret-key` property) — it's
   already on the classpath via the `service-commons` dependency.

## What's already wired

- **Actuator** with `health` and `info` exposed (`management.endpoints.web.exposure.include`).
- **X-Trace-Id propagation**: `TracingConfig` registers `TraceIdFilter` from
  `service-commons`, so every request gets a trace id (generated if the caller
  didn't send one), it's put in MDC (`%X{traceId}` in the log pattern), returned
  on the response, and forwarded on to anything this service calls downstream.
- **Docker**: same `eclipse-temurin:17-jdk-alpine` base image pattern as `back/Dockerfile`.
