package basic;

import lib.BlockIo;
import io.github.cdimascio.dotenv.Dotenv;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws Exception {
        Dotenv dotenv = Dotenv.load();
        final BlockIo blockIo = new BlockIo(dotenv.get("API_KEY"), dotenv.get("PIN"));

        System.out.println("Get New Address: " + blockIo.GetNewAddress(Map.of("label", "testDest15")));
        System.out.println("Withdraw from labels: " + blockIo.WithdrawFromLabels(Map.of("from_labels", "shibe1", "to_label", "default", "amount", "2.5")));
        System.out.println("Get Address Balance: " + blockIo.GetAddressBalance(Map.of("labels", "default, testDest15")));
        System.out.println("Get Sent Transactions: " + blockIo.GetTransactions(Map.of("type", "sent")));
        System.out.println("Get Received Transactions: " + blockIo.GetTransactions(Map.of("type", "received")));
        System.out.println("Get Current Price: " + blockIo.GetCurrentPrice(Map.of("base_price", "BTC")));
    }
}
