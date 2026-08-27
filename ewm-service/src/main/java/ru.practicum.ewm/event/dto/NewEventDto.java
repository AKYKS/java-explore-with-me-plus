package ru.practicum.ewm.event.dto;

import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NewEventDto {
    @NotBlank(message = "Заголовок не может быть пустым")
    @Size(min = 3, max = 120)
    String title;

    @NotBlank(message = "Аннотация не может быть пустой")
    @Size(min = 20, max = 2000)
    String annotation;

    @NotBlank(message = "Описание не может быть пустым")
    @Size(min = 20, max = 7000)
    String description;

    @Future(message = "Дата события должна быть в будущем")
    LocalDateTime eventDate;

    Long category;

    LocationDto location;

    Boolean paid = false;

    @PositiveOrZero(message = "Количество участников должно быть положительным")
    Long participantLimit = 0L;

    Boolean requestModeration = true;
}
