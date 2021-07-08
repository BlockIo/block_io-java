package lib;

import lib.blockIo.BlockIo;
import lib.blockIo.Key;
import org.bitcoinj.core.ECKey;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileReader;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

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
        JSONObject response = blockIo.CreateAndSignTransaction(prepareTransactionResponse);
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
        JSONObject response = blockIo.CreateAndSignTransaction(prepareTransactionResponse, sweepKeys);
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
        JSONObject response = blockIo.CreateAndSignTransaction(prepareTransactionResponse, sweepKeys);
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
        JSONObject response = blockIo.CreateAndSignTransaction(prepareTransactionResponse, sweepKeys);
        assertEquals(response.toJSONString(), createAndSignTransactionResponse.toJSONString());
    }

    @Test
    void testDTrustWitnessV04of5Keys() throws Exception {
        JSONParser parser = new JSONParser();
        JSONObject prepareTransactionResponse = (JSONObject) parser.parse(
                new FileReader("src/test/resources/__files/json/prepare_dtrust_transaction_response_witness_v0.json")
        );;
        JSONObject createAndSignTransactionResponse = (JSONObject) parser.parse(
                new FileReader("src/test/resources/__files/json/create_and_sign_transaction_response_dtrust_witness_v0_4_of_5_keys_non_lowR.json")
        );
        JSONObject response = blockIo.CreateAndSignTransaction(prepareTransactionResponse, dtrustKeys);
        assertEquals(response.toJSONString(), createAndSignTransactionResponse.toJSONString());
    }

    @Test
    void testDTrustWitnessV03of5Keys() throws Exception {
        JSONParser parser = new JSONParser();
        JSONObject prepareTransactionResponse = (JSONObject) parser.parse(
                new FileReader("src/test/resources/__files/json/prepare_dtrust_transaction_response_witness_v0.json")
        );;
        JSONObject createAndSignTransactionResponse = (JSONObject) parser.parse(
                new FileReader("src/test/resources/__files/json/create_and_sign_transaction_response_dtrust_witness_v0_3_of_5_keys_non_lowR.json")
        );
        JSONObject response = blockIo.CreateAndSignTransaction(prepareTransactionResponse, Arrays.copyOfRange(dtrustKeys, 0, 3));
        assertEquals(response.toJSONString(), createAndSignTransactionResponse.toJSONString());
    }

    @Test
    void testDTrustP2WSHOverP2SH4of5Keys() throws Exception {
        JSONParser parser = new JSONParser();
        JSONObject prepareTransactionResponse = (JSONObject) parser.parse(
                new FileReader("src/test/resources/__files/json/prepare_dtrust_transaction_response_p2wsh_over_p2sh.json")
        );;
        JSONObject createAndSignTransactionResponse = (JSONObject) parser.parse(
                new FileReader("src/test/resources/__files/json/create_and_sign_transaction_response_dtrust_p2wsh_over_p2sh_4_of_5_keys_non_lowR.json")
        );
        JSONObject response = blockIo.CreateAndSignTransaction(prepareTransactionResponse, dtrustKeys);
        assertEquals(response.toJSONString(), createAndSignTransactionResponse.toJSONString());
    }

    @Test
    void testDTrustP2WSHOverP2SH3of5Keys() throws Exception {
        JSONParser parser = new JSONParser();
        JSONObject prepareTransactionResponse = (JSONObject) parser.parse(
                new FileReader("src/test/resources/__files/json/prepare_dtrust_transaction_response_p2wsh_over_p2sh.json")
        );;
        JSONObject createAndSignTransactionResponse = (JSONObject) parser.parse(
                new FileReader("src/test/resources/__files/json/create_and_sign_transaction_response_dtrust_p2wsh_over_p2sh_3_of_5_keys_non_lowR.json")
        );
        JSONObject response = blockIo.CreateAndSignTransaction(prepareTransactionResponse, Arrays.copyOfRange(dtrustKeys, 0, 3));
        assertEquals(response.toJSONString(), createAndSignTransactionResponse.toJSONString());
    }

    @Test
    void testDTrustP2SH4of5Keys() throws Exception {
        JSONParser parser = new JSONParser();
        JSONObject prepareTransactionResponse = (JSONObject) parser.parse(
                new FileReader("src/test/resources/__files/json/prepare_dtrust_transaction_response_p2sh.json")
        );;
        JSONObject createAndSignTransactionResponse = (JSONObject) parser.parse(
                new FileReader("src/test/resources/__files/json/create_and_sign_transaction_response_dtrust_p2sh_4_of_5_keys_non_lowR.json")
        );
        JSONObject response = blockIo.CreateAndSignTransaction(prepareTransactionResponse, dtrustKeys);
        assertEquals(response.toJSONString(), createAndSignTransactionResponse.toJSONString());
    }

    @Test
    void testDTrustP2SH3of5Keys() throws Exception {
        JSONParser parser = new JSONParser();
        JSONObject prepareTransactionResponse = (JSONObject) parser.parse(
                new FileReader("src/test/resources/__files/json/prepare_dtrust_transaction_response_p2sh.json")
        );;
        JSONObject createAndSignTransactionResponse = (JSONObject) parser.parse(
                new FileReader("src/test/resources/__files/json/create_and_sign_transaction_response_dtrust_p2sh_3_of_5_keys_non_lowR.json")
        );
        JSONObject response = blockIo.CreateAndSignTransaction(prepareTransactionResponse, Arrays.copyOfRange(dtrustKeys, 0, 3));
        assertEquals(response.toJSONString(), createAndSignTransactionResponse.toJSONString());
    }

    @Test
    void testDTrustP2SH3of5UnorderedKeys() throws Exception {
        JSONParser parser = new JSONParser();
        JSONObject prepareTransactionResponse = (JSONObject) parser.parse(
                new FileReader("src/test/resources/__files/json/prepare_dtrust_transaction_response_p2sh.json")
        );;
        JSONObject createAndSignTransactionResponse = (JSONObject) parser.parse(
                new FileReader("src/test/resources/__files/json/create_and_sign_transaction_response_dtrust_p2sh_3_of_5_keys_non_lowR.json")
        );
        JSONObject response = blockIo.CreateAndSignTransaction(prepareTransactionResponse, new String[] {dtrustKeys[1], dtrustKeys[2], dtrustKeys[0]});
        assertEquals(response.toJSONString(), createAndSignTransactionResponse.toJSONString());
    }

    @Test
    void testUseOfExpectedUnsignedTxid() throws Exception {
        JSONParser parser = new JSONParser();
        JSONObject prepareTransactionResponse = (JSONObject) parser.parse(
                new FileReader("src/test/resources/__files/json/prepare_transaction_response_with_blockio_fee_and_expected_unsigned_txid.json")
        );;
        JSONObject createAndSignTransactionResponse = (JSONObject) parser.parse(
                new FileReader("src/test/resources/__files/json/create_and_sign_transaction_response_with_blockio_fee_and_expected_unsigned_txid_non_lowR.json")
        );
        JSONObject response = blockIo.CreateAndSignTransaction(prepareTransactionResponse);
        assertEquals(response.toJSONString(), createAndSignTransactionResponse.toJSONString());

        JSONObject dataObj = (JSONObject) prepareTransactionResponse.get("data");
        dataObj.put("expected_unsigned_txid", "");
        prepareTransactionResponse.put("data", dataObj);

        try{
            blockIo.CreateAndSignTransaction(prepareTransactionResponse);
            fail();
        } catch(Exception ex) {
            assertEquals("Expected unsigned transaction ID mismatch. Please report this error to support@block.io.", ex.getMessage());
        }
    }

    @Test
    void testSummarizePreparedTransaction() throws Exception {
        JSONParser parser = new JSONParser();
        JSONObject prepareTransactionResponse = (JSONObject) parser.parse(
                new FileReader("src/test/resources/__files/json/prepare_transaction_response_with_blockio_fee_and_expected_unsigned_txid.json")
        );;
        JSONObject summarizedPreparedTransactionResponse = (JSONObject) parser.parse(
                new FileReader("src/test/resources/__files/json/summarize_prepared_transaction_response_with_blockio_fee_and_expected_unsigned_txid.json")
        );
        JSONObject response = blockIo.SummarizePreparedTransaction(prepareTransactionResponse);
        assertEquals(response.toJSONString(), summarizedPreparedTransactionResponse.toJSONString());
    }
}
