package util;

import exception.InvalidHttpMethodException;

public enum HttpMethod {

    GET,
    POST,
    PATCH,
    PUT,
    DELETE;

    public static HttpMethod getHttpMethod(String method) {
        switch (method) {
            case "GET":
                return GET;
            case "POST":
                return POST;
            case "PATCH":
                return PATCH;
            case "PUT":
                return PUT;
            case "DELETE":
                return DELETE;
        }
        throw new InvalidHttpMethodException();
    }
}
