package lib;

import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.*;

class HelperTest {

    private String pin;
    private String controlClearText;
    private String controlCipherText;
    private String controlAesKey;
    private String controlHashedData;

    @BeforeEach
    void setUp() {
        pin = "123456";
        controlClearText = "I'm a little tea pot short and stout";
        controlCipherText = "7HTfNBYJjq09+vi8hTQhy6lCp3IHv5rztNnKCJ5RB7cSL+NjHrFVv1jl7qkxJsOg";
        controlAesKey = "0EeMOVtm5YihUYzdCNgleqIUWkwgvNBcRmr7M0t9GOc=";
        controlHashedData = "5f78c33274e43fa9de5659265c1d917e25c03722dcb0b8d27db8d5feaa813953";
    }

    @Test
    void pinToAesKey() throws UnsupportedEncodingException {
        assertEquals(controlAesKey, Helper.pinToAesKey(pin));
    }

    @Test
    void encrypt() {
        assertEquals(controlCipherText, Helper.encrypt(controlClearText, controlAesKey));
    }

    @Test
    void decrypt() {
        assertEquals(controlClearText, Helper.decrypt(controlCipherText, controlAesKey));
    }
    @Test
    void sha256Hash() throws NoSuchAlgorithmException {
        String testData = "deadbeef";
        String shaData = Hex.toHexString(Helper.sha256Hash(Hex.decode(testData)));
        assertEquals(controlHashedData, shaData);
    }
}