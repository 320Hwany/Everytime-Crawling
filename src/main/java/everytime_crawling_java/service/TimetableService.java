package everytime_crawling_java.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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
        headers.set("Accept", "*/*");
        headers.set("Connection", "keep-alive");
        headers.set("Pragma", "no-cache");
        headers.set("Cache-Control", "no-cache");
        headers.set("Host", "api.everytime.kr");
        headers.set("Origin", "https://everytime.kr");
        headers.set("Referer", "https://everytime.kr/");
        headers.set("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/93.0.4577.63 Safari/537.36");

        String postData = "identifier=" + identifier + "&friendInfo=true";

        HttpEntity<String> request = new HttpEntity<>(postData, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        return response.getBody();
    }
}
