package everytime_crawling_java.controller;

import everytime_crawling_java.dto.ScheduleResponse;
import everytime_crawling_java.service.TimetableService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@RestController
public class CrawlingController {

    private final TimetableService timetableService;

    public CrawlingController(final TimetableService timetableService) {
        this.timetableService = timetableService;
    }

    @PostMapping("/crawling/{id}")
    public List<ScheduleResponse> crawl(@PathVariable final String id,
                                        @RequestParam("begin") String beginDate,
                                        @RequestParam("end") String endDate) throws Exception {
        String timetableXML = timetableService.fetchTimetable(id);
        return processTimetable(timetableXML, beginDate, endDate);
    }

    private List<ScheduleResponse> processTimetable(String xml, String begin, String end) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(xml)));
        NodeList subjects = doc.getElementsByTagName("subject");

        List<ScheduleResponse> schedules = new ArrayList<>();
        LocalDate startDate = LocalDate.parse(begin);
        LocalDate endDate = LocalDate.parse(end);

        for (int i = 0; i < subjects.getLength(); i++) {
            Node node = subjects.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                String name = element.getElementsByTagName("name").item(0).getAttributes().getNamedItem("value").getNodeValue();
                NodeList times = element.getElementsByTagName("data");

                for (int j = 0; j < times.getLength(); j++) {
                    Element timeElement = (Element) times.item(j);
                    int day = Integer.parseInt(timeElement.getAttribute("day"));
                    LocalTime startTime = LocalTime.ofSecondOfDay(Long.parseLong(timeElement.getAttribute("starttime")) * 300);
                    LocalTime endTime = LocalTime.ofSecondOfDay(Long.parseLong(timeElement.getAttribute("endtime")) * 300);
                    String place = timeElement.getAttribute("place").isEmpty() ? "장소 정보 없음" : timeElement.getAttribute("place");

                    LocalDate nextDate = startDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.of((day % 7) + 1)));
                    while (!nextDate.isAfter(endDate)) {
                        LocalDateTime startDateTime = LocalDateTime.of(nextDate, startTime);
                        LocalDateTime endDateTime = LocalDateTime.of(nextDate, endTime);
                        schedules.add(new ScheduleResponse(name, startDateTime, endDateTime));
                        nextDate = nextDate.plusWeeks(1);
                    }
                }
            }
        }
        return schedules;
    }
}