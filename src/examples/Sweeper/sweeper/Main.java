package sweeper;

import lib.BlockIo;
import io.github.cdimascio.dotenv.Dotenv;
import org.json.simple.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Map;

public class Main {

    public static void main(String[] args) throws Exception {
        final BlockIo blockIo;
        final Dotenv dotenv;
        dotenv = Dotenv.load();
        blockIo = new BlockIo(dotenv.get("API_KEY"));
        if((dotenv.get("TO_ADDRESS").equals("") || dotenv.get("TO_ADDRESS") == null) ||
                (dotenv.get("PRIVATE_KEY_FROM_ADDRESS").equals("") || dotenv.get("PRIVATE_KEY_FROM_ADDRESS") == null) ||
                (dotenv.get("FROM_ADDRESS").equals("") || dotenv.get("FROM_ADDRESS") == null)){

            throw new Exception("Error: Missing parameters from env.");
        }

        Map<String, Object> res = blockIo.PrepareSweepTransaction(new JSONObject(Map.of(
                "to_address", dotenv.get("TO_ADDRESS"),
                "private_key", dotenv.get("PRIVATE_KEY_FROM_ADDRESS"),
                "from_address", dotenv.get("FROM_ADDRESS"))
        ));

        System.out.println("Sweep Res: " + res);
    }
}
