package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {
    private static final LocalDate MIN_FILMS_DATE = LocalDate.of(1895, 12, 28);

    private static final int MAX_DESCRIPTION_LENGTH = 200;
    private final FilmStorage filmStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    public Collection<Film> allFilms() {
        return filmStorage.allFilms();
    }

    public Film addFilm(Film film) {
        validate(film);
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film newFilm) {
        if (filmStorage.getFilmById(newFilm.getId()).isEmpty()) {
            throw new NotFoundException("Пользователь с таким id не найден!");
        }
        validate(newFilm);
        return filmStorage.updateFilm(newFilm);
    }

    public Film addLike(Long id, Long userId) {
        if (filmStorage.getFilmById(id).isEmpty()) {
            throw new NotFoundException("Фильм с таким id в списке отсутствует!");
        }
        Film film = getFilmById(id);
        film.getLikes().add(userId);
        filmStorage.updateFilm(film);
        return getFilmById(id);
    }

    public Film deleteLike(Long id, Long userId) {
        if (filmStorage.getFilmById(id).isEmpty()) {
            throw new NotFoundException("Фильм с таким id в списке отсутствует!");
        }
        Film film = getFilmById(id);
        film.getLikes().remove(userId);
        filmStorage.updateFilm(film);
        return getFilmById(id);
    }

    public Collection<Film> countFilms(Integer count) {
        Integer limitValue = count;
        if (count == null) {
            limitValue = 10;
        } else if (count > filmStorage.allFilms().size()) {
            limitValue = filmStorage.allFilms().size();
        }
        return filmStorage.allFilms().stream()
                .sorted(Comparator.comparing((Film film) -> -film.getLikes().size()))
                .limit(limitValue)
                .collect(Collectors.toList());
    }

    public Film getFilmById(Long id) {
        return filmStorage.getFilmById(id)
                .orElseThrow(() -> new NoSuchElementException("Фильм с таким id не найден:" + id));
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
