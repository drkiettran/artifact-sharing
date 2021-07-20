package com.drkiettran.sharing;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.SecretKey;
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
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

@ExtendWith(VertxExtension.class)
public class StixProcessorTest {
	private static final Logger logger = LoggerFactory.getLogger(StixProcessorTest.class);
	public static final String ALG = "AES";

	/* @formatter:off */
	
	public static final String STIX_TEST = "{\n"
			+ "  \"type\": \"artifact\",\n"
			+ "  \"spec_version\": \"2.1\",\n"
			+ "  \"id\": \"artifact--6f437177-6e48-5cf8-9d9e-872a2bddd641\",\n"
			+ "  \"mime_type\": \"application/zip\",\n"
			+ "  \"payload_bin\": \"ZX7HIBWPQA99NSUhEUgAAADI== ...\",\n"
			+ "  \"encryption_algorithm\": \"mime-type-indicated\",\n"
			+ "  \"decryption_key\": \"My voice is my passport\"\n"
			+ "}";
	
	public static final String REQ_TEST = "{\n"
			+ "  \"type\": \"artifact\",\n"
			+ "  \"spec_version\": \"2.1\",\n"
			+ "  \"id\": \"artifact--6f437177-6e48-5cf8-9d9e-872a2bddd641\"\n"
			+ "}";
	
	public static final String SECRET_KEY = "LP+MDHeBcFphSGJ+bDn9hPTs7ZEJhXjcXfl2yijiPo0=";
	public static final String IV = "5DXrwdzDLdwWWretX3SRgFq8+6FZioCg7k30rWybEkZc6TJ5Hwp1rV1VcqzJE7KIh3u/mbrhSf98h2HqmhzjVtenhinCe4toFAXkQOKQUsc9ISbktCBtk1LXMN6l9CrsEJll46dBsFNOPZxa7mUAkdiqNPCKNBXxSbL7k/rOb34=";
	public static final String ENCRYPTED_STIX = "{\n"
			+ "  	 \"id\" : \"artifact--6f437177-6e48-5cf8-9d9e-872a2bddd641\",\n"
			+ "  	 \"encrypted\" : \"7APPmg2YLM/8fLcaxnBWJo4oBZ8GfZYm9uPpBwF4hrztmbeJoydrlyFotmf28cGIWW9ZMxodSYefS04ryElwRED657TFyg4KNM4n9X31+jaWRlUqWw8brVbAnWdn79Ux+nIlShA71RLHdizXtrx/2Hy4Yt6fk89SkmMjXZVh4Oi4QgwEeFcLmeFxJ5HrogcAgHCoGZpyE9ptFNH7vlQ200tQZvu7j4i88Aj3UjAYsrmCeTCBZJbWb9lDx/wwJPsgjde13X5vwktNUarwe5eDkFc6jimTSIB2Kxny4fuicTZackPspR611CbIR0KRlZBSN9cPKqmAH9aoSwtmGiKQUM5tTnKD6/NAzWj465tA+zEK5TYrX4MbOg77Wm35XAZmoWOT4AbzDledNz7nlYL41A==\"\n"
			+ "	 }";
	/* @formatter:on */
	private static final DeploymentOptions depOptions = new DeploymentOptions();

	private SecretKey secretKey;

	private byte[] iv;

	private String ivStr;

	private String secretKeyStr;

	private String stixString;
	private JsonObject stix;
	private MainVerticle testVerticle;

	@BeforeAll
	public static void setUpTest() throws IOException {
		logger.info("BeforeAll: Loading config");
		depOptions.setConfig(new JsonObject(
				Vertx.vertx().fileSystem().readFileBlocking(TestingUtil.prepare2Run(Vertx.vertx().fileSystem()))));
		logger.info("config:\n" + depOptions.getConfig().encodePrettily());
	}

	@AfterAll
	public static void cleanUp(Vertx vertx, VertxTestContext testContext) {
		logger.info("@AfterAll ...");
		TestingUtil.cleaningUp(vertx.fileSystem());
		vertx.close(testContext.succeeding(response -> {
			System.out.println("Closing off ...");
			testContext.completeNow();
		}));
		logger.info("@AfterAll ends ...");
	}

	@BeforeEach
	void start(Vertx vertx, VertxTestContext testContext) throws NoSuchAlgorithmException {
		System.out.println("BeforeEach ...");
		secretKeyStr = SECRET_KEY;
		ivStr = IV;
		byte[] decodedKey = Base64.getDecoder().decode(SECRET_KEY.getBytes());
		secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, ALG);
		iv = Base64.getDecoder().decode(IV);

		stixString = STIX_TEST;
		stix = new JsonObject(stixString);
		System.out.println("secretKey: " + secretKeyStr);
		System.out.println("iv:" + ivStr);

		testVerticle = new MainVerticle(secretKeyStr, ivStr, depOptions);
		vertx.deployVerticle(testVerticle, depOptions, testContext.succeeding(id -> testContext.completeNow()));
		vertx.getOrCreateContext().put("main-verticle", testVerticle);

		System.out.println("BeforeEach ends ...");

		logger.info("BeforeEach ends ...");
	}

	@AfterEach
	public void finish(Vertx vertx, VertxTestContext testContext) {
		System.out.println("@AfterEach ...");
		vertx.close(testContext.succeeding(response -> {
			System.out.println("Closing off ...");
			testContext.completeNow();
		}));
		System.out.println("@AfterEach ends ...");
	}

	@Test
	public void shouldProcessPost(Vertx vertx, VertxTestContext testContext) throws Throwable {
		Checkpoint post = testContext.checkpoint();
		Checkpoint read = testContext.checkpoint();
		System.out.println("testing post ...");
		String filename = String.format("%s/encrypted-%s.json", depOptions.getConfig().getString("datastore"),
				stix.getString("id"));

		MessageConsumer<Buffer> consumer = vertx.eventBus().consumer("main.process.post");
		consumer.handler(message -> {
			post.flag();
			Buffer encryptedContent = vertx.fileSystem().readFileBlocking(filename);

			System.out.println("content: " + encryptedContent.toString());
			testContext.verify(() -> {
				read.flag();
				String completion = new String(message.body().getBytes());
				assertThat(completion, equalTo("true"));
				assertThat(encryptedContent, not(nullValue()));

				JsonObject stixJson = new JsonObject(encryptedContent.toString());
				assertThat(stixJson.getString("id"), not(nullValue()));
				assertThat(stixJson.getString("encrypted"), not(nullValue()));
				testContext.completeNow();
			});

		});
		StixProcessor.processPost(vertx, depOptions.getConfig().getString("datastore"), REQ_TEST, ALG, secretKey, iv);
		System.out.println("testing post exits ...");
	}

	/**
	 * <code>
	 * 
	 * secretKey: LP+MDHeBcFphSGJ+bDn9hPTs7ZEJhXjcXfl2yijiPo0=
	 * iv:5DXrwdzDLdwWWretX3SRgFq8+6FZioCg7k30rWybEkZc6TJ5Hwp1rV1VcqzJE7KIh3u/mbrhSf98h2HqmhzjVtenhinCe4toFAXkQOKQUsc9ISbktCBtk1LXMN6l9CrsEJll46dBsFNOPZxa7mUAkdiqNPCKNBXxSbL7k/rOb34=
	 * content: {
	 *	"id" : "artifact--6f437177-6e48-5cf8-9d9e-872a2bddd641",
	 *  "encrypted" : "7APPmg2YLM/8fLcaxnBWJo4oBZ8GfZYm9uPpBwF4hrztmbeJoydrlyFotmf28cGIWW9ZMxodSYefS04ryElwRED657TFyg4KNM4n9X31+jaWRlUqWw8brVbAnWdn79Ux+nIlShA71RLHdizXtrx/2Hy4Yt6fk89SkmMjXZVh4Oi4QgwEeFcLmeFxJ5HrogcAgHCoGZpyE9ptFNH7vlQ200tQZvu7j4i88Aj3UjAYsrmCeTCBZJbWb9lDx/wwJPsgjde13X5vwktNUarwe5eDkFc6jimTSIB2Kxny4fuicTZackPspR611CbIR0KRlZBSN9cPKqmAH9aoSwtmGiKQUM5tTnKD6/NAzWj465tA+zEK5TYrX4MbOg77Wm35XAZmoWOT4AbzDledNz7nlYL41A=="
	 * }
	 * 
	 * </code>
	 * 
	 * @param vertx
	 * @param testContext
	 * @throws Throwable
	 */

	@Test
	public void shouldProcessGet(Vertx vertx, VertxTestContext testContext) throws Throwable {
		logger.info("testing Get ...");
		MessageConsumer<Buffer> consumer = vertx.eventBus().consumer("main.process.get");
		logger.info("waiting for message main.process.get");
		consumer.handler(message -> {
			String payload = new String(message.body().getBytes());
			System.out.println("main.process.get: Got message: " + payload);
			testContext.verify(() -> {
				assertThat(payload, not(nullValue()));
				testContext.completeNow();
			});

		});

		String filename = String.format("%s/encrypted-%s.json", depOptions.getConfig().getString("datastore"),
				stix.getString("id"));
		vertx.fileSystem().writeFileBlocking(filename, Buffer.buffer(ENCRYPTED_STIX));
		secretKey = StixCipher.makeSecretKey(SECRET_KEY);
		iv = Base64.getDecoder().decode(IV.getBytes());
		System.out.println("secretKey:" + SECRET_KEY);
		System.out.println("iv:" + IV);
		System.out.println("content:" + ENCRYPTED_STIX);

		StixProcessor.processGet(vertx, depOptions.getConfig().getString("datastore"), REQ_TEST, ALG, secretKey, iv);
		logger.info("testing Get ends ...");
	}

}
