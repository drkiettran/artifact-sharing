package com.drkiettran.sharing;

import io.vertx.core.json.JsonObject;

public class MainVerticleConfig {
	private JsonObject config;

	public MainVerticleConfig(JsonObject config) {
		this.config = config;
	}

	public String toString() {
		return config.encodePrettily();
	}

	public String getClientPublicKey() {
		return config.getJsonObject("certs").getString("client-public-key");
	}

	public String getServerPublicKey() {
		return config.getJsonObject("certs").getString("server-public-key");
	}

	public String getClientPrivateKey() {
		return config.getJsonObject("certs").getString("client-private-key");
	}

	public String getServerPrivateKey() {
		return config.getJsonObject("certs").getString("server-private-key");
	}

	public Boolean isTls() {
		return config.getBoolean("tls");
	}

	public Boolean isTlsMutual() {
		return config.getBoolean("tls_mutual");
	}

	public String getHostName() {
		return config.getString("http.hostname");
	}

	public Integer getPortNo() {
		return config.getInteger("http.port");
	}

	public String getDatastore() {
		return config.getString("datastore");
	}

	public String getKeystore() {
		return config.getString("keystore");
	}

	public String getKeystorePassword() {
		return config.getString("keystore_password");
	}

	public String getTruststore() {
		return config.getString("truststore");
	}

	public String getTruststorePassword() {
		return config.getString("truststore_password");
	}

}
