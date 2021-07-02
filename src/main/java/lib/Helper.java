package lib;

import org.bitcoinj.core.*;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.util.encoders.Hex;
import org.json.simple.JSONObject;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public class Helper {

    public static byte[] sha256Hash(byte[] toHash) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(toHash);
        return hash;
    }

    public static String pinToAesKey(String pin) throws Exception {
        return pinToAesKey(pin, "", 2048, 16, 32, "SHA256");
    }
    public static String pinToAesKey(String pin, String salt) throws Exception {
        return pinToAesKey(pin, salt, 2048, 16, 32, "SHA256");
    }
    public static String pinToAesKey(String pin, String salt, int iterations) throws Exception {
        return pinToAesKey(pin, salt, iterations, 16, 32, "SHA256");
    }
    public static String pinToAesKey(String pin, String salt, int iterations, int phase1_key_length) throws Exception {
        return pinToAesKey(pin, salt, iterations, phase1_key_length, 32, "SHA256");
    }
    public static String pinToAesKey(String pin, String salt, int iterations, int phase1_key_length, int phase2_key_length) throws Exception {
        return pinToAesKey(pin, salt, iterations, phase1_key_length, phase2_key_length, "SHA256");
    }
    public static String pinToAesKey(String pin, String salt, int iterations, int phase1_key_length, int phase2_key_length, String hash_function) throws Exception {

        if (!hash_function.equals("SHA256"))
            throw new Exception("Unknown hash function specified. Are you using current version of this library?");

        PKCS5S2ParametersGenerator gen = new PKCS5S2ParametersGenerator(new SHA256Digest());

        //round 1
        gen.init(pin.getBytes("UTF-8"), salt.getBytes(StandardCharsets.UTF_8), iterations/2);
        byte[] dk = ((KeyParameter) gen.generateDerivedParameters(phase1_key_length * Byte.SIZE)).getKey();

        //round 2
        String hexStr = Hex.toHexString(dk).toLowerCase();
        gen.init(hexStr.getBytes(), salt.getBytes(StandardCharsets.UTF_8), iterations/2);
        dk = ((KeyParameter) gen.generateDerivedParameters(phase2_key_length * Byte.SIZE)).getKey();

        return Base64.getEncoder().encodeToString(dk);

    }

    public static JSONObject encrypt(String strToEncrypt, String secret) throws Exception {
        return encrypt(strToEncrypt, secret, null, "AES-256-ECB", null);
    }

    public static JSONObject encrypt(String strToEncrypt, String secret, String iv) throws Exception {
        return encrypt(strToEncrypt, secret, iv, "AES-256-ECB", null);
    }

    public static JSONObject encrypt(String strToEncrypt, String secret, String iv, String cipher_type) throws Exception {
        return encrypt(strToEncrypt, secret, iv, cipher_type, null);
    }

    public static JSONObject encrypt(String strToEncrypt, String secret, String iv, String cipher_type, String auth_data) throws Exception {
        JSONObject response = new JSONObject();
        response.put("aes_iv", iv);
        response.put("aes_cipher", cipher_type);
        response.put("aes_auth_data", auth_data);

        byte[] key = Base64.getDecoder().decode(secret);
        byte[] keyArrBytes32Value = Arrays.copyOf(key, 32);
        Cipher cipher = null;
        if(!cipher_type.equals("AES-256-GCM")){
            if(cipher_type.equals("AES-256-ECB")) {
                cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            } else if(cipher_type.equals("AES-256-CBC")) {
                cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            } else {
                throw new Exception("Unsupported cipher " + cipher_type);
            }
            SecretKeySpec secretKeySpec = new SecretKeySpec(keyArrBytes32Value, "AES");
            if(iv != null)
                cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, new IvParameterSpec(Hex.decode(iv)));
            else
                cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            response.put("aes_cipher_text", Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8))));
            response.put("aes_auth_tag", null);
        }
        else{
            // AES-256-GCM
            int AUTH_TAG_SIZE = 128; //16 bytes
            int CIPHER_TEXT_SIZE = strToEncrypt.getBytes(StandardCharsets.UTF_8).length;

            cipher = Cipher.getInstance("AES/GCM/NoPadding");
            byte[] ivBytes = Hex.decode(iv);
            SecretKey secretKey = new SecretKeySpec(key, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(AUTH_TAG_SIZE, ivBytes));
            cipher.updateAAD(Hex.decode(auth_data));

            byte[] cipherTextPlusAuthTag = cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8));
            byte[] cipherText = Arrays.copyOfRange(cipherTextPlusAuthTag, 0, CIPHER_TEXT_SIZE);;
            byte[] auth_tag = Arrays.copyOfRange(cipherTextPlusAuthTag, cipherTextPlusAuthTag.length - (AUTH_TAG_SIZE / Byte.SIZE), cipherTextPlusAuthTag.length);

            response.put("aes_cipher_text", Base64.getEncoder().encodeToString(cipherText));
            response.put("aes_auth_tag", Hex.toHexString(auth_tag));
        }
        return response;
    }

    public static String decrypt(String strToEncrypt, String secret) throws Exception {
        return decrypt(strToEncrypt, secret, null, "AES-256-ECB", null,null);
    }

    public static String decrypt(String strToEncrypt, String secret, String iv) throws Exception {
        return decrypt(strToEncrypt, secret, iv, "AES-256-ECB", null, null);
    }

    public static String decrypt(String strToEncrypt, String secret, String iv, String cipher_type) throws Exception {
        return decrypt(strToEncrypt, secret, iv, cipher_type, null, null);
    }

    public static String decrypt(String strToEncrypt, String secret, String iv, String cipher_type, String auth_tag) throws Exception {
        return decrypt(strToEncrypt, secret, iv, cipher_type, auth_tag, null);
    }

    public static String decrypt(String strToDecrypt, String secret, String iv, String cipher_type, String auth_tag, String auth_data) throws Exception {
        // encrypted_data, b64_enc_key, iv = nil, cipher_type = "AES-256-ECB", auth_tag = nil, auth_data = nil
        byte[] key = Base64.getDecoder().decode(secret);
        byte[] keyArrBytes32Value = Arrays.copyOf(key, 32);
        Cipher cipher;
        if(!cipher_type.equals("AES-256-GCM")){
            if(cipher_type.equals("AES-256-ECB")) {
                cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            } else if(cipher_type.equals("AES-256-CBC")) {
                cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            } else {
                throw new Exception("Unsupported cipher " + cipher_type);
            }
            SecretKeySpec secretKeySpec = new SecretKeySpec(keyArrBytes32Value, "AES");
            if(iv != null)
                cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(Hex.decode(iv)));
            else
                cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)), StandardCharsets.UTF_8);
        }
        else{
            // AES-256-GCM
            if (auth_tag.length() != 32)
                throw new Exception("Auth tag must be 16 bytes exactly.");

            int AUTH_TAG_SIZE = 128; //16 bytes
            byte[] authTag = Hex.decode(auth_tag);
            byte[] ivBytes = Hex.decode(iv);
            byte[] cipherText = Base64.getDecoder().decode(strToDecrypt);

            ByteBuffer byteBuffer = ByteBuffer.allocate(cipherText.length + authTag.length + ivBytes.length);
            byteBuffer.put(ivBytes);
            byteBuffer.put(cipherText);
            byteBuffer.put(authTag);

            byte[] bytesToDecrypt = byteBuffer.array();

            cipher = Cipher.getInstance("AES/GCM/NoPadding");
            SecretKey secretKey = new SecretKeySpec(key, "AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(AUTH_TAG_SIZE, bytesToDecrypt, 0, ivBytes.length));
            cipher.updateAAD(Hex.decode(auth_data));

            byte[] plainText = cipher.doFinal(bytesToDecrypt, ivBytes.length, bytesToDecrypt.length - ivBytes.length);

            return new String(plainText, StandardCharsets.UTF_8);
        }
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

    public static String txToHexString(Transaction tx){
        return Utils.HEX.encode(tx.unsafeBitcoinSerialize());
    }

    public static Script createBlockIoP2WSHScript(Script redeem) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        output.write((byte)0);
        output.write((byte)32);
        output.write(Sha256Hash.hash(redeem.getProgram()));
        byte[] out = output.toByteArray();
        return new ScriptBuilder().data(out).build();
    }

    public static Script createBlockIoP2WPKHScript(Script redeem) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        output.write((byte)0);
        output.write((byte)20);
        output.write(redeem.getPubKeyHash());
        byte[] out = output.toByteArray();
        return new ScriptBuilder().data(out).build();
    }

    public static TransactionWitness redeemP2WSH(List<TransactionSignature> signatures, Script redeem) {
        int witnesses = signatures.size() + 2;
        TransactionWitness wit = new TransactionWitness(witnesses);
        wit.setPush(0, new byte[0]);
        int witnessIte = 1;
        for (TransactionSignature signature : signatures) {
            wit.setPush(witnessIte, signature.encodeToBitcoin());
            witnessIte++;
        }
        wit.setPush(witnessIte, redeem.getProgram());
        return wit;
    }
}
