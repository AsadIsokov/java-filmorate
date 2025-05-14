import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import ru.yandex.practicum.filmorate.FilmorateApplication;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.repository.user.UserRepository;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserRepository.class, UserRowMapper.class})
@ContextConfiguration(classes = FilmorateApplication.class)
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private Long firstUserId;
    private Long secondUserId;

    @BeforeEach
    void initializeTestData() {
        User firstUser = User.builder()
                .email("user1@test.org")
                .login("first-user")
                .name("First Test User")
                .birthday(LocalDate.of(2010, 12, 15))
                .build();

        User secondUser = User.builder()
                .email("user2@test.org")
                .login("second-user")
                .name("Second Test User")
                .birthday(LocalDate.of(1994, 6, 10))
                .build();

        firstUserId = userRepository.saveUser(firstUser).getId();
        secondUserId = userRepository.saveUser(secondUser).getId();
    }

    @Test
    void savedUserShouldHaveValidId() {
        User savedUser = userRepository.getUserById(firstUserId).orElseThrow();
        assertThat(savedUser.getId()).isPositive();
    }

    @Test
    void savedUserShouldMatchInitialData() {
        User savedUser = userRepository.getUserById(firstUserId).orElseThrow();
        assertThat(savedUser.getLogin()).isEqualTo("first-user");
        assertThat(savedUser.getName()).isEqualTo("First Test User");
        assertThat(savedUser.getBirthday()).isEqualTo(LocalDate.of(2010, 12, 15));
    }

    @Test
    void shouldUpdateOnlyAllowedFields() {
        User originalUser = userRepository.getUserById(firstUserId).orElseThrow();

        User modifiedUser = User.builder()
                .id(originalUser.getId())
                .email("updated@example.com")
                .login(originalUser.getLogin())
                .name("Updated Name")
                .birthday(originalUser.getBirthday())
                .build();

        User updatedUser = userRepository.updateUser(modifiedUser);

        assertThat(updatedUser).usingRecursiveComparison()
                .ignoringFields("email", "name")
                .isEqualTo(originalUser);

        assertThat(updatedUser.getEmail()).isEqualTo("updated@example.com");
        assertThat(updatedUser.getName()).isEqualTo("Updated Name");
    }

    @Test
    void shouldContainAllCreatedUsers() {
        List<User> users = userRepository.getAllUsers();
        assertThat(users).hasSize(2);
        assertThat(users.stream().map(User::getId))
                .contains(firstUserId, secondUserId);
    }

    @Test
    void shouldAddFriendToFriendsList() {
        userRepository.addFriend(firstUserId, secondUserId);
        assertThat(userRepository.getFriendsList(firstUserId))
                .extracting(User::getId)
                .containsExactly(secondUserId);
    }

    @Test
    void shouldRemoveFriendship() {
        assertThat(userRepository.getFriendsList(firstUserId)).isEmpty();
        userRepository.addFriend(firstUserId, secondUserId);
        assertThat(userRepository.getFriendsList(firstUserId)).isNotEmpty();
        userRepository.deleteFriend(firstUserId, secondUserId);
        assertThat(userRepository.getFriendsList(firstUserId)).isEmpty();
    }

    @Test
    void shouldIdentifyMutualConnections() {
        User mutualFriend = userRepository.saveUser(
                User.builder()
                        .email("common@example.com")
                        .login("common-friend")
                        .name("Common Friend")
                        .birthday(LocalDate.of(2003, 5, 8))
                        .build());

        userRepository.addFriend(firstUserId, mutualFriend.getId());
        userRepository.addFriend(secondUserId, mutualFriend.getId());

        List<User> commonFriends = userRepository.getCommonFriends(firstUserId, secondUserId);
        assertThat(commonFriends)
                .anyMatch(user -> user.getId().equals(mutualFriend.getId()))
                .hasSize(1);
    }
}