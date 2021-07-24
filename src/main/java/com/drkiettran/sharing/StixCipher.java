package com.drkiettran.sharing;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.macasaet.fernet.Key;
import com.macasaet.fernet.StringValidator;
import com.macasaet.fernet.Token;
import com.macasaet.fernet.Validator;

/**
 * <code>
 * 
 * This class contains methods to:
 * - get a 16 digit 'Salt'
 * - generate a Secret Key for use of encryption/decryption
 * 
 * </code>
 * 
 * @author student
 *
 */
public class StixCipher {
	public static final Logger logger = LoggerFactory.getLogger(StixCipher.class);
	final static String KEY_ALG = "PBKDF2WithHmacSHA256";
	final static int ITERATION_COUNT = 65536;
	final static int KEY_LENGTH = 256;

	public static SecretKey getSecretKey(String alg) throws NoSuchAlgorithmException, InvalidKeySpecException {
		String password = "ToP$eCr3t_P@$$w0Rd";
		String salt = StixCipher.generateSalt();
		return StixCipher.generateKey(password, salt, alg);
	}

	/**
	 * Generates 16 digit long random value.
	 * 
	 * @return
	 */
	static public String generateSalt() {
		Long first14 = (long) (Math.random() * 100000000000000L);
		Long number = 5200000000000000L + first14;
		return number.toString();
	}

	/**
	 * Use Password-base encryption mechanism ...
	 * 
	 * @param password
	 * @param salt
	 * @param algorithm
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 */
	static public SecretKey generateKey(String password, String salt, String algorithm)
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		SecretKeyFactory factory = SecretKeyFactory.getInstance(KEY_ALG);
		KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), ITERATION_COUNT, KEY_LENGTH);
		SecretKey secret = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), algorithm);
		return secret;
	}

	/**
	 * Create a nonce
	 * 
	 * @param numBytes
	 * @return
	 */
	public static byte[] getRandomNonce(int numBytes) {
		byte[] nonce = new byte[numBytes];
		new SecureRandom().nextBytes(nonce);
		return nonce;
	}

	/**
	 * Create an AES key of a given key size.
	 * 
	 * @param keySize
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public static SecretKey getAESKey(int keySize) throws NoSuchAlgorithmException {
		KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
		keyGenerator.init(keySize);
		SecretKey key = keyGenerator.generateKey();
		return key;
	}

	public static SecretKey makeSecretKey(byte[] secretKey) {
		logger.info("Reusing Secret Key");
		return new SecretKeySpec(secretKey, 0, secretKey.length, "AES");
	}

	/**
	 * Encrypt input using `alg` and given `secret key`.
	 * 
	 * @param alg
	 * @param plainText
	 * @param key
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws InvalidAlgorithmParameterException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public static byte[] encrypt(String alg, byte[] plainText, SecretKey key, byte[] iv)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
			InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
		logger.info("plainText: {}", plainText);
		Cipher cipher = Cipher.getInstance(alg);
		cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, iv));
		byte[] cipherText = cipher.doFinal(plainText);
		logger.info("ciphertext: {} ", cipherText);
		return cipherText;
	}

	/**
	 * Decrypt ciphertext using `alg` and given `secret key`.
	 * 
	 * @param alg
	 * @param cipherText
	 * @param key
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws InvalidAlgorithmParameterException
	 */
	public static byte[] decrypt(String alg, byte[] cipherText, SecretKey key, byte[] iv)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException,
			BadPaddingException, InvalidAlgorithmParameterException {
		logger.info("cipherText: {}", cipherText);
		Cipher cipher = Cipher.getInstance(alg);
		cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, iv));
		byte[] plainText = cipher.doFinal(cipherText);
		logger.info("plain: {}", plainText);
		return plainText;
	}

	/**
	 * Encrypt using Fernet algorithm.
	 * 
	 * @param input
	 * @param key
	 * @return
	 */
	public static String fernetEncrypt(String input, SecretKey key) {
		Token token = Token.generate(new Key(key.getEncoded()), input);
		return token.serialise();
	}

	/**
	 * Decrypt using Fernet algorithm.
	 * 
	 * @param cipher
	 * @param key
	 * @return
	 */
	public static String fernetDecrypt(String cipher, SecretKey key) {
		final Validator<String> validator = new StringValidator() {
		};
		Token token = Token.fromString(cipher);
		return token.validateAndDecrypt(new Key(key.getEncoded()), validator);
	}

	/**
	 * RSA/ECB/PKCS1Padding RSA/ECB/OAEPWithSHA1AndMGF1Padding
	 * 
	 * @param key
	 * @param plaintext
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public static byte[] encrypt(PublicKey key, byte[] plainText) throws NoSuchAlgorithmException,
			NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		logger.info("clear_text:{}, {}", plainText, plainText.length);
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.ENCRYPT_MODE, key);
		byte[] cipherText = cipher.doFinal(plainText);
		logger.info("cipher_text: {}, {}", cipherText, cipherText.length);
		return cipherText;
	}

	public static byte[] decrypt(PrivateKey key, byte[] cipherText) throws NoSuchAlgorithmException,
			NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		logger.info("cipher text: {}, {}", cipherText, cipherText.length);
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.DECRYPT_MODE, key);
		return cipher.doFinal(cipherText);
	}

	public static byte[] sign(PrivateKey key, byte[] data) throws InvalidKeyException, Exception {
		Signature rsa = Signature.getInstance("SHA256withRSA");
		rsa.initSign(key);
		rsa.update(data);

		byte[] signature = rsa.sign();
		String encoded = Base64.getUrlEncoder().encodeToString(signature);
		byte[] decoded = Base64.getUrlDecoder().decode(encoded);
		logger.info("data: {}", data);
		logger.info("signature: {}", signature);
		logger.info("encoded: {}", encoded);
		logger.info("decoded: {}", decoded);

		return signature;
	}

	public static boolean verifySignature(PublicKey key, byte[] data, byte[] signature) throws Exception {
		logger.info("data: {}", data);
		logger.info("signature: {}", signature);

		Signature rsa = Signature.getInstance("SHA256withRSA");
		rsa.initVerify(key);
		rsa.update(data);

		boolean verified = rsa.verify(signature);
		logger.info("verified: {}", verified);
		return verified;
	}
}
