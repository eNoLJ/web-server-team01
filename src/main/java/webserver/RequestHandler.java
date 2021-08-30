package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.UserService;
import util.RequestInfo;

public class RequestHandler extends Thread {

    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
    private final Socket connection;
    private final UserService userService;

    public RequestHandler(Socket connectionSocket, UserService userService) {
        this.connection = connectionSocket;
        this.userService = userService;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}",
                connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            InputStreamReader inputStreamReader = new InputStreamReader(in);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            DataOutputStream dos = new DataOutputStream(out);
            RequestInfo requestInfo = RequestInfo.of(bufferedReader);

            if (requestInfo.matchMethod("GET")) {
                String uri = requestInfo.getUri();
                if (requestInfo.matchUri("/user/list.html") && !requestInfo.isLogin()) {
                    uri = "/user/login.html";
                }
                byte[] body = getBodyByUri(uri);
                response200Header(dos, body.length);
                responseBody(dos, body);
            }

            if (requestInfo.matchMethod("POST")) {
                if (requestInfo.matchUri("/user/create")) {
                    userService.save(requestInfo.getBodies());
                    response302Header(dos, "/index.html");
                }
                if (requestInfo.matchUri("/user/login")) {
                    boolean isLogin = userService.login(requestInfo.getBodies());
                    if (!isLogin) {
                        response302Header(dos, "/user/login_failed.html", false);
                    }
                    response302Header(dos, "/index.html", true);
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK\r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302Header(DataOutputStream dos, String redirectUri) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found\r\n");
            dos.writeBytes("Location: http://localhost:8080" + redirectUri + "\r\n");
            dos.writeBytes("\r\n");
            dos.close();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302Header(DataOutputStream dos, String redirectUri, boolean loginCookie) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found\r\n");
            dos.writeBytes("Location: http://localhost:8080" + redirectUri + "\r\n");
            dos.writeBytes("Set-Cookie: logined=" + loginCookie + "; Path=/" + "\r\n");
            dos.writeBytes("\r\n");
            dos.close();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
            dos.close();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private byte[] getBodyByUri(String uri) throws IOException {
        return Files.readAllBytes(new File("./webapp" + uri).toPath());
    }
}
