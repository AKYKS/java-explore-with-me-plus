package ru.practicum.ewm.compilations.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.compilations.dto.CompilationDto;
import ru.practicum.ewm.compilations.dto.NewCompilationDto;
import ru.practicum.ewm.compilations.dto.UpdateCompilationDto;
import ru.practicum.ewm.compilations.model.Compilation;
import ru.practicum.ewm.compilations.repository.CompilationRepository;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.event.services.EventService;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.exception.ValidationException;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static java.lang.String.format;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminCompilationService {
    CompilationRepository compilationRepository;
    EventRepository eventRepository;
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

    private Set<Event> loadEvent(Set<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return new LinkedHashSet<>();
        }
        return new LinkedHashSet<>(eventRepository.findAllById(eventIds));
    }

    private CompilationDto toDto(Compilation compilation) {
        List<Event> events = List.copyOf(compilation.getEvents());
        return new CompilationDto(
                compilation.getId(),
                compilation.getTitle(),
                compilation.getPinned(),
                eventService.toShortDtos(events)
        );
    }

    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
        Compilation compilation = Compilation.builder()
                .title(newCompilationDto.title())
                .pinned(Boolean.TRUE.equals(newCompilationDto.pinned()))
                .events(loadEvent(newCompilationDto.events()))
                .build();
        return toDto(compilationRepository.save(compilation));
    }

    public CompilationDto updateCompilation(Long compilationId, UpdateCompilationDto updateCompilationDto) {
        Compilation compilation = validationCompilationById(compilationId);
        if (updateCompilationDto.title() != null) {
            compilation.setTitle(updateCompilationDto.title());
        }
        if (updateCompilationDto.pinned() != null) {
            compilation.setPinned(updateCompilationDto.pinned());
        }
        if (updateCompilationDto.events() != null) {
            compilation.setEvents(loadEvent(updateCompilationDto.events()));
        }
        return toDto(compilationRepository.save(compilation));
    }

    public void deleteCompilation(Long compilationId) {
        compilationRepository.delete(validationCompilationById(compilationId));
    }
}
