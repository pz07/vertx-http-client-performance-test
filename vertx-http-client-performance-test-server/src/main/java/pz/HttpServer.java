package pz;

import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.platform.Verticle;

import java.util.Random;

public class HttpServer extends Verticle {

    private Random random = new Random();

    @Override
    public void start() {
        RouteMatcher routeMatcher = new RouteMatcher();

        routeMatcher.post("/res", new Handler<HttpServerRequest>() {
            @Override
            public void handle(HttpServerRequest req) {
                long willBeProcessingUntil = System.currentTimeMillis() + oneToThreeMinutes();

                String id = String.format("%s_%s", willBeProcessingUntil, Math.abs(random.nextInt()));

                container.logger().info("POST " + id);

                req.response()
                        .putHeader("Content-Length", String.valueOf(id.getBytes().length))
                        .putHeader("Content-Type", "text/plain; charset=utf-8")
                        .setStatusCode(201)
                        .write(id)
                        .end();
            }
        });

        routeMatcher.get("/res/:id", new Handler<HttpServerRequest>() {
            @Override
            public void handle(HttpServerRequest req) {
                container.logger().info("GET /res/" + req.params().get("id"));

                String[] idParam = req.params().get("id").split("_");
                if (idParam.length != 2) {
                    req.response().setStatusCode(404).end();
                    return;
                }

                Long validTo = Long.valueOf(idParam[0]);

                String msg = "FINISHED";
                if (validTo > System.currentTimeMillis()) {
                    msg = "IN_PROGRESS";
                }

                req.response()
                        .putHeader("Content-Length", String.valueOf(msg.getBytes().length))
                        .putHeader("Content-Type", "text/plain; charset=utf-8")
                        .write(msg)
                        .end();
            }
        });

        vertx.createHttpServer().requestHandler(routeMatcher).listen(7777, "localhost");
    }

    private int oneToThreeMinutes() {
        return 1000*60*(1 + random.nextInt(3));
    }
}
