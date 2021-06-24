package com.drkiettran.sharing;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

@ExtendWith(VertxExtension.class)
public class TestMainVerticle {
	private static final Logger logger = LoggerFactory.getLogger(TestMainVerticle.class);
	private static final DeploymentOptions depOptions = new DeploymentOptions();

	@BeforeAll
	public static void setUpTest() throws IOException {
		logger.info("BeforeAll: Loading config");
		List<String> config = Files.readAllLines(Paths.get("./conf/config.json"));
		StringBuilder sb = new StringBuilder();
		config.stream().forEach(line -> {
			sb.append(line).append('\n');
		});
		logger.info("Loaded: " + sb.toString());
		depOptions.setConfig(new JsonObject(sb.toString()));
	}

	@BeforeEach
	void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
		vertx.deployVerticle(new MainVerticle(), depOptions, testContext.succeeding(id -> testContext.completeNow()));

	}

	@Test
	void verticle_deployed(Vertx vertx, VertxTestContext testContext) throws Throwable {
		testContext.completeNow();
	}
}
