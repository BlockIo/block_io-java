package lib;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.google.gson.Gson;
import lib.BlockIo;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

class ClientMock {
    private String api_key;
    private WireMockServer wireMockServer;

    private JSONObject signAndFinalizeWithdraw;
    private JSONObject signAndFinalizeSweep;
    private JSONObject signAndFinalizeDtrust;

    private JSONObject withdrawRequestBodyContent;
    private JSONObject dTrustRequestBodyContent;
    private JSONObject sweepRequestBodyContent;

    private BlockIo blockIo;

    @BeforeEach
    void setup() {
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
        setupSweepStub();
        setupDtrustStub();
    }
    @AfterEach
    void stopWireMockServer() {
        this.wireMockServer.stop();
    }
    public void readSignAndFinalizeWithdrawRequestJson() throws FileNotFoundException, ParseException {
        JSONParser jsonParser = new JSONParser();
        try (FileReader reader = new FileReader("json/sign_and_finalize_withdrawal_request.json"))
        {
            //Read JSON file
            Object obj = jsonParser.parse(reader);

            signAndFinalizeWithdraw = (JSONObject) obj;
            System.out.println(signAndFinalizeWithdraw);

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }
    public void readSignAndFinalizeSweepRequestJson() throws FileNotFoundException, ParseException {
        JSONParser jsonParser = new JSONParser();
        try (FileReader reader = new FileReader("json/sign_and_finalize_sweep_request.json"))
        {
            //Read JSON file
            Object obj = jsonParser.parse(reader);

            signAndFinalizeSweep = (JSONObject) obj;
            System.out.println(signAndFinalizeSweep);

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }
    public void readSignAndFinalizeDtrustRequestJson() throws FileNotFoundException, ParseException {
        JSONParser jsonParser = new JSONParser();
        try (FileReader reader = new FileReader("json/sign_and_finalize_dtrust_withdrawal_request.json"))
        {
            //Read JSON file
            Object obj = jsonParser.parse(reader);

            signAndFinalizeDtrust = (JSONObject) obj;
            System.out.println(signAndFinalizeDtrust);

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    public void setupWithdrawStub(){
        wireMockServer.stubFor(post(urlEqualTo("/api/v2/withdraw"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("json/withdraw_response.json")));

        wireMockServer.stubFor(post(urlEqualTo("/api/v2/sign_and_finalize_withdrawal"))
                .withRequestBody(equalToJson(new Gson().toJson(signAndFinalizeWithdraw)))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("json/success_response.json")));
    }
    public void setupSweepStub(){
        wireMockServer.stubFor(post(urlEqualTo("/api/v2/sweep_from_address"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("json/sweep_from_address_response.json")));

        wireMockServer.stubFor(post(urlEqualTo("/api/v2/sign_and_finalize_sweep"))
                .withRequestBody(equalToJson(new Gson().toJson(signAndFinalizeSweep)))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("json/success_response.json")));
    }
    public void setupDtrustStub(){
        wireMockServer.stubFor(post(urlEqualTo("/api/v2/withdraw_from_dtrust_address"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("json/withdraw_from_dtrust_address_response.json")));

        wireMockServer.stubFor(post(urlEqualTo("/api/v2/sign_and_finalize_withdrawal"))
                .withRequestBody(equalToJson(new Gson().toJson(signAndFinalizeDtrust)))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("json/success_response.json")));
    }

    @Test
    void Withdraw() throws Exception {
        String pin = "blockiotestpininsecure";
        blockIo = new BlockIo(api_key, pin, 2, "{\"api_url\": \"http://localhost:8080\"}");
//        System.out.println(new Gson().toJson(withdrawRequestBodyContent));
        Map<String, Object> response = blockIo.Withdraw(new Gson().toJson(withdrawRequestBodyContent));
        assertEquals("success", response.get("status"));
        assertNotNull(response.get("data"));
    }
    @Test
    void Sweep() throws Exception {
        blockIo = new BlockIo(api_key, null, 2, "{\"api_url\": \"http://localhost:8080\"}");
        Map<String, Object> response = blockIo.SweepFromAddress(new Gson().toJson(sweepRequestBodyContent));
        assertEquals("success", response.get("status"));
        assertNotNull(response.get("data"));
    }
    @Test
    void Dtrust() throws Exception {
        String pin = "blockiotestpininsecure";
        blockIo = new BlockIo(api_key, pin, 2, "{\"api_url\": \"http://localhost:8080\"}");
        Map<String, Object> response = blockIo.WithdrawFromDtrustAddress(new Gson().toJson(dTrustRequestBodyContent));
        assertEquals("success", response.get("status"));
        assertNotNull(response.get("data"));
    }
}