import com.blockio.lib.Helper;
import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import static org.junit.jupiter.api.Assertions.*;

class HelperTest {

    @Test
    void pinToAesKey() throws InvalidKeySpecException, NoSuchAlgorithmException {
        assertEquals("zV8RzinrWYaxIXfbdfmmJThgRKUSaGZl3JQ/93LcLno=", Helper.pinToAesKey("123456"));
    }

    @Test
    void encrypt() {
    }

    @Test
    void decrypt() {
    }
}