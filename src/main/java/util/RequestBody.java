package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.Map;

import static util.HttpRequestUtils.parseValues;
import static util.IOUtils.readData;
import static util.status.HttpHeader.CONTENT_LENGTH;

public class RequestBody {

    private final Map<String, String> bodies;

    private RequestBody(Map<String, String> bodies) {
        this.bodies = bodies;
    }

    public static RequestBody of(BufferedReader bufferedReader, RequestHeader headers) throws IOException {
        if (!headers.containsKey(CONTENT_LENGTH)) {
            return null;
        }
        String body = URLDecoder.decode(readData(bufferedReader, headers.getContentLength()), "UTF-8");
        return new RequestBody(parseValues(body, "&"));
    }

    public Map<String, String> getBodies() {
        return bodies;
    }
}
