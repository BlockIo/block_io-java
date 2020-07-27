package examples;

import lib.BlockIo;
import io.github.cdimascio.dotenv.Dotenv;
import java.io.UnsupportedEncodingException;

public class Basic {
    private final BlockIo blockIo;
    public Basic() throws UnsupportedEncodingException {
        Dotenv dotenv = Dotenv.load();
        blockIo = new BlockIo(dotenv.get("API_KEY"), dotenv.get("PIN"));
    }
    public void RunBasicExample() throws Exception {
        System.out.println("Get New Address: " + blockIo.GetNewAddress("{\"label\": \"testDest15\"}"));
        System.out.println("Withdraw from labels: " + blockIo.WithdrawFromLabels("{\"from_labels\": \"shibe1\", \"to_label\": \"default\", \"amount\": \"2.5\"}"));
        System.out.println("Get Address Balance: " + blockIo.GetAddressBalance("{\"labels\": \"default, testDest15\"}"));
        System.out.println("Get Sent Transactions: " + blockIo.GetTransactions("{\"type\": \"sent\"}"));
        System.out.println("Get Received Transactions: " + blockIo.GetTransactions("{\"type\": \"received\"}"));
        System.out.println("Get Current Price: " + blockIo.GetCurrentPrice("{\"base_price\": \"BTC\"}"));
    }
}
