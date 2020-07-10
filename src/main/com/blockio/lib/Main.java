package com.blockio.lib;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Utils;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class Main {
    public static void main(String[] args) throws InvalidKeySpecException, NoSuchAlgorithmException {
        System.out.println("PIN TO AES TEST: " + Helper.pinToAesKey("123456"));
//        System.out.println("Encrypted data: " + Helper.encrypt("I'm a little tea pot short and stout", ));
//
//        byte[] hashedData = Sha256Hash.hash(Utils.HEX.decode("deadbeef"));
//        String dataToSign = "e76f0f78b7e7474f04cc14ad1343e4cc28f450399a79457d1240511a054afd63";
//        ECKey testKey = ECKey.fromPrivate(hashedData, true);
//        System.out.println("Private Key from deadbeef: " + testKey.getPrivateKeyAsHex());
//        System.out.println("Public Key from deadbeef: " + testKey.getPublicKeyAsHex());
//        System.out.println("Signed Data: " + SignInputs(testKey, dataToSign));

    }

    public static String SignInputs(ECKey k, String dataToSign){
        Sha256Hash hashedDataToSign = Sha256Hash.wrap(Utils.HEX.decode(dataToSign));
        ECKey.ECDSASignature sig = k.sign(hashedDataToSign);
        byte[] byteSignedData = sig.encodeToDER();
        return Utils.HEX.encode(byteSignedData);

    }
}

