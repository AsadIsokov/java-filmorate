package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final Map<Long, Film> films = new HashMap<>();
    private static final LocalDate MIN_FILMS_DATE = LocalDate.of(1895, 12, 28);

    private static final int MAX_DESCRIPTION_LENGTH = 200;

    @GetMapping
    public Collection<Film> allFilms() {
        log.info("Получен запрос на получение списка всех фильмов. Текущее количество: {}", films.size());
        return films.values();
    }

    @PostMapping
    public Film addFilm(@RequestBody Film film) {
        log.info("Получен запрос на добавление фильма: {}", film);
        log.info("Проверка на валидность");
        validate(film);
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Фильм успешно добавлен. ID: {}, название: {}", film.getId(), film.getName());
        return film;
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film newFilm) {
        log.info("Получен запрос на обновление фильма с ID: {}", newFilm.getId());
        log.info("Проверка на валидность");
        validate(newFilm);
        if (!films.containsKey(newFilm.getId())) {
            throw new ValidationException("Нет такого фильма с заданным ID");
        }
        Film oldFilm = films.get(newFilm.getId());
        oldFilm.setName(newFilm.getName());
        oldFilm.setDescription(newFilm.getDescription());
        oldFilm.setReleaseDate(newFilm.getReleaseDate());
        oldFilm.setDuration(newFilm.getDuration());
        log.info("Фильм с ID {} успешно обновлен. Новые данные: {}", newFilm.getId(), newFilm);
        return oldFilm;
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    private void validate(Film film) {
        if (film.getName().isEmpty() || film.getName().isBlank()) {
            log.error("Ошибка валидации: название фильма пустое");
            throw new ValidationException("Название не может быть пустым");
        }
        if (film.getDescription().length() > MAX_DESCRIPTION_LENGTH) {
            log.error("Ошибка валидации: описание фильма слишком длинное ({} символов)",
                    film.getDescription().length());
            throw new ValidationException("Максимальная длина описания должна быть меньше 200 символов");
        }
        if (film.getReleaseDate().isBefore(MIN_FILMS_DATE)) {
            log.error("Ошибка валидации: дата релиза {} раньше минимальной допустимой {}",
                    film.getReleaseDate(), MIN_FILMS_DATE);
            throw new ValidationException("Дата релиза должна быть не раньше 28.12.1895 года");
        }
        if (film.getDuration() < 0) {
            log.error("Ошибка валидации: продолжительность фильма отрицательная: {}", film.getDuration());
            throw new ValidationException("Продолжительность фильма должна быть положительной");
        }
        log.debug("Фильм прошел валидацию: {}", film);
    }
}
