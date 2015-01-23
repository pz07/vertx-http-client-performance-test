package pz;

import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.HttpClientResponse;
import org.vertx.java.platform.Verticle;
import sun.util.logging.resources.logging;

public class SetupTestDataVerticle extends Verticle{
    public static final String ADDRESS = "setup-test-data";

    @Override
    public void start() {
        String host = container.config().getString("host", "localhost");
        int port = container.config().getInteger("port", 7777);
        boolean keepAlive = container.config().getBoolean("keepAlive", true);

        HttpClient httpClient = vertx.createHttpClient()
                .setKeepAlive(keepAlive)
                .setHost(host)
                .setPort(port);


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
