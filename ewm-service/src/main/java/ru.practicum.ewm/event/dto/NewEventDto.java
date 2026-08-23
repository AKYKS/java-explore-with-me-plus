package ru.practicum.ewm.event.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewEventDto {
    @NotBlank(message = "Заголовок не может быть пустым")
    private String title;

    @NotBlank(message = "Аннотация не может быть пустой")
    private String annotation;

    @NotBlank(message = "Описание не может быть пустым")
    private String description;

    @NotNull(message = "Дата события не может быть пустой")
    @Future(message = "Дата события должна быть в будущем")
    private LocalDateTime eventDate;

    @NotNull(message = "Категория должна быть указана")
    private Long category;

    private Boolean paid = false;

    private Long participantLimit = 0L;

    private Boolean requestModeration = true;
}
