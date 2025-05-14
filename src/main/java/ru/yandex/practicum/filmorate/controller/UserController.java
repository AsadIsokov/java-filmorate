package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.user.UserService;
import ru.yandex.practicum.filmorate.service.validation.ValidationService;

import java.util.List;

@RestController
@RequestMapping("/users")
@Slf4j
@RequiredArgsConstructor
public class UserController {
    private final ValidationService validationService;
    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@RequestBody User user) {
        log.info("Получен запрос на создание пользователя");
        validationService.validateCreate(user);
        UserDto savedUser = userService.saveUser(user);
        log.info("Создан новый пользователь: {}", savedUser);
        return savedUser;
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public UserDto updateUser(@RequestBody User user) {
        log.info("Получен запрос на обновление пользователя");
        validationService.validateUpdate(user);
        UserDto updatedUser = userService.updateUser(user);
        log.info("Обновление пользователя завершено: {}", user);
        return updatedUser;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<UserDto> getAllUsers() {
        log.info("Получен запрос на получение списка пользователей");
        return userService.getAllUsers();
    }

    @PutMapping("/{userId}/friends/{friendId}")
    @ResponseStatus(HttpStatus.OK)
    public void addFriend(@PathVariable Long userId,
                          @PathVariable Long friendId) {
        log.info("Получен запрос на добавление друга");
        userService.addFriend(userId, friendId);
        log.info("Добавление друзей с id={} и id={} завершено", userId, friendId);
    }

    @DeleteMapping("/{userId}/friends/{friendId}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteFriend(@PathVariable Long userId,
                             @PathVariable Long friendId) {
        log.info("Получен запрос на удаление друга");
        userService.deleteFriend(userId, friendId);
        log.info("Пользователи с id={} и id={} больше не друзья", userId, friendId);
    }

    @GetMapping("/{userId}/friends")
    @ResponseStatus(HttpStatus.OK)
    public List<UserDto> getFriendsList(@PathVariable Long userId) {
        log.info("Получен запрос на получение списка друзей у пользователя с id={}", userId);
        return userService.getFriendsList(userId);
    }

    @GetMapping("/{id}/friends/common/{friendId}")
    @ResponseStatus(HttpStatus.OK)
    public List<UserDto> getCommonFriends(@PathVariable Long id,
                                          @PathVariable Long friendId) {
        log.info("Получен запрос на получение списка общих друзей у пользователей с id={} и id={}", id, friendId);
        return userService.getCommonFriends(id, friendId);
    }
}