package webserver;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import controller.UserController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.UserService;
import util.HttpMethod;
import util.RequestInfo;

import static util.HttpMethod.GET;
import static util.HttpMethod.POST;

public class RequestHandler extends Thread {

    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
    private static final Map<HttpMethod, Map<String, BiConsumer<DataOutputStream, RequestInfo>>> controller;
    private final Socket connection;

    static {
        UserController userController = new UserController(new UserService());
        controller = new HashMap<HttpMethod, Map<String, BiConsumer<DataOutputStream, RequestInfo>>>() {{
            put(GET, new HashMap<String, BiConsumer<DataOutputStream, RequestInfo>>() {{
                put("/user/list.html", userController::responseList);
                put("/default", userController::responseFile);
            }});
            put(POST, new HashMap<String, BiConsumer<DataOutputStream, RequestInfo>>() {{
                put("/user/create", userController::save);
                put("/user/login", userController::login);
            }});
        }};
    }

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
            DataOutputStream dos = new DataOutputStream(out);
            RequestInfo requestInfo = RequestInfo.of(bufferedReader);

            BiConsumer<DataOutputStream, RequestInfo> executionController = controller.get(requestInfo.getMethod()).get(requestInfo.getUri());
            if (executionController != null) {
                executionController.accept(dos, requestInfo);
                return;
            }
            controller.get(requestInfo.getMethod()).get("/default").accept(dos, requestInfo);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
