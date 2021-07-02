package lib;

import com.google.common.base.Strings;
import org.bitcoinj.core.Base58;
import org.bitcoinj.core.ECKey;
import org.bouncycastle.util.encoders.Hex;
import org.json.simple.JSONObject;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class Key extends ECKey {

    public static ECKey fromWif(String PrivKey) throws Exception {
        byte[] extendedKeyBytes = Base58.decodeChecked(PrivKey);

        //skip the version byte
        extendedKeyBytes = Arrays.copyOfRange(extendedKeyBytes, 1, extendedKeyBytes.length - 1);
        if (extendedKeyBytes.length == 33)
        {
            if (extendedKeyBytes[32] != 0x01)
            {
                throw new Exception("Invalid compression flag");
            }
            extendedKeyBytes = Arrays.copyOfRange(extendedKeyBytes, 0, extendedKeyBytes.length - 1);
        }
        if (extendedKeyBytes.length != 32)
        {
            throw new Exception("Invalid WIF payload length");
        }
        return ECKey.fromPrivate(extendedKeyBytes);
    }

    public static ECKey dynamicExtractKey(JSONObject userKey, String secretPin) throws Exception{

        JSONObject algorithm = (JSONObject) userKey.get("algorithm");

        if(algorithm.isEmpty()) {
            // use the legacy algorithm
            algorithm = new JSONObject();
            algorithm.put("pbkdf2_salt", "");
            algorithm.put("pbkdf2_iterations", 2048);
            algorithm.put("pbkdf2_hash_function", "SHA256");
            algorithm.put("pbkdf2_phase1_key_length", 16);
            algorithm.put("pbkdf2_phase2_key_length", 32);
            algorithm.put("aes_iv", null);
            algorithm.put("aes_cipher", "AES-256-ECB");
            algorithm.put("aes_auth_tag", null);
            algorithm.put("aes_auth_data", null);
        }
        // string pin, string salt = "", int iterations = 2048, int phase1_key_length = 16, int phase2_key_length = 32, string hash_function = "SHA256"
        String B64Key = Helper.pinToAesKey(secretPin, algorithm.get("pbkdf2_salt").toString(),
                                            Integer.parseInt(algorithm.get("pbkdf2_iterations").toString()),
                                            Integer.parseInt(algorithm.get("pbkdf2_phase1_key_length").toString()),
                                            Integer.parseInt(algorithm.get("pbkdf2_phase2_key_length").toString()),
                                            algorithm.get("pbkdf2_hash_function").toString());
        // string data, string key, string iv = null, string cipher_type = "AES-256-ECB", string auth_tag = null, string auth_data = null
        String Decrypted = Helper.decrypt(userKey.get("encrypted_passphrase").toString(),
                                            B64Key,
                                            algorithm.get("aes_iv") == null ? null : algorithm.get("aes_iv").toString(),
                                            algorithm.get("aes_cipher") == null ? null : algorithm.get("aes_cipher").toString(),
                                            algorithm.get("aes_auth_tag") == null ? null : algorithm.get("aes_auth_tag").toString(),
                                            algorithm.get("aes_auth_data") == null ? null : algorithm.get("aes_auth_data").toString());

        return Key.extractKeyFromPassphrase(Decrypted);
    }

    public static ECKey extractKeyFromEncryptedPassphrase(String encryptedData, String b64Key) throws Exception {
        String decrypted = Helper.decrypt(encryptedData, b64Key); // this returns a hex string
        return Key.extractKeyFromPassphrase(decrypted);
    }
    public static ECKey extractKeyFromPassphrase(String hexPass) throws NoSuchAlgorithmException {
        byte[] unHexlified = Hex.decode(hexPass);
        byte[] hashed = Helper.sha256Hash(unHexlified);

        return ECKey.fromPrivate(hashed);
    }
    public static ECKey extractKeyFromPassphraseString(String pass) throws NoSuchAlgorithmException {
        byte[] password = pass.getBytes(StandardCharsets.UTF_8);
        byte[] hashed = Helper.sha256Hash(password);

        return ECKey.fromPrivate(hashed);
    }

    public static ECKey fromHex(String privKeyHex) {
        byte[] hexBytes = Hex.decode(Strings.padStart(privKeyHex, 64, '0'));
        return ECKey.fromPrivate(hexBytes);
    }
}
