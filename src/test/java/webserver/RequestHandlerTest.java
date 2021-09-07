package webserver;

import controller.WebController;
import model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static db.DataBase.addUser;
import static db.DataBase.findUserById;
import static org.assertj.core.api.Assertions.assertThat;
import static util.status.HttpHeader.*;
import static util.status.HttpMethod.GET;
import static util.status.HttpMethod.POST;

public class RequestHandlerTest {

    private static final int PORT = 8081;

    private ServerSocket listenSocket;
    private Socket connection;
    private WebController webController;

    @BeforeEach
    void startServer() throws IOException {
        listenSocket = new ServerSocket(PORT);
        connection = new Socket("localhost", PORT);
        webController = new WebController();
    }

    @AfterEach
    void stopServer() throws IOException {
        listenSocket.close();
        connection.close();
    }

    @Test
    @DisplayName("Http 요청에 따라 응답이 정상적으로 오는지 확인")
    public void run() throws IOException {
        String requestHeaders = createRequestHeaders(GET.name(), "/index.html", new HashMap<String, String>() {{
            put(ACCEPT.getValue(), "text/html");
        }});
        sendRequest(requestHeaders);
        String expectedResponseMessage = createResponse200(6903, "/index.html");
        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        assertThat(br.lines().collect(Collectors.joining(System.lineSeparator()))).isEqualTo(expectedResponseMessage);
    }

    @Test
    @DisplayName("body를 파싱해 user를 저장하는지 확인")
    public void signUp() throws IOException {
        Map<String, String> headers = new HashMap<String, String>() {{
            put(ACCEPT.getValue(), "text/html");
            put(CONTENT_LENGTH.getValue(), "80");
        }};
        String requestHeaders = createRequestHeaders(POST.name(), "/user/create", headers)
                + "userId=testUser&password=testPassword&name=testName&email=testEmail@test.co.kr";
        sendRequest(requestHeaders);
        User user = findUserById("testUser");
        assertThat(createUser()).isEqualTo(user);
    }

    @Test
    @DisplayName("회원 가입 성공 시, HTTP 302 응답코드 발생 및 index.html로 redirect되는지 확인")
    public void redirectIndex() throws IOException {
        Map<String, String> headers = new HashMap<String, String>() {{
            put(ACCEPT.getValue(), "text/html");
            put(CONTENT_LENGTH.getValue(), "80");
        }};
        String requestHeaders = createRequestHeaders(POST.name(), "/user/create", headers)
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
        Map<String, String> headers = new HashMap<String, String>() {{
            put(ACCEPT.getValue(), "text/html");
            put(CONTENT_LENGTH.getValue(), "37");
        }};
        String requestHeaders = createRequestHeaders(POST.name(), "/user/login", headers)
                + "userId=testUser&password=testPassword";
        sendRequest(requestHeaders);
        String expectedResponseMessage = createResponse302("/index.html")
                + createCookie("logined=true; Path=/") + System.lineSeparator();
        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        assertThat(br.lines().collect(Collectors.joining(System.lineSeparator()))).isEqualTo(expectedResponseMessage);
    }

    @Test
    @DisplayName("로그인 실패 시, cookie값에 logined=false로 표시되고, /user/login_failed.html로 redirect되는지 확인")
    public void loginFailed() throws IOException {
        addUser(createUser());
        Map<String, String> headers = new HashMap<String, String>() {{
            put(ACCEPT.getValue(), "text/html");
            put(CONTENT_LENGTH.getValue(), "43");
        }};
        String requestHeaders = createRequestHeaders(POST.name(), "/user/login", headers)
                + "userId=testUser&password=testPasswordFailed";
        sendRequest(requestHeaders);
        String expectedResponseMessage = createResponse302("/user/login_failed.html")
                + createCookie("logined=false; Path=/") + System.lineSeparator();
        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        assertThat(br.lines().collect(Collectors.joining(System.lineSeparator()))).isEqualTo(expectedResponseMessage);
    }

    @Test
    @DisplayName("로그인이 되어있는 유저일 경우, /user/list.html을 반환하는지 확인")
    public void isLogin() throws IOException {
        addUser(createUser());
        Map<String, String> headers = new HashMap<String, String>() {{
            put(ACCEPT.getValue(), "text/html");
            put(COOKIE.getValue(), "logined=true");
        }};
        String requestHeaders = createRequestHeaders(GET.name(), "/user/list.html", headers);
        sendRequest(requestHeaders);
        String expectedResponseMessage = createResponse200(4801, "/user/list.html");
        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        assertThat(br.lines().collect(Collectors.joining(System.lineSeparator()))).isEqualTo(expectedResponseMessage);
    }

    @Test
    @DisplayName("로그인이 안되어있는 유저일 경우, /user/login.html을 반환하는지 확인")
    public void isNotLogin() throws IOException {
        addUser(createUser());
        Map<String, String> headers = new HashMap<String, String>() {{
            put(ACCEPT.getValue(), "text/html");
            put(COOKIE.getValue(), "logined=false");
        }};
        String requestHeaders = createRequestHeaders(GET.name(), "/user/login.html", headers);
        sendRequest(requestHeaders);
        String expectedResponseMessage = createResponse200(4759, "/user/login.html");
        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        assertThat(br.lines().collect(Collectors.joining(System.lineSeparator()))).isEqualTo(expectedResponseMessage);
    }

    @Test
    @DisplayName(".css 파일 요청 시, 정상적으로 response 되는지 확인.")
    public void extension() throws IOException {
        addUser(createUser());
        Map<String, String> headers = new HashMap<String, String>() {{
            put(ACCEPT.getValue(), "text/css");
        }};
        String requestHeaders = createRequestHeaders(GET.name(), "/css/styles.css", headers);
        sendRequest(requestHeaders);
        String expectedResponseMessage = createResponse200(7065, "/css/styles.css");
        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        assertThat(br.lines().collect(Collectors.joining(System.lineSeparator()))).isEqualTo(expectedResponseMessage);
    }

    private void sendRequest(String requestHeaders) throws IOException {
        RequestHandler requestHandler = new RequestHandler(listenSocket.accept(), webController);
        BufferedOutputStream bufferedStream = new BufferedOutputStream(connection.getOutputStream());
        bufferedStream.write(requestHeaders.getBytes(StandardCharsets.UTF_8));
        bufferedStream.write(System.lineSeparator().getBytes(StandardCharsets.UTF_8));
        bufferedStream.flush();
        requestHandler.run();
    }

    private String createResponse200(int contentLength, String uri) throws IOException {
        return "HTTP/1.1 200 OK" + System.lineSeparator() +
                createContentType(getExtensionByUri(uri)) + System.lineSeparator() +
                createContentLength(contentLength) + System.lineSeparator() +
                "" + System.lineSeparator() +
                Files.lines(new File("./webapp" + uri).toPath())
                        .collect(Collectors.joining(System.lineSeparator()));
    }

    private String createResponse302(String uri) {
        return "HTTP/1.1 302 Found" + System.lineSeparator() +
                createLocation(uri) + System.lineSeparator();
    }

    private String createRequestHeaders(String method, String uri, Map<String, String> headers) {
        StringBuilder result = new StringBuilder(method + " " + uri + " HTTP/1.1" + System.lineSeparator() +
                createHost("localhost:8080") + System.lineSeparator() +
                createConnection("keep-alive") + System.lineSeparator());
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            result.append(entry.getKey()).append(": ").append(entry.getValue()).append(System.lineSeparator());
        }
        return result.append("").append(System.lineSeparator()).toString();
    }

    private User createUser() {
        return User.of(new HashMap<String, String>() {{
            put("userId", "testUser");
            put("password", "testPassword");
            put("name", "testName");
            put("email", "testEmail@test.co.kr");
        }});
    }

    private String getExtensionByUri(String uri) {
        return uri.split("\\.")[1];
    }
}
