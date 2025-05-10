package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public Collection<User> allUsers() {
        return userStorage.allUsers();
    }

    public User addUser(User user) {
        validate(user);
        return userStorage.addUser(user);
    }

    public User updateUser(User newUser) {
        if (userStorage.getUserById(newUser.getId()).isEmpty()) {
            throw new NotFoundException("Пользователь с таким id не найден!");
        }
        validate(newUser);
        return userStorage.updateUser(newUser);
    }

    public Collection<User> getFriendsOfUser(Long id) {
        if (id == null) {
            throw new ValidationException("ID не может быть равно null!");
        }
        User user = userStorage.getUserById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с таким id не существует!"));
        if (user.getFriends() == null || user.getFriends().isEmpty()) {
            return Collections.emptyList();
        }
        return user.getFriends().stream()
                .map(userStorage::getUserById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public void addFriend(Long id, Long friendId) {
        if (id == null || friendId == null) {
            throw new ValidationException("ID не может быть равно null!");
        }
        if (id.equals(friendId)) {
            throw new ValidationException("Вы указали одинаковые id!");
        }
        User user = userStorage.getUserById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с таким id не существует!"));
        User friendUser = userStorage.getUserById(friendId)
                .orElseThrow(() -> new NotFoundException("Пользователь с таким id не существует!"));
        if (user.getFriends().add(friendId)) {
            friendUser.getFriends().add(id);
            userStorage.updateUser(user);
            userStorage.updateUser(friendUser);
        }
    }

    public User deleteFriend(Long id, Long friendId) {
        if (id == null || friendId == null) {
            throw new ValidationException("ID не может быть равно null!");
        }
        if (id.equals(friendId)) {
            throw new ValidationException("Вы указали одинаковые id!");
        }
        User user = userStorage.getUserById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с таким id не существует!"));
        User user1 = userStorage.getUserById(friendId)
                .orElseThrow(() -> new NotFoundException("Пользователь с таким id не существует!"));
        user.getFriends().remove(friendId);
        user1.getFriends().remove(id);
        userStorage.updateUser(user);
        userStorage.updateUser(user1);
        return user;
    }

    public Collection<User> commonFriends(Long id, Long otherId) {
        User firstUser = getUserById(id);
        User secondUser = getUserById(otherId);
        Set<Long> firstFriends = new HashSet<>(firstUser.getFriends());
        Set<Long> secondFriends = new HashSet<>(secondUser.getFriends());
        firstFriends.retainAll(secondFriends);
        return firstFriends.stream()
                .map(userStorage::getUserById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public User getUserById(Long id) {
        return userStorage.getUserById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с таким id не найден:" + id));
    }

    private void validate(User user) throws ValidationException {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.warn("Невалидный email: {}", user.getEmail());
            throw new ValidationException("Email должен содержать @ и не быть пустым");
        }

        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.warn("Невалидный логин: {}", user.getLogin());
            throw new ValidationException("Логин не может быть пустым или содержать пробелы");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            log.info("Имя пользователя пустое, будет использован логин");
            user.setName(user.getLogin());
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Невалидная дата рождения: {}", user.getBirthday());
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }
}
