package com.drkiettran.sharing;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class ArtifactCipherTest {
	private static Logger logger = LoggerFactory.getLogger(ArtifactCipherTest.class);

	public static final String ALG_AES = TestingUtil.ALG_AES;
	private Vertx vertx = Vertx.vertx();
	private MainVerticleConfig config;
	private Keys keys;

	@BeforeEach
	@DisplayName("Setting up ...")
	public void setUp() throws NoSuchAlgorithmException {
		TestingUtil.prepare2Run(vertx.fileSystem());
		config = new MainVerticleConfig(new JsonObject(TestingUtil.CONFIG_JSON));
		keys = new Keys(Vertx.vertx(), config, TestingUtil.SECRET_KEY, TestingUtil.IV);
	}

	@BeforeEach
	@DisplayName("Tear down ...")
	public void cleanUp() {
		TestingUtil.prepare2Run(vertx.fileSystem());
	}

	@Test
	public void shouldGetSecretKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
		SecretKey secretKey = ArtifactCipher.getSecretKey(ALG_AES);
		assertThat(secretKey, not(nullValue()));
		assertThat(secretKey.getAlgorithm(), equalTo(ALG_AES));
		assertThat(secretKey.getEncoded().length, is(32));
		assertThat(secretKey.getFormat(), equalTo("RAW"));
		logger.info("encoded:" + Base64.getUrlEncoder().encodeToString(secretKey.getEncoded()));
		logger.info("alg: " + secretKey.getAlgorithm());
	}

	@Test
	public void shouldGenerateSalt() {
		String salt = ArtifactCipher.generateSalt();
		assertThat(salt, not(nullValue()));
		assertThat(salt.length(), equalTo(16));
	}

	@Test
	public void shouldEncryptWithFernet() throws NoSuchAlgorithmException, InvalidKeySpecException {
		SecretKey secretKey = ArtifactCipher.getSecretKey(ALG_AES);
		String cipher = ArtifactCipher.fernetEncrypt(TestingUtil.TEST_ARTIFACT, secretKey);
		String clear = ArtifactCipher.fernetDecrypt(cipher, secretKey);
		assertThat(cipher, not(nullValue()));
		assertThat(clear, equalTo(TestingUtil.TEST_ARTIFACT));
		logger.info("cipher: " + cipher);
	}

	@Test
	public void shouldEncryptFernetWithGivenKey() {
		byte[] decodedKey = Base64.getUrlDecoder().decode(TestingUtil.SECRET_KEY.getBytes());
		SecretKey secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, ALG_AES);
		String cipher = ArtifactCipher.fernetEncrypt(TestingUtil.TEST_ARTIFACT, secretKey);
		String clear = ArtifactCipher.fernetDecrypt(cipher, secretKey);
		assertThat(cipher, not(nullValue()));
		assertThat(clear, equalTo(TestingUtil.TEST_ARTIFACT));
		logger.info("cipher: " + cipher);

	}

	@Test
	public void shouldSignPayload() throws InvalidKeyException, Exception {
		String signature = Base64.getUrlEncoder()
				.encodeToString(ArtifactCipher.sign(keys.getServerPrivKey(), TestingUtil.TEST_ARTIFACT.getBytes()));
		boolean verified = ArtifactCipher.verifySignature(keys.getServerPubKey(), TestingUtil.TEST_ARTIFACT.getBytes(),
				Base64.getUrlDecoder().decode(signature));
		assertThat(verified, equalTo(true));
	}

	@Test
	public void shouldEncryptWithPublicKey() throws InvalidKeyException, NoSuchAlgorithmException,
			NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		logger.info("clear_text: {}", TestingUtil.SECRET_KEY);
		String cipherText = Base64.getUrlEncoder()
				.encodeToString(ArtifactCipher.encrypt(keys.getClientPubKey(), TestingUtil.SECRET_KEY.getBytes()));
		logger.info("cipher_text: {}", cipherText);
		byte[] clearText = ArtifactCipher.decrypt(keys.getClientPrivKey(),
				Base64.getUrlDecoder().decode(cipherText.getBytes()));
		assertThat(clearText, equalTo(TestingUtil.SECRET_KEY.getBytes()));
	}

	@Test
	void shouldEncryptWithSecretKey() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
		logger.info("artifact:{}", TestingUtil.TEST_ARTIFACT);
		String cipherText = Base64.getUrlEncoder().encodeToString(ArtifactCipher.pkcsEncrypt(TestingUtil.AES_GCM_NOPADDING,
				TestingUtil.TEST_ARTIFACT.getBytes(), keys.getSecretKey(), keys.getIv()));
		logger.info("cipher: {}, length: {}", cipherText, cipherText.length());
		byte[] cleared = ArtifactCipher.pkcsDecrypt(TestingUtil.AES_GCM_NOPADDING, Base64.getUrlDecoder().decode(cipherText),
				keys.getSecretKey(), keys.getIv());
		logger.info("cleared:{}", cleared);
		logger.info("cleared in String: {}", new String(cleared));

		assertThat(cleared, equalTo(TestingUtil.TEST_ARTIFACT.getBytes()));
	}
}
