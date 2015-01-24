package pz;

import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.HttpClientResponse;
import org.vertx.java.platform.Verticle;

public class PerformanceTestVerticle extends Verticle {

    public static final String RESOURCE_INITIALIZED_ADDRESS = "resource-initialized";
    private static final String CHECK_STATUS_ADDRESS = "check-status-address";

    private int numberOfIterations;
    private int numberOfProcessedResources = 0;

    private HttpClient httpClient;

    @Override
    public void start() {
        httpClient = Util.createHttpClient(vertx, container);

        numberOfIterations = container.config().getInteger("iterations", 1000);

        prepareData(numberOfIterations);

        vertx.eventBus().registerHandler(RESOURCE_INITIALIZED_ADDRESS, msg -> {
            checkStatus(httpClient, msg.body().toString());
        });

        vertx.eventBus().registerHandler(CHECK_STATUS_ADDRESS, msg -> {
            checkStatus(httpClient, msg.body().toString());
        });
    }


    private void checkStatus(HttpClient httpClient, String resId) {
        httpClient.get("/res/" + resId, new CheckStatusHandler(resId, System.currentTimeMillis()))
                .end();
    }

    private void prepareData(int numberOfIterations) {
        vertx.eventBus().send(SetupTestDataVerticle.ADDRESS, numberOfIterations);
    }

    private class CheckStatusHandler implements Handler<HttpClientResponse> {

        private String resId;
        private long startedAt;

        public CheckStatusHandler(String resId, long startedAt) {
            this.resId = resId;
            this.startedAt = startedAt;
        }

        @Override
        public void handle(HttpClientResponse resp) {
            container.logger().info("Response in " + (System.currentTimeMillis() - this.startedAt) + " millis.");

            if (resp.statusCode() != 200) {
                vertx.eventBus().send(StartVerticle.END, "Invalid status GET " + resId + ": " + resp.statusCode());
                return;
            }

            resp.bodyHandler(body -> {
               container.logger().info(body.toString());
               if ("FINISHED".equals(body.toString())) {
                   container.logger().info("Processing finished.");
                   numberOfProcessedResources++;

                   if (numberOfProcessedResources == numberOfIterations) {
                       vertx.eventBus().send(StartVerticle.END, "Success.");
                   }
               } else {
                   vertx.setTimer(500, t -> vertx.eventBus().send(CHECK_STATUS_ADDRESS, resId));
               }
            });

        }
    }
}
