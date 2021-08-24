package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;

import exception.InvalidUriException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.UserService;
import util.RequestInfo;

public class RequestHandler extends Thread {

    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
    private final Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}",
                connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            InputStreamReader inputStreamReader = new InputStreamReader(in);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            RequestInfo requestInfo = RequestInfo.of(bufferedReader);
            byte[] body = "Hello World".getBytes();

            if (requestInfo.matchMethod("GET") && requestInfo.matchUri("/index.html")) {
                body = getBodyByUri("/index.html");
            }
            if (requestInfo.matchMethod("GET") && requestInfo.matchUri("/user/form.html")) {
                body = getBodyByUri("/user/form.html");
            }
            if (requestInfo.matchMethod("POST") && requestInfo.matchUri("/user/create")) {
                UserService.save(requestInfo.getBodies());
                body = getBodyByUri("/index.html");
            }

            DataOutputStream dos = new DataOutputStream(out);
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
        if (uri == null) {
            throw new InvalidUriException();
        }
        return Files.readAllBytes(new File("./webapp" + uri).toPath());
    }
}
