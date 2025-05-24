package ru.yandex.practicum.filmorate.repository.film;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FilmRepository implements FilmRepositoryInterface {
    private static final String INSERT_FILM = """
            INSERT INTO films (name, description, release_date, duration, mpa_id)
            VALUES (:name, :description, :releaseDate, :duration, :mpaId)
            """;

    private static final String UPDATE_FILM = """
            UPDATE films SET
                name = :name,
                description = :description,
                release_date = :releaseDate,
                duration = :duration,
                mpa_id = :mpaId
            WHERE film_id = :id
            """;

    private static final String GET_ALL_FILMS = """
            SELECT
                f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_id,
                g.genre_id, g.name as genre_name,
                m.mpa_id as mpa_id, m.name as mpa_name
            FROM films f
            LEFT JOIN film_genres fg ON f.film_id = fg.film_id
            LEFT JOIN genres g ON fg.genre_id = g.genre_id
            LEFT JOIN mpa m ON f.mpa_id = m.mpa_id
            ORDER BY f.film_id, g.genre_id
            """;

    private static final String GET_FILM_BY_ID;
    private static final String ADD_LIKE = """
            INSERT INTO likes (film_id, user_id)
            VALUES (:filmId, :userId)
            """;
    private static final String REMOVE_LIKE = """
            DELETE FROM likes
            WHERE user_id = :userId AND film_id = :filmId
            """;
    private static final String GET_POPULAR_FILMS = """
            SELECT
                 f.film_id,
                 f.name,
                 f.description,
                 f.release_date,
                 f.duration,
                 f.mpa_id,
                 g.genre_id,
                 g.name as genre_name,
                 m.mpa_id as mpa_id,
                 m.name as mpa_name,
                 l.likes_count
             FROM films f
             LEFT JOIN film_genres fg ON f.film_id = fg.film_id
             LEFT JOIN genres g ON fg.genre_id = g.genre_id
             LEFT JOIN mpa m ON f.mpa_id = m.mpa_id
             LEFT JOIN (
                 SELECT film_id, COUNT(user_id) as likes_count
                 FROM likes
                 GROUP BY film_id
             ) l ON f.film_id = l.film_id
             ORDER BY l.likes_count DESC, f.film_id ASC
             LIMIT :limit
            """;
    private static final String DELETE_GENRES = """
            DELETE FROM film_genres
            WHERE film_id = :filmId
            """;
    private static final String SET_FILM_GENRE = """
            INSERT INTO film_genres (film_id, genre_id)
            VALUES (:filmId, :genreId)
            """;
    private static final String GET_LIKES_BY_FILM_ID = """
            SELECT user_id
            FROM likes
            WHERE film_id = :filmId
            ORDER BY user_id
            """;
    private static final String FILM_NOT_FOUND = "Фильм с id = %d не найден";

    static {
        GET_FILM_BY_ID = """
                SELECT f.film_id,
                f.name,
                f.description,
                f.release_date,
                f.duration,
                f.mpa_id,
                g.genre_id,
                g.name as genre_name,
                m.mpa_id as mpa_id,
                m.name as mpa_name
                FROM films f
                LEFT JOIN film_genres fg ON f.film_id = fg.film_id
                LEFT JOIN genres g ON fg.genre_id = g.genre_id
                LEFT JOIN mpa m ON f.mpa_id = m.mpa_id
                WHERE f.film_id = :id ORDER BY g.genre_id""";
    }

    private static final int DEFAULT_POPULAR_FILMS_LIMIT = 10;

    private final NamedParameterJdbcOperations jdbc;

    @Transactional
    @Override
    public Film saveFilm(Film film) {
        validateMpaExists(film.getMpa().getId());

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", film.getName())
                .addValue("description", film.getDescription())
                .addValue("releaseDate", film.getReleaseDate())
                .addValue("duration", film.getDuration())
                .addValue("mpaId", film.getMpa().getId());

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(INSERT_FILM, params, keyHolder, new String[]{"film_id"});

        Number generatedId = keyHolder.getKey();
        if (generatedId == null) {
            throw new InternalServerException("Не удалось создать фильм");
        }
        Long id = generatedId.longValue();
        film.setId(id);

        updateFilmGenres(film);
        return film;
    }

    @Transactional
    @Override
    public Film updateFilm(Film film) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", film.getId())
                .addValue("name", film.getName())
                .addValue("description", film.getDescription())
                .addValue("releaseDate", film.getReleaseDate())
                .addValue("duration", film.getDuration())
                .addValue("mpaId", film.getMpa().getId());

        int updatedRows = jdbc.update(UPDATE_FILM, params);
        if (updatedRows == 0) {
            throw new NotFoundException(String.format(FILM_NOT_FOUND, film.getId()));
        }

        updateFilmGenres(film);

        return getFilmById(film.getId())
                .orElseThrow(() -> new NotFoundException(String.format(FILM_NOT_FOUND, film.getId())));
    }

    @Override
    public List<Film> getAllFilms() {
        return jdbc.query(GET_ALL_FILMS, new FilmResultSetExtractor());
    }

    @Override
    public Optional<Film> getFilmById(Long id) {
        MapSqlParameterSource params = new MapSqlParameterSource("id", id);
        List<Film> films = jdbc.query(GET_FILM_BY_ID, params, new FilmResultSetExtractor());
        return films.stream().findFirst(); // Возвращает Optional<Film>
    }

    @Override
    public List<Film> getTheMostPopularFilms(Integer count) {
        int limit = (count == null || count <= 0) ? DEFAULT_POPULAR_FILMS_LIMIT : count;
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("limit", limit);
        return jdbc.query(GET_POPULAR_FILMS, params, new FilmResultSetExtractor());
    }

    @Transactional
    @Override
    public void addLike(Long userId, Long filmId) {
        try {
            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("filmId", filmId)
                    .addValue("userId", userId);
            jdbc.update(ADD_LIKE, params);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicatedDataException("Пользователь уже поставил лайк этому фильму");
        }
    }

    @Transactional
    @Override
    public void removeLike(Long userId, Long filmId) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("filmId", filmId)
                .addValue("userId", userId);
        int deletedRows = jdbc.update(REMOVE_LIKE, params);
        if (deletedRows == 0) {
            throw new NotFoundException("Лайк не найден");
        }
    }

    @Override
    public List<Long> getLikes(Long filmId) {
        try {
            MapSqlParameterSource params = new MapSqlParameterSource("filmId", filmId);
            return jdbc.queryForList(GET_LIKES_BY_FILM_ID, params, Long.class);
        } catch (DataAccessException e) {
            throw new InternalServerException("Ошибка при получении списка лайков");
        }
    }

    private void updateFilmGenres(Film film) {
        if (film.getGenres() == null) {
            return;
        }
        List<Integer> genresIds = validateGenresExist(film.getGenres());
        MapSqlParameterSource params = new MapSqlParameterSource("filmId", film.getId());
        jdbc.update(DELETE_GENRES, params);

        if (genresIds != null && !genresIds.isEmpty()) {
            SqlParameterSource[] batchParams = genresIds.stream()
                    .map(genreId -> new MapSqlParameterSource()
                            .addValue("filmId", film.getId())
                            .addValue("genreId", genreId))
                    .toArray(SqlParameterSource[]::new);

            jdbc.batchUpdate(SET_FILM_GENRE, batchParams);
        }
    }

    private List<Integer> validateGenresExist(List<Genre> genres) {
        List<Integer> genreIds = genres.stream()
                .map(Genre::getId)
                .distinct()
                .toList();

        String sql = "SELECT COUNT(genre_id) FROM genres WHERE genre_id IN (:ids)";
        Integer count = jdbc.queryForObject(sql,
                new MapSqlParameterSource("ids", genreIds),
                Integer.class);

        if (count == null || count != genreIds.size()) {
            throw new NotFoundException("Один или несколько жанров не найдены");
        }
        return genreIds;
    }

    private void validateMpaExists(Integer mpaId) {
        if (mpaId != null) {
            String sql = "SELECT COUNT(mpa_id) FROM mpa WHERE mpa_id = :mpaId";
            Integer count = jdbc.queryForObject(sql,
                    new MapSqlParameterSource("mpaId", mpaId),
                    Integer.class);

            if (count == null || count == 0) {
                throw new NotFoundException("Рейтинг MPA с id=" + mpaId + " не найден");
            }
        }
    }

    static class FilmResultSetExtractor implements ResultSetExtractor<List<Film>> {
        @Override
        public List<Film> extractData(ResultSet rs) throws SQLException {
            Map<Long, Film> filmsMap = new LinkedHashMap<>();
            try {
                while (rs.next()) {
                    Long filmId = rs.getLong("film_id");
                    Film film = filmsMap.computeIfAbsent(filmId, id -> {
                        try {
                            return Film.builder()
                                    .id(rs.getLong("film_id"))
                                    .name(rs.getString("name"))
                                    .description(rs.getString("description"))
                                    .releaseDate(rs.getObject("release_date", LocalDate.class))
                                    .duration(rs.getInt("duration"))
                                    .mpa(new Mpa(
                                            rs.getInt("mpa_id"),
                                            rs.getString("mpa_name")
                                    ))
                                    .genres(new ArrayList<>())
                                    .build();
                        } catch (SQLException e) {
                            throw new InternalServerException("Ошибка при обработке данных фильма из БД");
                        }
                    });

                    if (rs.getObject("genre_id") != null) {
                        film.getGenres().add(new Genre(
                                rs.getInt("genre_id"),
                                rs.getString("genre_name")
                        ));
                    }
                }
            } catch (SQLException e) {
                throw new InternalServerException("Ошибка при чтении данных из ResultSet");
            }

            return new ArrayList<>(filmsMap.values());
        }
    }
}