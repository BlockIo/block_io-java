package com.blockio.lib;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Utils;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.util.encoders.Hex;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

public class Helper {

    public static byte[] sha256Hash(String hexStr) throws NoSuchAlgorithmException {
        byte[] value = Hex.decode(hexStr);
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(value);
        return hash;
    }

    public static String pinToAesKey(String pin) throws UnsupportedEncodingException {

        PKCS5S2ParametersGenerator gen = new PKCS5S2ParametersGenerator(new SHA256Digest());

        //round 1
        gen.init(pin.getBytes("UTF-8"), new byte[0], 1024);
        byte[] dk = ((KeyParameter) gen.generateDerivedParameters(128)).getKey();

        //round 2
        String hexStr = Hex.toHexString(dk).toLowerCase();
        gen.init(hexStr.getBytes(), new byte[0], 1024);
        dk = ((KeyParameter) gen.generateDerivedParameters(256)).getKey();

        return Base64.getEncoder().encodeToString(dk);

    }
    public static String encrypt(String strToEncrypt, String secret)
    {
        try {
            byte[] key = Base64.getDecoder().decode(secret);
            byte[] keyArrBytes32Value = Arrays.copyOf(key, 32);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            SecretKeySpec secretKeySpec = new SecretKeySpec(keyArrBytes32Value, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String decrypt(String strToDecrypt, String secret)
    {
        try
        {
            byte[] key = Base64.getDecoder().decode(secret);
            byte[] keyArrBytes32Value = Arrays.copyOf(key, 32);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            SecretKeySpec secretKeySpec = new SecretKeySpec(keyArrBytes32Value, "AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)), StandardCharsets.UTF_8);
        }
        catch (Exception e)
        {
            System.out.println("Error while decrypting: " + e.toString());
        }
        return null;
    }

    public static String signInputs(ECKey k, String dataToSign, String pubKeyToVerify){

        String pubKey = k.getPublicKeyAsHex();
        if(pubKey.equals(pubKeyToVerify)){
            Sha256Hash hashedDataToSign = Sha256Hash.wrap(Utils.HEX.decode(dataToSign));
            ECKey.ECDSASignature sig = k.sign(hashedDataToSign);
            byte[] byteSignedData = sig.encodeToDER();
            return Utils.HEX.encode(byteSignedData);
        }
        return null;
    }
}
