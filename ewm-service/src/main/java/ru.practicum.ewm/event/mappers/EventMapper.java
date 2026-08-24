package ru.practicum.ewm.event.mappers;

import ru.practicum.ewm.category.CategoryMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.user.UserMapper;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.category.Category;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.LocationDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.enums.EventState;
import ru.practicum.ewm.event.model.Location;
import ru.practicum.ewm.user.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class EventMapper {

    public static EventShortDto toEventShortDto(Event event) {
        if (event == null) {
            return null;
        }
        return EventShortDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(CategoryMapper.toCategoryDto(event.getCategory()))
                .eventDate(event.getEventDate())
                .initiator(UserMapper.toUserShortDto(event.getInitiator()))
                .paid(event.getPaid())
                .title(event.getTitle())
                .confirmedRequests(0L)
                .views(0L)
                .build();
    }

    public Event toEntity(NewEventDto dto, Category category, User initiator) {
        if (dto == null) {
            return null;
        }

        Event event = new Event();
        event.setTitle(dto.getTitle());
        event.setAnnotation(dto.getAnnotation());
        event.setDescription(dto.getDescription());
        event.setEventDate(dto.getEventDate());
        event.setPaid(dto.getPaid() != null ? dto.getPaid() : false);
        event.setParticipantLimit(dto.getParticipantLimit() != null ? dto.getParticipantLimit() : 0);
        event.setRequestModeration(dto.getRequestModeration() != null ? dto.getRequestModeration() : true);
        event.setCategory(category);
        event.setInitiator(initiator);
        event.setCreated(LocalDateTime.now());
        event.setState(EventState.PENDING);

        if (dto.getLocation() != null) {
            event.setLocation(toLocation(dto.getLocation()));
        }

        return event;
    }

    public EventShortDto toShortDto(Event event) {
        if (event == null) {
            return null;
        }

        return new EventShortDto(
                event.getId(),
                event.getAnnotation(),
                event.getTitle(),
                event.getEventDate(),
                event.getPaid(),
                CategoryMapper.toCategoryDto(event.getCategory()),
                UserMapper.toUserShortDto(event.getInitiator()),
                null,
                null
        );
    }

    public EventShortDto toShortDto(Event event, Long confirmedRequests, Long views) {
        if (event == null) {
            return null;
        }

        return new EventShortDto(
                event.getId(),
                event.getAnnotation(),
                event.getTitle(),
                event.getEventDate(),
                event.getPaid(),
                CategoryMapper.toCategoryDto(event.getCategory()),
                UserMapper.toUserShortDto(event.getInitiator()),
                confirmedRequests != null ? confirmedRequests : 0L,
                views != null ? views : 0L
        );
    }

    public EventFullDto toFullDto(Event event, Long confirmedRequests, Long views) {
        if (event == null) {
            return null;
        }

        return new EventFullDto(
                event.getId(),
                event.getAnnotation(),
                event.getDescription(),
                event.getTitle(),
                event.getEventDate(),
                event.getCreated(),
                event.getPublished(),
                event.getPaid(),
                event.getParticipantLimit(),
                event.getRequestModeration(),
                event.getState() != null ? event.getState().name() : null,
                CategoryMapper.toCategoryDto(event.getCategory()),
                UserMapper.toResponseDto(event.getInitiator()),
                toLocationDto(event.getLocation()),
                confirmedRequests,
                views
        );
    }

    public List<EventShortDto> toShortDtoList(List<Event> events) {
        if (events == null) {
            return null;
        }

        return events.stream()
                .map(this::toShortDto)
                .collect(Collectors.toList());
    }

    public Location toLocation(LocationDto dto) {
        if (dto == null) {
            return null;
        }

        Location location = new Location();
        location.setLat(dto.getLat());
        location.setLon(dto.getLon());
        return location;
    }

    public LocationDto toLocationDto(Location location) {
        if (location == null) {
            return null;
        }

        return new LocationDto(
                location.getLat(),
                location.getLon()
        );
    }
}
