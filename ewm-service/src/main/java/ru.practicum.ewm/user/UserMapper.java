package ru.practicum.ewm.user;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.user.dto.NewUserDto;
import ru.practicum.ewm.user.dto.UserResponseDto;
import ru.practicum.ewm.user.dto.UserShortResponseDto;

@UtilityClass
public class UserMapper {

    public User toUser(NewUserDto userDto) {
        return User.builder()
                .id(null)
                .name(userDto.getName())
                .email(userDto.getEmail())
                .build();
    }

    public UserResponseDto toResponseDto(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }

    public static UserShortResponseDto toUserShortDto(User user) {
        if (user == null) {
            return null;
        }

        return UserShortResponseDto.builder()
                .id(user.getId())
                .name(user.getName())
                .build();
    }
}
