package basic;

import lib.BlockIo;
import io.github.cdimascio.dotenv.Dotenv;
import org.json.simple.JSONObject;

import java.util.Map;

public class Main {
    public static void main(String[] args) throws Exception {
        Dotenv dotenv = Dotenv.load();
        // initiate the BlockIo library with the API Key and Secret PIN
        final BlockIo blockIo = new BlockIo(dotenv.get("API_KEY"), dotenv.get("PIN"));
        // get a new address
        System.out.println("Get New Address: " + blockIo.GetNewAddress(new JSONObject(Map.of("label", "my_new_label"))));
        // send coins to our new address
        JSONObject preparedTransaction = blockIo.PrepareTransaction(new JSONObject(Map.of(
                "to_label", "my_new_label",
                "amount","0.0005"
        )));
        System.out.println("Prepared Transaction: " + preparedTransaction.get("data"));

        // summarize the prepared transaction
        // inspect this in-depth yourself, we're just showing the summary here
        System.out.println("Summarized Prepared Transaction: " + blockIo.SummarizePreparedTransaction(preparedTransaction));

        // create and sign the prepared transaction
        // transactionData contains the unsigned tx_hex (inspect it yourself), and your signatures to append to the transaction
        JSONObject txData = blockIo.CreateAndSignTransaction(preparedTransaction);

        // submit the transaction
        // if partially signed, Block.io will add its signature to complete the transaction
        // and then broadcast to the peer-to-peer blockchain network
        System.out.println("Submit Transaction: " + blockIo.SubmitTransaction(new JSONObject(Map.of("transaction_data", txData))).get("data"));
        System.out.println("Get Address Balance: " + blockIo.GetAddressBalance(new JSONObject(Map.of("label", "my_new_label"))).get("data"));
        System.out.println("Get Sent Transactions: " + blockIo.GetTransactions(new JSONObject(Map.of("type", "sent"))).get("data"));
        System.out.println("Get Received Transactions: " + blockIo.GetTransactions(new JSONObject(Map.of("type", "received"))).get("data"));
        System.out.println("Get Current Price: " + blockIo.GetCurrentPrice(new JSONObject(Map.of("base_price", "BTC"))).get("data"));
    }
}
