package vertx.launcher;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

/**
 * @author zxy
 */
@Slf4j
public class StartupVerticle extends AbstractVerticle {

  @Override
  public void start(Promise<Void> promise) {
    envConfigure().getConfig(ar ->
      vertx.createHttpServer().requestHandler(req -> {
        req.response()
          .putHeader("content-type", "text/plain")
          .end("Hello from Vert.x!");

        log.debug("OK");
      }).listen(getServerPort(ar.result()), res -> listenHandle(res, promise)));
  }

  private ConfigRetriever envConfigure() {
    return ConfigRetriever.create(vertx,
      new ConfigRetrieverOptions().addStore(
        new ConfigStoreOptions()
          .setType("file")
          .setFormat("hocon")
          .setConfig(new JsonObject().put("path", "application.conf"))));
  }

  private int getServerPort(JsonObject conf) {
    return conf.getJsonObject("server").getInteger("port");
  }

  private void listenHandle(AsyncResult<HttpServer> res, Promise<Void> promise) {
    if (res.failed()) {
      promise.fail(res.cause());
      return;
    }

    promise.complete();
    log.info("HTTP server started on port 8888");
  }
}
