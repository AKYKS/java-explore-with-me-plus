package ru.practicum;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static java.lang.String.format;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StatsClient {
    static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    final RestTemplate restTemplate;
    @Value("${stats-server.url:http://localhost:9090}")
    String serverUrl;

    public void saveHit(String app, String uri, String ip) {
        EndpointHitDto hitDto = EndpointHitDto.builder()
                .app(app)
                .uri(uri)
                .ip(ip)
                .timestamp(LocalDateTime.now().format(FORMATTER))
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<EndpointHitDto> request = new HttpEntity<>(hitDto, headers);
        restTemplate.postForEntity(format("%s/hit", serverUrl), request, Void.class);
    }

    public List<ViewStatsDto> getStats(String start, String end, List<String> uris, Boolean unique) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(format("%s/stats", serverUrl))
                .queryParam("start", start)
                .queryParam("end", end);
        if (uris != null && !uris.isEmpty()) {
            uris.forEach(uri -> uriComponentsBuilder.queryParam("uris", uri));
        }
        if (unique != null) {
            uriComponentsBuilder.queryParam("unique", unique);
        }
        String url = uriComponentsBuilder.toUriString();
        try {
            ViewStatsDto[] response = restTemplate.getForObject(url, ViewStatsDto[].class);
            return response != null ? List.of(response) : List.of();
        } catch (Exception exception) {
            log.error("Ошибка при получении статистики по URL {}: {}", url, exception.getMessage(), exception);
            throw exception;
        }
    }
}
