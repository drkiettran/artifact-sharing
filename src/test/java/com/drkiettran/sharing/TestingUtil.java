package com.drkiettran.sharing;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.File;
import java.io.IOException;
import java.util.Base64;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystem;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

/**
 * The purpose of this testing utility is to ensure that unit tests are as
 * independent as possible.
 * 
 * <code>
 * 1. Make sure "./test_dir" exists for use.
 * </code>
 * 
 * @author student
 *
 */
@ExtendWith(VertxExtension.class)
public class TestingUtil {
	private static final Logger logger = LoggerFactory.getLogger(TestingUtil.class);

	private static final DeploymentOptions depOptions = new DeploymentOptions();

	public static final String TEST_DIR = "." + File.separator + "test_dir";
	public static final String CERTS_DIR = "." + File.separator + "certs";
	public static final String CONFIG_FILE = TEST_DIR + File.separator + "config.json";
	public static final String CONTENT_LOCALHOST_JKS = "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";
	public static final String CONTENT_CLIENT_PUB_DER = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEApt8nLx82e+aQMeCqc8gZxgQ5eoJVf00MAnIM4PMnI7gdHLX18s6S4eUFxrpn2hYC4Uv3TJ5AHgjfHgETyTSUuZ2DUyUZkTPEL2GhGEOua4dD3I469HTG09qAKbW/0rJs5aXVj90Kml62Ev8BYJWoOJ0lE4uIbHogc5iwVN9cujSk/VQu3MXagGeHhEZaVSPFJagjeUSo5WzUvQMHWFS8R1VWZlvC5dv1MpjF3Ya+QjKY+KfgWhu1nAWXQZip7pu5xIJlpHOIP3br0+3TRfHOC5jdpUkwQrRd9zihzP+4qEwh6dzRN78P7pmZ9verAd8fD7y7jk2wvNvD8+HpXQJ+FwIDAQAB";
	public static final String CONTENT_SERVER_PUB_DER = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAttJS+pGc/d4BMu/3sGWVMNFPJynT6fcO1xAOSpnM6tuMJFKipCErYV7w0zZrr0iEJOkw5R8lf9Y3U4ikMvzlR4kqVBtkiFuDkhP1wYrxibEkfvx3SzrkAVVbuUNzIpIvhe+HkVh7LfLS5nT3AEbe3sajY9RxFA44lvOXKDfhU0Ff3wWXyKF81L49WXgWms22PVFcFbAxuvlMGjTCNLgoGlJZff94WyQ2zT6mLPJM6WJUwkdSTCNsfQGoWu2RTOUR+lVC61z8k50e7DmSRxCnJ+daJ3M7vHcRoaTbuh5mOf0IK997thn7jNQIfaPjbtZMhpjTPzvbRnvewuytlRK4QQIDAQAB";
	public static final String CONTENT_CLIENT_PRIV_PEM = "QmFnIEF0dHJpYnV0ZXMKICAgIGZyaWVuZGx5TmFtZTogY2xpZW50CiAgICBsb2NhbEtleUlEOiA1NCA2OSA2RCA2NSAyMCAzMSAzNiAzMiAzMiAzMSAzOSAzOCAzNyAzNSAzMCAzMiAzNyAzMCAKS2V5IEF0dHJpYnV0ZXM6IDxObyBBdHRyaWJ1dGVzPgotLS0tLUJFR0lOIFBSSVZBVEUgS0VZLS0tLS0KTUlJRXZnSUJBREFOQmdrcWhraUc5dzBCQVFFRkFBU0NCS2d3Z2dTa0FnRUFBb0lCQVFDbTN5Y3ZIelo3NXBBeAo0S3B6eUJuR0JEbDZnbFYvVFF3Q2Nnemc4eWNqdUIwY3RmWHl6cExoNVFYR3VtZmFGZ0xoUy9kTW5rQWVDTjhlCkFSUEpOSlM1bllOVEpSbVJNOFF2WWFFWVE2NXJoMFBjampyMGRNYlQyb0FwdGIvU3NtemxwZFdQM1FxYVhyWVMKL3dGZ2xhZzRuU1VUaTRoc2VpQnptTEJVMzF5Nk5LVDlWQzdjeGRxQVo0ZUVSbHBWSThVbHFDTjVSS2psYk5TOQpBd2RZVkx4SFZWWm1XOExsMi9VeW1NWGRocjVDTXBqNHArQmFHN1djQlpkQm1LbnVtN25FZ21Xa2M0Zy9kdXZUCjdkTkY4YzRMbU4ybFNUQkN0RjMzT0tITS83aW9UQ0hwM05FM3Z3L3VtWm4yOTZzQjN4OFB2THVPVGJDODI4UHoKNGVsZEFuNFhBZ01CQUFFQ2dnRUFYdGpEcXRMdzR1ZWYyZG02eUdoWnplVWt6bTg2TFVzYm9tQTAxYjhlNWZ5RgovNFdsTDhjZVVXc2RKMkZZR2VzYzQvV0xzRzdhUHJnSUtVT3ZmVk4zblREY0xld09aaXVvUE00ZkJHMmp1OVo4ClNlWkZ1Q0NQeW1hTkppZjV5Y1pFZ3hzSkVlWXZXcUt1bG9sK3ZPa1RYbXBqWW9LU25pV2tRRS92R2E2RFJBb3QKN3RVZXVtcEp2MUVPOUQ3ZXFsNUhYd3BCU0gyYWxCVGlNeGsxWjFHWEZwdWFYdndwZ3FVZ0M2ZDlJb3UvTkZLQQo1dFl0ZmRqRkNGSnVhQnVzOE1YL2xQRzFOcW5zWUkrMEMzcWFUeXlFcWlpVk9WTlJKenRQSWNjTDVpVWRkeSt1CnB0YlAvcnVCWGsxRTNYblV4YWhLNTZEUGJIMFQ2bVBFNFNZL2QxUlVJUUtCZ1FEMEV4RTZnK0FZc0tSdS9MZXkKYUdBUTR1M2JJN0Z0NitMTEVhdFRJUDQ3Vm9aYjJDdUd3MTNHRm00b25JUm4xbzdKVTFabndSdmZhWFRqakFvYQpBZTFvdzc3cXRreXk1UGhvRTdXbE5zMll1OWd5MHA3dVFMQS9jellXVmgrVU9qTWJ2Q2owZW5nQlZWb3dOYmpPCjNVaDBXRm14NEw4Ni9CWFVGVTFtaERNU1R3S0JnUUN2Qm1yeERUbDJlbVo2K2NyNThrR0hBTTh6UG85WTBUY1YKd0ZKTWVySDF1dk56Zmt1cW5IbWhNNTVhN3lwZzNiMGwvd1l6am5nTFRQSU5kY3NkaGFmc0xWdHVBZWk5Z0sxYQpjKzR4TlZHN2tWWERYejZ2dXV5SHhHUGdCMmtnbDlqWm1acXdndDFwbXZtbVBjaXQwMjVTVHl5clV0WVZEQTU1ClpUREpnei9OdVFLQmdRQ1ZaYkp2QWRmQzVTVFJkc2t1WUNzbFN2SlM1NmNzWkcyKzVRTGNjRjZOamFuU1FDQnMKYmR0UmE5dGo2bUkxZVNTUFlQNkxwYTFjOUIwRFIyM0xlNUNKUnYvdWhVV3ZYdTRhTHE1S0FhQ1pNNm5qZWY2awpVVjVRaUVIOExCMUtTdEdMMFlHMEc5Nm0ya3JKSmFrSW5uUkNHdTVVTGdCL3AxdnBKRnpyT2xKVWl3S0JnUUNtCjNSWjl4QjdobnFZdlhoQ3VwTkRtTmRaVXc3TUVlVW5zR2RRY2grazhIa1ZWK2JXSDdmQmp5SU9UckdxWnVTMUkKbVU4L1BmZWl4blFLY3gvM2dHSnMzMzFJYnRlR083U0tCUGEwd1dHdjBrcVNuaTUwZVdCaHU5R0FWM0JabTRzcApRYkZoMFJIb1NkRHpOZm9xQnVZcDRNUDBmbUFONXRXeDFOQmpmaGNKT1FLQmdEUzhHb2h6WVVlWkFST1hzbUpPCnBRTzBTcDluL0x4eUd3OEVjdjBWcEx2aFBDMUt1ZEVaS29jZkRvR1BXd2laRGlSRTVkSFdINDdyN1dNc3g1bUEKZVpJV0tSSElnZmsyYktzWk5nQnB0Y2dUM1pUdXlNUCtDWVBMTDF6a3hFbnVzbFl2ejVwM0VtSTF0cEpGd1lWdQpaSlRuQUhJMjhFWkhMNXdlUlhaZ0VWbDEKLS0tLS1FTkQgUFJJVkFURSBLRVktLS0tLQo=";
	public static final String CONTENT_SERVER_PRIV_PEM = "QmFnIEF0dHJpYnV0ZXMKICAgIGZyaWVuZGx5TmFtZTogc2VydmVyCiAgICBsb2NhbEtleUlEOiA1NCA2OSA2RCA2NSAyMCAzMSAzNiAzMiAzMiAzMSAzOSAzNSAzMyAzMyAzMSAzNSAzMyAzNiAKS2V5IEF0dHJpYnV0ZXM6IDxObyBBdHRyaWJ1dGVzPgotLS0tLUJFR0lOIFBSSVZBVEUgS0VZLS0tLS0KTUlJRXZnSUJBREFOQmdrcWhraUc5dzBCQVFFRkFBU0NCS2d3Z2dTa0FnRUFBb0lCQVFDMjBsTDZrWno5M2dFeQo3L2V3WlpVdzBVOG5LZFBwOXc3WEVBNUttY3pxMjR3a1VxS2tJU3RoWHZEVE5tdXZTSVFrNlREbEh5Vi8xamRUCmlLUXkvT1ZIaVNwVUcyU0lXNE9TRS9YQml2R0pzU1IrL0hkTE91UUJWVnU1UTNNaWtpK0Y3NGVSV0hzdDh0TG0KZFBjQVJ0N2V4cU5qMUhFVURqaVc4NWNvTitGVFFWL2ZCWmZJb1h6VXZqMVplQmFhemJZOVVWd1ZzREc2K1V3YQpOTUkwdUNnYVVsbDkvM2hiSkRiTlBxWXM4a3pwWWxUQ1IxSk1JMng5QWFoYTdaRk01Ukg2VlVMclhQeVRuUjdzCk9aSkhFS2NuNTFvbmN6dThkeEdocE51NkhtWTUvUWdyMzN1MkdmdU0xQWg5bytOdTFreUdtTk0vTzl0R2U5N0MKN0syVkVyaEJBZ01CQUFFQ2dnRUFZNHFDMmFFYzdIRExFRlB0OWFKcHA4bmZJZ0M1UEVOalZoK00ySHVEUWtERgp2aVVzSHRkY2lraXFNU1lKWGNmTEExbmdZSEFqOThYSUcvaGpCc3dCZm9DbGhtUGZ5Z3FoeVpDS2w1V3lTM2tpCjJPMVhlcU9XNzA3dGdTTERkb2hIemRJTWEwdlV6Y0RQcWVEM2Q3UWV0d2RLZ1JsemRDc0dEdEtVYzdaeFBlV0cKcFJBTW9uLzhPN2I5M3ZIV0F6Wm5rZExSVytrRjhvYmZTc2tUYmNYZTIrUjMrRE5qTk1sY3RiSnJSRHVhUE1JNgpHNDRoMXNUb2dXampkVXNyaDR2cjN5Rmg5MUJCaHBEbGMzWmhKR0s2a05ibjFtUlVIaWV0VkRQK0VZZjZ5YXBwCmFhQVFtKzZPdzBnd2tPd1hxMU1EZ0w3bWtCM2UxMGZUZXRJT0lnQktrUUtCZ1FEdmE0OExsd2hMODZZTFkzOSsKSU15VHV0YWYvUE0rclZlTjM3TW5tTzRQZlhUYStMK2VnbDlkTDhWMmhIeEo3bTVLVWZkZDgvS0lMbWhEOG80dwo4RmlkZ3FuZGJzV1pWMzJ5YVhxaTl2RHVjYWhGRmZMcEJDQjVrNTI4QmxOSDZuNWUyKy9qRUtzV0JIQjZRTW1lClhIR0xLNTNOSm56aFFaTHhQSnVuZ21BM0JRS0JnUUREZTJLOEEzcEhZeHpLS0JRUTlnOGwwcFBad2MvTUczY1MKWFNRZFFySndJRnBvSEVQNi9aNkIwcThqRllMVUpyRUtoT3BIRmhNWVZTa3NTZ1ZWVHRld1RQV01sTi90a0VKOQozTno5b0pzZ2NxN0xuNElET2J3eFZaRkc3MG9ZQWNLTkV2R2pwczg0Z3Y2QnhKdGNoMTJLQ1VVTmxFNDdxYTFJClZRWjd0Wm5KRFFLQmdRRFFsa3ZNMUN1MVNEUHNqaVlBTkFFbjM4cW5IbENwMVltSElGTE1kODlJRFl3bVRqdGIKbE1nU3Z0RHhYYUdQSTd4UTRiSjYxMU1BMURXZ3BReStsRmNQKzB1VWtMSjAweVcrcjJqWjIveXlNTEZpWnluMwpXdElVT2NoZGpNRTMwWk9CZjJveTBFM040OVkrbkgxTGk4eWNiRWFSK2lzb2NPSGRiR2xMK2lsckpRS0JnRFJoClpwYU10QUpXKzRycjdGeVRJb0gzQ0Nrc2R0cnhiUm1kbmFTOWo0VGVGbnVaUDFvTkJhRXg3RDRSY0lvYWlBd1MKaTVoYXdPa2ZRTFllYTRsdFkveFkwdDlGc1M4K0hhTU9RS1V4bVArNzJ6eXkxQ3E3R01ON2N6ODN0WG56VnNkbwprUmxTQkdyWEp3MXN1bGl6NlF1bzZqajJTWFJSUmg2QXNna0tJMWd0QW9HQkFJNVVXMFQ5N0Y5OHF5cVFWbEFLCmtoajR0OFMrbkpqMDNpbXRnK3VYM2ZiZ1RZbWNlSzBRQ21nUzF3bC9odnh4eFlaUzFjM3RNYnc5SzFxckw2ek4KaTZuVE9oMTdDanVZdmJSQUxqSklYY09BM1RLTXl5NnpXb291cU1IaUhUTGZjbStlbXBTcnp2QUN4VVV4Q0FZRwpBeEhmV2I2R2JhZ3l5aHdsU3hNOEdIZ0oKLS0tLS1FTkQgUFJJVkFURSBLRVktLS0tLQo=";
	public static final String TEST_PASSWORD = "changeit";
	public static final String TEST_LOCALHOST_JKS_FILENAME = TEST_DIR + File.separator + "localhost.jks";
	public static final String TEST_ENCRYPTED_ARTIFACT_FILENAME = TEST_DIR + File.separator
			+ "encrypted-artifact--6f437177-6e48-5cf8-9d9e-872a2bddd641.json";

	/* @formatter:off */
	public static final String ENCRYPTED_ARTIFACT = "{\n"
			+ "  \"id\" : \"artifact--6f437177-6e48-5cf8-9d9e-872a2bddd641\",\n"
			+ "  \"encrypted\" : \"7APPmg2YLM/8fLcaxnBWJo4oBZ8GfZYm9uPpBwF4hrztmbeJoydrlyFotmf28cGIWW9ZMxodSYefS04ryElwRED657TFyg4KNM4n9X31+jaWRlUqWw8brVbAnWdn79Ux+nIlShA71RLHUFvpSLUK57fUdUCsXAaXKMsa\"\n"
			+ "}";
	
	public static final String CONFIG_JSON = "{\n"
			+ "	\"http.hostname\": \"0.0.0.0\",\n"
			+ "	\"http.port\": 9090,\n"
			+ "	\"tls\": true,\n"
			+ "	\"tls_mutual\": true,\n"
			+ "	\"keystore\": \"./certs/localhost.jks\",\n"
			+ "	\"keystore_password\": \"changeit\",\n"
			+ "	\"truststore\": \"./certs/localhost.jks\",\n"
			+ "	\"truststore_password\": \"changeit\",\n"
			+ "	\"datastore\":\"./test_dir\",\n"
			+ "	\n"
			+ "	\"certs\": {\n"
			+ "		\"server-public-key\":\"./certs/server-pub-key.der\",\n"
			+ "		\"server-private-key\": \"./certs/server-priv-key.pem\",\n"
			+ "		\"client-public-key\":\"./certs/client-pub-key.der\",\n"
			+ "		\"client-private-key\":\"./certs/client-priv-key.pem\"\n"
			+ "	}\n"
			+ "		\n"
			+ "}";
	
	/* @formatter:on */

	@BeforeAll
	public static void setUp() throws IOException {
		System.out.println("@BeforeAll ...");
		logger.info("creating TEST_DIR ...");

		FileSystem fs = Vertx.vertx().fileSystem();
		prepare2Run(fs);
		JsonObject configJson = new JsonObject(CONFIG_JSON);

		depOptions.setConfig(configJson);

		logger.info("config: " + configJson.encodePrettily());
		System.out.println("@BeforeAll ends ...");
	}

	public static String prepare2Run(FileSystem fs) {
		logger.info("Prepare 2 run ...");
		if (!fs.existsBlocking(TEST_DIR)) {
			fs.mkdirBlocking(TEST_DIR);
		}
		if (!fs.existsBlocking(CERTS_DIR)) {
			fs.mkdirBlocking(CERTS_DIR);
		}

		JsonObject configJson = new JsonObject(CONFIG_JSON);

		fs.writeFileBlocking(CONFIG_FILE, Buffer.buffer(configJson.encodePrettily()));
		fs.writeFileBlocking(TEST_ENCRYPTED_ARTIFACT_FILENAME, Buffer.buffer(ENCRYPTED_ARTIFACT));
		File curDir = new File(".");
		logger.info("curDir: " + curDir.getAbsolutePath());

		/*** In the beginning only ***/
//		loadBinFile(Vertx.vertx(), "/home/student/certs/client-pub-key.der");
//		loadBinFile(Vertx.vertx(), "/home/student/certs/server-pub-key.der");
//		loadBinFile(Vertx.vertx(), "/home/student/certs/client-priv-key.pem");
//		loadBinFile(Vertx.vertx(), "/home/student/certs/server-priv-key.pem");
		/*** In the beginning only ends ***/

		dumpBinFile(Vertx.vertx(), configJson.getString("keystore"), TestingUtil.CONTENT_LOCALHOST_JKS);
		dumpBinFile(Vertx.vertx(), configJson.getString("truststore"), TestingUtil.CONTENT_LOCALHOST_JKS);
		dumpBinFile(Vertx.vertx(), configJson.getJsonObject("certs").getString("client-public-key"),
				TestingUtil.CONTENT_CLIENT_PUB_DER);
		dumpBinFile(Vertx.vertx(), configJson.getJsonObject("certs").getString("client-private-key"),
				TestingUtil.CONTENT_CLIENT_PRIV_PEM);
		dumpBinFile(Vertx.vertx(), configJson.getJsonObject("certs").getString("server-public-key"),
				TestingUtil.CONTENT_SERVER_PUB_DER);
		dumpBinFile(Vertx.vertx(), configJson.getJsonObject("certs").getString("server-private-key"),
				TestingUtil.CONTENT_CLIENT_PRIV_PEM);
		logger.info("Prepare 2 run done ...");
		return CONFIG_FILE;
	}

	@AfterAll
	public static void tearDown() {
		System.out.println("@AfterAll ...");
		cleaningUp(Vertx.vertx().fileSystem());
		System.out.println("@AfterAll ends ");
		Vertx.vertx().close();
	}

	public static void cleaningUp(FileSystem fs) {
		logger.info("Cleaning up ...");
		fs.deleteRecursiveBlocking(CERTS_DIR + File.separator, true);
		fs.deleteRecursive(TEST_DIR + File.separator, true);
		logger.info("Cleaning up done ...");
	}

	@BeforeEach
	void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
		System.out.println("@BeforeEach ...");
		vertx.deployVerticle(new MainVerticle(), depOptions, testContext.succeeding(id -> testContext.completeNow()));
		System.out.println("@BeforeEach ends ...");
	}

	@AfterEach
	public void finish(Vertx vertx, VertxTestContext testContext) {
		System.out.println("@AfterEach ...");
		vertx.close(testContext.succeeding(response -> {
			testContext.completeNow();
		}));
		System.out.println("@AfterEach ends...");
	}

	@Test
	public void testBinFile(Vertx vertx, VertxTestContext testContext) {
		if (vertx.fileSystem().existsBlocking(TEST_LOCALHOST_JKS_FILENAME)) {
			logger.info("deleting test jks file ...");
			vertx.fileSystem().deleteBlocking(TEST_LOCALHOST_JKS_FILENAME);
		}
		dumpBinFile(vertx, TEST_LOCALHOST_JKS_FILENAME, CONTENT_LOCALHOST_JKS);

		Buffer content = loadBinFile(vertx, TEST_LOCALHOST_JKS_FILENAME);
		assertThat(content, equalTo(Buffer.buffer(CONTENT_LOCALHOST_JKS)));
		testContext.completeNow();
	}

	public static Buffer loadBinFile(Vertx vertx, String filename) {
		logger.info("loading .. " + filename);
		Buffer buf = vertx.fileSystem().readFileBlocking(filename);
		Buffer content = Buffer.buffer(Base64.getEncoder().encodeToString(buf.getBytes()));
		logger.info("-->" + content.toString() + "<--");
		return content;
	}

	public static void dumpBinFile(Vertx vertx, String filename, String b64Content) {
		vertx.fileSystem().writeFileBlocking(filename, Buffer.buffer(Base64.getDecoder().decode(b64Content)));
	}

}
