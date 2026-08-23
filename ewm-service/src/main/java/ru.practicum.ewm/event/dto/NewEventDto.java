package ru.practicum.ewm.event.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

    @NotNull(message = "Дата события не может быть пустой")
    @Future(message = "Дата события должна быть в будущем")
    private LocalDateTime eventDate;

    @NotNull(message = "Категория должна быть указана")
    private Long category;

    @NotNull
    @Valid
    private LocationDto locationDto;

    private Boolean paid = false;

    private Long participantLimit = 0L;

    private Boolean requestModeration = true;
}
