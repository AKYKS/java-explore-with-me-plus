package ru.practicum.ewm.event.mappers;

import ru.practicum.ewm.category.CategoryMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.user.UserMapper;

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
}
