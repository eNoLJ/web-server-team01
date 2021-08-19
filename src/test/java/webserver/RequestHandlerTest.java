package webserver;

import model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.stream.Collectors;

import static db.DataBase.findUserById;
import static org.assertj.core.api.Assertions.assertThat;

public class RequestHandlerTest {

    private static final Logger log = LoggerFactory.getLogger(RequestHandlerTest.class);
    private static final int PORT = 8081;

    private ServerSocket listenSocket;
    private Socket connection;

    @BeforeEach
    void startServer() throws IOException {
        listenSocket = new ServerSocket(PORT);
        connection = new Socket("localhost", PORT);
    }

    @AfterEach
    void stopServer() throws IOException {
        listenSocket.close();
        connection.close();
    }

    @Test
    @DisplayName("Http 요청에 따라 응답이 정상적으로 오는지 확인")
    public void run() throws IOException {
        String requestHeaders = "GET /index.html HTTP/1.1" + System.lineSeparator() +
                "Host: localhost:8080" + System.lineSeparator() +
                "Connection: keep-alive" + System.lineSeparator() +
                "Accept: */*" + System.lineSeparator() +
                "" + System.lineSeparator();

        sendRequest(requestHeaders);

        String expectedResponseMessage = "HTTP/1.1 200 OK" + System.lineSeparator() +
                "Content-Type: text/html;charset=utf-8" + System.lineSeparator() +
                "Content-Length: 6903" + System.lineSeparator() +
                "" + System.lineSeparator() +
                Files.lines(new File("./webapp/index.html").toPath())
                        .collect(Collectors.joining(System.lineSeparator()));

        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));

        assertThat(br.lines().collect(Collectors.joining(System.lineSeparator()))).isEqualTo(expectedResponseMessage);
    }

    @Test
    @DisplayName("uri를 파싱해 user를 저장하는지 확인")
    public void createUser() throws IOException {
        String requestHeaders = "GET /webapp/user/create?userId=testUser&password=testPassword&name=testName&email=testEmail%40test.co.kr HTTP/1.1" + System.lineSeparator() +
                "Host: localhost:8080" + System.lineSeparator() +
                "Connection: keep-alive" + System.lineSeparator() +
                "Accept: */*" + System.lineSeparator() +
                "" + System.lineSeparator();

        sendRequest(requestHeaders);

        User user = findUserById("testUser");
        assertThat(new User("testUser", "testPassword", "testName", "testEmail@test.co.kr")).isEqualTo(user);
    }

    private void sendRequest(String requestHeaders) throws IOException {
        RequestHandler requestHandler = new RequestHandler(listenSocket.accept());
        BufferedOutputStream bufferedStream = new BufferedOutputStream(connection.getOutputStream());

        bufferedStream.write(requestHeaders.getBytes(StandardCharsets.UTF_8));
        bufferedStream.write(System.lineSeparator().getBytes(StandardCharsets.UTF_8));
        bufferedStream.flush();
        requestHandler.run();
    }
}
