package com.drkiettran.sharing;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.vertx.core.json.JsonObject;

public class ResponseTest {
	private final String RESPONSE_TEST = "{\"status_code\":1234,\"reason\":\"Reason is\"}";

	@Test
	@DisplayName("load data test")
	public void loadDataTest() {
		Response response = new Response();
		response.setReason("Reason is");
		response.setStatusCode(1234);

		assertThat(response.toJsonObject(), equalTo(new JsonObject(RESPONSE_TEST)));
	}

	@Test
	@DisplayName("fromJsonObject test")
	public void fromObjectTest() {
		JsonObject expected = new JsonObject(RESPONSE_TEST);
		Response actual = Response.fromJsonObject(expected, Response.class);
		assertThat(actual.toJsonObject(), equalTo(expected));
	}

	@Test
	@DisplayName("Validate test")
	public void validateTest() {
		Response response = new Response();
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			System.out.println("setting null ...");
			response.setReason(null);
		});
	}

	@Test
	@DisplayName("toString test")
	public void toStringTest() {
		Response response = new Response();
		response.setReason("Reason is");
		response.setStatusCode(1234);

		JsonObject expected = new JsonObject(RESPONSE_TEST);
		JsonObject actual = new JsonObject(response.toString());
		assertThat(actual, equalTo(expected));
	}
}
