package io.block.examples;

import com.google.gson.JsonObject;
import io.block.lib.BlockIo;
import io.block.lib.JsonUtils;
import io.github.cdimascio.dotenv.Dotenv;
import java.io.UnsupportedEncodingException;

public class Basic {
    private BlockIo blockIo;
    public Basic() throws UnsupportedEncodingException {
        Dotenv dotenv = Dotenv.load();
        System.out.println("APi key: " + dotenv.get("API_KEY"));
        blockIo = new BlockIo(dotenv.get("API_KEY"), dotenv.get("PIN"));
    }
    public void RunBasicExample() throws Exception {
        System.out.println("Get New Address: " + blockIo.GetNewAddress("{\"label\": \"testDest7\"}"));
        System.out.println("Withdraw from labels: " + blockIo.WithdrawFromLabels("{\"from_labels\": \"default\", \"to_label\": \"testDest7\", \"amount\": 2.5}"));
        System.out.println("Get Address Balance: " + blockIo.GetAddressBalance("{\"labels\": \"default, testDest7\"]}"));
        System.out.println("Get Sent Transactions: " + blockIo.GetTransactions("{\"type\": \"sent\"}"));
        System.out.println("Get Received Transactions: " + blockIo.GetTransactions("{\"type\": \"received\"}"));
        System.out.println("Get Current Price: " + blockIo.GetCurrentPrice("{\"base_price\": \"BTC\"}"));
    }
}
