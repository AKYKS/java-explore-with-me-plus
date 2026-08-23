package ru.practicum.ewm.event.services;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.ewm.event.dto.*;
import ru.practicum.ewm.event.enums.EventSort;
import ru.practicum.ewm.event.enums.EventState;
import ru.practicum.ewm.event.model.Event;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {

    List<EventShortDto> toShortDtos(List<Event> events);

    EventFullDto createEvent(Long userId, NewEventDto dto);

    List<EventShortDto> getEventsByUser(Long userId, Integer from, Integer size);

    EventFullDto getEventByUser(Long userId, Long eventId);

    EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventDto dto);

    List<EventFullDto> searchEventsAdmin(List<Long> users, List<EventState> states, List<Long> categories,
                                         LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size
    );

    EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminDto dto);

    List<EventShortDto> getPublicEvents(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                        LocalDateTime rangeEnd, Boolean onlyAvailable, EventSort sort, Integer from,
                                        Integer size, HttpServletRequest request
    );

    void saveHit(HttpServletRequest request);

    EventFullDto getPublicEventById(Long id, HttpServletRequest request);
}
