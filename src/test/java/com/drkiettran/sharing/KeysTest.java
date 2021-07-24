package com.drkiettran.sharing;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.security.NoSuchAlgorithmException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

//@ExtendWith(VertxExtension.class)
public class KeysTest {

	private MainVerticleConfig config;
	private Keys keys;
	private Vertx vertx = Vertx.vertx();

	@BeforeEach
	@DisplayName("Setting up ...")
	public void setUp() {
		TestingUtil.prepare2Run(vertx.fileSystem());
		config = new MainVerticleConfig(new JsonObject(TestingUtil.CONFIG_JSON));
	}

	@BeforeEach
	@DisplayName("Tear down ...")
	public void cleanUp() {
		TestingUtil.prepare2Run(vertx.fileSystem());
	}

	@Test
	@DisplayName("testing constructor")
	public void shouldCreate() throws NoSuchAlgorithmException {
		keys = new Keys(Vertx.vertx(), config, TestingUtil.SECRET_KEY, TestingUtil.IV);
		keys.getJksKeystoreOptions();
		keys.getJksTruststoreOptions();
		assertThat(keys.getIvStr(), equalTo(TestingUtil.IV));
	}
}
