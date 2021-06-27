package com.drkiettran.sharing;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.Test;

public class StixCipherTest {
	private static final String TEST_CLEAR = "Hello, world!";
	private static final String SECRET_KEY = "9ebQDnZNZ+D60NpGBDKggdEtozWBGo6cVHZi7YjaMWU=";
	private static final String ALG = "AES";

	@Test
	public void shouldGetSecretKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
		SecretKey secretKey = StixCipher.getSecretKey("AES");
		assertThat(secretKey, not(nullValue()));
		assertThat(secretKey.getAlgorithm(), equalTo("AES"));
		assertThat(secretKey.getEncoded().length, is(32));
		assertThat(secretKey.getFormat(), equalTo("RAW"));
		System.out.println("encoded:" + Base64.getEncoder().encodeToString(secretKey.getEncoded()));
		System.out.println("alg: " + secretKey.getAlgorithm());
	}

	@Test
	public void shouldGenerateSalt() {
		String salt = StixCipher.generateSalt();
		assertThat(salt, not(nullValue()));
		assertThat(salt.length(), equalTo(16));
	}

	@Test
	public void shouldEncryptWithFernet() throws NoSuchAlgorithmException, InvalidKeySpecException {
		SecretKey secretKey = StixCipher.getSecretKey("AES");
		String cipher = StixCipher.fernetEncrypt(TEST_CLEAR, secretKey);
		String clear = StixCipher.fernetDecrypt(cipher, secretKey);
		assertThat(cipher, not(nullValue()));
		assertThat(clear, equalTo(TEST_CLEAR));
		System.out.println("cipher: " + cipher);
	}

	@Test
	public void ShouldEncryptFernetWithGivenKey() {
		byte[] decodedKey = Base64.getDecoder().decode(SECRET_KEY.getBytes());
		SecretKey secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, ALG);
		String cipher = StixCipher.fernetEncrypt(TEST_CLEAR, secretKey);
		String clear = StixCipher.fernetDecrypt(cipher, secretKey);
		assertThat(cipher, not(nullValue()));
		assertThat(clear, equalTo(TEST_CLEAR));
		System.out.println("cipher: " + cipher);

	}

}
