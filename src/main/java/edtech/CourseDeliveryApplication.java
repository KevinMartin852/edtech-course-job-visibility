package edtech;

import java.time.Instant;

public final class CourseDeliveryApplication {
    public static void main(String[] args) {
        Course course = new Course("course-math-7", "Fractions", "learner-42", Instant.parse("2030-09-01T09:00:00Z"));
        DeliveryJob job = new DeliveryJob("job-18", course.id(), false, null, "video publish step failed");
        EducatorReport report = new CourseDeliveryService(new InfraiErrorReporter()).review(course, job);
        System.out.println(report.urgentItems());
    }
}
