package examples;

import lib.BlockIo;
import io.github.cdimascio.dotenv.Dotenv;
import java.io.UnsupportedEncodingException;
import java.util.Map;

public class Sweeper {
    private final BlockIo blockIo;
    private final Dotenv dotenv;

    public Sweeper() throws UnsupportedEncodingException {
        dotenv = Dotenv.load();
        blockIo = new BlockIo(dotenv.get("API_KEY"));
    }

    public void RunSweeperExample() throws Exception {

        if((dotenv.get("TO_ADDRESS").equals("") || dotenv.get("TO_ADDRESS") == null) ||
            (dotenv.get("PRIVATE_KEY_FROM_ADDRESS").equals("") || dotenv.get("PRIVATE_KEY_FROM_ADDRESS") == null) ||
            (dotenv.get("FROM_ADDRESS").equals("") || dotenv.get("FROM_ADDRESS") == null)){

            throw new Exception("Error: Missing parameters from env.");
        }

        Map<String, Object> res = blockIo.SweepFromAddress("{" +
                "\"to_address\": \""          +     dotenv.get("TO_ADDRESS") +
                "\", \"private_key\": \""     +     dotenv.get("PRIVATE_KEY_FROM_ADDRESS") +
                "\", \"from_addresss\": \""   +     dotenv.get("FROM_ADDRESS") +
                "\"}");

        System.out.println("Sweep Res: " + res);
    }
}
