package ru.practicum.ewm.compilations.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.compilations.dto.CompilationDto;
import ru.practicum.ewm.compilations.model.Compilation;
import ru.practicum.ewm.compilations.repository.CompilationRepository;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.exception.ValidationException;

import java.util.List;

import static java.lang.String.format;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PublicCompilationService {
    CompilationRepository compilationRepository;
    EventService eventService;

    private Compilation validationCompilationById(Long compilationId) {
        if (compilationId == null) {
            log.warn("Id удаляемой подборки событий не может быть null");
            throw new ValidationException("Id удаляемой подборки событий не может быть null");
        }
        return compilationRepository.findById(compilationId).orElseThrow(() -> {
            log.warn("Подборки событий с id {} не существует", compilationId);
            throw new NotFoundException(format("Подборки событий с id %d не существует", compilationId));
        });
    }

    private CompilationDto toDto(Compilation compilation) {
        return new CompilationDto(
                compilation.getId(),
                compilation.getTitle(),
                compilation.getPinned(),
                eventService.toShortDtos(compilation.getEvents().stream()
                        .filter(event -> event.getState() == Event.EventState.PUBLISHED)
                        .toList())
        );
    }

    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {
        PageRequest pageRequest = PageRequest.of(
                from / Math.max(1, size),
                Math.max(1, size),
                Sort.by("id"));
        Page<Compilation> compilations = pinned == null
                ? compilationRepository.findAll(pageRequest)
                : compilationRepository.findByPinned(pinned, pageRequest);
        return compilations.stream().map(this::toDto).toList();
    }

    public CompilationDto getCompilationById(Long compilationId) {
        return toDto(validationCompilationById(compilationId));
    }
}
