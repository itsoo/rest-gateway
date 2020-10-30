package com.cupshe.restgateway;

import io.vertx.core.Vertx;
import vertx.launcher.StartupVerticle;

/**
 * Application
 *
 * @author zxy
 */
public class Application {

  public static void main(String[] args) {
    Vertx.vertx().deployVerticle(StartupVerticle.class.getName());
  }
}
