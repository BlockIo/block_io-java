package lib;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.*;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

class ClientMockTest {
    private String api_key;
    private WireMockServer wireMockServer;

    private JSONObject signAndFinalizeWithdraw;
    private JSONObject signAndFinalizeSweep;
    private JSONObject signAndFinalizeDtrust;

    private Map<String, Object> withdrawRequestBodyContent;
    private Map<String, Object> dTrustRequestBodyContent;
    private Map<String, Object> sweepRequestBodyContent;

    private BlockIo blockIo;

    @BeforeEach
    void setup() throws IOException, ParseException {
        api_key = "0000-0000-0000-0000";
        this.wireMockServer = new WireMockServer(8080);
        this.wireMockServer.start();

        signAndFinalizeWithdraw = new JSONObject();
        signAndFinalizeSweep= new JSONObject();
        signAndFinalizeDtrust= new JSONObject();

        withdrawRequestBodyContent= new JSONObject();
        dTrustRequestBodyContent= new JSONObject();
        sweepRequestBodyContent= new JSONObject();

        withdrawRequestBodyContent.put("from_labels", "testdest");
        withdrawRequestBodyContent.put("amounts", "100");
        withdrawRequestBodyContent.put("to_labels", "default");

        dTrustRequestBodyContent.put("to_addresses", "QhSWVppS12Fqv6dh3rAyoB18jXh5mB1hoC");
        dTrustRequestBodyContent.put("from_address", "tltc1q8y9naxlsw7xay4jesqshnpeuc0ap8fg9ejm2j2memwq4ng87dk3s88nr5j");
        dTrustRequestBodyContent.put("amounts", 0.09);

        sweepRequestBodyContent.put("to_address", "QhSWVppS12Fqv6dh3rAyoB18jXh5mB1hoC");
        sweepRequestBodyContent.put("from_address", "tltc1qpygwklc39wl9p0wvlm0p6x42sh9259xdjl059s");
        sweepRequestBodyContent.put("private_key", "cTYLVcC17cYYoRjaBu15rEcD5WuDyowAw562q2F1ihcaomRJENu5");

        setupWithdrawStub();
        readSignAndFinalizeWithdrawRequestJson();

        setupSweepStub();
        readSignAndFinalizeSweepRequestJson();

        setupDtrustStub();
        readSignAndFinalizeDtrustRequestJson();
    }
    @AfterEach
    void stopWireMockServer() {
        this.wireMockServer.stop();
    }
    void readSignAndFinalizeWithdrawRequestJson() throws IOException, ParseException {
        JSONParser parser = new JSONParser();
        signAndFinalizeWithdraw = (JSONObject) parser.parse(
                new FileReader("src/test/resources/__files/json/sign_and_finalize_withdrawal_request.json")
        );
    }
    void readSignAndFinalizeSweepRequestJson() throws IOException, ParseException {
        JSONParser parser = new JSONParser();
        signAndFinalizeSweep = (JSONObject) parser.parse(
                new FileReader("src/test/resources/__files/json/sign_and_finalize_sweep_request.json")
        );
    }
    void readSignAndFinalizeDtrustRequestJson() throws IOException, ParseException {
        JSONParser parser = new JSONParser();
        signAndFinalizeDtrust = (JSONObject) parser.parse(
                new FileReader("src/test/resources/__files/json/sign_and_finalize_dtrust_withdrawal_request.json")
        );
    }

    public void setupWithdrawStub(){
        wireMockServer.stubFor(post(urlPathEqualTo("/api/v2/withdraw"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withHeader("Accept", "application/json")
                        .withBodyFile("json/withdraw_response.json")));

        wireMockServer.stubFor(post(urlPathEqualTo("/api/v2/sign_and_finalize_withdrawal"))
                .withHeader("Accept", containing("application/json"))
                .withRequestBody(equalToJson(signAndFinalizeWithdraw.toJSONString(), true, true))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withHeader("Accept", "application/json")
                        .withBodyFile("json/success_response.json")));
    }
    public void setupSweepStub(){
        wireMockServer.stubFor(post(urlPathEqualTo("/api/v2/sweep_from_address"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withHeader("Accept", "application/json")
                        .withBodyFile("json/sweep_from_address_response.json")));

        wireMockServer.stubFor(post(urlPathEqualTo("/api/v2/sign_and_finalize_sweep"))
                .withHeader("Accept", containing("application/json"))
                .withRequestBody(equalToJson(signAndFinalizeSweep.toJSONString(), true, true))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withHeader("Accept", "application/json")
                        .withBodyFile("json/success_response.json")));
    }
    public void setupDtrustStub(){
        wireMockServer.stubFor(post(urlPathEqualTo("/api/v2/withdraw_from_dtrust_address"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withHeader("Accept", "application/json")
                        .withBodyFile("json/withdraw_from_dtrust_address_response.json")));

        wireMockServer.stubFor(post(urlPathEqualTo("/api/v2/sign_and_finalize_withdrawal"))
                .withHeader("Accept", containing("application/json"))
                .withRequestBody(equalToJson(signAndFinalizeDtrust.toJSONString(), true, true))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withHeader("Accept", "application/json")
                        .withBodyFile("json/success_response.json")));
    }

    /*
    @Test
    void Withdraw() throws Exception {
        String pin = "blockiotestpininsecure";

        blockIo = new BlockIo(api_key, pin, 2, new Options("http://localhost:8080"));
        Map<String, Object> response = blockIo.Withdraw(withdrawRequestBodyContent);
        assertNotNull(response.get("txid"));
    }
    @Test
    void Sweep() throws Exception {
        blockIo = new BlockIo(api_key, null, 2, new Options("http://localhost:8080"));
        Map<String, Object> response = blockIo.SweepFromAddress(sweepRequestBodyContent);
        assertNotNull(response.get("txid"));
    }
    @Test
    void Dtrust() throws Exception {
        String pin = "blockiotestpininsecure";
        blockIo = new BlockIo(api_key, pin, 2, new Options("http://localhost:8080"));
        Map<String, Object> response = blockIo.WithdrawFromDtrustAddress(dTrustRequestBodyContent);
        assertNotNull(response);
    }

     */
}