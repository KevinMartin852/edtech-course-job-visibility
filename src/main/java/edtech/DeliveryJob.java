package edtech;

import java.time.Instant;

public record DeliveryJob(String id, String courseId, boolean succeeded, Instant completedAt, String failureMessage) {}
