package edtech;

import java.time.Instant;
import java.util.List;

public final class CourseDeliveryService {
    private final FailureReporter reporter;

    public CourseDeliveryService(FailureReporter reporter) { this.reporter = reporter; }

    public EducatorReport review(Course course, DeliveryJob job) {
        boolean late = !job.succeeded() || (job.completedAt() != null && job.completedAt().isAfter(course.deadline()));
        if (!late) return new EducatorReport(course.id(), List.of());
        String reason = job.succeeded() ? "completed after learner deadline" : "scheduled delivery failed: " + job.failureMessage();
        reporter.capture(course, job, reason);
        return new EducatorReport(course.id(), List.of(course.learnerId() + " needs a delivery review (" + reason + ")"));
    }
}
