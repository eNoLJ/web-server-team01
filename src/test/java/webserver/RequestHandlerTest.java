package webserver;

import org.junit.BeforeClass;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Header;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.StringBody.exact;

class RequestHandlerTest {

    static private ClientAndServer mockServer;

    @BeforeAll
    static void startServer() {
        mockServer = startClientAndServer(8081);
    }

    @AfterAll
    static void stopServer() {
        mockServer.stop();
    }

    @Test
    void run() {
        new MockServerClient("127.0.0.1",8081)
            .when(
                request()
                    .withMethod("GET")
                    .withPath("/"),
//                    .withHeader("\"Content-type\", \"application/json\""),
                exactly(1))
            .respond(
                response()
                    .withStatusCode(404)
//                    .withHeaders(
//                            new Header("Content-Type", "application/json; charset=utf-8"))
                    .withBody("Hello World")
//                    .withDelay(TimeUnit.SECONDS,1)
            );


    }
}
