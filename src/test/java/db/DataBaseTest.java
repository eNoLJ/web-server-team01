package db;

import model.User;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

class DataBaseTest {

    private SoftAssertions softly;
    private User user;

    @BeforeEach
    void set() {
        softly = new SoftAssertions();
        user = User.of(new HashMap<String, String>() {{
            put("userId", "testUser");
            put("password", "testPassword");
            put("name", "testName");
            put("email", "testEmail@test.co.kr");
        }});
    }

    @AfterEach
    void execution() {
        softly.assertAll();
    }

    @Test
    void saveUser() {
        DataBase.addUser(user);
        User findUser = DataBase.findUserById("testUser");
        softly.assertThat(findUser).isEqualTo(user);
    }

    @Test
    void matchUserByIdAndPassword() {
        DataBase.addUser(user);
        softly.assertThat(DataBase.matchUserByIdAndPassword(user.getUserId(), user.getPassword())).isTrue();
    }

    @Test
    void findAll() {
        User user2 = User.of(new HashMap<String, String>() {{
            put("userId", "testUser2");
            put("password", "testPassword2");
            put("name", "testName2");
            put("email", "testEmail2@test.co.kr");
        }});
        DataBase.addUser(user);
        DataBase.addUser(user2);
        Collection<User> findAll = DataBase.findAll();

        softly.assertThat(findAll).containsAll(new ArrayList<User>() {{
            add(user);
            add(user2);
        }});
    }
}
