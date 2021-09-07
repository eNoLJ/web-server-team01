package util.status;

public enum HttpHeader {

    HOST("Host"),
    CONNECTION("Connection"),
    CONTENT_TYPE("Content-Type"),
    CONTENT_LENGTH("Content-length"),
    ACCEPT("Accept"),
    LOCATION("Location"),
    SET_COOKIE("Set-Cookie"),
    COOKIE("Cookie");

    private final String value;

    HttpHeader(String value) {
        this.value = value;
    }

    public static String createHost(String host) {
        return HOST.value + ": " + host;
    }

    public static String createConnection(String connection) {
        return CONNECTION.value + ": " + connection;
    }

    public static String createContentType(String extension) {
        return CONTENT_TYPE + ": text/" + extension + ";charset=utf-8;";
    }

    public static String createContentLength(int contentLength) {
        return CONTENT_LENGTH + ": " + contentLength;
    }

    public static String createLocation(String location) {
        return LOCATION.value + ": http://localhost:8080" + location;
    }

    public static String createCookie(String cookies) {
        return SET_COOKIE.value + ": " + cookies;
    }

    public String getValue() {
        return value;
    }
}
