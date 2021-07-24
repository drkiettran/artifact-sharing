package com.drkiettran.sharing;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystem;
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
	public static final String CONTENT_LOCALHOST_JKS = "MIIWQQIBAzCCFfoGCSqGSIb3DQEHAaCCFesEghXnMIIV4zCCBW8GCSqGSIb3DQEHAaCCBWAEggVcMIIFWDCCBVQGCyqGSIb3DQEMCgECoIIE-zCCBPcwKQYKKoZIhvcNAQwBAzAbBBSSaOSSqZR_gjNgxs5o2hTJlkgOmgIDAMNQBIIEyN1ANeTgtjLtbSL9kX-BPPefQDkcnraZghTiTTj4Huy_A0uXPXWOnQeYV6K_urIvg455RenXbKqyBmYIkN4dwxhKtkAG3ZMdg6UX7F_ncVBHc3uA6KA6T1U9TG3ZvKuPXuayppvCtwk5xYuACHByvuDrpKBCqjFFSRWQnVz_pO5cc9a4HPCah7vv5Eph2ra0iiXwoCwDyVB0iMefKnBx1P4GzYsX8L4C7FDmdMwDs2y1WndgGrmFoPdj5KJ3gAlYlnAYRJmzHyrIIpCK9z_8FPezxJziI4GwI2_edbvXwLxqrrJ9UDOO3KKvx60ZUJCOgRck-PSc-Ht2agijcrjbCVVspWpB92tg7ZWWv5mWP8kxQ63gx5lTzBQH_rznEhOYJMdaMr0laO3k98TWu1Gs4Iy3qpNZEtz8dhFFrMRb_DvaoALYSjVndCCLNwH4rmlbAxOVLRqiA0kT0xGF4Tmok_mv2JSUBIir4bv_WobEPWs8jaJ3w81EAqL10j7syPUjL4hrXpwPYWEdktq_-JFC-HU5uS_iRbtViIyTU5es4FUvw3Hr3_3LIHVqHnWvVv2knizqvJnPWMBKQ1osUghhXQ4PlQZc4-LdsClUdrUpYKDA28cc9yuvQ1fOpaIqJBmgyKiv_6KclP7eNMKvE0K_iYB0T7hJhzrzFYmH5ouGeyj51U4liB-FOd4biHW-GvEF0XtrH5CPu2129qWPvMJ87huqa3R30mjDQaJv-GRj29K4i-KKtUP_0U8uUN9z2RVbqZLOKKDQaFJR0FlMMggBjQzYhYgbF281GNKm7rcV68OYjFlt5zQyHIKZSh_54N5NolK1OXrxUGR9ROsouRpVqJzugsJnUjJHLQostwkZfQVLFAAkRrSOn1td3K0TVPfbttlNAH73AyhQgGqILijDSGuUWsRPcD2HW2pNGtGkA1B4PC59Kn8c5Z3OiMrFqDjJ6PsW5nlkTDnd_IebVRbnIeN2Entmv7ejdNg8JR69TgNnYlui3M8T_eN8ngakWClL0CvRqjJ9o6k5wUdjs_YOBVzPMnL82NYTdjM0hEE0GsViYA0NvulgxNikTfa5kOx5fRelL5cLitXPHX-xWmn4Ff-AO6_quQHh6uR33sEBu-5hN5l--drPJIx6ovSJ5E_a43-0wwje-PIuNwWnO94qv9yIOtTf9eS1cNttFNK2jBi2A_SYe2pffsEFeSSDDk_KAqfAIiBs0jlF-hoGfMvA23FKf9WkRf250_fyCmP8eclMc4tC_UAdVshuu_6uetKCctyQTlujjNIfAjFmoQPkczH4B_m75DmABE5Az5NCBYWAkeRbZVYSq4gBJDU7d3IyLaL8df81V0SvX09uDktTyzlR9QKi2pfgxyMskKwBeeA03pi4n9xf2FIQ4NJiDLrNLKt5ly-ArAQbIAiVoWVDTZDt9J7G6vze7qNyBY_0ZC7XNqdNsX_Q9aeZXJuIf-XBlVfGlLP2geudVeAeY4LOzFWVuhbvRv_rnSeMh_cK1Hw0Gz3FKhOe_uifwHVFffnZLVYKliGmp0_x24z0G7isFOemchoeNY5cqTZylteSnEmvGn8SFWKYChEhhrS78YbsyIp6tGkuzxb7rWomjclDRbTH196aLmLM2DFGMCEGCSqGSIb3DQEJFDEUHhIAbABvAGMAYQBsAGgAbwBzAHQwIQYJKoZIhvcNAQkVMRQEElRpbWUgMTYyMjk3MzI3OTg1NDCCEGwGCSqGSIb3DQEHBqCCEF0wghBZAgEAMIIQUgYJKoZIhvcNAQcBMCkGCiqGSIb3DQEMAQYwGwQUAiTISduPDboucuhzCjB-NIHMwNMCAwDDUICCEBirWXt-5jszoINL09LsY-ttf2yZsDL0w1CtAkYFAfDVB2gY0FTF87uBERjArUO5XD644BPEm4GjJ0Y1PNj8Il9MuuHYJmWrGc4y3A8Mv5GQXd0PwVcdJoR7eXTaO7YY7g97NpiRJ1JdmrJTnONPnlB5RpmwLnsimiZk9rPSbjBktLeApEJ3XERx2FXsNszK2onKY4pOknSXB3b0w0IPLNND3kg9arewe9MDnLNoZD_oNn6SP1RoiEOrSBs8jtGT-UeYXRe_vjFfl8Kce3FdRFJxNPuY3U9xeiWT9mwMmJxyX4J4Yx0jMvaJMBmbn0lY5lLabYziZHxAWKLI2nHvCv27fHUMNIvxUdH0bG8EcitRZfkpPKRs-pSc7osRXU7tMWVjy9b_lpYGd7PQiFU3Mmgtswwq1qAddApFhG_Jm8U3JpbWFgn5wqelVRoqICkdCrJDx4TRHdEnW48QaDXxdZiY7yKRGh7alX4Qf7flrZh4AoP_oA9UMqLsAYtBLujbKZ-QSi7c11LnWB5P0W6M1BWZsQzO1JKRoSKqRmG6FRIks0HXht3qoECRvSQuSUV6oJ6hS_hThRi18kgWh7ZOoXvgFi9m5gNX6Uq3Gjrxa21T_gR7lA2ocwfrCCjOTh73uNbhqSuVTK1z29h2yhmwsEkgfxgh0VtRoZt8J9qb0C5s5flJS-Apz8-mAdh5f2W-BHgc6DFfGMdZI8h_FyIBZQrh-s9i3zbcAOrcrY2TtGO7eTd6aQWUg6jH9brK_4jlX5Y-d8fNkirxXxQF4Kf236OKV2tA7-2IlVlVME5lpH2K56hpKPSuPKfoyWzTIuuAm7iv3pGpUlIryZylzFCnBjuiGa1GW6u8GQMzFTEA8r2fjWsHhsbyhKJc6gLeT-gLU17RPhtseJa5u9_HfY6jm_H3TUXRWxK7elQQqgfe9ss_pllPnWcGAaO1YU8iP7TWyEESbBXeFY8F-Hb_fzfZZb_xHKkju2EhnHUbUDZ-1pBWs7wu5Jc0an_-9_vzLBmEjGFaaTqjudaAy-4KAyXT7jHSTkmaKt8Kzdk2uCWc4gvaNZny2cT7UlP4tSp8nXX1qviNvLYmFUeFJICcuZwizN534A0c4c1Ax5rHoUkxjF2OtH09J04NUTH6PwWKrvV9ldOAO-6-Ty7QgYhrQsF2kdhOUDiRVxLKdD39kiyYt0HnJKRl3R9euGZYYEU3sSOF_0RoRev5SyBbKjGeXs7YKweDcJV1RGUkmawhwdhCpwPINpKUlf4ITL4j5Y0eTpHxl-H_wGOBL16MJX03djFDy6vroUgsJhCudedLeQ9Tr7gC_ANzb-l117uE8nWRd9fgqcGECiT57VuaiLK-QFbKiMnpYzsaDm5Zh3VrfNGvyYLKXBZtF6t42arBb0XhpQo9TUpjbfIPpcFEOg_ZMg2r3y5-h0h8XQKNIp2_iH5CBDRN0teSw_SojBZlknAzopWg95gZnNPwjZDZblpht3byUm6E3QDD_Oeoa7mh5wzyKIWELSFzH2VAYPEbRwEVW1kOCXUgwgq3OCBv62FfB3UqD2fsy0ZTdd797Rlxh6qsKrRPNxIUdneabgkrxsC_O1yUDDrLVaJsxEEzXvO0dstdKHa2JYSU_qBug4aX-pSwkJFO6nw6zV8bXOYsmPqQNp4hBy43BQEjPMJAm1sgwwVrm8yQyV0MmFe09el4KwK8ZTfIxrgTTGPB1hbN-G7jZLfIn90l6132tRTuLTRMjx7t7K8FnNDxc1_8aNY2HZlgLtnTeo9ubcLEOg2zlDaRAaurzcAN-AriNh-98h8rvi64dbuRKTXKHtKsyuUhJumhctKkV_lqzY5NskuRrRXMGp1PwSTXfJMRaQMuFWA8GyfHkj8vP7eUIe1kZgwHzkVLGKHDAvq4XeezMVT48YIFHKYMe9QdjP-F6P_LyAaz0qtAHdNnSvd3ojhmz2bgYMTaWNsDbm5yvLY8izXdXzfurk3iBOsBfqrWoy53zYgN1nJFbNf_lxNZgzOWDL8bGlZgLUxytY4Y3t8fYTnPrnIM-te7dkJRTMGrRCgOBiUhl4OltGCL8VD_RrFUC7vciauUfi6_cBGYyCN2zmprZMDp4enyN1KTlN6G5SY4G5GiFlFmNHD1-lZsHYS4VpJpTmrP7OX813EK7yY_Um3Tb5klHukvKsKGAVOrDhOzBeoCHilqgGuJOmkuzfp9Sk455cFeGVcjCpmgZhS5V7q3tvOf-RhT6q2BjzLJsJZBdWxpDCwAJxnGq_3KGPKnUWU835n_QH0tLnGJdAIjTy2220UfBdvtgdj-8Q2Zyogyj0BjSm4_nLiGR34o-1UrDoTvB519CLunsbYUUr1wsHF5Bv-GaP2Qi7h7g_DkWXNNV_4L1uexNfBwRBG_h76g_CyImsnRMO_-rZLOHf4D1AuDWsYN0p4TH9zC9kAHne_A8jt1D4ckXxc41LEQGkQzIxPaB00MA4T6hbAmaojocWOJPgOqBnbWUfypEM9UJfPlzX50aaqbW0myNTSBu7FhGxNn38Weecg2zukcKF6zIRgZdQWDQqHlTAxRDICvZ_FgB7LbTpstcAv2AG47FLMkFthhEwTFABcUDorjx2LP8aYx_gw1Uu-FYhAGauVeg9yZVALXtUx7TdMjJBoCg3WfkrW9GbNrYwpwzfNifkFwI2-RT4F2x4CG3ruiZP0nnsFlYJfkf9itHyeiqdTQEcOqtb3IKKFYF9sY7kQx5-9LOvLT92T5IYK1bB-TmNTji48KO3o08e9om4rKN2DsB9RMbVjhZEpjZUH37wJZb4JlbGA4JOpldkQzdayxoirhQYbUWEduXSp62IJWk8v23P0OV9JQsuww0Zptm188Y_1FlrYFBw-NNoDxE0qDI82WWfhMZPqWhUKj39U5IxWoBkX77cyMWFX714J5s4NW4HXerg5qzC4VXLMcLig58TMi0PxU0AClvFvbwOKCyf6c9yXdvoGiHyg5u8uibBG2jMGABa5JXC97S2xyDzXH7cRrRCnVqRs7Dvb3TRtPn2qTi5BSl2k4wHvEqkxZxnghWPo3pON9ZI4RKV3MoHOsu4SjBH3Cc0AVgPapMlNR8qq_mzTDVwWhV2lJKI4FQcufUjxgBypNJpXL8ihjQuYNHb8hAXBJvpT7Gs2_uJpm2u_Q2Nq02gnO4pLmBWxLikaasFTPEl2veiP6A1IFrZ4L_ffw_Y3LTWir8_LtgAiYWpjBJoHRuLHv81vTctZ4q1qF8evwIORU69bGYCqMQQEzcNb1uLeFjDjG1AxU9fsevoHyx0zcRp-QXdO9WvbSFciWkBcjSZ4ICgVNL9nR5lPbsGqiEgGDTWf-Nu5k1GXXoCkIMHd1QXziJ7g4ofjM6FwzUpYGrKRz-EKompqrZ3jKA8ttKBPUe4Ku1hCRgzjryQOB3r1zkv4qUOvEw212L9rVYsl_hVJdMxKrgq2cnLVMbf33FOTTgw7L-69EP865SP3HwCVkRX9fPIVR4Fbs6ue-vGbrFMDUcixheukusmgypB5oFg1i_UnivZF0QJ0neQRjkPfNO70vvnyMYbM-NWF36FWcBONXx7IgXiwIlA-cJoDPvKodle62Oet4v542CijLW_EFJW6QA9KbNw_Pj98P0NZCmzOSSRIfNGnBdfFpYCYiSCQgmEBKV8gSC0XOsQmlu6DuVefrFWcZXSafOkQjuxQgJZ_R0vGTNSJc10O6P4vUjG6S7KHdUyeTd00OE_08KuiJZ6fmFY1zoHXC7IMboxFlUpafQPFighVPkVRWvGtqsziaQ1dzXmgcr2U8SKsc1DvYUkQzo4_--ltoWgrrMHmYU1kFmB_5mRb3MmXxY8bfwbtxiZPw_2tCBxXeufEUX02G1PwYQf710JlC5EPLZntOCncjMlxzmc1tsZU1KA0ie5LoOjc6IzbygGYdBPbFZqc5FvPqrRGh8rcOv4Zxtyt4F4d0FD4SRtJjaPnk-IR6qq3xROnPYxMNyjYXdP-Wzsdv-tZqHDq8CefQ_pUIIyS63Y4FwQXy9ZrXVi_1t2P5GE-ip83kgTYNTDSNXzt-w0aP67jSxbzVn_LVSDXeD52imPIXH3K2f9hOUO28B3VPOyb3hBroqI2qrKa47CAcUn_hSHfxihs3r001K80V62vnrRg-FAHtLaRWx0mNjMxl00agrhw5muAkYXL-rYAS7pTCZPl4YF1Lej0WBfJQRIVsMSOjevx9aQMl4qsjDO1cJDfxb8bOzwvql0WP17C_6R89YQ1dIoW4aS9q1yLsm4LdBlYQ9keV-LgBjBFw5EzmaPtNgl8qeIdRmxT7YFRN0vkOIkB7jgrHMVEWUewa9l3PReMGdkkppMRAxzX5aijTvNwDt92WCqVUZmBdDXZuriuzdjvcquuqp3kGwf-8RkKsdFz8l6lRk8r601TAuOY50-gKak4I3yOuphzO3qxGQgGS2sqDlIreqQLd7PskjOPlBlckIsLQc94Kb38KJ4oKTHZjYDQVyeWgeqtgfFGPLSg2Fdy6zwfOys_oZqJOcUcLHWElokc7zsSXAnasNw7Nvqn_UlT8jA3Sk2WbUps4D3M2eGZvQ1-QMOQAXF4bxh7pQpvvgKl_LUy7obsjyb_VYn0R5ng7ZLhb8Q7LyBJzKGoQshUx65vSu3_1wEZ0ejskvI4EGj_0JbYQTpN0m8KymUtkFOzq3rKd3e74lbbirH8TeQRiFUnpqiQeYiVnJ1gE7Zt01eAI9xgdgA2EnJNHjIDUcttGQEMH8mx0jhwvModoPzbyom4S50i7685JN2df6hJqJVP6E_JWgP2iqpK28fPJ0QQoZw6eBoOpfrcm4GhfuwOT6hm6nMHQCtzqhd3aQeHWcIf0egFuXfwX8XNk_FBs9ntXJ9ZNVLy3Rg_0GYpa3jHpRoeZcYb8uNC53x454PEfWI-c9R11RYwTGZfaKy7-hUxbx2tx70-mDK1Fsb1sZLPkSDrPyPnqbgc58dfdQd0oppfLQKhH6Essxzf4X2xwzUYmp3KIH8WGS41Lt_yAqs2Fau8vDV_MBL_qvxAncx9MT94Efnp9f-SlQ1_8_uvVLkZPn_DCz2kg78IT2ckp1PM9glxB9vR6HSMUrM6cIfDrZehRzg9UPnlp8KSyFn5KTX4l0iy3PsB4qJbJEq0z20U-NWzF-tK3eWLTwEqIRGMt9yzY_SKo73ZAmqUDnFiVdwasjpyQ62eUfz0d2XxsHojpzP_WDduxaBxI5Bobcf_6Jv_DOXly_q0DkF7nhX9DxOzffxM3a7Zdtt6C8L5wDFejfic8nhBTjfJHWdeNs6SIueYeRqF_625Hy4oKX-d3y3QSyvNbi0jUvGWP4TXpxEsOHJtz5YfMhKftpM4TDOEBE4UceE59sVBCmVKWIxV1L6IqaErIDB2Dlja6h4ppE5C2CyK5gPhCtTxWt_pnSp_Yy2xnPm3FcDagbyx2VUrwj-hz1kPamd-2gAPj6KhQVsNIjKfLiD2fWgMfYrq-Q8DyMD4wITAJBgUrDgMCGgUABBTBhyCzbz6w6FZvK82Fe7HNo922qAQUbHwFdMdrrNVjdYC3H5LLBnYDkgwCAwGGoA==";
	public static final String CONTENT_CLIENT_PUB_DER = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEApt8nLx82e-aQMeCqc8gZxgQ5eoJVf00MAnIM4PMnI7gdHLX18s6S4eUFxrpn2hYC4Uv3TJ5AHgjfHgETyTSUuZ2DUyUZkTPEL2GhGEOua4dD3I469HTG09qAKbW_0rJs5aXVj90Kml62Ev8BYJWoOJ0lE4uIbHogc5iwVN9cujSk_VQu3MXagGeHhEZaVSPFJagjeUSo5WzUvQMHWFS8R1VWZlvC5dv1MpjF3Ya-QjKY-KfgWhu1nAWXQZip7pu5xIJlpHOIP3br0-3TRfHOC5jdpUkwQrRd9zihzP-4qEwh6dzRN78P7pmZ9verAd8fD7y7jk2wvNvD8-HpXQJ-FwIDAQAB";
	public static final String CONTENT_SERVER_PUB_DER = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAttJS-pGc_d4BMu_3sGWVMNFPJynT6fcO1xAOSpnM6tuMJFKipCErYV7w0zZrr0iEJOkw5R8lf9Y3U4ikMvzlR4kqVBtkiFuDkhP1wYrxibEkfvx3SzrkAVVbuUNzIpIvhe-HkVh7LfLS5nT3AEbe3sajY9RxFA44lvOXKDfhU0Ff3wWXyKF81L49WXgWms22PVFcFbAxuvlMGjTCNLgoGlJZff94WyQ2zT6mLPJM6WJUwkdSTCNsfQGoWu2RTOUR-lVC61z8k50e7DmSRxCnJ-daJ3M7vHcRoaTbuh5mOf0IK997thn7jNQIfaPjbtZMhpjTPzvbRnvewuytlRK4QQIDAQAB";
	public static final String CONTENT_CLIENT_PRIV_PEM = "QmFnIEF0dHJpYnV0ZXMKICAgIGZyaWVuZGx5TmFtZTogY2xpZW50CiAgICBsb2NhbEtleUlEOiA1NCA2OSA2RCA2NSAyMCAzMSAzNiAzMiAzMiAzMSAzOSAzOCAzNyAzNSAzMCAzMiAzNyAzMCAKS2V5IEF0dHJpYnV0ZXM6IDxObyBBdHRyaWJ1dGVzPgotLS0tLUJFR0lOIFBSSVZBVEUgS0VZLS0tLS0KTUlJRXZnSUJBREFOQmdrcWhraUc5dzBCQVFFRkFBU0NCS2d3Z2dTa0FnRUFBb0lCQVFDbTN5Y3ZIelo3NXBBeAo0S3B6eUJuR0JEbDZnbFYvVFF3Q2Nnemc4eWNqdUIwY3RmWHl6cExoNVFYR3VtZmFGZ0xoUy9kTW5rQWVDTjhlCkFSUEpOSlM1bllOVEpSbVJNOFF2WWFFWVE2NXJoMFBjampyMGRNYlQyb0FwdGIvU3NtemxwZFdQM1FxYVhyWVMKL3dGZ2xhZzRuU1VUaTRoc2VpQnptTEJVMzF5Nk5LVDlWQzdjeGRxQVo0ZUVSbHBWSThVbHFDTjVSS2psYk5TOQpBd2RZVkx4SFZWWm1XOExsMi9VeW1NWGRocjVDTXBqNHArQmFHN1djQlpkQm1LbnVtN25FZ21Xa2M0Zy9kdXZUCjdkTkY4YzRMbU4ybFNUQkN0RjMzT0tITS83aW9UQ0hwM05FM3Z3L3VtWm4yOTZzQjN4OFB2THVPVGJDODI4UHoKNGVsZEFuNFhBZ01CQUFFQ2dnRUFYdGpEcXRMdzR1ZWYyZG02eUdoWnplVWt6bTg2TFVzYm9tQTAxYjhlNWZ5RgovNFdsTDhjZVVXc2RKMkZZR2VzYzQvV0xzRzdhUHJnSUtVT3ZmVk4zblREY0xld09aaXVvUE00ZkJHMmp1OVo4ClNlWkZ1Q0NQeW1hTkppZjV5Y1pFZ3hzSkVlWXZXcUt1bG9sK3ZPa1RYbXBqWW9LU25pV2tRRS92R2E2RFJBb3QKN3RVZXVtcEp2MUVPOUQ3ZXFsNUhYd3BCU0gyYWxCVGlNeGsxWjFHWEZwdWFYdndwZ3FVZ0M2ZDlJb3UvTkZLQQo1dFl0ZmRqRkNGSnVhQnVzOE1YL2xQRzFOcW5zWUkrMEMzcWFUeXlFcWlpVk9WTlJKenRQSWNjTDVpVWRkeSt1CnB0YlAvcnVCWGsxRTNYblV4YWhLNTZEUGJIMFQ2bVBFNFNZL2QxUlVJUUtCZ1FEMEV4RTZnK0FZc0tSdS9MZXkKYUdBUTR1M2JJN0Z0NitMTEVhdFRJUDQ3Vm9aYjJDdUd3MTNHRm00b25JUm4xbzdKVTFabndSdmZhWFRqakFvYQpBZTFvdzc3cXRreXk1UGhvRTdXbE5zMll1OWd5MHA3dVFMQS9jellXVmgrVU9qTWJ2Q2owZW5nQlZWb3dOYmpPCjNVaDBXRm14NEw4Ni9CWFVGVTFtaERNU1R3S0JnUUN2Qm1yeERUbDJlbVo2K2NyNThrR0hBTTh6UG85WTBUY1YKd0ZKTWVySDF1dk56Zmt1cW5IbWhNNTVhN3lwZzNiMGwvd1l6am5nTFRQSU5kY3NkaGFmc0xWdHVBZWk5Z0sxYQpjKzR4TlZHN2tWWERYejZ2dXV5SHhHUGdCMmtnbDlqWm1acXdndDFwbXZtbVBjaXQwMjVTVHl5clV0WVZEQTU1ClpUREpnei9OdVFLQmdRQ1ZaYkp2QWRmQzVTVFJkc2t1WUNzbFN2SlM1NmNzWkcyKzVRTGNjRjZOamFuU1FDQnMKYmR0UmE5dGo2bUkxZVNTUFlQNkxwYTFjOUIwRFIyM0xlNUNKUnYvdWhVV3ZYdTRhTHE1S0FhQ1pNNm5qZWY2awpVVjVRaUVIOExCMUtTdEdMMFlHMEc5Nm0ya3JKSmFrSW5uUkNHdTVVTGdCL3AxdnBKRnpyT2xKVWl3S0JnUUNtCjNSWjl4QjdobnFZdlhoQ3VwTkRtTmRaVXc3TUVlVW5zR2RRY2grazhIa1ZWK2JXSDdmQmp5SU9UckdxWnVTMUkKbVU4L1BmZWl4blFLY3gvM2dHSnMzMzFJYnRlR083U0tCUGEwd1dHdjBrcVNuaTUwZVdCaHU5R0FWM0JabTRzcApRYkZoMFJIb1NkRHpOZm9xQnVZcDRNUDBmbUFONXRXeDFOQmpmaGNKT1FLQmdEUzhHb2h6WVVlWkFST1hzbUpPCnBRTzBTcDluL0x4eUd3OEVjdjBWcEx2aFBDMUt1ZEVaS29jZkRvR1BXd2laRGlSRTVkSFdINDdyN1dNc3g1bUEKZVpJV0tSSElnZmsyYktzWk5nQnB0Y2dUM1pUdXlNUCtDWVBMTDF6a3hFbnVzbFl2ejVwM0VtSTF0cEpGd1lWdQpaSlRuQUhJMjhFWkhMNXdlUlhaZ0VWbDEKLS0tLS1FTkQgUFJJVkFURSBLRVktLS0tLQo=";
	public static final String CONTENT_SERVER_PRIV_PEM = "QmFnIEF0dHJpYnV0ZXMKICAgIGZyaWVuZGx5TmFtZTogc2VydmVyCiAgICBsb2NhbEtleUlEOiA1NCA2OSA2RCA2NSAyMCAzMSAzNiAzMiAzMiAzMSAzOSAzNSAzMyAzMyAzMSAzNSAzMyAzNiAKS2V5IEF0dHJpYnV0ZXM6IDxObyBBdHRyaWJ1dGVzPgotLS0tLUJFR0lOIFBSSVZBVEUgS0VZLS0tLS0KTUlJRXZnSUJBREFOQmdrcWhraUc5dzBCQVFFRkFBU0NCS2d3Z2dTa0FnRUFBb0lCQVFDMjBsTDZrWno5M2dFeQo3L2V3WlpVdzBVOG5LZFBwOXc3WEVBNUttY3pxMjR3a1VxS2tJU3RoWHZEVE5tdXZTSVFrNlREbEh5Vi8xamRUCmlLUXkvT1ZIaVNwVUcyU0lXNE9TRS9YQml2R0pzU1IrL0hkTE91UUJWVnU1UTNNaWtpK0Y3NGVSV0hzdDh0TG0KZFBjQVJ0N2V4cU5qMUhFVURqaVc4NWNvTitGVFFWL2ZCWmZJb1h6VXZqMVplQmFhemJZOVVWd1ZzREc2K1V3YQpOTUkwdUNnYVVsbDkvM2hiSkRiTlBxWXM4a3pwWWxUQ1IxSk1JMng5QWFoYTdaRk01Ukg2VlVMclhQeVRuUjdzCk9aSkhFS2NuNTFvbmN6dThkeEdocE51NkhtWTUvUWdyMzN1MkdmdU0xQWg5bytOdTFreUdtTk0vTzl0R2U5N0MKN0syVkVyaEJBZ01CQUFFQ2dnRUFZNHFDMmFFYzdIRExFRlB0OWFKcHA4bmZJZ0M1UEVOalZoK00ySHVEUWtERgp2aVVzSHRkY2lraXFNU1lKWGNmTEExbmdZSEFqOThYSUcvaGpCc3dCZm9DbGhtUGZ5Z3FoeVpDS2w1V3lTM2tpCjJPMVhlcU9XNzA3dGdTTERkb2hIemRJTWEwdlV6Y0RQcWVEM2Q3UWV0d2RLZ1JsemRDc0dEdEtVYzdaeFBlV0cKcFJBTW9uLzhPN2I5M3ZIV0F6Wm5rZExSVytrRjhvYmZTc2tUYmNYZTIrUjMrRE5qTk1sY3RiSnJSRHVhUE1JNgpHNDRoMXNUb2dXampkVXNyaDR2cjN5Rmg5MUJCaHBEbGMzWmhKR0s2a05ibjFtUlVIaWV0VkRQK0VZZjZ5YXBwCmFhQVFtKzZPdzBnd2tPd1hxMU1EZ0w3bWtCM2UxMGZUZXRJT0lnQktrUUtCZ1FEdmE0OExsd2hMODZZTFkzOSsKSU15VHV0YWYvUE0rclZlTjM3TW5tTzRQZlhUYStMK2VnbDlkTDhWMmhIeEo3bTVLVWZkZDgvS0lMbWhEOG80dwo4RmlkZ3FuZGJzV1pWMzJ5YVhxaTl2RHVjYWhGRmZMcEJDQjVrNTI4QmxOSDZuNWUyKy9qRUtzV0JIQjZRTW1lClhIR0xLNTNOSm56aFFaTHhQSnVuZ21BM0JRS0JnUUREZTJLOEEzcEhZeHpLS0JRUTlnOGwwcFBad2MvTUczY1MKWFNRZFFySndJRnBvSEVQNi9aNkIwcThqRllMVUpyRUtoT3BIRmhNWVZTa3NTZ1ZWVHRld1RQV01sTi90a0VKOQozTno5b0pzZ2NxN0xuNElET2J3eFZaRkc3MG9ZQWNLTkV2R2pwczg0Z3Y2QnhKdGNoMTJLQ1VVTmxFNDdxYTFJClZRWjd0Wm5KRFFLQmdRRFFsa3ZNMUN1MVNEUHNqaVlBTkFFbjM4cW5IbENwMVltSElGTE1kODlJRFl3bVRqdGIKbE1nU3Z0RHhYYUdQSTd4UTRiSjYxMU1BMURXZ3BReStsRmNQKzB1VWtMSjAweVcrcjJqWjIveXlNTEZpWnluMwpXdElVT2NoZGpNRTMwWk9CZjJveTBFM040OVkrbkgxTGk4eWNiRWFSK2lzb2NPSGRiR2xMK2lsckpRS0JnRFJoClpwYU10QUpXKzRycjdGeVRJb0gzQ0Nrc2R0cnhiUm1kbmFTOWo0VGVGbnVaUDFvTkJhRXg3RDRSY0lvYWlBd1MKaTVoYXdPa2ZRTFllYTRsdFkveFkwdDlGc1M4K0hhTU9RS1V4bVArNzJ6eXkxQ3E3R01ON2N6ODN0WG56VnNkbwprUmxTQkdyWEp3MXN1bGl6NlF1bzZqajJTWFJSUmg2QXNna0tJMWd0QW9HQkFJNVVXMFQ5N0Y5OHF5cVFWbEFLCmtoajR0OFMrbkpqMDNpbXRnK3VYM2ZiZ1RZbWNlSzBRQ21nUzF3bC9odnh4eFlaUzFjM3RNYnc5SzFxckw2ek4KaTZuVE9oMTdDanVZdmJSQUxqSklYY09BM1RLTXl5NnpXb291cU1IaUhUTGZjbStlbXBTcnp2QUN4VVV4Q0FZRwpBeEhmV2I2R2JhZ3l5aHdsU3hNOEdIZ0oKLS0tLS1FTkQgUFJJVkFURSBLRVktLS0tLQo=";
	public static final String TEST_PASSWORD = "changeit";
	public static final String TEST_LOCALHOST_JKS_FILENAME = TEST_DIR + File.separator + "localhost.jks";
	public static final String TEST_ENCRYPTED_ARTIFACT_FILENAME = TEST_DIR + File.separator
			+ "encrypted-artifact--6f437177-6e48-5cf8-9d9e-872a2bddd641.json";
	public static final String TEST_ARTIFACT_FILENAME = TEST_DIR + File.separator
			+ "artifact--6f437177-6e48-5cf8-9d9e-872a2bddd641.json";
	public static final String SECRET_KEY = "LP-MDHeBcFphSGJ-bDn9hPTs7ZEJhXjcXfl2yijiPo0=";
	public static final String IV = "5DXrwdzDLdwWWretX3SRgFq8-6FZioCg7k30rWybEkZc6TJ5Hwp1rV1VcqzJE7KIh3u_mbrhSf98h2HqmhzjVtenhinCe4toFAXkQOKQUsc9ISbktCBtk1LXMN6l9CrsEJll46dBsFNOPZxa7mUAkdiqNPCKNBXxSbL7k_rOb34=";
	public static final String ALG_AES = "AES";
	public static final String AES_GCM_NOPADDING = "AES/GCM/NoPadding";

	/* @formatter:off */
	public static final String TEST_ARTIFACT = "{\n"
			+ "  \"type\": \"artifact\",\n"
			+ "  \"spec_version\": \"2.1\",\n"
			+ "  \"id\": \"artifact--6f437177-6e48-5cf8-9d9e-872a2bddd641\",\n"
			+ "  \"mime_type\": \"application/zip\",\n"
			+ "  \"payload_bin\": \"ZX7HIBWPQA99NSUhEUgAAADI== ...\",\n"
			+ "  \"encryption_algorithm\": \"mime-type-indicated\",\n"
			+ "  \"decryption_key\": \"My voice is my passport\"\n"
			+ "}";
	
	public static final String ENCRYPTED_ARTIFACT = "{\n"
			+ "  \"id\" : \"artifact--6f437177-6e48-5cf8-9d9e-872a2bddd641\",\n"
			+ "  \"encrypted\" : \"7APPmg2YLM_8fLcaxnBWJo4oBZ8GfZYm9uPpBwF4hrztmbeJoydrlyFotmf28cGIWW9ZMxodSYefS04ryElwRED657TFyg4KNM4n9X31-jaWRlUqWw8brVbAnWdn79Ux-nIlShA71RLHdizXtrx_2Hy4Yt6fk89SkmMjXZVh4Oi4QgwEeFcLmeFxJ5HrogcAgHCoGZpyE9ptFNH7vlQ200tQZvu7j4i88Aj3UjAYsrmCeTCBZJbWb9lDx_wwJPsgjde13X5vwktNUarwe5eDkFc6jimTSIB2Kxny4fuicTZackPspR611CbIR0KRlZBSN9cPKqmAH9aoSwtmGiKQUM5tTnKD6_NAzWj465tA-zEK5TYrX4MbOg77Wm35XAZmoWOT4AbzDledNz7nlYL41A==\"\n"
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
	
	public static final String PAYLOAD_JSON = "{\n"
			+ "  \"encrypted_key\" : \"SdmQt4iEv02xC+xARCNL3iRCYMOH6/7w6KfYyyFuZiwMOHw1nTX+wZDaXAbgqbSrFJE473ygqxaHmw9y5hny+rTYs3DnJs6vWHQpaNfBeUMA0LgLHpnqJ1+4goQCiUh8Box7FF/bgzL1c3bXZBdlgcVgZnyFKviBoE9BuR0twSGAZdpHVCcMNXxmo6HnNUcGHHfAI1w8zO7waiQ/ApAmQJLmwS82TGdltU/CFInwKigrLJM2lCKrP5oJmTBOYWVkS6pDlC0hSbwwSD/wnH+CIYEXIJAz/HqjeLX6jD3uJrB+O6UGRHO/f7Zz8WGPl8bUWqbBRkvEYvLmqMypSo2k3A==\",\n"
			+ "  \"id\" : \"artifact--6f437177-6e48-5cf8-9d9e-872a2bddd641\",\n"
			+ "  \"iv\" : \"Wfw3QKCzY3OGFM817tvB/Q==\",\n"
			+ "  \"encrypted\" : \"6LVtUlYqjicImuWvLpFHBthVHk6BBBjlOHTfWaWxRFOyAGxzaOt/5efKOXGEW1wiFFCr5ppDlN042HELt5NEldVN8OE4RW27RzsFTbBu9H67MySoNC3gOLwH+oWC3Vg5jzuEHaohQmKWpY40z8KnXLOZFPpfuxJpAQLvzuQYj3+qxUFjRz7xmzNnq6uJDieov51Fpz1d2nr9xgz7Zq6Xq+Lx3VOjY2BCo6UhFize0f+k+rq9wr2eUfWoLakoG8WuaBsUcGmbs9nCYZAiGVbRRvpda6UHcq/XBY0vC/yV0pLLOL9p1i2XpiqIqhke+JF8x4ahbSRVFpgQXQjAHkPtUpw8Q77sAx9cnhZhEOdGevAW9AT8d5jD9XARglz5kTfG76cCEAmC/icuxJlMhQNlkw==\",\n"
			+ "  \"signature\" : \"kxHLFOo83RW94ALiEDFouC9e/EKGORRWXOvxjoM3fvaXg/VihXxWwaFcqju9UzZscGA+zE0DExbJAwu992MLKWtYO1e4mtWAQQgZPqIW3kJi/yWq9TtV+TiOvQWofo96aUdVST9NuCeXsb5WQXdbHcCf1bA1FzoO3xJKsY0+VaV/tSmVy0CE07eEJeXIyWSCxWJw0tTCN0+YYbwvxWHfi06AJF9qR6FoLxte1DZhtMZsiJdid0Ywa1ZrwbiRjkYV1e69I0tcUZGAWFXycHUo9WKe+KGgaV9GXkshlmpCjuWhd5t45PYNfqAbUJtEW//XQABdyvhBipAuC46Hx9EnLQ==\",\n"
			+ "  \"length\" : 288\n"
			+ "}";
	
	public static final String RESP_OK = "{\"status_code\":200,\"reason\":\"OK\"}";
	/* @formatter:on */

	private static Vertx vertx = Vertx.vertx();

	@BeforeAll
	public static void setUp() throws IOException {
		System.out.println("@BeforeAll ...");
		System.out.println(PAYLOAD_JSON);
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
		fs.writeFileBlocking(TEST_ARTIFACT_FILENAME, Buffer.buffer(TEST_ARTIFACT));
		File curDir = new File(".");
		logger.info("curDir: " + curDir.getAbsolutePath());

//		byte[] secret = Base64.getUrlDecoder().decode("LP+MDHeBcFphSGJ+bDn9hPTs7ZEJhXjcXfl2yijiPo0=");
//		logger.info("base64: {}", secret);
//		logger.info("base64url: {}", Base64.getUrlEncoder().encodeToString(secret));
//
//		byte[] iv = Base64.getUrlDecoder().decode(
//				"5DXrwdzDLdwWWretX3SRgFq8+6FZioCg7k30rWybEkZc6TJ5Hwp1rV1VcqzJE7KIh3u/mbrhSf98h2HqmhzjVtenhinCe4toFAXkQOKQUsc9ISbktCBtk1LXMN6l9CrsEJll46dBsFNOPZxa7mUAkdiqNPCKNBXxSbL7k/rOb34=");
//		logger.info("iv base64: {}", iv);
//		logger.info("iv bas64url: {}", Base64.getUrlEncoder().encodeToString(iv));

		/*** In the beginning only ***/
//		loadBinFile(Vertx.vertx(), "/home/student/certs/localhost.jks");
//		loadBinFile(Vertx.vertx(), "/home/student/certs/client-pub-key.der");
//		loadBinFile(Vertx.vertx(), "/home/student/certs/server-pub-key.der");
//		loadBinFile(Vertx.vertx(), "/home/student/certs/client-priv-key.pem");
//		loadBinFile(Vertx.vertx(), "/home/student/certs/server-priv-key.pem");
		/*** In the beginning only ends ***/

		dumpBinFile(vertx, configJson.getString("keystore"), TestingUtil.CONTENT_LOCALHOST_JKS);
		dumpBinFile(vertx, configJson.getString("truststore"), TestingUtil.CONTENT_LOCALHOST_JKS);
		dumpBinFile(vertx, configJson.getJsonObject("certs").getString("client-public-key"),
				TestingUtil.CONTENT_CLIENT_PUB_DER);
		dumpBinFile(vertx, configJson.getJsonObject("certs").getString("client-private-key"),
				TestingUtil.CONTENT_CLIENT_PRIV_PEM);
		dumpBinFile(vertx, configJson.getJsonObject("certs").getString("server-public-key"),
				TestingUtil.CONTENT_SERVER_PUB_DER);
		dumpBinFile(vertx, configJson.getJsonObject("certs").getString("server-private-key"),
				TestingUtil.CONTENT_SERVER_PRIV_PEM);

		List<String> dirs = Arrays.asList(TEST_DIR, CERTS_DIR);

		dirs.forEach(dir -> {
			List<String> files = vertx.fileSystem().readDirBlocking(dir);
			logger.info("dir: {}", dir);
			files.forEach(x -> {
				logger.info("file: {}", x);
			});
		});

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
		Buffer content = Buffer.buffer(Base64.getUrlEncoder().encodeToString(buf.getBytes()));
		logger.info("-->" + content.toString() + "<--");
		return content;
	}

	public static void dumpBinFile(Vertx vertx, String filename, String b64Content) {
		vertx.fileSystem().writeFileBlocking(filename, Buffer.buffer(Base64.getUrlDecoder().decode(b64Content)));
	}

}
