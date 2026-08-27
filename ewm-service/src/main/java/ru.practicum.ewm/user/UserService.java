package ru.practicum.ewm.user;

import ru.practicum.ewm.user.dto.NewUserDto;
import ru.practicum.ewm.user.dto.UserResponseDto;

import java.util.List;

public interface UserService {

    UserResponseDto addUserAdmin(NewUserDto userDto);

    List<UserResponseDto> getUsersAdmin(List<Long> ids, Integer from, Integer size);

    void deleteUserAdmin(Long userId);
}
