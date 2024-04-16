package everytime_crawling_java.dto;

import java.time.LocalDateTime;

public record ScheduleResponse(
        String subject,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime
) {
}
