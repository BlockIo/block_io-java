package examples;

import com.google.gson.Gson;
import lib.*;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Objects;

public class MaxWithdrawal {
    private final BlockIo blockIo;
    private final Dotenv dotenv;

    public MaxWithdrawal() throws UnsupportedEncodingException {
        dotenv = Dotenv.load();
        blockIo = new BlockIo(dotenv.get("API_KEY"), dotenv.get("PIN"));
    }

    public void RunMaxWithdrawalExample() throws Exception {
        String balance = blockIo.GetBalance(null).get("available_balance").toString();

        System.out.println("Balance: " + balance);

        while (true)
        {
            Map<String, Object> res = blockIo.Withdraw(Map.of("to_address", Objects.requireNonNull(dotenv.get("TO_ADDRESS")), "amount", balance));
            if(res.get("status") != null) { System.out.println(res.get("data").toString());}

            res = JsonUtils.parseJson(new Gson().toJson(res.get("data")));

            String maxWithdraw = res.get("max_withdrawal_available").toString();

            System.out.println("Max Withdraw Available: " + maxWithdraw);

            if (Double.parseDouble(maxWithdraw) == 0) break;
            blockIo.Withdraw(Map.of("to_address", Objects.requireNonNull(dotenv.get("TO_ADDRESS")), "amount", maxWithdraw));
        }

        balance = blockIo.GetBalance(null).get("available_balance").toString();

        System.out.println("Final Balance: " + balance);
    }
}
