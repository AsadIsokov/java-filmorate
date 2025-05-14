import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.practicum.filmorate.FilmorateApplication;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = FilmorateApplication.class)
@ActiveProfiles("test")
class FilmorateApplicationTests {
    @Test
    void contextLoads() {
        assertTrue(true, "Context loaded successfully");
    }
}