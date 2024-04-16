package everytime_crawling_java.service;

import everytime_crawling_java.dto.ScheduleResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
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

@Service
public class TimetableService {

    private final RestTemplate restTemplate;

    public TimetableService(final RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String fetchTimetable(final String identifier) {
        String url = "https://api.everytime.kr/find/timetable/table/friend";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Origin", "https://everytime.kr");
        headers.set("Referer", "https://everytime.kr/");
        headers.set("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.212 Safari/537.36");

        String postData = "identifier=" + identifier + "&friendInfo=true";
        HttpEntity<String> request = new HttpEntity<>(postData, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        return response.getBody();
    }

    public List<ScheduleResponse> processTimetable(final String xml, final String begin, final String end) throws Exception {
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
                    DayOfWeek dayOfWeek = DayOfWeek.of((day % 7) + 1);
                    LocalTime startTime = LocalTime.ofSecondOfDay(Long.parseLong(timeElement.getAttribute("starttime")) * 300);
                    LocalTime endTime = LocalTime.ofSecondOfDay(Long.parseLong(timeElement.getAttribute("endtime")) * 300);

                    LocalDate nextDate = adjustStartDate(startDate, dayOfWeek);
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

    private LocalDate adjustStartDate(LocalDate startDate, DayOfWeek dayOfWeek) {
        return startDate.getDayOfWeek().equals(dayOfWeek) ? startDate : startDate.with(TemporalAdjusters.nextOrSame(dayOfWeek));
    }
}
