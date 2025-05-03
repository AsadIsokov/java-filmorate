package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerValidationTest {
    private UserController userController;
    private User validUser;

    @BeforeEach
    void setUp() {
        userController = new UserController();
        validUser = User.builder()
                .email("valid@email.com")
                .login("validLogin")
                .name("Valid Name")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();
    }

    @Test
    void shouldAcceptValidUser() {
        User addedUser = userController.addUser(validUser);
        assertNotNull(addedUser);
        assertTrue(addedUser.getId() > 0);
    }

    @Test
    void shouldRejectEmptyEmail() {
        User user = validUser.toBuilder().email("").build();

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.addUser(user));
        assertTrue(exception.getMessage().contains("Электронная почта не может быть пустой"));
    }

    @Test
    void shouldRejectEmailWithoutAt() {
        User user = validUser.toBuilder().email("invalid.email.com").build();

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.addUser(user));
        assertTrue(exception.getMessage().contains("должна содержать символ @"));
    }

    @Test
    void shouldUseLoginWhenNameIsEmpty() {
        User user = validUser.toBuilder().name("").build();

        User addedUser = userController.addUser(user);
        assertEquals(user.getLogin(), addedUser.getName());
    }


    @Test
    void shouldRejectEmptyLogin() {
        User user = validUser.toBuilder().login("").build();

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.addUser(user));
        assertTrue(exception.getMessage().contains("Логин не может быть пустым"));
    }

    @Test
    void shouldRejectFutureBirthday() {
        User user = validUser.toBuilder()
                .birthday(LocalDate.now().plusDays(1))
                .build();

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.addUser(user));
        assertTrue(exception.getMessage().contains("Дата рождения не может быть в будущем"));
    }
}