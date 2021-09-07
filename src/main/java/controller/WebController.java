package controller;

import service.UserService;
import util.status.HttpMethod;
import util.RequestInfo;

import java.io.DataOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import static util.status.HttpMethod.GET;
import static util.status.HttpMethod.POST;

public class WebController {

    private final Map<HttpMethod, Map<String, BiConsumer<DataOutputStream, RequestInfo>>> controller;

    public WebController() {
        this.controller = init();
    }

    private Map<HttpMethod, Map<String, BiConsumer<DataOutputStream, RequestInfo>>> init() {
        UserController userController = new UserController(new UserService());
        return new HashMap<HttpMethod, Map<String, BiConsumer<DataOutputStream, RequestInfo>>>() {{
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

    public void run(DataOutputStream dos, RequestInfo requestInfo) {
        BiConsumer<DataOutputStream, RequestInfo> executionController = controller.get(requestInfo.getMethod()).get(requestInfo.getUri());
        if (executionController != null) {
            executionController.accept(dos, requestInfo);
            return;
        }
        controller.get(requestInfo.getMethod()).get("/default").accept(dos, requestInfo);
    }
}
