package com.drkiettran.sharing;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.concurrent.ExecutionException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

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
import io.vertx.core.net.JksOptions;

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
	private int portNo;
	private Boolean tls;
	private Boolean tlsMutual;
	private String keystore;
	private String keystorePassword;
	private String truststore;
	private String truststorePassword;
	private String hostName;
	private String datastore;
	private SecretKey secretKey;
	private byte[] iv;
	private String secretKeyStr;
	private String ivStr;
	private String serverPubKeyFilename;
	private String serverPrivKeyFilename;
	private String clientPubKeyFilename;
	private String clientPrivKeyFilename;
	private PublicKey serverPubKey;
	private PublicKey clientPubKey;
	private PrivateKey clientPrivKey;
	private PrivateKey serverPrivKey;
	final static Logger logger = LoggerFactory.getLogger(MainVerticle.class);
	final static String ALG = "AES/GCM/NoPadding";

	public String getIvStr() {
		return ivStr;
	}

	public String getClientPubKeyFilename() {
		return clientPubKeyFilename;
	}

	public String getClientPrivKeyFilename() {
		return clientPrivKeyFilename;
	}

	public PublicKey getServerPubKey() {
		return serverPubKey;
	}

	public PublicKey getClientPubKey() {
		return clientPubKey;
	}

	public PrivateKey getClientPrivKey() {
		return clientPrivKey;
	}

	public PrivateKey getServerPrivKey() {
		return serverPrivKey;
	}

	private String getSecretKey() {
		return System.getenv("SECRET_KEY");
	}

	private String getIV() {
		return System.getenv("IV");
	}

	/**
	 * <code>
	 * From Log output the first time: 
	 *   INFO: secret key: XnPxvsty7Jg5G8ifKIWWST3mnlhY2LwJtqeHbOMTcmw= alg: AES 
	 * From Python sample:
	 * 	WHnM/EmaPTHNW7h+9bhqgZYTlz4uskZcoUba2rPxIms=
	 * </code>
	 * 
	 */
	@Override
	public void start(Promise<Void> startPromise)
			throws URISyntaxException, NoSuchAlgorithmException, InvalidKeySpecException {
		logger.info("*** Sharing Service starts ...");
		vertx.getOrCreateContext().put("main-verticle", this);
		secretKeyStr = this.getSecretKey();
		ivStr = this.getIV();
		logger.info("SECRET_KEY:" + secretKeyStr);

		getWebConfig();
		logger.info(this);

		if (secretKeyStr == null || secretKeyStr.isEmpty()) {
			logger.info("Creating new Secret Key & IV!");
			secretKey = StixCipher.getAESKey(256); // getSecretKey();
			iv = StixCipher.getRandomNonce(128);
			ivStr = Base64.getEncoder().encodeToString(iv);
			secretKeyStr = Base64.getEncoder().encodeToString(secretKey.getEncoded());
		} else {
			logger.info("Reusing Secret Key & IV!");
			byte[] decodedKey = Base64.getDecoder().decode(secretKeyStr.getBytes());
			secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
			iv = Base64.getDecoder().decode(ivStr.getBytes());
			secretKeyStr = Base64.getEncoder().encodeToString(secretKey.getEncoded());
			ivStr = Base64.getEncoder().encodeToString(iv);
		}

		String msg = String.format("\nsecret key: '%s' \nkey length: '%d' \nalgorithm: '%s', \niv: '%s'", secretKeyStr,
				secretKey.getEncoded().length, secretKey.getAlgorithm(), ivStr);
		logger.info(msg);

		getPubKey("client", clientPubKeyFilename);
		getPubKey("server", serverPubKeyFilename);
		getPrivKey("client", clientPrivKeyFilename);
		getPrivKey("server", serverPrivKeyFilename);
		HttpServerOptions serverOptions = new HttpServerOptions();
		if (tls) {
			serverOptions.setSsl(true);
			serverOptions.setKeyStoreOptions(getJksKeystoreOptions());
			if (tlsMutual) {
				serverOptions.setTrustStoreOptions(getJksTruststoreOptions());
				serverOptions.setClientAuth(ClientAuth.REQUIRED);
			}
		}

		vertx.createHttpServer(serverOptions).requestHandler(req -> {
			logger.info("*** request handler ..." + req.uri());
			req.body(bh -> {

				try {
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
		}).listen(portNo, hostName, http -> {
			if (http.succeeded()) {
				startPromise.complete();
				logger.info("*** HTTP server started on port: " + portNo);
			} else {
				startPromise.fail(http.cause());
			}
		});
	}

	private void getPrivKey(String keyType, String filename) {
		logger.info(String.format("getPrivKey: type: %s\nfilename: %s\n", keyType, filename));
		MessageConsumer<Buffer> consumer = vertx.eventBus().consumer(filename);
		consumer.handler(message -> {
			String key = new String(message.body().getBytes(), Charset.defaultCharset());
			logger.info("PEM:" + message.body().toString());
			key = key.substring(key.indexOf("-----BEGIN PRIVATE KEY-----"));
			String privateKeyPEM = key.replace("-----BEGIN PRIVATE KEY-----", "").replaceAll(System.lineSeparator(), "")
					.replace("-----END PRIVATE KEY-----", "");
			logger.info("after trim: " + key);
			byte[] encoded = Base64.getDecoder().decode(privateKeyPEM);

			KeyFactory keyFactory;
			try {
				keyFactory = KeyFactory.getInstance("RSA");
				PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
				if ("client".equals(keyType)) {
					clientPrivKey = keyFactory.generatePrivate(keySpec);
					this.showKeyInfo("client-private-key", clientPrivKey);
				} else if ("server".equals(keyType)) {
					serverPrivKey = keyFactory.generatePrivate(keySpec);
					this.showKeyInfo("server-private-key", serverPrivKey);
				} else {
					logger.error("Invalid key type");
				}
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidKeySpecException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		});

		vertx.fileSystem().readFile(filename, result -> {
			if (result.succeeded()) {
				logger.info("input: " + Base64.getEncoder().encodeToString(result.result().getBytes()));
				vertx.eventBus().publish(filename, result.result());
				logger.info("Placed content of " + filename + " on bus");
			} else {
				logger.error("Failed to read PrivateKey");
			}
		});

	}

	private void getPubKey(String keyType, String filename) {
		logger.info(String.format("getPubKey: type: %s\nfilename: %s\n", keyType, filename));
		MessageConsumer<Buffer> consumer = vertx.eventBus().consumer(filename);
		consumer.handler(message -> {
			logger.info("DER:" + message.body().toString());
			X509EncodedKeySpec spec = new X509EncodedKeySpec(message.body().getBytes());
			KeyFactory kf;
			try {
				kf = KeyFactory.getInstance("RSA");
				if ("client".equals(keyType)) {
					clientPubKey = kf.generatePublic(spec);
					this.showKeyInfo("client-public-key", clientPubKey);
				} else if ("server".equals(keyType)) {
					serverPubKey = kf.generatePublic(spec);
					this.showKeyInfo("server-public-key", serverPubKey);
				} else {
					logger.error("Invalid key type");
				}
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidKeySpecException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		});

		vertx.fileSystem().readFile(filename, result -> {
			if (result.succeeded()) {
				logger.info("input: " + Base64.getEncoder().encodeToString(result.result().getBytes()));
				vertx.eventBus().publish(filename, result.result());
				logger.info("Placed content of " + filename + " on bus");
			} else {
				logger.error("Failed to read PublicKey");
			}
		});
 
	}

	private void showKeyInfo(String name, Key key) {

		logger.info(String.format("name: %s\nalg: %s\nEncoded: %s\nformat:%s\n", name, key.getAlgorithm(),
				Base64.getEncoder().encodeToString(key.getEncoded()), key.getFormat()));
	}

	private JksOptions getJksKeystoreOptions() {
		JksOptions options = new JksOptions();
		options.setPath(keystore);
		options.setPassword(keystorePassword);
		return options;
	}

	private JksOptions getJksTruststoreOptions() {
		JksOptions options = new JksOptions();
		options.setPath(truststore);
		options.setPassword(truststorePassword);
		return options;
	}

	private void getWebConfig() {
		this.hostName = config().getString("http.hostname");
		this.portNo = config().getInteger("http.port");
		this.tls = config().getBoolean("tls");
		this.tlsMutual = config().getBoolean("tls_mutual");
		this.keystore = config().getString("keystore");
		this.keystorePassword = config().getString("keystore_password");
		this.truststore = config().getString("truststore");
		this.truststorePassword = config().getString("truststore_password");
		this.datastore = config().getString("datastore");
		this.serverPubKeyFilename = config().getJsonObject("certs").getString("server-public-key");
		this.serverPrivKeyFilename = config().getJsonObject("certs").getString("server-private-key");
		this.clientPubKeyFilename = config().getJsonObject("certs").getString("client-public-key");
		this.clientPrivKeyFilename = config().getJsonObject("certs").getString("client-private-key");
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
			processPost(bh, resp);
			req.response().setStatusCode(resp.getStatusCode()).putHeader("content-type", "application/json")
					.end(resp.toString());
			return;
		} else if ("GET".equals(req.method().toString())) {
			MessageConsumer<Buffer> consumer = vertx.eventBus().consumer("main.process.get");
			consumer.handler(message -> {
				String payload = new String(message.body().getBytes());
				logger.info("main.process.get: Got message: " + payload);
				req.response().setStatusCode(resp.getStatusCode()).putHeader("content-type", "application/json")
						.end(payload);
			});
			processGet(bh);
			return;
		} else

		{
			resp.setStatusCode(405);
			resp.setReason("Method not found!");
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
	private Payload processGet(AsyncResult<Buffer> bh) throws InvalidKeyException, NoSuchAlgorithmException,
			NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException,
			InterruptedException, ExecutionException {
		logger.info("Processing GET");
		logger.info("received (GET): " + bh.result().toString());
		return StixProcessor.processGet(vertx, this.datastore, bh.result().toString(), ALG, secretKey, iv);
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
	private void processPost(AsyncResult<Buffer> bh, Response resp)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
			InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
		logger.info("Processing POST");
		logger.info("Received: " + bh.result().toString());

		if (StixProcessor.processPost(vertx, this.datastore, bh.result().toString(), ALG, secretKey, iv)) {
			resp.setStatusCode(200);
			resp.setReason("OK");
		} else {
			resp.setStatusCode(500);
			resp.setReason("Unable to process STIX artifact");
		}
		logger.info("Processing POST successfully!");
	}

	public String toString() {
		StringBuilder sb = new StringBuilder("Main Verticle Cfg:");
		sb.append("\n\thost name: ").append(hostName);
		sb.append("\n\tport no: ").append(portNo);
		sb.append("\n\ttls: ").append(tls);
		sb.append("\n\tmutual tls: ").append(tlsMutual);
		sb.append("\n\tkeystore: ").append(keystore);
		sb.append("\n\tkeystore password: ").append(keystorePassword);
		sb.append("\n\ttruststore: ").append(truststore);
		sb.append("\n\ttruststore password: ").append(truststorePassword);
		sb.append("\n\tdatastore: ").append(datastore);
		return sb.toString();
	}
}
