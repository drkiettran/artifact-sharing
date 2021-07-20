package com.drkiettran.sharing;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;

import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.JksOptions;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

@ExtendWith(VertxExtension.class)
public class TestMainVerticle {
	private static final Logger logger = LoggerFactory.getLogger(TestMainVerticle.class);
	private static final DeploymentOptions depOptions = new DeploymentOptions();

	/* @formatter:off */
	
//	public final String SECRET_KEY = "CaQ9KyRT9iPmgyDEyGxgjt/fzdS84bybYLKdYgINgJM=";
//	public final String IV = "V/gF6ibctMSNc/RZwaqjw7MCkhoXDrabiJiLqI6xaqqDIvPstTAW7UkbB7GCzBwuUBEnWYhBcUK6rOFJW49HrgsZLYOq2gaqEaDbMHmfzOVYdsuJBhvRFQnaOUhqQ/51st5rFF6W7/EzKFgW0kQoAV3C7PpWOaRj1n9ChzZP81c=";

	public static final String SECRET_KEY = StixProcessorTest.SECRET_KEY;
	public static final String IV = StixProcessorTest.IV;
	public static final String STIX_TEST = StixProcessorTest.STIX_TEST;
	
	public static final String REQ_TEST = StixProcessorTest.REQ_TEST;
	
	public static final String RESP_OK = "{\"status_code\":200,\"reason\":\"OK\"}";
	/* @formatter:on */

	private String secretKeyStr;
	private String ivStr;
	private SecretKeySpec secretKey;
	private byte[] iv;
	private MainVerticle testVerticle;

	@BeforeAll
	public static void setUpTest() throws IOException {
		logger.info("BeforeAll: Loading config");
		TestingUtil.setUp();
		String cfgFile = TestingUtil.prepare2Run(Vertx.vertx().fileSystem());
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
		System.out.println("@AfterAll ...");
		TestingUtil.cleaningUp(Vertx.vertx().fileSystem());
		vertx.close(testContext.succeeding(response -> {
			testContext.completeNow();
		}));
		System.out.println("@AfterAll ends ...");
	}

	@BeforeEach
	void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
		System.out.println("BeforeEach ...");
		secretKeyStr = SECRET_KEY;
		ivStr = IV;
		byte[] decodedKey = Base64.getDecoder().decode(SECRET_KEY.getBytes());
		secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
		iv = Base64.getDecoder().decode(IV);

		System.out.println("secretKey: " + secretKeyStr);
		System.out.println("iv:" + ivStr);

		testVerticle = new MainVerticle(secretKeyStr, ivStr, depOptions);
		vertx.deployVerticle(testVerticle, depOptions, testContext.succeeding(id -> testContext.completeNow()));
		vertx.getOrCreateContext().put("main-verticle", testVerticle);
		System.out.println("BeforeEach ends ...");
	}

	@AfterEach
	public void finish(Vertx vertx, VertxTestContext testContext) {
		System.out.println("@AfterEach ...");
		vertx.close(testContext.succeeding(response -> {
			testContext.completeNow();
		}));
		System.out.println("@AfterEach ends ...");
	}

	@Test
	void verticle_deployed(Vertx vertx, VertxTestContext testContext) throws Throwable {
		testContext.completeNow();
	}

	@Test
	public void shouldPost(Vertx vertx, VertxTestContext testContext) {
		JksOptions jksOptions = new JksOptions();
		jksOptions.setPath("/home/student/certs/client.jks");
		jksOptions.setPassword("changeit");

		WebClientOptions options = new WebClientOptions();

		options.setSsl(true);
		options.setKeyStoreOptions(jksOptions);
		options.setTrustStoreOptions(jksOptions);

		WebClient client = WebClient.create(vertx, options);

		client.post(9090, "localhost", "/artifact").putHeader("content-type", "application/json")
				.sendBuffer(Buffer.buffer(STIX_TEST))
				.onComplete(testContext.succeeding(buffer -> testContext.verify(() -> {
					HttpResponse<Buffer> resp = buffer;
					System.out.println("resp: " + resp.bodyAsString());
					assertThat(resp.bodyAsJsonObject(), equalTo(new JsonObject(RESP_OK)));
					testContext.completeNow();
				})));
	}

	@Test
	public void shouldGetInvalidUrl(Vertx vertx, VertxTestContext testContext) {
		JksOptions jksOptions = new JksOptions();
		jksOptions.setPath("/home/student/certs/client.jks");
		jksOptions.setPassword("changeit");

		WebClientOptions options = new WebClientOptions();

		options.setSsl(true);
		options.setKeyStoreOptions(jksOptions);
		options.setTrustStoreOptions(jksOptions);

		WebClient client = WebClient.create(vertx, options);
		client.get(9090, "localhost", "/artifactX").putHeader("Content-type", "application/json")
				.sendBuffer(Buffer.buffer(REQ_TEST))
				.onComplete(testContext.succeeding(buffer -> testContext.verify(() -> {
					HttpResponse<Buffer> resp = buffer;
					System.out.println("resp: " + resp.bodyAsString());
					JsonObject payloadJson = resp.bodyAsJsonObject();
					assertThat(payloadJson.getInteger("status_code"), is(400));
					testContext.completeNow();
				})));

	}

	@Test
	public void shouldGet(Vertx vertx, VertxTestContext testContext) {
		JsonObject reqJson = new JsonObject(REQ_TEST);

		JksOptions jksOptions = new JksOptions();
		jksOptions.setPath("/home/student/certs/client.jks");
		jksOptions.setPassword("changeit");

		WebClientOptions options = new WebClientOptions();

		options.setSsl(true);
		options.setKeyStoreOptions(jksOptions);
		options.setTrustStoreOptions(jksOptions);

		WebClient client = WebClient.create(vertx, options);
		client.get(9090, "localhost", "/artifact").putHeader("Content-type", "application/json")
				.sendBuffer(Buffer.buffer(REQ_TEST))
				.onComplete(testContext.succeeding(buffer -> testContext.verify(() -> {
					HttpResponse<Buffer> resp = buffer;
					System.out.println("resp: " + resp.bodyAsString());
					JsonObject payloadJson = resp.bodyAsJsonObject();
					assertThat(payloadJson.getString("id"), equalTo(reqJson.getString("id")));
					verify(vertx, payloadJson);
					testContext.completeNow();
				})));

	}

	@Test
	public void shouldGetInvalidContentType(Vertx vertx, VertxTestContext testContext) {
		JksOptions jksOptions = new JksOptions();
		jksOptions.setPath("/home/student/certs/client.jks");
		jksOptions.setPassword("changeit");

		WebClientOptions options = new WebClientOptions();

		options.setSsl(true);
		options.setKeyStoreOptions(jksOptions);
		options.setTrustStoreOptions(jksOptions);

		WebClient client = WebClient.create(vertx, options);
		client.get(9090, "localhost", "/artifact").putHeader("Content-type", "application/xml")
				.sendBuffer(Buffer.buffer(REQ_TEST))
				.onComplete(testContext.succeeding(buffer -> testContext.verify(() -> {
					HttpResponse<Buffer> resp = buffer;
					System.out.println("resp: " + resp.bodyAsString());
					JsonObject payloadJson = resp.bodyAsJsonObject();
					assertThat(payloadJson.getInteger("status_code"), is(400));
					testContext.completeNow();
				})));

	}

	@Test
	public void shouldGetInvalidPutMethod(Vertx vertx, VertxTestContext testContext) {
		JksOptions jksOptions = new JksOptions();
		jksOptions.setPath("/home/student/certs/client.jks");
		jksOptions.setPassword("changeit");

		WebClientOptions options = new WebClientOptions();

		options.setSsl(true);
		options.setKeyStoreOptions(jksOptions);
		options.setTrustStoreOptions(jksOptions);

		WebClient client = WebClient.create(vertx, options);
		client.put(9090, "localhost", "/artifact").putHeader("Content-type", "application/json")
				.sendBuffer(Buffer.buffer(REQ_TEST))
				.onComplete(testContext.succeeding(buffer -> testContext.verify(() -> {
					HttpResponse<Buffer> resp = buffer;
					System.out.println("resp: " + resp.bodyAsString());
					JsonObject payloadJson = resp.bodyAsJsonObject();
					assertThat(payloadJson.getInteger("status_code"), is(405));
					testContext.completeNow();
				})));

	}

	private boolean verify(Vertx vertx, JsonObject payloadJson) throws Exception {
		Payload payload = payloadJson.mapTo(Payload.class);

		System.out.println("one-time key:" + payload.getEncrypted_key());
		System.out.println("client-private: " + testVerticle.getKeys().getClientPrivKey());
		byte[] clearKey = StixCipher.decrypt(testVerticle.getKeys().getClientPrivKey(),
				Base64.getDecoder().decode(payload.getEncrypted_key()));

		System.out.println("clear key str: " + clearKey);
		System.out.println("clear key str len: " + clearKey.length);
		secretKey = new SecretKeySpec(clearKey, 0, clearKey.length, "AES");
		iv = Base64.getDecoder().decode(payload.getIv());

		String clearText = StixCipher.decrypt("AES/GCM/NoPadding", payload.getEncrypted(), secretKey, iv);
		logger.info("clearedkey: " + clearKey); // uni-code ctrl-shft letters
		logger.info("cleared text:" + clearText);
		Boolean verified = StixCipher.verifySignature(testVerticle.getKeys().getServerPubKey(), clearText.getBytes(),
				Base64.getDecoder().decode(payload.getSignature()));
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
