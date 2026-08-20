package edtech;

import java.time.Instant;

public final class CourseDeliveryServiceTest {
    public static void main(String[] args) {
        Course c = new Course("c1", "Algebra", "l1", Instant.parse("2030-01-02T09:00:00Z"));
        DeliveryJob failed = new DeliveryJob("j1", "c1", false, null, "worker stopped");
        EducatorReport urgent = new CourseDeliveryService((course, job, reason) -> {}).review(c, failed);
        if (!urgent.needsAttention() || urgent.urgentItems().size() != 1) throw new AssertionError("failed job must alert educator");
        DeliveryJob done = new DeliveryJob("j2", "c1", true, Instant.parse("2030-01-02T08:00:00Z"), "");
        if (new CourseDeliveryService((course, job, reason) -> {}).review(c, done).needsAttention()) throw new AssertionError("on-time job must stay quiet");
        System.out.println("CourseDeliveryServiceTest passed");
    }
}
