package util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import static util.HttpRequestUtils.parseHeader;
import static util.HttpRequestUtils.parseValues;
import static util.IOUtils.readData;

public class RequestInfo {

    private static final Logger log = LoggerFactory.getLogger(RequestInfo.class);
    private final Map<String, String> startLine;
    private final Map<String, String> headers;
    private final Map<String, String> bodies;

    public static RequestInfo of(BufferedReader bufferedReader) throws IOException {
        Map<String, String> startLine = getStartLine(bufferedReader);
        Map<String, String> headers = getHeaders(bufferedReader);
        Map<String, String> bodies = new HashMap<>();
        try {
            bodies = getBodies(bufferedReader, Integer.parseInt(headers.get("Content-Length")));
        } catch (NumberFormatException e) {
            log.error(e.getMessage());
        }
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

    private static Map<String, String> getBodies(BufferedReader bufferedReader, int contentLength) throws IOException {
        String body = URLDecoder.decode(readData(bufferedReader, contentLength), "UTF-8");
        return parseValues(body, "&");
    }

    private RequestInfo(Map<String, String> startLine, Map<String, String> headers, Map<String, String> bodies) {
        this.startLine = startLine;
        this.headers = headers;
        this.bodies = bodies;
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
}
