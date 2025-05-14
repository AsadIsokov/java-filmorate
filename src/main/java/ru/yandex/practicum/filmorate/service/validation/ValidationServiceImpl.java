package ru.yandex.practicum.filmorate.service.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.repository.film.FilmRepositoryInterface;
import ru.yandex.practicum.filmorate.repository.user.UserRepositoryInterface;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;


import java.time.LocalDate;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ValidationServiceImpl implements ValidationService {
    private static final int MAX_DESCRIPTION_SIZE = 200;
    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);
    private final UserRepositoryInterface userRepository;
    private final FilmRepositoryInterface filmRepository;

    @Override
    public void validateCreate(User user) {
        validateEmail(user.getEmail());
        validateLogin(user.getLogin());
        validateBirthday(user.getBirthday());
        if (isEmailUsed(user.getEmail())) {
            throw new DuplicatedDataException("Этот email уже используется");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    @Override
    public void validateUpdate(User user) {
        if (user.getId() == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }

        User userFromRepository = userRepository.getUserById(user.getId())
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        if (user.getEmail() != null && !userFromRepository.getEmail().equals(user.getEmail())) {
            validateEmail(user.getEmail());
            if (isEmailUsed(user.getEmail())) {
                throw new DuplicatedDataException("Этот email уже используется");
            }
        }
        if (user.getLogin() != null) validateLogin(user.getLogin());
        if (user.getBirthday() != null) validateBirthday(user.getBirthday());
    }

    @Override
    public void validateCreate(Film film) {
        validateFilmName(film.getName());
        validateDescription(film.getDescription());
        validateReleaseDate(film.getReleaseDate());
        validateDuration(film.getDuration());
    }

    @Override
    public void validateUpdate(Film film) {
        if (film.getId() == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }

        filmRepository.getFilmById(film.getId())
                .orElseThrow(() -> new NotFoundException("Фильм не найден"));

        if (film.getDescription() != null) {
            validateDescription(film.getDescription());
        }
        if (film.getDuration() != 0) {
            validateDuration(film.getDuration());
        }
        if (film.getReleaseDate() != null) {
            validateReleaseDate(film.getReleaseDate());
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ConditionsNotMetException("Email должен быть указан");
        }
        if (!email.contains("@")) {
            throw new ConditionsNotMetException("Email должен содержать символ @");
        }
    }

    private void validateLogin(String login) {
        if (login == null || login.isBlank() || login.contains(" ")) {
            throw new ConditionsNotMetException("Логин не может быть пустым и содержать пробелы");
        }
    }

    private void validateBirthday(LocalDate birthday) {
        if (birthday.isAfter(LocalDate.now())) {
            throw new ConditionsNotMetException("Дата рождения не может быть в будущем");
        }
    }

    private void validateFilmName(String name) {
        if (name == null || name.isBlank()) {
            throw new ConditionsNotMetException("Название фильма должно быть указано");
        }
    }

    private void validateDescription(String description) {
        if (description != null && description.length() > MAX_DESCRIPTION_SIZE) {
            throw new ConditionsNotMetException("Максимальная длина описания — " + MAX_DESCRIPTION_SIZE);
        }
    }

    private void validateDuration(int duration) {
        if (duration <= 0) {
            throw new ConditionsNotMetException("Продолжительность должна быть положительным числом");
        }
    }

    private boolean isEmailUsed(String email) {
        return userRepository.getAllUsers().stream()
                .anyMatch(user -> user.getEmail().equals(email));
    }

    private void validateReleaseDate(LocalDate releaseDate) {
        if (releaseDate.isBefore(CINEMA_BIRTHDAY)) {
            throw new ConditionsNotMetException(String.format("Дата релиза должна быть не раньше %s", CINEMA_BIRTHDAY));
        }
    }
}