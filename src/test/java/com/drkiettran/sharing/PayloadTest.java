package com.drkiettran.sharing;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;


public class PayloadTest {
	public static Logger logger = LoggerFactory.getLogger(PayloadTest.class);
	private Payload testPayload;
	private Payload payload;

	@BeforeEach
	public void setUp() {
		testPayload = Payload.fromJsonObject(new JsonObject(TestingUtil.PAYLOAD_JSON), Payload.class);
		payload = new Payload();
		payload.setEncrypted(testPayload.getEncrypted());
		payload.setEncryptedKey(testPayload.getEncryptedKey());
		payload.setId(testPayload.getId());
		payload.setIv(testPayload.getIv());
		payload.setLength(testPayload.getLength());
		payload.setSignature(testPayload.getSignature());

	}

	@Test
	@DisplayName("toObject should be a JSON object")
	public void loadDataTest() {
		assertThat(payload.toJsonObject(), equalTo(new JsonObject(TestingUtil.PAYLOAD_JSON)));
	}

	@Test
	@DisplayName("fromJsonObject test")
	public void fromJsonObjectTest() {
		JsonObject jsonObject = new JsonObject(TestingUtil.PAYLOAD_JSON);
		Payload payload = Payload.fromJsonObject(jsonObject, Payload.class);
		assertThat(payload.toJsonObject(), equalTo(new JsonObject(TestingUtil.PAYLOAD_JSON)));
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
		JsonObject expected = new JsonObject(TestingUtil.PAYLOAD_JSON);
		JsonObject actual = new JsonObject(payload.toString());
		assertThat(actual, equalTo(expected));
	}
}