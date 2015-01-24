package pz;

import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

public class StartVerticle extends Verticle {

    public static final String END = "end";

    @Override
    public void start() {
        JsonObject config = container.config();

        container.deployWorkerVerticle(SetupTestDataVerticle.class.getCanonicalName(), config, 1, false,
                v -> container.deployVerticle(PerformanceTestVerticle.class.getCanonicalName(), config));

        vertx.eventBus().registerHandler(END, msg -> {
            container.logger().info("Got exit message: " + msg.body());
            container.exit();
        });
    }


}
