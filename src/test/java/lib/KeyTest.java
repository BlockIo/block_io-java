package lib;

import org.bitcoinj.core.ECKey;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KeyTest {

    String wif;
    String passphrase;
    String dataToSign;
    String controlPrivKeyFromPassphrase;
    String controlPubKeyFromPassphrase;
    String controlPrivKeyFromWif;
    String controlPubKeyFromWif;
    String controlSignedDataWifKey;
    String controlSignedDataPassphraseKey;

    ECKey privKeyFromWif;
    ECKey privKeyFromPassphrase;

    String pubKeyFromWif;
    String pubKeyFromPassphrase;

    @BeforeEach
    void setUp() throws Exception {
        wif = "L1cq4uDmSKMiViT4DuR8jqJv8AiiSZ9VeJr82yau5nfVQYaAgDdr";
        passphrase = "deadbeef";
        dataToSign = "e76f0f78b7e7474f04cc14ad1343e4cc28f450399a79457d1240511a054afd63";
        controlPrivKeyFromPassphrase = "5f78c33274e43fa9de5659265c1d917e25c03722dcb0b8d27db8d5feaa813953";
        controlPubKeyFromPassphrase = "02953b9dfcec241eec348c12b1db813d3cd5ec9d93923c04d2fa3832208b8c0f84";
        controlPrivKeyFromWif = "833e2256c42b4a41ee0a6ee284c39cf8e1978bc8e878eb7ae87803e22d48caa9";
        controlPubKeyFromWif = "024988bae7e0ade83cb1b6eb0fd81e6161f6657ad5dd91d216fbeab22aea3b61a0";
        controlSignedDataWifKey = "3045022100aec97f7ad7a9831d583ca157284a68706a6ac4e76d6c9ee33adce6227a40e675022008894fb35020792c01443d399d33ffceb72ac1d410b6dcb9e31dcc71e6c49e92";
        controlSignedDataPassphraseKey = "30450221009a68321e071c94e25484e26435639f00d23ef3fbe9c529c3347dc061f562530c0220134d3159098950b81b678f9e3b15e100f5478bb45345d3243df41ae616e70032";

        privKeyFromWif = Key.fromWif(wif);
        privKeyFromPassphrase = Key.extractKeyFromPassphrase(passphrase);

        pubKeyFromWif = privKeyFromWif.getPublicKeyAsHex();
        pubKeyFromPassphrase = privKeyFromPassphrase.getPublicKeyAsHex();
    }

    @Test
    void fromWif() {
        assertEquals(controlPrivKeyFromWif, privKeyFromWif.getPrivateKeyAsHex());
    }

    @Test
    void extractKeyFromPassphrase() {
        assertEquals(controlPrivKeyFromPassphrase, privKeyFromPassphrase.getPrivateKeyAsHex());
    }
    @Test
    void pubKeyFromWif() {
        assertEquals(controlPubKeyFromWif, pubKeyFromWif);
    }
    @Test
    void pubKeyFromPassphrase() {
        assertEquals(controlPubKeyFromPassphrase, pubKeyFromPassphrase);
    }
    @Test
    void signDataWifKey() {
        assertEquals(Helper.signInputs(privKeyFromWif, dataToSign, pubKeyFromWif), controlSignedDataWifKey);
    }
    @Test
    void signDataPassphraseKey() {
        assertEquals(Helper.signInputs(privKeyFromPassphrase, dataToSign, pubKeyFromPassphrase), controlSignedDataPassphraseKey);
    }
    @Test
    void dynamicExtractKeyWithAes256Ecb() throws Exception {
        JSONParser parser = new JSONParser();
        JSONObject userKey = (JSONObject) parser.parse("{\"encrypted_passphrase\":\"3wIJtPoC8KO6S7x6LtrN0g==\",\"public_key\":\"02f87f787bffb30396984cb6b3a9d6830f32d5b656b3e39b0abe4f3b3c35d99323\",\"algorithm\":{\"pbkdf2_salt\":\"\",\"pbkdf2_iterations\":2048,\"pbkdf2_hash_function\":\"SHA256\",\"pbkdf2_phase1_key_length\":16,\"pbkdf2_phase2_key_length\":32,\"aes_iv\":null,\"aes_cipher\":\"AES-256-ECB\",\"aes_auth_tag\":null,\"aes_auth_data\":null}}");
        ECKey key = Key.dynamicExtractKey(userKey, "deadbeef");
        assertEquals(key.getPublicKeyAsHex(), userKey.get("public_key"));
    }
    @Test
    void dynamicExtractKeyWithAes256Cbc() throws Exception {
        JSONParser parser = new JSONParser();
        JSONObject userKey = (JSONObject) parser.parse("{\"encrypted_passphrase\":\"LExu1rUAtIBOekslc328Lw==\",\"public_key\":\"02f87f787bffb30396984cb6b3a9d6830f32d5b656b3e39b0abe4f3b3c35d99323\",\"algorithm\":{\"pbkdf2_salt\":\"922445847c173e90667a19d90729e1fb\",\"pbkdf2_iterations\":500000,\"pbkdf2_hash_function\":\"SHA256\",\"pbkdf2_phase1_key_length\":16,\"pbkdf2_phase2_key_length\":32,\"aes_iv\":\"11bc22166c8cf8560e5fa7e5c622bb0f\",\"aes_cipher\":\"AES-256-CBC\",\"aes_auth_tag\":null,\"aes_auth_data\":null}}");
        ECKey key = Key.dynamicExtractKey(userKey, "deadbeef");
        assertEquals(key.getPublicKeyAsHex(), userKey.get("public_key"));
    }
    @Test
    void dynamicExtractKeyWithAes256Gcm() throws Exception {
        JSONParser parser = new JSONParser();
        JSONObject userKey = (JSONObject) parser.parse("{\"encrypted_passphrase\":\"ELV56Z57KoA=\",\"public_key\":\"02f87f787bffb30396984cb6b3a9d6830f32d5b656b3e39b0abe4f3b3c35d99323\",\"algorithm\":{\"pbkdf2_salt\":\"922445847c173e90667a19d90729e1fb\",\"pbkdf2_iterations\":500000,\"pbkdf2_hash_function\":\"SHA256\",\"pbkdf2_phase1_key_length\":16,\"pbkdf2_phase2_key_length\":32,\"aes_iv\":\"a57414b88b67f977829cbdca\",\"aes_cipher\":\"AES-256-GCM\",\"aes_auth_tag\":\"adeb7dfe53027bdda5824dc524d5e55a\",\"aes_auth_data\":\"\"}}");
        ECKey key = Key.dynamicExtractKey(userKey, "deadbeef");
        assertEquals(key.getPublicKeyAsHex(), userKey.get("public_key"));
    }
}