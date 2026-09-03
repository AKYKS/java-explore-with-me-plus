package ru.practicum.ewm.comment.dto;

import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateCommentDto {
    @Size(min = 1, max = 2000, message = "Комментарий должен содержать от 1 до 2000 символов")
    String text;
}
