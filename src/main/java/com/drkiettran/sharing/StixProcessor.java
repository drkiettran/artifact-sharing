package com.drkiettran.sharing;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.concurrent.ExecutionException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;

public class StixProcessor {
	final static Logger logger = LoggerFactory.getLogger(StixProcessor.class);

	public static Boolean processPost(Vertx vertx, String dir, String stixStr, String alg, SecretKey secretKey,
			byte[] iv) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {

		JsonObject stix = new JsonObject(stixStr);
		String cipherText = StixCipher.encrypt(alg, stixStr, secretKey, iv);
		String filename = String.format("%s/encrypted-%s.json", dir, stix.getString("id"));

		JsonObject encryptedStix = new JsonObject();
		encryptedStix.put("id", stix.getString("id"));
		encryptedStix.put("encrypted", cipherText);
		Buffer buffer = Buffer.buffer(encryptedStix.encodePrettily());

		logger.info(String.format("Writing file %s", filename));
		vertx.fileSystem().writeFile(filename, buffer, new Handler<AsyncResult<Void>>() {
			@Override
			public void handle(AsyncResult<Void> result) {
				String completion = "false";
				if (result.succeeded()) {
					logger.info("Write to file successfully!");
					completion = "true";
				} else {
					logger.info("Failed to write file!");
				}
				Buffer buffer = Buffer.buffer(completion);
				vertx.eventBus().publish("main.process.post", buffer);
				logger.info("Completed! ...");
			}
		});

		logger.info("stix id:" + stix.getString("id"));
		logger.info("encrypted stix artifact:" + cipherText);
		String original = StixCipher.decrypt(alg, cipherText, secretKey, iv);
		logger.info("****: " + original);
		if (original.compareTo(stixStr) == 0) {
			logger.info("STIX object encrypted correctedly!");
		} else {
			logger.info("STIX object encrypted incorrectedly!");
		}
		return true;
	}

	/**
	 * Process GET request for a STIX object.
	 * 
	 * <code>
	 * 
	 * 1. Receive an request for a given artifac id.
	 * 2. Retrieve the encrypted artifact from the datastore.
	 * 3. Decrypt the encrypted STIX artifact. This is the cleared_stix_artifact to be sent to the partner.
	 * 4. cleared_stix_artifact: the 'decrypted' version from the stored_encrypted_stix_artifact.
	 * 5. Generate the one-time encryption key.
	 * 6. one_time_encrypted_stix_artifact: the randomly generated one-time key to be used for encryption of the cleared_stix_artifact. 
	 *      This one-time encrypted data to be shared with an external partner.
	 * 7. shared_encrypted_stix_artifact: The encrypted stix object to be shared with an external partner. 
	 *    	This will be sent to the partner using Mutual TLS connection
	 * {
	 *	  "id": "artifact--6f437177-6e48-5cf8-9d9e-872a2bddd641",
	 *    "iv": "mfSklhfwaCKoLMJe",
	 *    "encrypted": "",
	 *    "encryped_key": "",
	 *    "AIS_signature": ""
	 * }
	 * 
	 * </code>
	 * 
	 * @throws ExecutionException
	 * @throws InterruptedException
	 * 
	 */
	public static void processGet(Vertx vertx, String dir, String reqStr, String alg, SecretKey secretKey, byte[] iv)
			throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, InterruptedException,
			ExecutionException {
		logger.info("Processing GET");
		JsonObject reqObj = new JsonObject(reqStr);
		String stixId = reqObj.getString("id");
		String filename = String.format("%s/encrypted-%s.json", dir, stixId);

		Payload payload = new Payload();
		logger.info("Comsuning data ...");
		MessageConsumer<Buffer> consumer = vertx.eventBus().consumer("process.get");
		consumer.handler(message -> {
			logger.info("process.get receives: " + message.body().toString());
			JsonObject encryptedJson = new JsonObject(message.body().toString());
			String encrypted = encryptedJson.getString("encrypted");
			try {
				/**
				 * - Create one-time key - Encrypt text with one-time key - Encrypt one-time key
				 * with client public key - Sign text with server private key
				 */
				String plain = StixCipher.decrypt(alg, encrypted, secretKey, iv);
				logger.info("plain:" + plain);
				SecretKey onetimeKey = StixCipher.getAESKey(256);
				logger.info(message);
				byte[] onetimeIv = StixCipher.getRandomNonce(16);

				MainVerticle main = vertx.getOrCreateContext().get("main-verticle");

				String msgCipherText = StixCipher.encrypt(alg, plain, onetimeKey, onetimeIv);
				logger.info("encoded one-time: " + Base64.getEncoder().encodeToString(onetimeKey.getEncoded()));
				String keyCipherText = StixCipher.encrypt(main.getKeys().getClientPubKey(), onetimeKey.getEncoded());
				String signature = StixCipher.sign(main.getKeys().getServerPrivKey(), plain.getBytes());

				payload.setId(stixId);
				payload.setIv(Base64.getEncoder().encodeToString(onetimeIv));
				payload.setEncrypted_key(keyCipherText);
				payload.setEncrypted(msgCipherText);
				payload.setSignature(signature);
				payload.setLength(plain.length());

				/**
				 * Reverse - Verify signature with server public key - Decrypt one-time key with
				 * client private key - Decrypt text with one-time key
				 */
				Boolean verified = StixCipher.verifySignature(main.getKeys().getServerPubKey(), plain.getBytes(),
						Base64.getDecoder().decode(signature));
				logger.info("verified:" + verified);
				logger.info("Keylen:" + Base64.getDecoder().decode(keyCipherText).length);
				byte[] clearedKey = StixCipher.decrypt(main.getKeys().getClientPrivKey(),
						Base64.getDecoder().decode(keyCipherText));
				String clearedText = StixCipher.decrypt(alg, msgCipherText, onetimeKey, onetimeIv);
				logger.info("clearedkey: " + clearedKey);
				logger.info("cleared text:" + clearedText);

				logger.info("done reverse...");

				Buffer buffer = Buffer.buffer();
				buffer.appendBytes(payload.toJsonObject().encodePrettily().getBytes());
				vertx.eventBus().publish("main.process.get", buffer);

			} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException
					| BadPaddingException | InvalidAlgorithmParameterException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});

		vertx.fileSystem().readFile(filename, result -> {
			logger.info("reading encrypted stix files ...");
			if (result.succeeded()) {
				logger.info("input: " + result.result());
				vertx.eventBus().publish("process.get", result.result());
				logger.info("process.get published!");
				try {
					Thread.sleep(50L);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				logger.info("Error reading STIX artifact" + result.cause());
			}
			logger.info(String.format("Successfully processed GET"));
		});

		logger.info(String.format("processed GET"));
		return;
	}
}
