package ru.practicum.ewm.event.dto;

import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.practicum.ewm.event.enums.AdminStateAction;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateEventAdminDto {

    @Size(min = 3, max = 120)
    String title;

    @Size(min = 20, max = 2000)
    String annotation;

    @Size(min = 20, max = 7000)
    String description;

    @Future(message = "Дата события должна быть в будущем")
    LocalDateTime eventDate;

    Long category;

    LocationDto location;

    Boolean paid;

    @PositiveOrZero(message = "Количество участников должно быть положительным")
    Long participantLimit;

    Boolean requestModeration;

    AdminStateAction stateAction;
}
