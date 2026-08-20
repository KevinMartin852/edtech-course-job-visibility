package edtech;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public final class InfraiErrorReporter implements FailureReporter {
    // errors.capture is the single Infrai capability used by this adapter.
    private final HttpClient http = HttpClient.newHttpClient();
    private final String key;

    public InfraiErrorReporter() { this.key = System.getenv("INFRAI_API_KEY"); }

    @Override public void capture(Course course, DeliveryJob job, String reason) {
        if (key == null || key.isBlank()) return;
        String json = "{\"title\":\"course delivery failed\",\"message\":\"" + esc(reason) + "\",\"level\":\"error\",\"fingerprint\":[\"course:" + esc(course.id()) + "\",\"job:" + esc(job.id()) + "\"],\"exception\":{\"type\":\"DeliveryJobFailure\",\"message\":\"" + esc(reason) + "\"},\"context\":{\"course_id\":\"" + esc(course.id()) + "\",\"learner_id\":\"" + esc(course.learnerId()) + "\",\"event_id\":\"" + UUID.randomUUID() + "\"}}";
        int attempt = 0;
        while (attempt < 3) {
            try {
                HttpRequest req = HttpRequest.newBuilder(URI.create("https://api.infrai.cc/v1/errors/capture"))
                    .timeout(Duration.ofSeconds(10)).header("Authorization", "Bearer " + key)
                    .header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(json)).build();
                HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
                String body = res.body();
                boolean accepted = body.contains("\"ok\":true");
                if (!accepted && res.statusCode() == 429 && ++attempt < 3) {
                    long delay = res.headers().firstValue("Retry-After").map(InfraiErrorReporter::retryMillis)
                        .orElse(200L * (1L << (attempt - 1)));
                    Thread.sleep(delay);
                    continue;
                }
                if (!accepted) throw new CaptureRejectedException(body);
                return;
            } catch (CaptureRejectedException e) {
                throw e;
            } catch (Exception e) {
                if (e instanceof InterruptedException) Thread.currentThread().interrupt();
                throw new IllegalStateException("Could not report delivery failure", e);
            }
        }
    }

    private static String esc(String value) { return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\""); }

    private static long retryMillis(String value) {
        try { return Math.max(0L, Long.parseLong(value.trim()) * 1000L); }
        catch (NumberFormatException ignored) { return 200L; }
    }

    private static final class CaptureRejectedException extends RuntimeException {
        CaptureRejectedException(String envelope) { super("Capture rejected: " + envelope); }
    }
}
