package com.drkiettran.sharing;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.vertx.core.json.JsonObject;

public class PayloadTest {

	private final String JSON_TEST = "{\n  \"id\" : \"id-1234\",\n  \"iv\" : \"iv-1234\",\n  "
			+ "\"encrypted_key\" : \"12345\",\n  \"encrypted\" : \"abcdef\",\n  "
			+ "\"signature\" : \"hash-1234\",\n  \"length\" : 6\n}";

	@Test
	@DisplayName("toObject should be a JSON object")
	public void loadDataTest() {
		Payload payload = new Payload();
		payload.setEncrypted("abcdef");
		payload.setEncrypted_key("12345");
		payload.setId("id-1234");
		payload.setIv("iv-1234");
		payload.setLength(6);
		payload.setSignature("hash-1234");

		assertThat(payload.toJsonObject(), equalTo(new JsonObject(JSON_TEST)));
	}

	@Test
	@DisplayName("fromJsonObject test")
	public void fromJsonObjectTest() {
		JsonObject jsonObject = new JsonObject(JSON_TEST);
		Payload payload = Payload.fromJsonObject(jsonObject, Payload.class);
		assertThat(payload.toJsonObject(), equalTo(new JsonObject(JSON_TEST)));
	}

	@Test
	@DisplayName("Validate test")
	public void validateTest() {
		Payload payload = new Payload();
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			payload.setEncrypted(null);
		});
	}

	@Test
	@DisplayName("toString test")
	public void toStringTest() {
		Payload payload = new Payload();
		payload.setEncrypted("abcdef");
		payload.setEncrypted_key("12345");
		payload.setId("id-1234");
		payload.setIv("iv-1234");
		payload.setLength(6);
		payload.setSignature("hash-1234");

		JsonObject expected = new JsonObject(JSON_TEST);
		JsonObject actual = new JsonObject(payload.toString());
		assertThat(actual, equalTo(expected));
	}
}