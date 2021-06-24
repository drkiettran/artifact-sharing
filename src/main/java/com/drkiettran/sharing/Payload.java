package com.drkiettran.sharing;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

public class Payload {
	@JsonProperty("id")
	private String id;

	@JsonProperty("iv")
	private String iv;

	@JsonProperty("encrypted_key")
	private String encryptedKey;

	@JsonProperty("encrypted")
	private String encrypted;

	@JsonProperty("signature")
	private String signature;

	@JsonProperty("length")
	private int length;

	public static <T extends Payload> T fromJsonObject(JsonObject source, Class<T> clazz) {
		return Json.decodeValue(source.encode(), clazz);
	}

	public JsonObject toJsonObject() {
		return new JsonObject(Json.encode(this));
	}

	protected static <T> T validate(final String key, final T value) {
		if (null == value) {
			throw new IllegalArgumentException(key + " must not be null");
		} else {
			return (T) value;
		}
	}

	public String toString() {
		return toJsonObject().encodePrettily();
	}

	public int getLength() {
		return length;
	}

	public void setLength(Integer length) {
		validate("length", length);
		this.length = length;
	}

	public String getEncrypted_key() {
		return encryptedKey;
	}

	public void setEncrypted_key(String encrypted_key) {
		validate("encrypted_key", encrypted_key);
		this.encryptedKey = encrypted_key;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		validate("signature", signature);
		this.signature = signature;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		validate("id", id);
		this.id = id;
	}

	public String getIv() {
		return iv;
	}

	public void setIv(String iv) {
		validate("iv", iv);
		this.iv = iv;
	}

	public String getEncrypted() {
		return encrypted;
	}

	public void setEncrypted(String encrypted) {
		validate("encrypted", encrypted);
		this.encrypted = encrypted;
	}

}
