package edtech;

import java.time.Instant;

public record Course(String id, String title, String learnerId, Instant deadline) {}
