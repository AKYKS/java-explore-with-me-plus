package ru.practicum.ewm.event.service;

import ru.practicum.ewm.event.Event;
import ru.practicum.ewm.event.dto.EventShortDto;

import java.util.List;

public interface EventService {

    List<EventShortDto> toShortDtos(List<Event> events);
}
