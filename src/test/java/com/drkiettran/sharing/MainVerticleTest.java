package com.drkiettran.sharing;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;

import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

@ExtendWith(VertxExtension.class)
public class MainVerticleTest {
//	private static final String JKS_PASSWORD = "changeit";
	private static final String AES = "AES";
	private static final String AES_GCM_NO_PADDING = "AES/GCM/NoPadding";
//	private static final String CLIENT_CERT_JKS = "/home/student/certs/client.jks";
	private static final Logger logger = LoggerFactory.getLogger(MainVerticleTest.class);
	private static final DeploymentOptions depOptions = new DeploymentOptions();

	public static final String SECRET_KEY = TestingUtil.SECRET_KEY;
	public static final String IV = TestingUtil.IV;
	public static final String STIX_TEST = TestingUtil.TEST_ARTIFACT;
	public static final String RESP_OK = TestingUtil.RESP_OK;
	private static MainVerticleConfig config;

	private String secretKeyStr;
	private String ivStr;
	private SecretKeySpec secretKey;
	private byte[] iv;
	private MainVerticle testVerticle;
	private static Keys keys;

	@BeforeAll
	public static void setUpTest() throws IOException, NoSuchAlgorithmException {
		logger.info("BeforeAll: Loading config");
		TestingUtil.setUp();
		String cfgFile = TestingUtil.prepare2Run(Vertx.vertx().fileSystem());
		config = new MainVerticleConfig(new JsonObject(TestingUtil.CONFIG_JSON));
		keys = new Keys(Vertx.vertx(), config, TestingUtil.SECRET_KEY, TestingUtil.IV);
		List<String> config = Files.readAllLines(Paths.get(cfgFile));
		StringBuilder sb = new StringBuilder();
		config.stream().forEach(line -> {
			sb.append(line).append('\n');
		});
		logger.info("Loaded: " + sb.toString());
		depOptions.setConfig(new JsonObject(sb.toString()));
	}

	@AfterAll
	public static void cleanUp(Vertx vertx, VertxTestContext testContext) {
		logger.info("@AfterAll ...");
		TestingUtil.cleaningUp(Vertx.vertx().fileSystem());
		vertx.close(testContext.succeeding(response -> {
			testContext.completeNow();
		}));
		logger.info("@AfterAll ends ...");
	}

	@BeforeEach
	void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
		logger.info("BeforeEach ...");
		secretKeyStr = SECRET_KEY;
		ivStr = IV;
		byte[] decodedKey = Base64.getUrlDecoder().decode(SECRET_KEY.getBytes());
		secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, AES);
		iv = Base64.getUrlDecoder().decode(IV);

		logger.info("secretKey: " + secretKeyStr);
		logger.info("iv:" + ivStr);

		testVerticle = new MainVerticle(secretKeyStr, ivStr, depOptions);
		vertx.deployVerticle(testVerticle, depOptions, testContext.succeeding(id -> testContext.completeNow()));
		vertx.getOrCreateContext().put("main-verticle", testVerticle);
		logger.info("BeforeEach ends ...");
	}

	@AfterEach
	public void finish(Vertx vertx, VertxTestContext testContext) {
		logger.info("@AfterEach ...");

		vertx.close(testContext.succeeding(response -> {
			testContext.completeNow();
		}));
		logger.info("@AfterEach ends ...");
	}

	@Test
	void verticle_deployed(Vertx vertx, VertxTestContext testContext) throws Throwable {
		testContext.completeNow();
	}

	@Test
	public void shouldPost(Vertx vertx, VertxTestContext testContext) {
		WebClientOptions options = new WebClientOptions();

		options.setSsl(true);
		options.setKeyStoreOptions(keys.getJksKeystoreOptions());
		options.setTrustStoreOptions(keys.getJksTruststoreOptions());

		WebClient client = WebClient.create(vertx, options);

		client.post(9090, "localhost", "/artifact").putHeader("content-type", "application/json")
				.sendBuffer(Buffer.buffer(STIX_TEST))
				.onComplete(testContext.succeeding(buffer -> testContext.verify(() -> {
					HttpResponse<Buffer> resp = buffer;
					logger.info("resp: " + resp.bodyAsString());
					assertThat(resp.bodyAsJsonObject(), equalTo(new JsonObject(RESP_OK)));
					testContext.completeNow();
				})));
	}

	@Test
	public void shouldGetInvalidUrl(Vertx vertx, VertxTestContext testContext) {
		WebClientOptions options = new WebClientOptions();

		options.setSsl(true);
		options.setKeyStoreOptions(keys.getJksKeystoreOptions());
		options.setTrustStoreOptions(keys.getJksTruststoreOptions());

		WebClient client = WebClient.create(vertx, options);
		client.get(9090, "localhost", "/artifactX").putHeader("Content-type", "application/json")
				.sendBuffer(Buffer.buffer(STIX_TEST))
				.onComplete(testContext.succeeding(buffer -> testContext.verify(() -> {
					HttpResponse<Buffer> resp = buffer;
					logger.info("resp: " + resp.bodyAsString());
					JsonObject payloadJson = resp.bodyAsJsonObject();
					assertThat(payloadJson.getInteger("status_code"), is(400));
					testContext.completeNow();
				})));

	}

	@Test
	public void shouldGet(Vertx vertx, VertxTestContext testContext) {
		JsonObject reqJson = new JsonObject(STIX_TEST);
		WebClientOptions options = new WebClientOptions();

		options.setSsl(true);
		options.setKeyStoreOptions(keys.getJksKeystoreOptions());
		options.setTrustStoreOptions(keys.getJksTruststoreOptions());

		WebClient client = WebClient.create(vertx, options);
		client.get(9090, "localhost", "/artifact").putHeader("Content-type", "application/json")
				.sendBuffer(Buffer.buffer(STIX_TEST))
				.onComplete(testContext.succeeding(buffer -> testContext.verify(() -> {
					HttpResponse<Buffer> resp = buffer;
					logger.info("resp: " + resp.bodyAsString());
					JsonObject payloadJson = resp.bodyAsJsonObject();
					assertThat(payloadJson.getString("id"), equalTo(reqJson.getString("id")));
					verify(vertx, payloadJson);
					testContext.completeNow();
				})));

	}

	@Test
	public void shouldGetInvalidContentType(Vertx vertx, VertxTestContext testContext) {
		WebClientOptions options = new WebClientOptions();

		options.setSsl(true);
		options.setKeyStoreOptions(keys.getJksKeystoreOptions());
		options.setTrustStoreOptions(keys.getJksTruststoreOptions());

		WebClient client = WebClient.create(vertx, options);
		client.get(9090, "localhost", "/artifact").putHeader("Content-type", "application/xml")
				.sendBuffer(Buffer.buffer(STIX_TEST))
				.onComplete(testContext.succeeding(buffer -> testContext.verify(() -> {
					HttpResponse<Buffer> resp = buffer;
					logger.info("resp: " + resp.bodyAsString());
					JsonObject payloadJson = resp.bodyAsJsonObject();
					assertThat(payloadJson.getInteger("status_code"), is(400));
					testContext.completeNow();
				})));

	}

	@Test
	public void shouldGetInvalidPutMethod(Vertx vertx, VertxTestContext testContext) {
		WebClientOptions options = new WebClientOptions();

		options.setSsl(true);
		options.setKeyStoreOptions(keys.getJksKeystoreOptions());
		options.setTrustStoreOptions(keys.getJksTruststoreOptions());

		WebClient client = WebClient.create(vertx, options);
		client.put(9090, "localhost", "/artifact").putHeader("Content-type", "application/json")
				.sendBuffer(Buffer.buffer(STIX_TEST))
				.onComplete(testContext.succeeding(buffer -> testContext.verify(() -> {
					HttpResponse<Buffer> resp = buffer;
					logger.info("resp: " + resp.bodyAsString());
					JsonObject payloadJson = resp.bodyAsJsonObject();
					assertThat(payloadJson.getInteger("status_code"), is(405));
					testContext.completeNow();
				})));

	}

	private boolean verify(Vertx vertx, JsonObject payloadJson) throws Exception {
		Payload payload = payloadJson.mapTo(Payload.class);

		logger.info("one-time key:" + payload.getEncryptedKey());
		logger.info("client-private: " + testVerticle.getKeys().getClientPrivKey());
		byte[] clearKey = StixCipher.decrypt(testVerticle.getKeys().getClientPrivKey(),
				Base64.getUrlDecoder().decode(payload.getEncryptedKey()));

		logger.info("clear key str: " + clearKey);
		logger.info("clear key str len: " + clearKey.length);
		secretKey = new SecretKeySpec(clearKey, 0, clearKey.length, AES);
		iv = Base64.getUrlDecoder().decode(payload.getIv());

		String clearText = new String(StixCipher.decrypt(AES_GCM_NO_PADDING,
				Base64.getUrlDecoder().decode(payload.getEncrypted().getBytes()), secretKey, iv));
		logger.info("clearedkey: " + clearKey); // uni-code ctrl-shft letters
		logger.info("cleared text:" + clearText);
		Boolean verified = StixCipher.verifySignature(testVerticle.getKeys().getServerPubKey(), clearText.getBytes(),
				Base64.getUrlDecoder().decode(payload.getSignature()));
		logger.info("verified:" + verified);

		logger.info("done reverse...");

		Buffer buffer = Buffer.buffer();
		buffer.appendBytes(payload.toJsonObject().encodePrettily().getBytes());

		JsonObject expected = new JsonObject(STIX_TEST);
		JsonObject actual = new JsonObject(clearText);
		assertThat(actual, equalTo(expected));
		assertThat(verified, is(true));

		return true;
	}
}
