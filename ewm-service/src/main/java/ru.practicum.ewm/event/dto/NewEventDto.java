package ru.practicum.ewm.event.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewEventDto {
    @NotBlank(message = "Заголовок не может быть пустым")
    @Size(min = 3, max = 120)
    private String title;

    @NotBlank(message = "Аннотация не может быть пустой")
    @Size(min = 20, max = 2000)
    private String annotation;

    @NotBlank(message = "Описание не может быть пустым")
    @Size(min = 20, max = 7000)
    private String description;

    @Future(message = "Дата события должна быть в будущем")
    private LocalDateTime eventDate;

    private Long category;

    private LocationDto location;

    private Boolean paid = false;

    @PositiveOrZero(message = "Количество участников должно быть положительным")
    private Long participantLimit = 0L;

    private Boolean requestModeration = true;
}
