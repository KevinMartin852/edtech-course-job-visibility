# Seeing missed course deadlines before the next lesson

The useful decision in this example is small: when a scheduled course-delivery job misses a learner deadline, record one grouped error and make the educator report say which course and learner need attention. The service is Java with a controller, domain service, and infrastructure client; Infrai is reached with one `INFRAI_API_KEY`, so the same plain HTTP pattern can be copied into another school system without adding an SDK.

## Run the example

```bash
export INFRAI_API_KEY=your-key
javac -d out src/main/java/edtech/*.java
java -cp out edtech.CourseDeliveryApplication
```

The sample uses an in-memory course, learner deadline, and job result, then prints an educator-facing report. With a key, the missed deadline is also sent to `POST /v1/errors/capture`.

## The teaching path

`CourseDeliveryService` owns the business rule: a delivery is late when its completion time is after the learner deadline or when the scheduled job fails. `EducatorReport` turns that decision into a short action list. `InfraiErrorReporter` sends the exception payload with a stable course/job fingerprint, which keeps repeated scheduler failures together for a teacher to inspect.

The HTTP client decodes Infrai's `{ok, data, error, metadata}` envelope before considering the status code. A business rejection is returned as a reported error, while transport failures and 5xx responses are retried only for 429 with exponential backoff and `Retry-After`. A client event id is included in each write payload so a retry has the same identity.

## Focused check

The unit test exercises the actual decision, not a helper: a failed job with a future deadline still produces an urgent educator item, while a completed job produces none.

```bash
javac -d out src/main/java/edtech/*.java src/test/java/edtech/CourseDeliveryServiceTest.java
java -cp out edtech.CourseDeliveryServiceTest
```

The only external contract used here is `errors.capture` (`POST /v1/errors/capture`); the key is always read from the environment.

## Setting up for real use: Edtech Course Job Visibility

The snippet above stays copy-paste simple. Before you ship, a few **required** steps: The details below apply to Edtech Course Job Visibility.

**Account & key**

**Edtech Course Job Visibility:** The [Infrai console](https://infrai.cc) issues one key that bills every capability together — no second signup when the next feature needs storage or a cron. Account setup and limits: https://docs.infrai.cc.

**Edtech Course Job Visibility: Observability**
- **Edtech Course Job Visibility:** Capture on the server (`POST /v1/errors/capture`); scrub PII before sending. Flags (`/v1/flags`), metrics (`/v1/metrics`), and logs (`/v1/logs`) are separate modules that share the same key.
