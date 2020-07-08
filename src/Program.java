import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Utils;
import org.bouncycastle.util.encoders.Base64;

import java.nio.charset.StandardCharsets;

public class Program {
    public static void main(String[] args) {
        System.out.println("Hello World");

        byte[] hashedData = Sha256Hash.hash(Utils.HEX.decode("deadbeef"));
        String dataToSign = "e76f0f78b7e7474f04cc14ad1343e4cc28f450399a79457d1240511a054afd63";
        ECKey testKey = ECKey.fromPrivate(hashedData, true);
        System.out.println("Private Key from deadbeef: " + testKey.getPrivateKeyAsHex());
        System.out.println("Public Key from deadbeef: " + testKey.getPublicKeyAsHex());
        System.out.println("Signed Data: " + SignInputs(testKey, dataToSign));
        //test commit 5
    }

    public static String SignInputs(ECKey k, String dataToSign){
        Sha256Hash hashedDataToSign = Sha256Hash.wrap(Utils.HEX.decode(dataToSign));
        ECKey.ECDSASignature sig = k.sign(hashedDataToSign);
        byte[] byteSignedData = sig.encodeToDER();
        return Utils.HEX.encode(byteSignedData);

    }
}
