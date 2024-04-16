package everytime_crawling_java.controller;

import everytime_crawling_java.dto.ScheduleResponse;
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
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@RestController
public class CrawlingController {

    @PostMapping("/crawling/{id}")
    public List<ScheduleResponse> crawl(@PathVariable String id,
                                        @RequestParam("begin") String beginDate,
                                        @RequestParam("end") String endDate) throws Exception {
        String timetableXML = fetchTimetable(id);
        return processTimetable(timetableXML, beginDate, endDate);
    }

    private String fetchTimetable(String identifier) {
        String response = "";
        try {
            URL url = new URL("https://api.everytime.kr/find/timetable/table/friend");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            con.setRequestProperty("Accept", "*/*");
            con.setRequestProperty("Connection", "keep-alive");
            con.setRequestProperty("Pragma", "no-cache");
            con.setRequestProperty("Cache-Control", "no-cache");
            con.setRequestProperty("Host", "api.everytime.kr");
            con.setRequestProperty("Origin", "https://everytime.kr");
            con.setRequestProperty("Referer", "https://everytime.kr/");
            con.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/93.0.4577.63 Safari/537.36");
            con.setDoOutput(true);

            String postData = "identifier=" + identifier + "&friendInfo=true";
            con.getOutputStream().write(postData.getBytes());

            Scanner scanner = new Scanner(con.getInputStream());
            while (scanner.hasNextLine()) {
                response += scanner.nextLine();
            }
            scanner.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    private List<ScheduleResponse> processTimetable(String xml, String begin, String end) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(xml)));
        NodeList subjects = doc.getElementsByTagName("subject");

        List<ScheduleResponse> schedules = new ArrayList<>();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
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
                    LocalTime startTime = LocalTime.ofSecondOfDay(Integer.parseInt(timeElement.getAttribute("starttime")) * 300);
                    LocalTime endTime = LocalTime.ofSecondOfDay(Integer.parseInt(timeElement.getAttribute("endtime")) * 300);
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