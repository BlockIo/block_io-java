package examples;

import lib.BlockIo;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.UnsupportedEncodingException;

public class Proxy {
    private final BlockIo blockIo;

    public Proxy() throws UnsupportedEncodingException {
        Dotenv dotenv = Dotenv.load();
        blockIo = new BlockIo(dotenv.get("API_KEY"), dotenv.get("PIN"));
    }

    public void RunProxyExample() throws Exception {
        System.out.println("Get Balance: " + blockIo.GetBalance("{}"));
        System.out.println("Get New Address: " + blockIo.GetNewAddress("{\"label\": \"testDest4\"}"));
        System.out.println("Withdraw from labels: " + blockIo.WithdrawFromLabels("{\"from_labels\": \"default\", \"to_label\": \"testDest4\", \"amount\": \"0.003\"}"));
        System.out.println("Get Address Balance: " + blockIo.GetAddressBalance("{\"labels\": \"default, testDest4\"}"));
        System.out.println("Get Sent Transactions: " + blockIo.GetTransactions("{\"type\": \"sent\"}"));
        System.out.println("Get Received Transactions: " + blockIo.GetTransactions("{\"type\": \"received\"}"));
        System.out.println("Get Current Price: " + blockIo.GetCurrentPrice("{\"base_price\": \"BTC\"}"));
    }
}
