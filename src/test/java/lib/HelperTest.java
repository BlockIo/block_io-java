package lib;

import com.google.common.collect.ImmutableList;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.libdohj.params.LitecoinTestNet3Params;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class HelperTest {

    private String pin;
    private String controlClearText;
    private String controlCipherText;
    private String controlAesKey;
    private String controlHashedData;
    private ECKey privKey1;
    private ECKey privKey2;

    @BeforeEach
    void setUp() {
        pin = "123456";
        controlClearText = "I'm a little tea pot short and stout";
        controlCipherText = "7HTfNBYJjq09+vi8hTQhy6lCp3IHv5rztNnKCJ5RB7cSL+NjHrFVv1jl7qkxJsOg";
        controlAesKey = "0EeMOVtm5YihUYzdCNgleqIUWkwgvNBcRmr7M0t9GOc=";
        controlHashedData = "5f78c33274e43fa9de5659265c1d917e25c03722dcb0b8d27db8d5feaa813953";
        privKey1 = ECKey.fromPrivate(Hex.decode("ef4fc6cfd682494093bbadf041ba4341afbe22b224432e21a4bc4470c5b939d4"));
        privKey2 = ECKey.fromPrivate(Hex.decode("123f37eb9a7f24a120969a1b2d6ac4859fb8080cfc2e8d703abae0f44305fc12"));
    }

    @Test
    void pinToAesKey() throws Exception {
        assertEquals(controlAesKey, Helper.pinToAesKey(pin));
    }

    @Test
    void pinToAesWithSalt() throws Exception {
        String salt = "922445847c173e90667a19d90729e1fb";
        String s_pin = "deadbeef";
        String encryptionKey = Helper.pinToAesKey(s_pin, salt, 500000);
        System.out.println("encryptionKey= " + encryptionKey);
        assertEquals(Hex.toHexString(Base64.getDecoder().decode(encryptionKey)), "f206403c6bad20e1c8cb1f3318e17cec5b2da0560ed6c7b26826867452534172");
    }

    @Test
    void encryptWithAes256Ecb() {
        assertEquals(controlCipherText, Helper.encrypt(controlClearText, controlAesKey));
    }

    @Test
    void decryptWithAes256Ecb() {
        assertEquals(controlClearText, Helper.decrypt(controlCipherText, controlAesKey));
    }
    @Test
    void encryptWithAes256Cbc() throws Exception {
        String encryptionKey = Helper.pinToAesKey("deadbeef", "922445847c173e90667a19d90729e1fb", 500000);
        String encryptedData = Helper.encrypt("beadbeef", encryptionKey, "11bc22166c8cf8560e5fa7e5c622bb0f", "AES-256-CBC");
        assertEquals(encryptedData, "LExu1rUAtIBOekslc328Lw==");
    }

    @Test
    void decryptWithAes256Cbc() throws Exception {
        String encryptionKey = Helper.pinToAesKey("deadbeef", "922445847c173e90667a19d90729e1fb", 500000);
        String encryptedData = "LExu1rUAtIBOekslc328Lw==";
        assertEquals(Helper.decrypt(encryptedData, encryptionKey, "11bc22166c8cf8560e5fa7e5c622bb0f",  "AES-256-CBC"), "beadbeef");
    }

    @Test
    void encryptWithAes256Gcm() throws Exception {
        String encryptionKey = Helper.pinToAesKey("deadbeef", "922445847c173e90667a19d90729e1fb", 500000);
        String encryptedData = Helper.encrypt("beadbeef", encryptionKey, "a57414b88b67f977829cbdca", "AES-256-GCM", "");
        assertEquals(encryptedData, "ELV56Z57KoA=");
    }
    @Test
    void sha256Hash() throws NoSuchAlgorithmException {
        String testData = "deadbeef";
        String shaData = Hex.toHexString(Helper.sha256Hash(Hex.decode(testData)));
        assertEquals(controlHashedData, shaData);
    }

    @Test
    void createBlockIoP2WSHScript() throws IOException {
        Script redeemScript = ScriptBuilder.createMultiSigOutputScript(2, ImmutableList.of(privKey1, privKey2));
        assertEquals("PUSHDATA(34)[0020d42b8341140559b7da105e8669e8f7d5a03773642ad82403ba91b80ffcc415de]", Helper.createBlockIoP2WSHScript(redeemScript).toString());
    }

    @Test
    void createBlockIoP2WPKHScript() throws IOException {
        Script redeemScript = ScriptBuilder.createP2WPKHOutputScript(privKey1);
        assertEquals("PUSHDATA(22)[0014b2b2380a1e486aff5ae5ae74c892e902a72c0a4c]", Helper.createBlockIoP2WPKHScript(redeemScript).toString());
    }

    @Test
    void txToHexString() {
        String txHex = "01000000000101e6195346d5c118b140eec5744821890269df2e309f2645fa2dc7c48b129148d10000000000ffffffff01c02d9a3b0000000017a914dd4edd1406541e476450fda7924720fe19f337b9870400483045022100b6b658f7d3d592645cdc7ca21d45504ffde7d9b2ef22e97b7b57c507e952b006022059631267d3fcdfb06a4efdf940dabaf022e051bda9d93de2ef400e94ea2b39be01473044022033d8136791bc5658700b385ca5728b9e188a3ba1aa3bc691d6adfd1b8431cee6022073d565e5d1e96c0257f7cefdab946e48fb3857248f49048e00f6b701e97457c30147522103820317ad251bca573c8fda2b8f26ffc9aae9d5ecb15b50ee08d8f9e009def38e210238de8c9eb2842ecaf0cc61ee6ba23fe4e46f1cfd82eac0910e1d8e865bd76df952ae00000000";
        Transaction testTx = new Transaction(LitecoinTestNet3Params.get(), Hex.decode(txHex));
        assertEquals(Helper.txToHexString(testTx), txHex);
    }
}