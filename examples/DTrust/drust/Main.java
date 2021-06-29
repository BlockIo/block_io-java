package drust;

import lib.*;
import io.github.cdimascio.dotenv.Dotenv;
import org.bitcoinj.core.ECKey;
import org.json.simple.JSONObject;

import java.util.*;

public class Main {

    public static void main(String[] args) throws Exception {

        Dotenv dotenv = Dotenv.load();
        final BlockIo blockIo;
        String DtrustAddress;
        String DtrustAddressLabel;
        List<String> PublicKeys;
        List<ECKey> PrivKeys;
        DtrustAddressLabel = "dTrust1_witness_v0_3";
        blockIo = new BlockIo(dotenv.get("API_KEY"), dotenv.get("PIN"));

        // WARNING: THIS IS JUST A DEMO
        // private keys must always be generated using secure random number generators
        // for instance, by using new Key(true)
        // ofcourse, you will store the private keys yourself before using them anywhere

        PrivKeys = new ArrayList<>() {
            {
                add(Key.extractKeyFromPassphraseString("verysecretkey1"));
                add(Key.extractKeyFromPassphraseString("verysecretkey2"));
                add(Key.extractKeyFromPassphraseString("verysecretkey3"));
                add(Key.extractKeyFromPassphraseString("verysecretkey4"));
            }
        };

        // the public keys for our private keys
        PublicKeys = new ArrayList<>() {
            {
                add(PrivKeys.get(0).getPublicKeyAsHex());
                add(PrivKeys.get(1).getPublicKeyAsHex());
                add(PrivKeys.get(2).getPublicKeyAsHex());
                add(PrivKeys.get(3).getPublicKeyAsHex());
            }
        };

        String signers = String.join(",", PublicKeys);
        JSONObject res = blockIo.GetNewDtrustAddress(new JSONObject(Map.of(
                "label", DtrustAddressLabel,
                "public_keys", signers,
                "required_signatures", "3",
                "address_type", "witness_v0"
        )));
        if(!res.get("status").toString().equals("success")){
            System.out.println("Error: " + res.get("data"));

            // if this failed, we probably created the same label before. let's fetch the address then
            res = (JSONObject) blockIo.GetDtrustAddressByLabel(new JSONObject(Map.of("label", DtrustAddressLabel))).get("data");
            DtrustAddress = res.get("address").toString();
        }
        else{
            res = (JSONObject) res.get("data");
            DtrustAddress = res.get("address").toString();
        }
        System.out.println("Our dTrust Address: " + DtrustAddress);

        // send coins from our basic wallet to the new dTrust address
        // below is just a quick demo: you will always inspect data from responses yourself to ensure everything's as you expect it
        // prepare the transaction

        res = blockIo.PrepareTransaction(new JSONObject(Map.of("from_labels", "default", "to_address", DtrustAddress, "amounts", "0.0003")));

        System.out.println("Summarized Prepared Transaction: " + blockIo.SummarizePreparedTransaction(res));
        // create and sign the transaction
        res = blockIo.CreateAndSignTransaction(res);

        // submit the transaction to Block.io for its signature and to broadcast to the peer-to-peer network
        res = blockIo.SubmitTransaction(new JSONObject(Map.of("transaction_data", res)));
        System.out.println("Withdrawal Response: " + res.get("data"));

        res = blockIo.GetDtrustAddressBalance(new JSONObject(Map.of("label", DtrustAddressLabel)));
        System.out.println("Dtrust address label Balance: " + res);

        res = (JSONObject) blockIo.GetAddressByLabel(new JSONObject(Map.of("label", "default"))).get("data");
        String normalAddress = res.get("address").toString();

        System.out.println("Withdrawing from dtrust_address_label to the 'default' label in normal multisig");

        // prepare the dTrust transaction
        res = blockIo.PrepareDtrustTransaction(new JSONObject(Map.of("from_labels", DtrustAddressLabel, "to_address", normalAddress, "amounts", "0.0002" )));

        System.out.println("Summarized Prepared dTrust Transaction: " + blockIo.SummarizePreparedTransaction(res));
        // create and sign the transaction using just three keys (you can use all 4 keys to create the final transaction for broadcasting as well)
        res = blockIo.CreateAndSignTransaction(res, Arrays.copyOfRange(PrivKeys.stream().map(ECKey::getPrivateKeyAsHex).toArray(String[]::new), 0, 3));
        
        // submit the transaction
        res = blockIo.SubmitTransaction(new JSONObject(Map.of("transaction_data", res)));

        System.out.println("Withdraw from Dtrust Address response: " + res.get("data"));

        System.out.println("Get transactions sent by our dtrust_address_label address: ");
        System.out.println(blockIo.GetDtrustTransactions(new JSONObject(Map.of("type", "sent", "labels", DtrustAddressLabel))));

    }
}
