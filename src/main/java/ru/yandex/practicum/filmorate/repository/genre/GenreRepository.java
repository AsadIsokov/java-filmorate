package ru.yandex.practicum.filmorate.repository.genre;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.repository.mappers.GenreRowMapper;

import java.util.List;

@Repository
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class GenreRepository implements GenreRepositoryInterface {
    private static final String GET_ALL_SQL = """
            SELECT
                genre_id,
                name
            FROM genres
            ORDER BY genre_id""";
    private static final String GET_BY_ID_SQL = """
            SELECT
                genre_id,
                name
                FROM genres
                WHERE genre_id = :id""";
    private static final String FIND_BY_FILM_ID = """
            SELECT
                g.genre_id,
                g.name
            FROM genres g
            JOIN film_genres fg ON g.genre_id = fg.genre_id
            WHERE fg.film_id = :filmId
            ORDER BY g.genre_id
            """;

    private final NamedParameterJdbcOperations jdbc;
    private final GenreRowMapper mapper;

    @Override
    public List<Genre> getAllGenres() {
        return jdbc.query(GET_ALL_SQL, mapper);
    }

    @Override
    public Genre getById(int id) {
        MapSqlParameterSource params = new MapSqlParameterSource("id", id);
        try {
            return jdbc.queryForObject(GET_BY_ID_SQL, params, mapper);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Жанр не найден");
        }
    }

    @Override
    public List<Genre> findByFilmId(Long filmId) {
        return jdbc.query(
                FIND_BY_FILM_ID,
                new MapSqlParameterSource("filmId", filmId),
                new GenreRowMapper()
        );
    }
}