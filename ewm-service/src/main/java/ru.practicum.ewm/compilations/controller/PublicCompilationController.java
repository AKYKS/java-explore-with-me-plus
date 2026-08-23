package ru.practicum.ewm.compilations.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.compilations.dto.CompilationDto;
import ru.practicum.ewm.compilations.service.PublicCompilationService;

import java.util.List;


@RestController
@RequestMapping("/compilations")
@Slf4j
@Validated
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PublicCompilationController {
    PublicCompilationService publicCompilationService;

    @GetMapping
    public List<CompilationDto> getCompilations(@RequestParam (required = false) Boolean pinned,
                                                @RequestParam(defaultValue = "0") Integer from,
                                                @RequestParam(defaultValue = "10") Integer size) {
        log.info("Запрос на получение подборок событий");
        return publicCompilationService.getCompilations(pinned, from, size);
    }

    @GetMapping("/{compId}")
    public CompilationDto getCompilationById(@PathVariable Long compId) {
        log.info("Запрос на получение подбороки событий по id {}", compId);
        return publicCompilationService.getCompilationById(compId);
    }
}
