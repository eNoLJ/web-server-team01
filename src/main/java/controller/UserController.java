package controller;

import service.UserService;
import util.RequestInfo;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static util.ResponseUtils.*;

public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    public void responseFile(DataOutputStream dos, RequestInfo requestInfo) {
        byte[] body = getResponseBodyByUri(requestInfo.getUri());
        response200Header(dos, body.length, requestInfo.getExtension());
        responseBody(dos, body);
    }

    public void responseList(DataOutputStream dos, RequestInfo requestInfo) {
        String uri = requestInfo.isLogin() ? requestInfo.getUri() : "/user/login.html";
        byte[] body = getResponseBodyByUri(uri);
        response200Header(dos, body.length, requestInfo.getExtension());
        responseBody(dos, body);
    }

    private byte[] getResponseBodyByUri(String uri) {
        try {
            return Files.readAllBytes(new File("./webapp" + uri).toPath());
        } catch (IOException e) {
            return new byte[]{};
        }
    }

    public void save(DataOutputStream dos, RequestInfo requestInfo) {
        userService.save(requestInfo.getBodies());
        response302Header(dos, "/index.html");
    }

    public void login(DataOutputStream dos, RequestInfo requestInfo) {
        boolean isLogin = userService.login(requestInfo.getBodies());
        if (!isLogin) {
            response302Header(dos, "/user/login_failed.html", false);
            return;
        }
        response302Header(dos, "/index.html", true);
    }
}
