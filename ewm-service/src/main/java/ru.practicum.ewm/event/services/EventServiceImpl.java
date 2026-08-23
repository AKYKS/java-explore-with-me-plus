package ru.practicum.ewm.event.services;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import ru.practicum.StatsClient;
import ru.practicum.ViewStatsDto;
import ru.practicum.ewm.category.Category;
import ru.practicum.ewm.category.CategoryRepository;
import ru.practicum.ewm.event.dto.*;
import ru.practicum.ewm.event.enums.AdminStateAction;
import ru.practicum.ewm.event.enums.EventSort;
import ru.practicum.ewm.event.enums.EventState;
import ru.practicum.ewm.event.mappers.EventMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.event.model.EventRequestStatusUpdateResult;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.request.Request;
import ru.practicum.ewm.request.RequestMapper;
import ru.practicum.ewm.request.RequestRepository;
import ru.practicum.ewm.request.dto.RequestResponseDto;
import ru.practicum.ewm.user.User;
import ru.practicum.ewm.user.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final EventMapper eventMapper;
    private final RequestRepository requestRepository;
    private final StatsClient statsClient;

    private static final int MIN_HOURS_BEFORE_EVENT = 2;
    private static final String APP_NAME = "ewm-main";
    private static final String PUBLIC_APP = "ewm-service";
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final LocalDateTime STATS_BEGIN =
            LocalDateTime.of(2000, 1, 1, 0, 0);

    @Override
    public EventFullDto createEvent(Long userId, NewEventDto dto) {

        User initiator = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        Category category = categoryRepository.findById(dto.getCategory())
                .orElseThrow(() -> new NotFoundException("Категория с id " + dto.getCategory() + " не найдена"));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime minEventDate = now.plusHours(MIN_HOURS_BEFORE_EVENT);

        if (dto.getEventDate().isBefore(minEventDate)) {
            throw new ConflictException(
                    "Дата события должна быть не раньше, чем через " + MIN_HOURS_BEFORE_EVENT +
                            " часа от текущего момента. Текущее время: " + now +
                            ", минимальная дата: " + minEventDate +
                            ", переданная дата: " + dto.getEventDate()
            );
        }

        Event event = eventMapper.toEntity(dto, category, initiator);

        Event saved = eventRepository.save(event);

        return eventMapper.toFullDto(saved, 0L, 0L);
    }

    @Override
    @Transactional
    public List<EventShortDto> getEventsByUser(Long userId, Integer from, Integer size) {

        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        Pageable pageable = PageRequest.of(from / size, size, Sort.by(Sort.Direction.DESC, "created"));

        Page<Event> page = eventRepository.findByInitiatorId(userId, pageable);

        if (page.isEmpty()) {
            return Collections.emptyList();
        }

        return page.getContent().stream()
                .map(eventMapper::toShortDto)
                .collect(toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getEventByUser(Long userId, Long eventId) {

        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id " + eventId + " не найдено"));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("Событие с id " + eventId + " не принадлежит пользователю " + userId);
        }

        Long views = getViews("/events/" + eventId);
        Long confirmedRequests = requestRepository.countByEventIdAndStatus(eventId, Request.Status.CONFIRMED);

        return eventMapper.toFullDto(event, confirmedRequests, views);
    }


    @Override
    @Transactional
    public EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventDto dto) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id " + eventId + " не найдено"));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("Событие с id " + eventId + " не принадлежит пользователю " + userId);
        }

        if (event.getState() != EventState.PENDING && event.getState() != EventState.CANCELED) {
            throw new ConflictException("Только события в статусе PENDING или CANCELED можно редактировать. Текущий статус: " + event.getState());
        }

        LocalDateTime now = LocalDateTime.now();
        if (dto.getEventDate() != null) {
            LocalDateTime minEventDate = now.plusHours(2);
            if (dto.getEventDate().isBefore(minEventDate)) {
                throw new ConflictException("Дата события должна быть не раньше, чем через 2 часа от текущего момента");
            }
        }

        updateEventFields(event,dto);

        Event updated = eventRepository.save(event);

        Long views = getViews("/events/" + eventId);
        Long confirmedRequests = requestRepository.countByEventIdAndStatus(eventId, Request.Status.CONFIRMED);

        return eventMapper.toFullDto(updated, confirmedRequests, views);
    }

    @Override
    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminDto dto) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id " + eventId + " не найдено"));

        if (dto.getStateAction() == AdminStateAction.PUBLISH_EVENT) {
            if (event.getState() != EventState.PENDING) {
                throw new ConflictException("Событие можно опубликовать только в статусе PENDING. Текущий статус: " + event.getState());
            }

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime minEventDate = now.plusHours(1);
            if (event.getEventDate().isBefore(minEventDate)) {
                throw new ConflictException("Дата события должна быть не раньше, чем через час от текущего момента. " +
                        "Текущее время: " + now + ", дата события: " + event.getEventDate());
            }

            event.setState(EventState.PUBLISHED);
            event.setPublished(LocalDateTime.now());
        }

        if (dto.getStateAction() == AdminStateAction.REJECT_EVENT) {
            if (event.getState() == EventState.PUBLISHED) {
                throw new ConflictException("Нельзя отклонить уже опубликованное событие. Текущий статус: " + event.getState());
            }

            event.setState(EventState.CANCELED);
        }

        updateEventFields(event, dto);

        Event updated = eventRepository.save(event);

        return eventMapper.toFullDto(updated, 0L, 0L);
    }

    @Override
    public List<EventFullDto> searchEventsAdmin(List<Long> users, List<EventState> states, List<Long> categories,
                                                LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from,
                                                Integer size) {

        if (rangeStart == null) {
            rangeStart = LocalDateTime.now().minusYears(100);
        }
        if (rangeEnd == null) {
            rangeEnd = LocalDateTime.now().plusYears(100);
        }

        Pageable pageable = PageRequest.of(from / size, size, Sort.by(Sort.Direction.DESC, "eventDate"));

        Page<Event> page = eventRepository.searchEventsAdmin(
                users,
                states != null ? states.stream().map(Enum::name).collect(toList()) : null,
                categories,
                rangeStart,
                rangeEnd,
                pageable
        );

        if (page.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> eventIds = page.getContent().stream()
                .map(Event::getId)
                .collect(toList());

        Map<Long, Long> confirmedMap = getConfirmedRequestsMap(eventIds);
        Map<Long, Long> viewsMap = getViewsForEvents(eventIds);

        return page.getContent().stream()
                .map(event -> {
                    Long confirmedRequests = confirmedMap.getOrDefault(event.getId(), 0L);
                    Long views = viewsMap.getOrDefault(event.getId(), 0L);
                    return eventMapper.toFullDto(event, confirmedRequests, views);
                })
                .collect(toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getPublicEvents(String text, List<Long> categories, Boolean paid,
                                               LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                               Boolean onlyAvailable, EventSort sort, Integer from,
                                               Integer size, HttpServletRequest request) {

        saveHit(request);

        if (rangeStart == null) {
            rangeStart = LocalDateTime.now();
        }
        if (rangeEnd == null) {
            rangeEnd = LocalDateTime.now().plusYears(100);
        }

        Sort sortBy = sort == EventSort.EVENT_DATE
                ? Sort.by(Sort.Direction.ASC, "eventDate")
                : Sort.by(Sort.Direction.DESC, "views");

        Pageable pageable = PageRequest.of(from / size, size, sortBy);

        Page<Event> page = eventRepository.findPublicEvents(text, categories, paid, rangeStart, rangeEnd,
                onlyAvailable, pageable
        );

        if (page.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> eventIds = page.getContent().stream()
                .map(Event::getId)
                .collect(toList());

        Map<Long, Long> confirmedMap = getConfirmedRequestsMap(eventIds);
        Map<Long, Long> viewsMap = getViewsForEvents(eventIds);

        return page.getContent().stream()
                .map(event -> {
                    Long confirmedRequests = confirmedMap.getOrDefault(event.getId(), 0L);
                    Long views = viewsMap.getOrDefault(event.getId(), 0L);
                    return eventMapper.toShortDto(event, confirmedRequests, views);
                })
                .collect(toList());
    }

    @Override
    public void saveHit(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String ip = request.getRemoteAddr();
        statsClient.saveHit(APP_NAME, uri, ip);
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getPublicEventById(Long id, HttpServletRequest request) {

        saveHit(request);

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Событие с id " + id + " не найдено"));

        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Событие с id " + id + " не опубликовано");
        }

        Long confirmedRequests = requestRepository.countByEventIdAndStatus(id, Request.Status.CONFIRMED);

        Long views = getViews("/events/" + id);

        return eventMapper.toFullDto(event, confirmedRequests, views);
    }

    @Override
    public List<EventShortDto> toShortDtos(List<Event> events) {
        Map<Long, Long> views = loadViewsSafely(events);
        return events.stream()
                .map(event -> toShortDto(event, views.getOrDefault(event.getId(), 0L)))
                .toList();
    }

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

    private void updateEventFields(Event event, UpdateEventDto dto) {

        if (dto.getTitle() != null) {
            event.setTitle(dto.getTitle());
        }
        if (dto.getAnnotation() != null) {
            event.setAnnotation(dto.getAnnotation());
        }
        if (dto.getDescription() != null) {
            event.setDescription(dto.getDescription());
        }
        if (dto.getEventDate() != null) {
            event.setEventDate(dto.getEventDate());
        }
        if (dto.getPaid() != null) {
            event.setPaid(dto.getPaid());
        }
        if (dto.getParticipantLimit() != null) {
            event.setParticipantLimit(dto.getParticipantLimit());
        }
        if (dto.getRequestModeration() != null) {
            event.setRequestModeration(dto.getRequestModeration());
        }
        if (dto.getCategory() != null) {
            Category category = categoryRepository.findById(dto.getCategory())
                    .orElseThrow(() -> new NotFoundException("Категория с id " + dto.getCategory() + " не найдена"));
            event.setCategory(category);
        }
        if (dto.getLocation() != null) {
            event.setLocation(eventMapper.toLocation(dto.getLocation()));
        }

        if (dto.getStateAction() != null) {
            switch (dto.getStateAction()) {
                case SEND_TO_REVIEW:
                    event.setState(EventState.PENDING);
                    break;
                case CANCEL_REVIEW:
                    event.setState(EventState.CANCELED);
                    break;
            }
        }
    }

    private void updateEventFields(Event event, UpdateEventAdminDto dto) {
        if (dto.getTitle() != null) {
            event.setTitle(dto.getTitle());
        }
        if (dto.getAnnotation() != null) {
            event.setAnnotation(dto.getAnnotation());
        }
        if (dto.getDescription() != null) {
            event.setDescription(dto.getDescription());
        }
        if (dto.getEventDate() != null) {
            event.setEventDate(dto.getEventDate());
        }
        if (dto.getPaid() != null) {
            event.setPaid(dto.getPaid());
        }
        if (dto.getParticipantLimit() != null) {
            event.setParticipantLimit(dto.getParticipantLimit());
        }
        if (dto.getRequestModeration() != null) {
            event.setRequestModeration(dto.getRequestModeration());
        }
        if (dto.getCategory() != null) {
            Category category = categoryRepository.findById(dto.getCategory())
                    .orElseThrow(() -> new NotFoundException("Категория с id " + dto.getCategory() + " не найдена"));
            event.setCategory(category);
        }
        if (dto.getLocation() != null) {
            event.setLocation(eventMapper.toLocation(dto.getLocation()));
        }
    }

    private Long getViews(String uri) {
        LocalDateTime start = LocalDateTime.now().minusYears(100);
        LocalDateTime end = LocalDateTime.now();

        String startStr = start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        String endStr = end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));

        try {
            List<ViewStatsDto> stats = statsClient.getStats(startStr, endStr, List.of(uri), false);
            if (!stats.isEmpty()) {
                return stats.get(0).getHits();
            }
        } catch (Exception e) {
            throw new NotFoundException("Ошибка при получении статистики");
        }
        return 0L;
    }

    private Map<Long, Long> getViewsForEvents(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<String> uris = eventIds.stream()
                .map(id -> "/events/" + id)
                .collect(toList());

        LocalDateTime start = LocalDateTime.now().minusYears(100);
        LocalDateTime end = LocalDateTime.now();

        String startStr = start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        String endStr = end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));

        List<ViewStatsDto> stats = statsClient.getStats(startStr, endStr, uris, false);

        Map<Long, Long> viewsMap = new HashMap<>();

        for (ViewStatsDto stat : stats) {
            String uri = stat.getUri();
            Long eventId = Long.valueOf(uri.substring(uri.lastIndexOf('/') + 1));
            viewsMap.put(eventId, stat.getHits());
        }

        for (Long eventId : eventIds) {
            viewsMap.putIfAbsent(eventId, 0L);
        }

        return viewsMap;
    }

    private Map<Long, Long> getConfirmedRequestsMap(List<Long> eventIds) {
        Map<Long, Long> map = new HashMap<>();

        List<Object[]> results = requestRepository.countByEventIdInAndStatus(
                eventIds,
                Request.Status.CONFIRMED
        );

        for (Object[] row : results) {
            map.put((Long) row[0], (Long) row[1]);
        }

        return map;
    }

    @Override
    public List<RequestResponseDto> getRequestsByEventPrivate(Long userId, Long eventId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User was not found " + userId);
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event was not found: " + eventId));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("User " + userId + " does not match the initiator " + event.getInitiator().getId());
        }

        List<Request> requests = requestRepository.findAllByEventId(eventId);

        return requests.stream()
                .map(RequestMapper::toResponseDto)
                .toList();
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateRequestStatusesPrivate(
            Long userId, Long eventId,
            EventRequestStatusUpdateRequest updateRequest) {

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User was not found " + userId);
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event was not found: " + eventId));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("User " + userId + " does not match the initiator " + event.getInitiator().getId());
        }

        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            throw new ConflictException("Event does not require request moderation");
        }

        Long confirmedRequestsCount = requestRepository.countByEventIdAndStatus(
                eventId,
                Request.Status.CONFIRMED
        );

        List<Request> requests = requestRepository.findAllByIdInAndEventId(updateRequest.getRequestIds(), eventId);

        boolean hasNonPending = requests.stream()
                .anyMatch(request -> request.getStatus() != Request.Status.PENDING);

        if (hasNonPending) {
            throw new ConflictException("All requests must have PENDING status");
        }

        if (updateRequest.getStatus() == Request.Status.CONFIRMED) {
            return confirmation(event, requests, confirmedRequestsCount);
        } else {
            return rejection(requests);
        }
    }

    private EventRequestStatusUpdateResult confirmation(Event event, List<Request> requests, Long confirmedRequestsCount) {
        long limit = event.getParticipantLimit();
        if (limit > 0 && confirmedRequestsCount + requests.size() > limit) {
            throw new ConflictException("Participant limit exceeded");
        }

        requests.forEach(r -> r.setStatus(Request.Status.CONFIRMED));
        requestRepository.saveAll(requests);

        return new EventRequestStatusUpdateResult(requests.stream()
                .filter(r -> r.getStatus() == Request.Status.CONFIRMED)
                .map(RequestMapper::toResponseDto)
                .toList(), Collections.emptyList());
    }

    private EventRequestStatusUpdateResult rejection(List<Request> requests) {
        requests.forEach(r -> r.setStatus(Request.Status.REJECTED));
        requestRepository.saveAll(requests);

        return new EventRequestStatusUpdateResult(Collections.emptyList(), requests.stream()
                .filter(r -> r.getStatus() == Request.Status.REJECTED)
                .map(RequestMapper::toResponseDto)
                .toList());
    }
}
