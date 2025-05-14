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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.film.FilmService;
import ru.yandex.practicum.filmorate.service.validation.ValidationService;
import java.util.List;

@RestController
@RequestMapping("/films")
@Slf4j
@RequiredArgsConstructor
public class FilmController {
    private final FilmService filmService;
    private final ValidationService validationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FilmDto addFilm(@RequestBody Film film) {
        log.info("Получен запрос на добавление фильма");
        validationService.validateCreate(film);
        FilmDto savedFilm = filmService.saveFilm(film);
        log.info("Создан новый фильм: {}", film);
        return savedFilm;
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public FilmDto updateFilm(@RequestBody Film film) {
        log.info("Получен запрос на обновление фильма");
        validationService.validateUpdate(film);
        FilmDto updatedFilm = filmService.updateFilm(film);
        log.info("Обновление фильма завершено");
        return updatedFilm;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<FilmDto> getAllFilms() {
        log.info("Получен запрос на получение списка всех фильмов");
        return filmService.getAllFilms();
    }

    @GetMapping("/{filmId}")
    @ResponseStatus(HttpStatus.OK)
    public FilmDto getFilmById(@PathVariable Long filmId) {
        log.info("Получен запрос на получение фильма с id={}", filmId);
        return filmService.getFilmById(filmId);
    }

    @PutMapping("/{filmId}/like/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public void addLike(@PathVariable Long userId,
                        @PathVariable Long filmId) {
        log.info("Пользователь с id={} запросил добавление лайка на фильма с id={}", userId, filmId);
        filmService.addLike(userId, filmId);
        log.info("Добавление лайка завершено");
    }

    @DeleteMapping("/{filmId}/like/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public void removeLike(@PathVariable Long userId,
                           @PathVariable Long filmId) {
        log.info("Пользователь с id={} запросил удаление лайка с фильма с id={}", userId, filmId);
        filmService.removeLike(userId, filmId);
        log.info("Удаление лайка завершено");
    }

    @GetMapping("/popular")
    @ResponseStatus(HttpStatus.OK)
    public List<FilmDto> getTheMostPopularFilms(@RequestParam(required = false, defaultValue = "10") Integer count) {
        log.info("Получен запрос на получение {} самых популярных фильмов", count);
        return filmService.getTheMostPopularFilms(count);
    }
}