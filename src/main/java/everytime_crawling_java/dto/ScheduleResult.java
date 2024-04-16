package everytime_crawling_java.dto;

import java.util.List;

public record ScheduleResult(
        List<ScheduleResponse> scheduleResponses
) {
}
