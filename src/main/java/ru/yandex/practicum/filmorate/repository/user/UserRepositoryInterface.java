package ru.yandex.practicum.filmorate.repository.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepositoryInterface {

    User saveUser(User user);

    User updateUser(User user);

    List<User> getAllUsers();

    Optional<User> getUserById(Long id);

    void addFriend(Long userId, Long friendId);

    void deleteFriend(Long userId, Long friendId);

    List<User> getCommonFriends(Long userId, Long friendId);

    List<User> getFriendsList(Long userId);
}