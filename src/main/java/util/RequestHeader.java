package util;

import util.status.HttpHeader;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static util.HttpRequestUtils.parseCookies;
import static util.HttpRequestUtils.parseHeader;
import static util.status.HttpHeader.*;

public class RequestHeader {

    private final Map<String, String> headers;

    public RequestHeader(Map<String, String> headers) {
        this.headers = headers;
    }

    public static RequestHeader of(BufferedReader bufferedReader) throws IOException {
        Map<String, String> headers = new HashMap<>();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            if (line.equals("")) {
                break;
            }
            if (line.contains(": ")) {
                HttpRequestUtils.Pair pair = parseHeader(line);
                headers.put(pair.key, pair.value);
            }
        }
        return new RequestHeader(headers);
    }

    public int getContentLength() {
        return Integer.parseInt(headers.get(CONTENT_LENGTH.getValue()));
    }

    public boolean containsKey(HttpHeader httpHeader) {
        return headers.containsKey(httpHeader.getValue());
    }

    public boolean isLogin() {
        String isLogin = parseCookies(headers.get(COOKIE.getValue())).get("logined");
        return Boolean.parseBoolean(isLogin);
    }

    public String getExtension() {
        return headers.get(ACCEPT.getValue()).split(",")[0].split("/")[1];
    }
}
