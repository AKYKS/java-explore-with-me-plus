package ru.practicum.ewm.event.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.category.dto.CategoryResponseDto;
import ru.practicum.ewm.user.dto.UserResponseDto;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventShortDto {

    private Long id;
    private String annotation;
    private String title;
    private LocalDateTime eventDate;
    private Boolean paid;

    private CategoryResponseDto category;
    private UserResponseDto initiator;

    private Long confirmedRequests;
    private Long views;
}
