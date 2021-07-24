package com.drkiettran.sharing;

import java.nio.charset.Charset;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.JksOptions;

public class Keys {
	private static final Logger logger = LoggerFactory.getLogger(Keys.class);
	private String keystore;
	private String keystorePassword;
	private String truststore;
	private String truststorePassword;
	private SecretKey secretKey;
	private byte[] iv;
	private String secretKeyStr;
	private String ivStr;
	private PublicKey serverPubKey;
	private PublicKey clientPubKey;
	private PrivateKey clientPrivKey;
	private PrivateKey serverPrivKey;

	private Vertx vertx;

	public String getKeystore() {
		return keystore;
	}

	public String getKeystorePassword() {
		return keystorePassword;
	}

	public String getTruststore() {
		return truststore;
	}

	public String getTruststorePassword() {
		return truststorePassword;
	}

	public String getSecretKeyStr() {
		return secretKeyStr;
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

	public Keys(Vertx vertx, MainVerticleConfig config, String secretKeyStr, String ivStr)
			throws NoSuchAlgorithmException {
		logger.info("Constructing keys ...");
		this.vertx = vertx;
		this.secretKeyStr = secretKeyStr;
		this.ivStr = ivStr;
		getSecretKeyAndIv();

		keystore = config.getKeystore();
		keystorePassword = config.getKeystorePassword();
		truststore = config.getTruststore();
		truststorePassword = config.getTruststorePassword();

		getPubKey("server", config.getServerPublicKey());
		getPrivKey("client", config.getClientPrivateKey());
		getPrivKey("server", config.getServerPrivateKey());
		getPubKey("client", config.getClientPublicKey());
		logger.info("Constructing keys ends ...");
	}

	public JksOptions getJksKeystoreOptions() {
		JksOptions options = new JksOptions();
		options.setPath(keystore);
		options.setPassword(keystorePassword);
		return options;
	}

	public JksOptions getJksTruststoreOptions() {
		JksOptions options = new JksOptions();
		options.setPath(truststore);
		options.setPassword(truststorePassword);
		return options;
	}

	public SecretKey getSecretKey() {
		return secretKey;
	}

	public String getIvStr() {
		return ivStr;
	}

	public byte[] getIv() {
		return iv;
	}

	private void getPrivKey(String keyType, String filename) {
		logger.info(String.format("getPrivKey: type: %s\nfilename: %s\n", keyType, filename));

		Buffer buffer = vertx.fileSystem().readFileBlocking(filename);
		String key = new String(buffer.getBytes(), Charset.defaultCharset());
		logger.debug("PEM:" + buffer.toString());
		key = key.substring(key.indexOf("-----BEGIN PRIVATE KEY-----"));
		String privateKeyPEM = key.replace("-----BEGIN PRIVATE KEY-----", "").replaceAll(System.lineSeparator(), "")
				.replace("-----END PRIVATE KEY-----", "");
		logger.debug("after trim: " + key);
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
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		}

	}

	private void getPubKey(String keyType, String filename) {
		logger.debug(String.format("getPubKey: type: %s\nfilename: %s\n", keyType, filename));
		Buffer buffer = vertx.fileSystem().readFileBlocking(filename);
		logger.debug("DER:" + buffer.toString());
		X509EncodedKeySpec spec = new X509EncodedKeySpec(buffer.getBytes());
		KeyFactory kf;
		try {
			kf = KeyFactory.getInstance("RSA");
			if ("client".equals(keyType)) {
				clientPubKey = kf.generatePublic(spec);
				showKeyInfo("client-public-key", clientPubKey);
			} else if ("server".equals(keyType)) {
				serverPubKey = kf.generatePublic(spec);
				showKeyInfo("server-public-key", serverPubKey);
			} else {
				logger.error("Invalid key type");
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		}

	}

	private void getSecretKeyAndIv() throws NoSuchAlgorithmException {
		if (secretKeyStr == null || secretKeyStr.isEmpty()) {
			logger.info("Creating new Secret Key & IV!");
			secretKey = StixCipher.getAESKey(256); // getSecretKey();
			iv = StixCipher.getRandomNonce(128);
			ivStr = Base64.getUrlEncoder().encodeToString(iv);
			secretKeyStr = Base64.getUrlEncoder().encodeToString(secretKey.getEncoded());
		} else {
			logger.info("Reusing Secret Key & IV!");
			byte[] decodedKey = Base64.getUrlDecoder().decode(secretKeyStr.getBytes());
			secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
			iv = Base64.getUrlDecoder().decode(ivStr.getBytes());
			secretKeyStr = Base64.getUrlEncoder().encodeToString(secretKey.getEncoded());
			ivStr = Base64.getUrlEncoder().encodeToString(iv);
		}

		String msg = String.format("\nsecret key: '%s' \nkey length: '%d' \nalgorithm: '%s', \niv: '%s'", secretKeyStr,
				secretKey.getEncoded().length, secretKey.getAlgorithm(), ivStr);
		logger.info(msg);
	}

	private void showKeyInfo(String name, Key key) {
		logger.info(String.format("name: %s\nalg: %s\nEncoded: %s\nformat:%s\n", name, key.getAlgorithm(),
				Base64.getUrlEncoder().encodeToString(key.getEncoded()), key.getFormat()));
	}

}
