package ru.practicum.ewm.event.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import ru.practicum.StatsClient;
import ru.practicum.ViewStatsDto;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.mappers.EventMapper;
import ru.practicum.ewm.request.Request;
import ru.practicum.ewm.request.RequestRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EventServiceImpl implements EventService {
    static String PUBLIC_APP = "ewm-service";
    static DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    static LocalDateTime STATS_BEGIN =
            LocalDateTime.of(2000, 1, 1, 0, 0);

    StatsClient statsClient;
    RequestRepository requestRepository;

    private Map<Long, Long> loadViews(List<Event> events) {
        if (events.isEmpty()) {
            return Map.of();
        }
        List<String> uris = events.stream()
                .map(event -> "/events/" + event.getId())
                .toList();
        List<ViewStatsDto> stats = statsClient.getStats(
                STATS_BEGIN.format(FORMATTER),
                LocalDateTime.now().plusSeconds(1).format(FORMATTER),
                uris,
                true
        );
        Map<Long, Long> result = new HashMap<>();
        for (ViewStatsDto stat : stats) {
            if (!PUBLIC_APP.equals(stat.getApp())) {
                continue;
            }

            String prefix = "/events/";
            if (stat.getUri() != null && stat.getUri().startsWith(prefix)) {
                try {
                    long eventId = Long.parseLong(stat.getUri().substring(prefix.length()));
                    result.put(eventId, stat.getHits());
                } catch (NumberFormatException ignored) {
                    // Statistics for unrelated URIs are ignored.
                }
            }
        }
        return result;
    }

    private Map<Long, Long> loadViewsSafely(List<Event> events) {
        try {
            return loadViews(events);
        } catch (RestClientException exception) {
            log.warn("Stats-server недоступен", exception);
            return Map.of();
        }
    }

    private long confirmedRequests(Long eventId) {
        return requestRepository.countByEventIdAndStatus(eventId, Request.Status.CONFIRMED);
    }

    private EventShortDto toShortDto(Event event, Long views) {
        EventShortDto dto = EventMapper.toEventShortDto(event);
        dto.setConfirmedRequests(confirmedRequests(event.getId()));
        dto.setViews(views);
        return dto;
    }

    @Override
    public List<EventShortDto> toShortDtos(List<Event> events) {
        Map<Long, Long> views = loadViewsSafely(events);
        return events.stream()
                .map(event -> toShortDto(event, views.getOrDefault(event.getId(), 0L)))
                .toList();
    }
}
