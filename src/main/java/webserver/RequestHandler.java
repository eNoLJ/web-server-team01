package webserver;

import java.io.*;
import java.net.Socket;

import controller.WebController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.RequestInfo;

public class RequestHandler extends Thread {

    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
    private final Socket connection;
    WebController webController;

    public RequestHandler(Socket connectionSocket, WebController webController) {
        this.connection = connectionSocket;
        this.webController = webController;
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

            webController.run(dos, requestInfo);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
