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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.ClientAuth;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;

/**
 * MainVerticle class: -Dvertx.options.blockedThreadCheckInterval=12345
 * 
 * @author student
 *
 * @implNote datastore directory must exist.
 * 
 */
public class MainVerticle extends AbstractVerticle {
	private final Logger logger = LoggerFactory.getLogger(MainVerticle.class);
	public final static String AES_GCM_NOPADDING = "AES/GCM/NoPadding";
	public static final String AES = "AES";

	public final Integer PORTNO = 9090;
	private String secretKeyStr;
	private String ivStr;

	private MainVerticleConfig config;

	private Keys keys;

	private DeploymentOptions depOptions;

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

	public MainVerticle(String secretKeyStr, String ivStr, DeploymentOptions depOptions) {
		logger.info("Constructing ..." + secretKeyStr + " -- " + ivStr);
		this.secretKeyStr = secretKeyStr;
		this.ivStr = ivStr;
		this.depOptions = depOptions;
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

		ConfigRetriever retriever;

		if (depOptions == null) {
			logger.info("loading default config ...");
			retriever = ConfigRetriever.create(vertx);
		} else {
			logger.info("loading from deployment options ...");
			ConfigStoreOptions cso = new ConfigStoreOptions().setConfig(depOptions.getConfig()).setType("json");
			ConfigRetrieverOptions cro = new ConfigRetrieverOptions().addStore(cso);
			retriever = ConfigRetriever.create(vertx, cro);
		}

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
				logger.info("--> {}", config);
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
					logger.error("Invalid URI: " + e);
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
		logger.info("headers: {}", req.headers());
		Response resp = new Response();
		URI uri = new URI(req.absoluteURI());

		if (!"/artifact".equals(uri.getPath())) {
			resp.setStatusCode(400);
			resp.setReason("INVALID REQUEST!");
			req.response().setStatusCode(resp.getStatusCode()).putHeader("content-type", "application/json")
					.end(resp.toString());
			return;
		}

		if (!"application/json".equals(req.headers().get("Content-type"))) {
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
				if (!req.response().ended()) {
					req.response().setStatusCode(resp.getStatusCode()).putHeader("content-type", "application/json")
							.end(resp.toString());
				}
			});

			processPost(req, resp, bh);

			return;
		} else if ("GET".equals(req.method().toString())) {
			MessageConsumer<Buffer> consumer = vertx.eventBus().consumer("main.process.get");
			consumer.handler(message -> {
				String payload = new String(message.body().getBytes());
				JsonObject respBody = new JsonObject(payload);
				if (respBody.getString("reason") == null) {
					resp.setStatusCode(200);
					resp.setReason("OK");
				} else {
					resp.setStatusCode(500);
					resp.setReason("*** error ***");
				}
				if (!req.response().ended()) {
					logger.info("===> Sending response ***");
					logger.info("main.process.get: Got message: " + payload);
					req.response().setStatusCode(resp.getStatusCode()).putHeader("content-type", "application/json")
							.end(payload);
				}
			});
			processGet(req, resp, bh);
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
	 * @param resp
	 * @param req
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
	private void processGet(HttpServerRequest req, Response resp, AsyncResult<Buffer> bh) throws InvalidKeyException,
			NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException,
			IllegalBlockSizeException, BadPaddingException, InterruptedException, ExecutionException {
		logger.info("Processing GET");
		String reqBody = bh.result().toString();
		if (reqBody != null && !reqBody.isEmpty()) {
			ArtifactProcessor.processGet(vertx, config.getDatastore(), bh.result().toString(), AES_GCM_NOPADDING,
					keys.getSecretKey(), keys.getIv());
		} else {
			Response response = new Response();
			response.setReason("** No artifact id **");
			response.setStatusCode(500);
			Buffer buffer = Buffer.buffer(response.toJsonObject().encodePrettily());
			vertx.eventBus().publish("main.process.get", buffer);
		}
	}

	/**
	 * Processing a POST request ...
	 * 
	 * @param resp
	 * @param req
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
	private void processPost(HttpServerRequest req, Response resp, AsyncResult<Buffer> bh)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
			InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
		logger.info("Processing POST");
		String reqBody = bh.result().toString();

		logger.info("Received: " + bh.result().toString());
		if (reqBody != null && !reqBody.isEmpty()) {
			ArtifactProcessor.processPost(vertx, config.getDatastore(), bh.result().toString(), AES_GCM_NOPADDING,
					keys.getSecretKey(), keys.getIv());
		} else {
			Response response = new Response();
			response.setReason("** No artifact **");
			response.setStatusCode(500);
			Buffer buffer = Buffer.buffer(response.toJsonObject().encodePrettily());
			vertx.eventBus().publish("main.process.post", buffer);
		}

	}

	public Keys getKeys() {
		return keys;
	}
}
