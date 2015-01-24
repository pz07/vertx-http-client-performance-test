package pz;

import org.vertx.java.core.Vertx;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.platform.Container;

public class Util {

    public static HttpClient createHttpClient(Vertx vertx, Container container) {
        String host = container.config().getString("host", "localhost");
        int port = container.config().getInteger("port", 7777);
        boolean keepAlive = container.config().getBoolean("keepAlive", true);

        return vertx.createHttpClient()
                    .setKeepAlive(keepAlive)
                    .setHost(host)
                    .setPort(port);
    }

}
