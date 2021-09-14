package util;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import static util.status.HttpHeader.*;

class RequestInfoTest {

    private SoftAssertions softly;
    private BufferedReader reader;

    @BeforeEach
    void set() {
        softly = new SoftAssertions();
        reader = new BufferedReader(new StringReader(createRequestHeaders()));
    }

    @AfterEach
    void execution() {
        softly.assertAll();
    }

    @Test
    void isLogin() throws IOException {
        RequestInfo requestInfo = RequestInfo.of(reader);
        softly.assertThat(requestInfo.isLogin()).isTrue();
    }

    @Test
    void getExtension() throws IOException{
        RequestInfo requestInfo = RequestInfo.of(reader);
        softly.assertThat(requestInfo.getExtension()).isEqualTo("html");
    }

    private String createRequestHeaders() {
        return "GET /index.html HTTP/1.1" + System.lineSeparator() +
                createHost("localhost:8080") + System.lineSeparator() +
                createConnection("keep-alive") + System.lineSeparator() +
                createCookie("logined=true") + System.lineSeparator() +
                createAccept("text/html") + System.lineSeparator() +
                "" + System.lineSeparator();
    }
}
