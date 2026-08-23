package ru.practicum.ewm.event.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.ewm.category.dto.CategoryResponseDto;
import ru.practicum.ewm.user.dto.UserShortResponseDto;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventShortDto {
    Long id;
    String annotation;
    String title;
    LocalDateTime eventDate;
    Boolean paid;
    CategoryResponseDto category;
    UserShortResponseDto initiator;
    Long views;
    Long confirmedRequests;
}
