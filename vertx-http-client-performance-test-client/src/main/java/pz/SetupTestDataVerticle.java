package pz;

import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.HttpClientResponse;
import org.vertx.java.platform.Verticle;

public class SetupTestDataVerticle extends Verticle{
    public static final String ADDRESS = "setup-test-data";

    @Override
    public void start() {
        HttpClient httpClient = Util.createHttpClient(vertx, container);

        vertx.eventBus().registerHandler(ADDRESS, msg -> {
            int iterations = (int) msg.body();

            container.logger().info("Preparing " + iterations + " items.");

            for (int i = 0; i < iterations; i++) {
                httpClient.post("/res", new CreateResponseHandler())
                        .end();
            }
        });
    }

    private class CreateResponseHandler implements Handler<HttpClientResponse> {
        @Override
        public void handle(HttpClientResponse resp) {
            if (resp.statusCode() != 201) {
                vertx.eventBus().send(StartVerticle.END, "Invalid status POST " + resp.statusCode());
                return;
            }

            resp.bodyHandler(body -> {
               container.logger().info("Resource created: " + body.toString());
               vertx.eventBus().send(PerformanceTestVerticle.RESOURCE_INITIALIZED_ADDRESS, body.toString());
            });
        }
    }
}
