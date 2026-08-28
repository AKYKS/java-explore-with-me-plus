package ru.practicum.ewm.event.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.ewm.category.dto.CategoryResponseDto;
import ru.practicum.ewm.user.dto.UserResponseDto;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventFullDto {
    Long id;
    String annotation;
    String description;
    String title;
    LocalDateTime eventDate;
    LocalDateTime createdOn;
    LocalDateTime publishedOn;
    Boolean paid;
    Long participantLimit;
    Boolean requestModeration;
    String state;

    CategoryResponseDto category;
    UserResponseDto initiator;
    LocationDto location;

    Long confirmedRequests;
    Long views;
}
