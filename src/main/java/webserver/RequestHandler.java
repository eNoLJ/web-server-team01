package webserver;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import controller.UserController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.RequestInfo;

public class RequestHandler extends Thread {

    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
    private final Socket connection;
    private final UserController userController;

    public RequestHandler(Socket connectionSocket, UserController userController) {
        this.connection = connectionSocket;
        this.userController = userController;
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

            Map<String, Map<String, BiConsumer<DataOutputStream, RequestInfo>>> controller = new HashMap<String, Map<String, BiConsumer<DataOutputStream, RequestInfo>>>() {{
                put("GET", new HashMap<String, BiConsumer<DataOutputStream, RequestInfo>>() {{
                    put("/user/list.html", userController::responseList);
                }});
                put("POST", new HashMap<String, BiConsumer<DataOutputStream, RequestInfo>>() {{
                    put("/user/create", userController::save);
                    put("/user/login", userController::login);
                }});
            }};

            BiConsumer<DataOutputStream, RequestInfo> method = controller.get(requestInfo.getMethod().name()).get(requestInfo.getUri());
            if (method != null) {
                method.accept(dos, requestInfo);
            }
            userController.responseFile(dos, requestInfo);

        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
