package ru.practicum.ewm.compilations.dto;

import ru.practicum.ewm.event.dto.EventShortDto;

import java.util.List;

public record CompilationDto(
        Long id,
        String title,
        Boolean pinned,
        List<EventShortDto> events
) {
}
