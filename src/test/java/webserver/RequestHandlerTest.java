package webserver;

import model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import service.UserService;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.stream.Collectors;

import static db.DataBase.addUser;
import static db.DataBase.findUserById;
import static org.assertj.core.api.Assertions.assertThat;

public class RequestHandlerTest {

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
        String requestHeaders = createRequestHeaders("GET", "/index.html");
        sendRequest(requestHeaders);
        String expectedResponseMessage = createResponse200(6903, "/index.html");
        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        assertThat(br.lines().collect(Collectors.joining(System.lineSeparator()))).isEqualTo(expectedResponseMessage);
    }

    @Test
    @DisplayName("body를 파싱해 user를 저장하는지 확인")
    public void signUp() throws IOException {
        String requestHeaders = createRequestHeaders("POST", "/user/create", 80)
                + "userId=testUser&password=testPassword&name=testName&email=testEmail@test.co.kr";
        sendRequest(requestHeaders);
        User user = findUserById("testUser");
        assertThat(createUser()).isEqualTo(user);
    }

    @Test
    @DisplayName("회원 가입 성공 시, HTTP 302 응답코드 발생 및 index.html로 redirect되는지 확인")
    public void redirectIndex() throws IOException {
        String requestHeaders = createRequestHeaders("POST", "/user/create", 80)
                + "userId=testUser&password=testPasswordFailed";
        sendRequest(requestHeaders);
        String expectedResponseMessage = createResponse302("/index.html");
        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        assertThat(br.lines().collect(Collectors.joining(System.lineSeparator()))).isEqualTo(expectedResponseMessage);
    }

    @Test
    @DisplayName("로그인 성공 시, cookie값에 logined=true로 표시되고, index.html로 redirect되는지 확인")
    public void LoginSuccess() throws IOException {
        addUser(createUser());
        String requestHeaders = createRequestHeaders("POST", "/user/login", 37)
                + "userId=testUser&password=testPassword";
        sendRequest(requestHeaders);
        String expectedResponseMessage = createResponse302("/index.html")
                + "Set-Cookie: logined=true; Path=/" + System.lineSeparator();
        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        assertThat(br.lines().collect(Collectors.joining(System.lineSeparator()))).isEqualTo(expectedResponseMessage);
    }

    @Test
    @DisplayName("로그인 실패 시, cookie값에 logined=false로 표시되고, /user/login_failed.html로 redirect되는지 확인")
    public void loginFailed() throws IOException {
        addUser(createUser());
        String requestHeaders = createRequestHeaders("POST", "/user/login", 43)
                + "userId=testUser&password=testPasswordFailed";
        sendRequest(requestHeaders);
        String expectedResponseMessage = createResponse302("/user/login_failed.html")
                + "Set-Cookie: logined=false; Path=/" + System.lineSeparator();
        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        assertThat(br.lines().collect(Collectors.joining(System.lineSeparator()))).isEqualTo(expectedResponseMessage);
    }

    private void sendRequest(String requestHeaders) throws IOException {
        RequestHandler requestHandler = new RequestHandler(listenSocket.accept(), new UserService());
        BufferedOutputStream bufferedStream = new BufferedOutputStream(connection.getOutputStream());
        bufferedStream.write(requestHeaders.getBytes(StandardCharsets.UTF_8));
        bufferedStream.write(System.lineSeparator().getBytes(StandardCharsets.UTF_8));
        bufferedStream.flush();
        requestHandler.run();
    }

    private String createResponse200(int contentLength, String uri) throws IOException {
        return "HTTP/1.1 200 OK" + System.lineSeparator() +
                "Content-Type: text/html;charset=utf-8" + System.lineSeparator() +
                "Content-Length: " + contentLength + System.lineSeparator() +
                "" + System.lineSeparator() +
                Files.lines(new File("./webapp" + uri).toPath())
                        .collect(Collectors.joining(System.lineSeparator()));
    }

    private String createResponse302(String uri) {
        return "HTTP/1.1 302 Found" + System.lineSeparator() +
                "Location: http://localhost:8080" + uri + System.lineSeparator();
    }

    private String createRequestHeaders(String method, String uri) {
        return method + " " + uri + " HTTP/1.1" + System.lineSeparator() +
                "Host: localhost:8080" + System.lineSeparator() +
                "Connection: keep-alive" + System.lineSeparator() +
                "Accept: */*" + System.lineSeparator() +
                "" + System.lineSeparator();
    }

    private String createRequestHeaders(String method, String uri, int contentLength) {
        return method + " " + uri + " HTTP/1.1" + System.lineSeparator() +
                "Host: localhost:8080" + System.lineSeparator() +
                "Connection: keep-alive" + System.lineSeparator() +
                "Accept: */*" + System.lineSeparator() +
                "Content-Length: " + contentLength + System.lineSeparator() +
                "" + System.lineSeparator();
    }

    private User createUser() {
        return User.of(new HashMap<String, String>() {{
            put("userId", "testUser");
            put("password", "testPassword");
            put("name", "testName");
            put("email", "testEmail@test.co.kr");
        }});
    }
}
