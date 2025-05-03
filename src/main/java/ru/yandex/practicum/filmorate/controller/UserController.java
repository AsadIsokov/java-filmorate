package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> allUsers(){
        log.info("Получен запрос на получение всех пользователей. Текущее количество: {}", users.size());
        return users.values();
    }

    @PostMapping
    public User addUser(@RequestBody User user){
        log.info("Получен запрос на добавление пользователя: {}", user);
        log.info("Проверка на валидность");
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            log.info("Имя пользователя не указано, используется логин '{}' в качестве имени", user.getLogin());
            user.setName(user.getLogin());
        }
        validate(user);
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Пользователь успешно добавлен. ID: {}", user.getId());
        return user;
    }

    @PutMapping
    public User updateUser(@RequestBody User newUser){
        log.info("Получен запрос на обновление пользователя с ID: {}", newUser.getId());
        if (!users.containsKey(newUser.getId())) {
            log.error("Пользователь с ID {} не найден", newUser.getId());
            throw new ValidationException("Нет пользователя с таким ID");
        }
        log.info("Проверка на валидность");

        validate(newUser);
        User oldUser = users.get(newUser.getId());
        oldUser.setEmail(newUser.getEmail());
        if (newUser.getName() == null || newUser.getName().trim().isEmpty()) {
            log.info("Имя пользователя не указано, используется логин '{}' в качестве имени", newUser.getLogin());
            newUser.setName(newUser.getLogin());
        }
        oldUser.setName(newUser.getName());
        oldUser.setLogin(newUser.getLogin());
        oldUser.setBirthday(newUser.getBirthday());
        log.info("Пользователь с ID {} успешно обновлен", newUser.getId());
        return oldUser;
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    private void validate(User user){
        if(user.getEmail().isEmpty() || user.getEmail().isBlank()
                || !user.getEmail().contains("@")){
            log.error("Ошибка валидации пользователя: неверный email {}", user.getEmail());
            throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ @");
        }
        if(user.getLogin().isEmpty() || user.getLogin().contains(" ")){
            log.error("Ошибка валидации пользователя: неверный login {}", user.getLogin());
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }
        if(user.getBirthday().isAfter(LocalDate.now())){
            log.error("Ошибка валидации пользователя: дата рождения в будущем {}", user.getBirthday());
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
        log.debug("Пользователь прошел валидацию: {}", user);
    }
}
