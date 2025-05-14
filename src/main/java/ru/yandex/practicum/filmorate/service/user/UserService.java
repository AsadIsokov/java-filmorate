package ru.yandex.practicum.filmorate.service.user;

import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserService {
    void addFriend(Long userId, Long friendId);

    void deleteFriend(Long userId, Long friendId);

    UserDto saveUser(User user);

    UserDto updateUser(User user);

    List<UserDto> getCommonFriends(Long user1Id, Long user2Id);

    List<UserDto> getFriendsList(Long userId);

    List<UserDto> getAllUsers();
}