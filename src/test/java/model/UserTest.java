package model;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

class UserTest {

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
    void matchPassword() {
        softly.assertThat(user.matchPassword("testPassword")).isTrue();
        softly.assertThat(user.matchPassword("wrongPassword")).isFalse();
    }
}
