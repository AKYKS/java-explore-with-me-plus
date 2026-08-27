package ru.practicum.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.EndpointHitDto;
import ru.practicum.ViewStatsDto;
import ru.practicum.exception.ValidationException;
import ru.practicum.model.Hit;
import ru.practicum.repository.HitRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.List;

import static java.lang.String.format;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional(readOnly = true)
public class HitServiceImpl implements HitService {
    static DateTimeFormatter FORMATTER =
            new DateTimeFormatterBuilder()
                    .appendPattern("yyyy-MM-dd")
                    .appendOptional(new DateTimeFormatterBuilder().appendLiteral('T').toFormatter())
                    .appendOptional(new DateTimeFormatterBuilder().appendLiteral(' ').toFormatter())
                    .appendPattern("HH:mm:ss")
                    .toFormatter();
    HitRepository hitRepository;

    private LocalDateTime parseDateTime(String value) {
        return LocalDateTime.parse(value, FORMATTER);
    }

    @Override
    @Transactional
    public void saveHit(EndpointHitDto hitDto) {
        Hit hit = Hit.builder()
                .app(hitDto.getApp())
                .uri(hitDto.getUri())
                .ip(hitDto.getIp())
                .timestamp(LocalDateTime.parse(hitDto.getTimestamp(), FORMATTER))
                .build();
        hitRepository.save(hit);
    }

    @Override
    public List<ViewStatsDto> getStats(String start, String  end, List<String> uris, Boolean unique) {
        LocalDateTime startDateTime = parseDateTime(start);
        LocalDateTime endDateTime = parseDateTime(end);
        if (startDateTime != null && endDateTime != null && startDateTime.isAfter(endDateTime)) {
            log.warn("Дата начала {} не может быть позже даты окончания {}",startDateTime, endDateTime);
            throw new ValidationException(format("Дата начала %s не может быть позже даты окончания %s", startDateTime,
                    endDateTime));
        }
        boolean hasUris = uris == null || uris.isEmpty();
        if (Boolean.TRUE.equals(unique)) {
            return hasUris
                    ? hitRepository.findStatsByRangeUniqueIp(startDateTime, endDateTime, null)
                    : hitRepository.findStatsByRangeUniqueIp(startDateTime, endDateTime, uris);
        } else {
            return hasUris
                    ? hitRepository.findStatsByRange(startDateTime, endDateTime, null)
                    : hitRepository.findStatsByRange(startDateTime, endDateTime, uris);
        }
    }
}
