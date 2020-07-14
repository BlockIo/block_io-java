package io.block.lib;

import io.block.examples.Basic;
import io.block.examples.DTrust;
import io.block.examples.MaxWithdrawal;

public class Main {
    public static void main(String[] args) throws Exception {
//        JsonObject sweepObj = new JsonObject();
//        sweepObj.addProperty("to_address", "my9gXk65EzZUL962MSJadPXJFmJzPDc1WT");
//        sweepObj.addProperty("private_key", "cUhedoiwPkprm99qfUKzixsrpN3w6wT2XrrMjqo3Yh1tHz8ykVKc");
//        JsonObject withdrawObj = new JsonObject();
//        withdrawObj.addProperty("amounts", "0.002");
//        withdrawObj.addProperty("to_addresses", "2N8pEWg9ZPyxa2yioZWDYAzNFyTnYp6TkHF");
//        obj.addProperty("port", "8080");
//        obj.addProperty("pin", "was1qwas1q");
//        JsonObject options = new JsonObject();
//        options.addProperty("option1", 1);
//        options.addProperty("options2", 2);
//        obj.addProperty("options", new Gson().toJson(options));
//        JsonArray objArr = new JsonArray();
//        objArr.add("this");
//        objArr.add("is");
//        objArr.add("arr");
//        obj.add("param3", objArr);
//        System.out.println(new Gson().toJson(obj));
//        BlockIo test = new BlockIo(new Gson().toJson(obj));
//        String apiKey = "27d6-fc3a-1606-e6d4";
//        String pin = "";
//        BlockIo test = new BlockIo(apiKey, pin);
//        System.out.println("get balance: " + test.GetBalance("{}").get("available_balance"));
//        Map<String, Object> res = test._sweep("POST", "sweep_from_address", new Gson().toJson(sweepObj));
//        System.out.println(res);
        //new Gson().toJson(Options) Converts maps and Json object to json string

        MaxWithdrawal maxWithdrawalEx = new MaxWithdrawal();
        maxWithdrawalEx.RunMaxWithdrawalExample();
    }
}

