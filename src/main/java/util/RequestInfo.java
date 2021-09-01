package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import static util.HttpHeader.*;
import static util.HttpMethod.getHttpMethod;
import static util.HttpRequestUtils.*;
import static util.IOUtils.readData;

public class RequestInfo {

    private final HttpMethod httpMethod;
    private final String uri;
    private final Map<String, String> headers;
    private final Map<String, String> bodies;

    private RequestInfo(HttpMethod httpMethod, String uri, Map<String, String> headers, Map<String, String> bodies) {
        this.httpMethod = httpMethod;
        this.uri = uri;
        this.headers = headers;
        this.bodies = bodies;
    }

    public static RequestInfo of(BufferedReader bufferedReader) throws IOException {
        String[] splitStartLine = bufferedReader.readLine().split(" ");
        HttpMethod httpMethod = getHttpMethod(splitStartLine[0]);
        String uri = splitStartLine[1];
        Map<String, String> headers = getHeaders(bufferedReader);
        Map<String, String> bodies = getBodies(bufferedReader, headers);
        return new RequestInfo(httpMethod, uri, headers, bodies);
    }

    private static Map<String, String> getHeaders(BufferedReader bufferedReader) throws IOException {
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
        return headers;
    }

    private static Map<String, String> getBodies(BufferedReader bufferedReader, Map<String, String> headers) throws IOException {
        if (!headers.containsKey(CONTENT_LENGTH.getValue())) {
            return null;
        }
        String body = URLDecoder.decode(readData(bufferedReader, Integer.parseInt(headers.get(CONTENT_LENGTH.getValue()))), "UTF-8");
        return parseValues(body, "&");
    }

    public boolean matchMethod(HttpMethod method) {
        return httpMethod == method;
    }

    public boolean matchUri(String uri) {
        return this.uri.equals(uri);
    }

    public Map<String, String> getBodies() {
        return bodies;
    }

    public String getUri() {
        return this.uri;
    }

    public boolean isLogin() {
        String isLogin = parseCookies(headers.get(COOKIE.getValue())).get("logined");
        return Boolean.parseBoolean(isLogin);
    }

    public String getExtension() {
        return headers.get(ACCEPT.getValue()).split(",")[0].split("/")[1];
    }
}
