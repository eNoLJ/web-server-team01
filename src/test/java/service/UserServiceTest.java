package service;

import db.DataBase;
import model.User;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

class UserServiceTest {

    private SoftAssertions softly;
    private UserService userService;
    private Map<String, String> user;

    @BeforeEach
    void set() {
        softly = new SoftAssertions();
        userService = new UserService();
        user = new HashMap<String, String>() {{
            put("userId", "testUser");
            put("password", "testPassword");
            put("name", "testName");
            put("email", "testEmail@test.co.kr");
        }};
    }

    @AfterEach
    void execution() {
        softly.assertAll();
    }

    @Test
    void save() {
        userService.save(user);
        User saveUser = DataBase.findUserById("testUser");
        softly.assertThat(saveUser.getUserId()).isEqualTo("testUser");
        softly.assertThat(saveUser.getPassword()).isEqualTo("testPassword");
        softly.assertThat(saveUser.getEmail()).isEqualTo("testEmail@test.co.kr");
        softly.assertThat(saveUser.getName()).isEqualTo("testName");
    }

    @Test
    void login() {
        userService.save(user);
        softly.assertThat(userService.login(new HashMap<String, String>() {{
                put("userId", "testUser");
                put("password", "testPassword");
            }})).isTrue();
        softly.assertThat(userService.login(new HashMap<String, String>() {{
            put("userId", "testUser");
            put("password", "wrongPassword");
        }})).isFalse();
    }
}
