package edtech;

@FunctionalInterface
public interface FailureReporter {
    void capture(Course course, DeliveryJob job, String reason);
}
