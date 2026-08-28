package ru.practicum.ewm.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.user.dto.NewUserDto;
import ru.practicum.ewm.user.dto.UserResponseDto;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/users")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserControllerAdmin {
    UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDto addUser(
            @RequestBody @Valid NewUserDto userDto) {
        log.info("Add user {}", userDto);
        return userService.addUserAdmin(userDto);
    }

    @GetMapping
    public List<UserResponseDto> getUsers(
            @RequestParam(required = false)
            List<Long> ids,

            @RequestParam(defaultValue = "0")
            @PositiveOrZero
            Integer from,

            @RequestParam(defaultValue = "10")
            @Positive
            Integer size) {

        log.info("Get users with params {}, {}, {}", ids, from, size);
        return userService.getUsersAdmin(ids, from, size);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long userId) {
        log.info("Delete user with id {}", userId);
        userService.deleteUserAdmin(userId);
    }
}
