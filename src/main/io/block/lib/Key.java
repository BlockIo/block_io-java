package io.block.lib;

import org.bitcoinj.core.Base58;
import org.bitcoinj.core.ECKey;
import org.bouncycastle.util.encoders.Hex;

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

    public static ECKey extractKeyFromEncryptedPassphrase(String encryptedData, String b64Key) throws NoSuchAlgorithmException {
        String decrypted = Helper.decrypt(encryptedData, b64Key); // this returns a hex string
        byte[] unHexlified = Hex.decode(decrypted);
        byte[] hashed = Helper.sha256Hash(unHexlified);

        return ECKey.fromPrivate(hashed);
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
}
