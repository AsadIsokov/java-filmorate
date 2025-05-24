package ru.yandex.practicum.filmorate.repository.user;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.mappers.UserRowMapper;

import java.sql.Types;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserRepository implements UserRepositoryInterface {
    private static final String GET_USER_BY_ID = """
            SELECT
                user_id,
                email,
                login,
                name,
                birthday
            FROM users WHERE user_id = :id""";
    private static final String INSERT_USER = """
            INSERT INTO users (email, login, name, birthday)
            VALUES (:email, :login, :name, :birthday)
            """;
    private static final String UPDATE_USER = """
            UPDATE users SET
                email = :email,
                login = :login,
                name = :name,
                birthday = :birthday
            WHERE user_id = :id
            """;
    private static final String GET_ALL_USERS = """
            SELECT
                user_id,
                email,
                login,
                name,
                birthday
            FROM users""";
    private static final String ADD_FRIEND = """
            INSERT INTO friends(user_id, friend_id)
            VALUES (:userId, :friendId)
            """;
    private static final String GET_COMMON_FRIENDS = """
            SELECT
                u.user_id,
                u.email,
                u.login,
                u.name,
                u.birthday
            FROM users u
            JOIN friends f1 ON u.user_id = f1.friend_id
            JOIN friends f2 ON u.user_id = f2.friend_id
            WHERE f1.user_id = :userId AND f2.user_id = :friendId
            """;
    private static final String GET_FRIENDS_LIST = """
            SELECT
                u.user_id,
                u.email,
                u.login,
                u.name,
                u.birthday
            FROM users u
            JOIN friends f ON u.user_id = f.friend_id
            WHERE f.user_id = :userId
            """;
    private static final String DELETE_FRIEND = """
            DELETE FROM friends
            WHERE user_id = :userId AND friend_id = :friendId""";

    private static final String USER_NOT_FOUND = "Пользователь с id = %d не найден";

    private final NamedParameterJdbcOperations jdbc;
    private final UserRowMapper mapper;

    @Override
    public User saveUser(User user) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("email", user.getEmail())
                .addValue("login", user.getLogin())
                .addValue("name", user.getName())
                .addValue("birthday", user.getBirthday());

        jdbc.update(INSERT_USER, params, keyHolder, new String[]{"user_id"});

        Number generatedId = keyHolder.getKey();
        if (generatedId == null) {
            throw new InternalServerException("Не удалось создать пользователя");
        }
        Long id = generatedId.longValue();
        user.setId(id);

        return getUserById(id).orElseThrow(() ->
                new InternalServerException("Пользователь создан, но не найден"));

    }

    @Override
    public User updateUser(User user) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", user.getId())
                .addValue("email", user.getEmail())
                .addValue("login", user.getLogin())
                .addValue("name", user.getName())
                .addValue("birthday", user.getBirthday());

        int updatedRows = jdbc.update(UPDATE_USER, params);

        if (updatedRows == 0) {
            throw new NotFoundException(String.format(USER_NOT_FOUND, user.getId()));
        }
        return getUserById(user.getId())
                .orElseThrow(() ->
                        new NotFoundException(String.format("%s %d после обновления", USER_NOT_FOUND, user.getId())));
    }

    @Override
    public List<User> getAllUsers() {
        return jdbc.query(GET_ALL_USERS, mapper);
    }

    @Override
    public Optional<User> getUserById(Long id) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", id);
        try {
            User result = jdbc.queryForObject(GET_USER_BY_ID, params, mapper);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        try {
            jdbc.update(ADD_FRIEND, new MapSqlParameterSource()
                    .addValue("userId", userId, Types.BIGINT)
                    .addValue("friendId", friendId, Types.BIGINT));
        } catch (DataAccessException e) {
            throw new InternalServerException("Не удалось добавить друга");
        }
    }

    @Transactional
    @Override
    public void deleteFriend(Long userId, Long friendId) {
        jdbc.update(DELETE_FRIEND, new MapSqlParameterSource()
                .addValue("userId", userId, Types.BIGINT)
                .addValue("friendId", friendId, Types.BIGINT));
    }

    @Override
    public List<User> getCommonFriends(Long userId, Long friendId) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("friendId", friendId);

        return jdbc.query(GET_COMMON_FRIENDS, params, mapper);
    }

    @Override
    public List<User> getFriendsList(Long userId) {
        return jdbc.query(GET_FRIENDS_LIST, new MapSqlParameterSource("userId", userId), mapper);
    }
}