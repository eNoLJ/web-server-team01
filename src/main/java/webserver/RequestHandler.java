package webserver;

import java.io.*;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.util.Map;

import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static db.DataBase.addUser;
import static util.HttpRequestUtils.getUriByStartLine;
import static util.HttpRequestUtils.parseQueryString;

public class RequestHandler extends Thread {

    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            InputStreamReader inputStreamReader = new InputStreamReader(in);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String uri = getUriByStartLine(bufferedReader.readLine());

            User user = getUserByUri(uri);
            saveUser(user);

            DataOutputStream dos = new DataOutputStream(out);

            byte[] body = getBodyByUri(uri);
            response200Header(dos, body.length);
            responseBody(dos, body);
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

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private byte[] getBodyByUri(String uri) throws IOException {
        byte[] body = "Hello World".getBytes();
        if (uri != null) {
            if (!uri.equals("/")) {
                body = Files.readAllBytes(new File("./webapp" + uri).toPath());
            }
        }
        return body;
    }

    private User getUserByUri(String uri) {
        try {
            String[] queryString = URLDecoder.decode(uri, "UTF-8").split("\\?");
            Map<String, String> parsedQueryString = parseQueryString(queryString[1]);
            return new User(parsedQueryString.get("userId"),
                    parsedQueryString.get("password"),
                    parsedQueryString.get("name"),
                    parsedQueryString.get("email"));
        } catch (ArrayIndexOutOfBoundsException | UnsupportedEncodingException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    private void saveUser(User user) {
        if (user != null) {
            addUser(user);
        }
    }
}
