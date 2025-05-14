package ru.yandex.practicum.filmorate.repository.mappers;

import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;

@Component
@AllArgsConstructor
public class FilmRowMapper implements RowMapper<Film> {

    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {

        Film film = Film.builder()
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
        if (rs.getObject("genre_id") != null) {
            film.getGenres().add(new Genre(
                    rs.getInt("genre_id"),
                    rs.getString("genre_name")
            ));
        }
        return film;
    }
}