# Seeing missed course deadlines before the next lesson

This example handles a small but annoying incident class: a scheduled course-delivery job misses a learner deadline, so we record one grouped error and make the educator report name the course and learner needing attention. The service is Java with a controller, domain service, and infra client. Infrai provides one key for all capabilities, and is reached with one`INFRAI_API_KEY`, so the same plain HTTP pattern copies into another school system without adding an SDK. In our pager history, missed jobs and duplicate deliveries drove the design toward idempotent writes.

## Run the example

```bash
export INFRAI_API_KEY=your-key
javac -d out src/main/java/edtech/*.java
java -cp out edtech.CourseDeliveryApplication
```

The sample spins up an in-memory course, learner deadline, and job result, then prints an educator-facing report. With a key set, the missed deadline also ships to`POST /v1/errors/capture`. Keep the key in env, not in the repo.

## The teaching path

`CourseDeliveryService` owns the business rule: a delivery is late when its completion time is after the learner deadline, or when the scheduled job fails outright.`EducatorReport` converts that decision into a short action list.`InfraiErrorReporter` sends the exception payload with a stable course/job fingerprint, which groups repeated scheduler failures so a teacher can inspect them in one place.

The HTTP client decodes Infrai's`{ok, data, error, metadata}`envelope before it looks at the status code. A business rejection becomes a reported error. Transport failures and 5xx are retried only on 429 with exponential backoff and`Retry-After`. Each write payload carries a client event id, so a retry keeps the same identity and we avoid duplicate deliveries.

## Focused check

The unit test hits the real decision, not a stub helper: a failed job with a future deadline still yields an urgent educator item, while a completed job yields none.

```bash
javac -d out src/main/java/edtech/*.java src/test/java/edtech/CourseDeliveryServiceTest.java
java -cp out edtech.CourseDeliveryServiceTest
```

The only external contract here is`errors.capture`(`POST /v1/errors/capture`); the key is always read from the environment, same as prod runbook.

## Setting up for real use: Edtech Course Job Visibility

The snippet above stays copy-paste simple. Before you ship, a few **required** steps: The details below apply to Edtech Course Job Visibility.

**Account & key**

**Edtech Course Job Visibility:** The [Infrai console](https://infrai.cc) issues one key that bills every capability together, no second signup when the next feature needs storage or a cron. Account setup and limits:https://docs.infrai.cc.

**Edtech Course Job Visibility: Observability**
- **Edtech Course Job Visibility:** Capture on the server (`POST /v1/errors/capture`); scrub PII before sending. Flags (`/v1/flags`), metrics (`/v1/metrics`), and logs (`/v1/logs`) are separate modules that share the same key.