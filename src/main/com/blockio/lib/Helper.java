package com.blockio.lib;
import org.bouncycastle.util.encoders.Hex;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;

public class Helper {

    public static String pinToAesKey(String pin) throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        char[] pinChars = pin.toCharArray();

        //first round
        KeySpec spec = new PBEKeySpec(pinChars, new byte[1], 1024, 128);
        SecretKey key = factory.generateSecret(spec);
        byte[] secret = new SecretKeySpec(key.getEncoded(), "AES").getEncoded();

        //second round
        String hexStr = Hex.toHexString(secret).toLowerCase();
        spec = new PBEKeySpec(hexStr.toCharArray(), new byte[1], 1024, 256);
        key = factory.generateSecret(spec);
        secret = new SecretKeySpec(key.getEncoded(), "AES").getEncoded();

        return Base64.getEncoder().encodeToString(secret);

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
}
