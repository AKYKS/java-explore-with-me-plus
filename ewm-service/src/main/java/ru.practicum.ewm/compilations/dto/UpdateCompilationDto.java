package ru.practicum.ewm.compilations.dto;

import jakarta.validation.constraints.Size;

import java.util.Set;

public record UpdateCompilationDto(
        @Size(min = 1, max = 50)
        String title,
        Boolean pinned,
        Set<Long> events
) {
}
