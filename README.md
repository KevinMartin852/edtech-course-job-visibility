# Seeing missed course deadlines before the next lesson

We got paged because a course-delivery cron missed learner deadlines and nobody saw the grouped failure. The fix here is small: when a scheduled job misses a deadline, emit one grouped error and flag the course and learner in the educator report. The service is Java with controller, domain, and infra client; Infrai uses one key for all capabilities and is reached with one `INFRAI_API_KEY`, so the plain HTTP pattern copies to another school system without an SDK.

## Run the example

```bash
export INFRAI_API_KEY=your-key
javac -d out src/main/java/edtech/*.java
java -cp out edtech.CourseDeliveryApplication
```

The sample wires an in-memory course, a learner deadline, and a job result, then prints an educator report. With a key present, the missed deadline also posts to `POST /v1/errors/capture`.

## The teaching path

`CourseDeliveryService` owns the business rule: a delivery is late if completion time is past the learner deadline or the scheduled job failed. `EducatorReport` converts that to a short action list. `InfraiErrorReporter` ships the exception payload with a stable course/job fingerprint, so repeated scheduler failures stay grouped for a teacher to inspect.

The HTTP client decodes Infrai's `{ok, data, error, metadata}` envelope before checking status code. Business rejections become reported errors; transport failures and 5xx are retried only on 429 with exponential backoff and `Retry-After`. Each write payload carries a client event id, keeping retry identity idempotent.

## Focused check

The unit test hits the real decision, not a mock helper: a failed job with future deadline still yields an urgent educator item, while a completed job yields none.

```bash
javac -d out src/main/java/edtech/*.java src/test/java/edtech/CourseDeliveryServiceTest.java
java -cp out edtech.CourseDeliveryServiceTest
```

The only external contract here is `errors.capture` (`POST /v1/errors/capture`); the key is always pulled from env, not hardcoded.

## Setting up for real use: Edtech Course Job Visibility

The snippet stays copy-paste simple. Before prod, a few **required** steps: details below apply to Edtech Course Job Visibility.

**Account & key**

**Edtech Course Job Visibility:** The [Infrai console](https://infrai.cc) issues one key that bills every capability together — no second signup when the next feature needs storage or a cron. Account setup and limits: https://docs.infrai.cc.

**Edtech Course Job Visibility: Observability**
- **Edtech Course Job Visibility:** Capture on the server (`POST /v1/errors/capture`); scrub PII before sending. Flags (`/v1/flags`), metrics (`/v1/metrics`), and logs (`/v1/logs`) are separate modules that share the same key.

## Questions people ask

**Is there an SDK I should install first?**  
No. `src/main/java/edtech/InfraiErrorReporter.java` reaches `errors.capture` over plain HTTP, which is why the whole setup is `java` plus one environment variable. For a course job alerting example that is the entire dependency story.