package com.drkiettran.sharing;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

public class Response {
	@JsonProperty("status_code")
	private int statusCode;

	@JsonProperty("reason")
	private String reason;

	public static <T extends Response> T fromJsonObject(JsonObject source, Class<T> clazz) {
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
		return Json.encode(this);
	}

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(Integer i) {
		validate("status_code", i);
		this.statusCode = i;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		validate("reason", reason);
		this.reason = reason;
	}

}
