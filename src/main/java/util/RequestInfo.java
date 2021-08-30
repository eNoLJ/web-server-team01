package util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import static util.HttpRequestUtils.*;
import static util.IOUtils.readData;

public class RequestInfo {

    private static final Logger log = LoggerFactory.getLogger(RequestInfo.class);
    private final Map<String, String> startLine;
    private final Map<String, String> headers;
    private final Map<String, String> bodies;

    private RequestInfo(Map<String, String> startLine, Map<String, String> headers, Map<String, String> bodies) {
        this.startLine = startLine;
        this.headers = headers;
        this.bodies = bodies;
    }

    public static RequestInfo of(BufferedReader bufferedReader) throws IOException {
        Map<String, String> startLine = getStartLine(bufferedReader);
        Map<String, String> headers = getHeaders(bufferedReader);
        Map<String, String> bodies = getBodies(bufferedReader, headers);
        return new RequestInfo(startLine, headers, bodies);
    }

    private static Map<String, String> getStartLine(BufferedReader bufferedReader) throws IOException {
        Map<String, String> startLine = new HashMap<>();
        String[] splitStartLine = bufferedReader.readLine().split(" ");
        startLine.put("Method", splitStartLine[0]);
        startLine.put("Uri", splitStartLine[1]);
        return startLine;
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
        if (!headers.containsKey("Content-Length")) {
            return null;
        }
        String body = URLDecoder.decode(readData(bufferedReader,  Integer.parseInt(headers.get("Content-Length"))), "UTF-8");
        return parseValues(body, "&");
    }

    public boolean matchMethod(String method) {
        return startLine.get("Method").equals(method);
    }

    public boolean matchUri(String uri) {
        return startLine.get("Uri").equals(uri);
    }

    public Map<String, String> getBodies() {
        return bodies;
    }

    public String getUri() {
        return startLine.get("Uri");
    }

    public boolean isLogin() {
        String isLogin = parseCookies(headers.get("Cookie")).get("logined");
        return Boolean.parseBoolean(isLogin);
    }
}
