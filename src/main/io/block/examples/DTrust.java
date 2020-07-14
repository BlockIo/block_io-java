package io.block.examples;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import io.block.lib.*;
import io.github.cdimascio.dotenv.Dotenv;
import org.bitcoinj.core.ECKey;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class DTrust {
    private final BlockIo blockIo;
    String DtrustAddress;
    String DtrustAddressLabel;
    List<String> PublicKeys;
    List<ECKey> PrivKeys;

    public DTrust() throws UnsupportedEncodingException, NoSuchAlgorithmException {

        Dotenv dotenv = Dotenv.load();

        DtrustAddress = null;
        DtrustAddressLabel = "dTrust1_witness_v0";
        blockIo = new BlockIo(dotenv.get("API_KEY"), dotenv.get("PIN"));

        PrivKeys = new ArrayList<ECKey>() {
            {
                add(Key.extractKeyFromPassphraseString("verysecretkey1"));
                add(Key.extractKeyFromPassphraseString("verysecretkey2"));
                add(Key.extractKeyFromPassphraseString("verysecretkey3"));
                add(Key.extractKeyFromPassphraseString("verysecretkey4"));
            }
        };

        PublicKeys = new ArrayList<>() {
            {
                add(PrivKeys.get(0).getPublicKeyAsHex());
                add(PrivKeys.get(1).getPublicKeyAsHex());
                add(PrivKeys.get(2).getPublicKeyAsHex());
                add(PrivKeys.get(3).getPublicKeyAsHex());
            }
        };
    }

    public void RunDtrustExample() throws Exception {

        String signers = String.join(",", PublicKeys);
        Map<String, Object> res = blockIo.GetNewDtrustAddress("{\"label\": \"" + DtrustAddressLabel + "\", \"public_keys\": \"" + signers + "\", \"required_signatures\": \"3\", \"address_type\": \"witness_v0\"}");
        if(!res.get("status").toString().equals("success")){
            System.out.println("Error: " + res.get("data"));

            // if this failed, we probably created the same label before. let's fetch the address then
            res = blockIo.GetDtrustAddressByLabel("{\"label\": \"" + DtrustAddressLabel + "\"}");
            DtrustAddress = res.get("address").toString();
        }
        else{
            DtrustAddress = res.get("address").toString();
        }
        System.out.println("Our dTrust Address: " + DtrustAddress);

        res = blockIo.WithdrawFromLabels("{\"from_labels\": \"default\", \"to_address\": \"" + DtrustAddress + "\", \"amounts\": \"0.001\"}");
        System.out.println("Withdrawal Response: " + res);

        res = blockIo.GetDtrustAddressBalance("{\"label\": \"" + DtrustAddressLabel + "\"}");
        System.out.println("Dtrust address label Balance: " + res);

        res = blockIo.GetAddressByLabel("{\"label\": \"default\"}");
        String normalAddress = res.get("address").toString();

        System.out.println("Withdrawing from dtrust_address_label to the 'default' label in normal multisig");

        res = blockIo.WithdrawFromDtrustAddress("{\"from_labels\": \"" + DtrustAddressLabel + "\", \"to_address\": \"" + normalAddress + "\", \"amounts\": \"0.0009\"}");
        JsonElement jsonElement = new Gson().toJsonTree(res);
        SignatureJson pojo = new Gson().fromJson(jsonElement, SignatureJson.class);
        System.out.println("Withdraw from Dtrust Address response: " + res);

        int keyIte;
        for(Input input : pojo.getInputs()){
            keyIte = 0;
            for(Signer signer : input.getSigners()){
                signer.setSignedData(Helper.signInputs(PrivKeys.get(keyIte++), input.getDataToSign(), signer.getSignerPublicKey()));
            }
        }
        System.out.println("Our Signed Request: " + new Gson().toJson(pojo));
        System.out.println("Finalize Withdrawal: ");
        System.out.println(blockIo.SignAndFinalizeWithdrawal(new Gson().toJson(pojo)));
        System.out.println("Get transactions sent by our dtrust_address_label address: ");
        System.out.println(blockIo.GetDtrustTransactions("{\"type\": \"sent\", \"labels\": \"" + DtrustAddressLabel + "\"}"));

    }
}
