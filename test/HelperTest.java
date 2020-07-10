import com.blockio.lib.Helper;
import org.junit.jupiter.api.Test;
import java.io.UnsupportedEncodingException;
import static org.junit.jupiter.api.Assertions.*;

class HelperTest {

    private String pin = "123456";
    private String controlClearText = "I'm a little tea pot short and stout";
    private String controlCipherText = "7HTfNBYJjq09+vi8hTQhy6lCp3IHv5rztNnKCJ5RB7cSL+NjHrFVv1jl7qkxJsOg";
    private String controlAesKey = "0EeMOVtm5YihUYzdCNgleqIUWkwgvNBcRmr7M0t9GOc=";
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
}