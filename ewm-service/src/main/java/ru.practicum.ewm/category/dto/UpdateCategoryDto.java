package ru.practicum.ewm.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class UpdateCategoryDto {
    @Pattern(regexp = "^\\S.*\\S$|^\\S$", message = "Поле не должно быть пустым или содержать только пробелы")
    @Size(min = 1, max = 50, message = "Название должно содержать от 1 до 50 символов")
    private String name;
}
