package lib;

import org.bitcoinj.core.*;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.util.encoders.Hex;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
        byte[] dk = ((KeyParameter) gen.generateDerivedParameters(phase1_key_length * 8)).getKey();

        //round 2
        String hexStr = Hex.toHexString(dk).toLowerCase();
        gen.init(hexStr.getBytes(), salt.getBytes(StandardCharsets.UTF_8), iterations/2);
        dk = ((KeyParameter) gen.generateDerivedParameters(phase2_key_length * 8)).getKey();

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
