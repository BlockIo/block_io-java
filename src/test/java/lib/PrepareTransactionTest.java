package lib;

import org.bitcoinj.core.ECKey;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileReader;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PrepareTransactionTest {
    private String pin;
    private BlockIo blockIo;
    private String[] sweepKeys;
    private String[] dtrustKeys;

    @BeforeEach
    void setUp() throws Exception {
        pin = "d1650160bd8d2bb32bebd139d0063eb6063ffa2f9e4501ad";
        blockIo = new BlockIo("", pin);
        ECKey sweepKeyFromWif = Key.fromWif("cTj8Ydq9LhZgttMpxb7YjYSqsZ2ZfmyzVprQgjEzAzQ28frQi4ML");
        sweepKeys = new String[]{sweepKeyFromWif.getPrivateKeyAsHex()};
        dtrustKeys = new String[]{
            "b515fd806a662e061b488e78e5d0c2ff46df80083a79818e166300666385c0a2",
            "1584b821c62ecdc554e185222591720d6fe651ed1b820d83f92cdc45c5e21f",
            "2f9090b8aa4ddb32c3b0b8371db1b50e19084c720c30db1d6bb9fcd3a0f78e61",
            "6c1cefdfd9187b36b36c3698c1362642083dcc1941dc76d751481d3aa29ca65"
        };
    }

    @Test
    void testCreateAndSignTransaction() throws Exception {
        JSONParser parser = new JSONParser();
        JSONObject prepareTransactionResponse = (JSONObject) parser.parse(
                new FileReader("src/test/resources/__files/json/prepare_transaction_response.json")
        );;
        JSONObject createAndSignTransactionResponse = (JSONObject) parser.parse(
                new FileReader("src/test/resources/__files/json/create_and_sign_transaction_response_non_lowR.json")
        );
        JSONObject response = blockIo.createAndSignTransaction(prepareTransactionResponse);
        assertEquals(response.toJSONString(), createAndSignTransactionResponse.toJSONString());
    }

    @Test
    void testSweepP2WPKH() throws Exception {
        JSONParser parser = new JSONParser();
        JSONObject prepareTransactionResponse = (JSONObject) parser.parse(
                new FileReader("src/test/resources/__files/json/prepare_sweep_transaction_response_p2wpkh.json")
        );;
        JSONObject createAndSignTransactionResponse = (JSONObject) parser.parse(
                new FileReader("src/test/resources/__files/json/create_and_sign_transaction_response_sweep_p2wpkh_non_lowR.json")
        );
        JSONObject response = blockIo.createAndSignTransaction(prepareTransactionResponse, sweepKeys);
        assertEquals(response.toJSONString(), createAndSignTransactionResponse.toJSONString());
    }

    @Test
    void testSweepP2WPKHOverP2SH() throws Exception {
        JSONParser parser = new JSONParser();
        JSONObject prepareTransactionResponse = (JSONObject) parser.parse(
                new FileReader("src/test/resources/__files/json/prepare_sweep_transaction_response_p2wpkh_over_p2sh.json")
        );;
        JSONObject createAndSignTransactionResponse = (JSONObject) parser.parse(
                new FileReader("src/test/resources/__files/json/create_and_sign_transaction_response_sweep_p2wpkh_over_p2sh_non_lowR.json")
        );
        JSONObject response = blockIo.createAndSignTransaction(prepareTransactionResponse, sweepKeys);
        assertEquals(response.toJSONString(), createAndSignTransactionResponse.toJSONString());
    }

    @Test
    void testSweepP2PKH() throws Exception {
        JSONParser parser = new JSONParser();
        JSONObject prepareTransactionResponse = (JSONObject) parser.parse(
                new FileReader("src/test/resources/__files/json/prepare_sweep_transaction_response_p2pkh.json")
        );;
        JSONObject createAndSignTransactionResponse = (JSONObject) parser.parse(
                new FileReader("src/test/resources/__files/json/create_and_sign_transaction_response_sweep_p2pkh_non_lowR.json")
        );
        JSONObject response = blockIo.createAndSignTransaction(prepareTransactionResponse, sweepKeys);
        assertEquals(response.toJSONString(), createAndSignTransactionResponse.toJSONString());
    }

    @Test
    void testDTrustWitnessV04of5Keys() throws Exception {
        JSONParser parser = new JSONParser();
        JSONObject prepareTransactionResponse = (JSONObject) parser.parse(
                new FileReader("src/test/resources/__files/json/prepare_dtrust_transaction_response_witness_v0.json")
        );;
        JSONObject createAndSignTransactionResponse = (JSONObject) parser.parse(
                new FileReader("src/test/resources/__files/json/create_and_sign_transaction_response_dtrust_witness_v0_4_of_5_keys.json")
        );
        JSONObject response = blockIo.createAndSignTransaction(prepareTransactionResponse, dtrustKeys);
        assertEquals(response.toJSONString(), createAndSignTransactionResponse.toJSONString());
    }
}
