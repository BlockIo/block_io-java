import lib.blockIo.BlockIo;
import io.github.cdimascio.dotenv.Dotenv;
import org.json.simple.JSONObject;
import java.util.Map;
import java.util.Objects;

public class sweep {

    public static void main(String[] args) throws Exception {
        final BlockIo blockIo;
        final Dotenv dotenv;
        dotenv = Dotenv.load();
        blockIo = new BlockIo(dotenv.get("API_KEY"));
        if((Objects.equals(dotenv.get("TO_ADDRESS"), "") || dotenv.get("TO_ADDRESS") == null) ||
                (Objects.equals(dotenv.get("PRIVATE_KEY"), "") || dotenv.get("PRIVATE_KEY") == null)){

            throw new Exception("Error: Missing parameters from env.");
        }

        // prepare the transaction
        JSONObject res = blockIo.PrepareSweepTransaction(new JSONObject(Map.of(
                "to_address", Objects.requireNonNull(dotenv.get("TO_ADDRESS")),
                "private_key", Objects.requireNonNull(dotenv.get("PRIVATE_KEY"))
        )));
        System.out.println(res);
        // summarize the transaction
        // inspect it in-depth yourself to ensure everything as you expect
        System.out.println("Summarized Prepared Sweep Transaction: " + blockIo.SummarizePreparedTransaction(res));

        // create and sign the transaction
        res = blockIo.CreateAndSignTransaction(res);

        // submit the final transaction to broadcast to the peer-to-peer blockchain network
        res = blockIo.SubmitTransaction(new JSONObject(Map.of("transaction_data", res)));


        System.out.println("Sweep Res: " + res);
    }
}
