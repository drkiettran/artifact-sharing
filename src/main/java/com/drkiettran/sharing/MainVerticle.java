package com.drkiettran.sharing;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.concurrent.ExecutionException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import io.vertx.config.ConfigRetriever;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.ClientAuth;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;

/**
 * MainVerticle class: -Dvertx.options.blockedThreadCheckInterval=12345
 * 
 * @author student
 *
 * @implNote datastore directory must exist.
 * 
 */
public class MainVerticle extends AbstractVerticle {
	public final Integer PORTNO = 9090;
	private String secretKeyStr;
	private String ivStr;

	private MainVerticleConfig config;

	private Keys keys;

	final static Logger logger = LoggerFactory.getLogger(MainVerticle.class);
	final static String ALG = "AES/GCM/NoPadding";

	public String getIvStr() {
		return ivStr;
	}

	private String getSecretKey() {
		if (secretKeyStr == null) {
			secretKeyStr = System.getenv("SECRET_KEY");
		}
		return secretKeyStr;
	}

	private String getIV() {
		if (ivStr == null) {
			ivStr = System.getenv("IV");
		}
		return ivStr;
	}

	public MainVerticle(String secretKeyStr, String ivStr) {
		logger.info("Constructing ..." + secretKeyStr + " -- " + ivStr);
		this.secretKeyStr = secretKeyStr;
		this.ivStr = ivStr;
	}

	public MainVerticle() {

	}

	/**
	 * <code>
	 * From Log output the first time: 
	 *   INFO: secret key: XnPxvsty7Jg5G8ifKIWWST3mnlhY2LwJtqeHbOMTcmw= alg: AES 
	 * From Python sample:
	 * 	WHnM/EmaPTHNW7h+9bhqgZYTlz4uskZcoUba2rPxIms=
	 * </code>
	 * 
	 * @throws InterruptedException
	 * 
	 */
	@Override
	public void start(Promise<Void> startPromise)
			throws URISyntaxException, NoSuchAlgorithmException, InvalidKeySpecException, InterruptedException {
		logger.info("*** Sharing Service starts ...");
		vertx.getOrCreateContext().put("main-verticle", this);
		secretKeyStr = this.getSecretKey();
		ivStr = this.getIV();
		logger.info("SECRET_KEY:" + secretKeyStr);

		ConfigRetriever retriever = ConfigRetriever.create(vertx);

		retriever.getConfig(ar -> {
			if (ar.failed()) {
				logger.info("failed to retrieve configuration ...");
				return;
			} else {
				config = new MainVerticleConfig(ar.result());
				try {
					keys = new Keys(vertx, config, this.getSecretKey(), this.getIvStr());
					startHttpServer(startPromise);
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				}
				logger.info(config);
			}
		});

	}

	private void startHttpServer(Promise<Void> startPromise) {
		logger.info("Starting HTTP Server ...");
		HttpServerOptions serverOptions = new HttpServerOptions();
		if (config.isTls()) {
			serverOptions.setSsl(true);
			serverOptions.setKeyStoreOptions(keys.getJksKeystoreOptions());
			logger.info("jksKeystoreOptions ... " + keys.getJksKeystoreOptions());
			logger.info("TLS is required ...");
			if (config.isTlsMutual()) {
				serverOptions.setTrustStoreOptions(keys.getJksTruststoreOptions());
				serverOptions.setClientAuth(ClientAuth.REQUIRED);
				logger.info("Mutual TLS is required");
				logger.info("jksTruststoreOptions ... " + keys.getJksTruststoreOptions());
			}
		}

		logger.info("Starting up server ...");
		vertx.createHttpServer(serverOptions).requestHandler(req -> {
			logger.info("*** request handler ..." + req.uri());
			req.body(bh -> {
				try {
					logger.info("Processing request ...");
					processRequest(req, bh);
				} catch (URISyntaxException e) {
					logger.error("Invalid URI: ", e);
				} catch (InvalidKeyException e) {
					e.printStackTrace();
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				} catch (NoSuchPaddingException e) {
					e.printStackTrace();
				} catch (InvalidAlgorithmParameterException e) {
					e.printStackTrace();
				} catch (IllegalBlockSizeException e) {
					e.printStackTrace();
				} catch (BadPaddingException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			});
		}).listen(config.getPortNo(), config.getHostName(), http -> {
			if (http.succeeded()) {
				startPromise.complete();
				logger.info("*** HTTP server started on port: " + config.getPortNo());
			} else {
				startPromise.fail(http.cause());
			}
		});
	}

	private void processRequest(HttpServerRequest req, AsyncResult<Buffer> bh) throws URISyntaxException,
			InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException,
			IllegalBlockSizeException, BadPaddingException, InterruptedException, ExecutionException {
		Response resp = new Response();
		URI uri = new URI(req.absoluteURI());

		if (!"/artifact".equals(uri.getPath())) {
			resp.setStatusCode(400);
			resp.setReason("INVALID REQUEST!");
			req.response().setStatusCode(resp.getStatusCode()).putHeader("content-type", "application/json")
					.end(resp.toString());
			return;
		}

		if (!req.headers().get("Content-type").equals("application/json")) {
			resp.setStatusCode(400);
			resp.setReason("Invalid content-type");
			req.response().setStatusCode(resp.getStatusCode()).putHeader("content-type", "application/json")
					.end(resp.toString());
			return;
		}

		if ("POST".equals(req.method().toString())) {
			MessageConsumer<Buffer> consumer = vertx.eventBus().consumer("main.process.post");
			consumer.handler(message -> {
				String completion = new String(message.body().getBytes());
				if ("true".equals(completion)) {
					resp.setStatusCode(200);
					resp.setReason("OK");
				} else {
					resp.setStatusCode(500);
					resp.setReason("Unable to process STIX artifact");
				}

				req.response().setStatusCode(resp.getStatusCode()).putHeader("content-type", "application/json")
						.end(resp.toString());
			});

			processPost(bh);

			return;
		} else if ("GET".equals(req.method().toString())) {
			MessageConsumer<Buffer> consumer = vertx.eventBus().consumer("main.process.get");
			consumer.handler(message -> {
				String payload = new String(message.body().getBytes());
				logger.debug("main.process.get: Got message: " + payload);
				req.response().setStatusCode(resp.getStatusCode()).putHeader("content-type", "application/json")
						.end(payload);
			});
			processGet(bh);
			return;
		} else {
			resp.setStatusCode(405);
			resp.setReason("Method not found!");
			req.response().setStatusCode(resp.getStatusCode()).putHeader("content-type", "application/json")
					.end(resp.toString());
			return;
		}
	}

	/**
	 * Processing a GET request.
	 * 
	 * @param bh
	 * @param payload
	 * @throws BadPaddingException
	 * @throws IllegalBlockSizeException
	 * @throws InvalidAlgorithmParameterException
	 * @throws NoSuchPaddingException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	private void processGet(AsyncResult<Buffer> bh) throws InvalidKeyException, NoSuchAlgorithmException,
			NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException,
			InterruptedException, ExecutionException {
		logger.info("Processing GET");
		logger.debug("received (GET): " + bh.result().toString());
		StixProcessor.processGet(vertx, config.getDatastore(), bh.result().toString(), ALG, keys.getSecretKey(),
				keys.getIv());
	}

	/**
	 * Processing a POST request ...
	 * 
	 * @param bh
	 * @param resp
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws InvalidAlgorithmParameterException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	private Boolean processPost(AsyncResult<Buffer> bh) throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
		logger.info("Processing POST");
		logger.debug("Received: " + bh.result().toString());
		return StixProcessor.processPost(vertx, config.getDatastore(), bh.result().toString(), ALG, keys.getSecretKey(),
				keys.getIv());

	}

	public Keys getKeys() {
		return keys;
	}
}
