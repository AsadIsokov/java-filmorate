package ru.yandex.practicum.filmorate.repository.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;

public interface FilmRepositoryInterface {
    Film saveFilm(Film film);

    Film updateFilm(Film film);

    List<Film> getAllFilms();

    Optional<Film> getFilmById(Long id);

    void addLike(Long userId, Long filmId);

    void removeLike(Long userId, Long filmId);

    List<Film> getTheMostPopularFilms(Integer count);

    List<Long> getLikes(Long filmId);
}