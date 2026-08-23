package ru.practicum.ewm.event.controllers;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.dto.UpdateEventDto;
import ru.practicum.ewm.event.model.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.event.model.EventRequestStatusUpdateResult;
import ru.practicum.ewm.event.services.EventService;
import ru.practicum.ewm.request.dto.RequestResponseDto;

import java.util.List;

@RestController
@RequestMapping(path = "/users/{userId}/events")
@Validated
public class EventUserController {

    private final EventService eventService;

    public EventUserController(EventService eventUserService) {
        this.eventService = eventUserService;
    }

    @PostMapping
    public ResponseEntity<EventFullDto> createEvent(
            @PathVariable Long userId,
            @Valid @RequestBody NewEventDto dto) {

        EventFullDto created = eventService.createEvent(userId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<EventShortDto>> getEventsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size) {

        List<EventShortDto> events = eventService.getEventsByUser(userId, from, size);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventFullDto> getEventByUser(
            @PathVariable Long userId,
            @PathVariable Long eventId) {

        EventFullDto event = eventService.getEventByUser(userId, eventId);
        return ResponseEntity.ok(event);
    }

    @PatchMapping("/{eventId}")
    public ResponseEntity<EventFullDto> updateEventByUser(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @Valid @RequestBody UpdateEventDto dto) {

        EventFullDto event = eventService.updateEventByUser(userId, eventId, dto);
        return ResponseEntity.ok(event);
    }

    @GetMapping("/{eventId}/requests")
    public List<RequestResponseDto> getRequestsByEvent(
            @PathVariable Long userId,
            @PathVariable Long eventId) {
        return eventService.getRequestsByEventPrivate(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult updateRequestStatuses(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @Valid @RequestBody EventRequestStatusUpdateRequest updateRequest) {
        return eventService.updateRequestStatusesPrivate(userId, eventId, updateRequest);
    }
}
