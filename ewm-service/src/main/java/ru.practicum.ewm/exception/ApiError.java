package ru.practicum.ewm.exception;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ApiError {
    String message;
    String reason;
    String status;
    String timestamp;
    List<String> errors;
}