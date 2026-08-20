package edtech;

import java.util.List;

public record EducatorReport(String courseId, List<String> urgentItems) {
    public boolean needsAttention() { return !urgentItems.isEmpty(); }
}
