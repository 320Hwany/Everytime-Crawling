package everytime_crawling_java.controller;

import everytime_crawling_java.dto.ScheduleResponse;
import everytime_crawling_java.dto.ScheduleResult;
import everytime_crawling_java.service.TimetableService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CrawlingController {

    private final TimetableService timetableService;

    public CrawlingController(final TimetableService timetableService) {
        this.timetableService = timetableService;
    }

    @PostMapping("/crawling/{id}")
    public ScheduleResult crawl(@PathVariable final String id,
                                @RequestParam final String startDate,
                                @RequestParam final String endDate) throws Exception {
        String timetableXML = timetableService.fetchTimetable(id);
        List<ScheduleResponse> scheduleResponses = timetableService.processTimetable(timetableXML, startDate, endDate);
        return new ScheduleResult(scheduleResponses);
    }
}