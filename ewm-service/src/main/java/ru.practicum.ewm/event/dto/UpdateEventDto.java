package ru.practicum.ewm.event.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.event.enums.UserStateAction;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEventDto {

    @Size(min = 3, max = 120)
    private String title;

    @Size(min = 20, max = 2000)
    private String annotation;

    @Size(min = 20, max = 7000)
    private String description;

    @Future(message = "Дата события должна быть в будущем")
    private LocalDateTime eventDate;

    private Long category;

    private LocationDto location;

    private Boolean paid;

    @PositiveOrZero(message = "Количество участников должно быть положительным")
    private Long participantLimit;

    private Boolean requestModeration;

    private UserStateAction stateAction;
}
