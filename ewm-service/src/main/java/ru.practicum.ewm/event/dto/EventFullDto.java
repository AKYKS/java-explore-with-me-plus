package ru.practicum.ewm.event.dto;

import lombok.*;
import ru.practicum.ewm.category.dto.CategoryResponseDto;
import ru.practicum.ewm.user.dto.UserResponseDto;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventFullDto {

    private Long id;
    private String annotation;
    private String description;
    private String title;
    private LocalDateTime eventDate;
    private LocalDateTime createdOn;
    private LocalDateTime publishedOn;
    private Boolean paid;
    private Long participantLimit;
    private Boolean requestModeration;
    private String state;

    private CategoryResponseDto category;
    private UserResponseDto initiator;
    private LocationDto location;

    private Long confirmedRequests;
    private Long views;
}
