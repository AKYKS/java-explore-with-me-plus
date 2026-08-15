package ru.practicum.service;

import jakarta.validation.ValidationException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.EndpointHitDto;
import ru.practicum.ViewStatsDto;
import ru.practicum.model.Hit;
import ru.practicum.repository.HitRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static java.lang.String.format;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional(readOnly = true)
public class HitServiceImpl implements HitService{
    static DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    HitRepository hitRepository;

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
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        if (start != null && end != null && start.isAfter(end)) {
            log.warn("Дата начала {} не может быть позже даты окончания {}", start, end);
            throw new ValidationException(format("Дата начала %s не может быть позже даты окончания %s", start, end));
        }
        boolean hasUris = uris == null || uris.isEmpty();
        if (Boolean.TRUE.equals(unique)) {
            return hasUris
                    ? hitRepository.findStatsByRangeUniqueIp(start, end, null)
                    : hitRepository.findStatsByRangeUniqueIp(start, end, uris);
        } else {
            return hasUris
                    ? hitRepository.findStatsByRange(start, end, null)
                    : hitRepository.findStatsByRange(start, end, uris);
        }
    }
}
