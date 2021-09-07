package util;

import util.status.HttpMethod;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;

public class RequestInfo {

    private final HttpMethod httpMethod;
    private final String uri;
    private final RequestHeader requestHeader;
    private final RequestBody requestBody;

    private RequestInfo(HttpMethod httpMethod, String uri, RequestHeader requestHeader, RequestBody requestBody) {
        this.httpMethod = httpMethod;
        this.uri = uri;
        this.requestHeader = requestHeader;
        this.requestBody = requestBody;
    }

    public static RequestInfo of(BufferedReader bufferedReader) throws IOException {
        String[] splitStartLine = bufferedReader.readLine().split(" ");
        HttpMethod httpMethod = HttpMethod.valueOf(splitStartLine[0]);
        String uri = splitStartLine[1];
        RequestHeader requestHeader = RequestHeader.of(bufferedReader);
        RequestBody requestBody = RequestBody.of(bufferedReader, requestHeader);
        return new RequestInfo(httpMethod, uri, requestHeader, requestBody);
    }

    public boolean isLogin() {
        return requestHeader.isLogin();
    }

    public String getExtension() {
        return requestHeader.getExtension();
    }

    public HttpMethod getMethod() {
        return httpMethod;
    }

    public Map<String, String> getBodies() {
        return requestBody.getBodies();
    }

    public String getUri() {
        return this.uri;
    }
}
