package com.blockio.lib;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Utils;
import org.bouncycastle.util.encoders.Hex;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import static com.blockio.lib.JsonUtils.getMapFromObj;

public class Main {
    public static void main(String[] args) throws Exception {
        JsonObject sweepObj = new JsonObject();
        sweepObj.addProperty("to_address", "my9gXk65EzZUL962MSJadPXJFmJzPDc1WT");
        sweepObj.addProperty("private_key", "cUhedoiwPkprm99qfUKzixsrpN3w6wT2XrrMjqo3Yh1tHz8ykVKc");
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
        String apiKey = "27d6-fc3a-1606-e6d4";
        String pin = "";
        BlockIo test = new BlockIo(apiKey, pin);
        System.out.println("get balance: " + test.GetBalance("{}").get("available_balance"));
//        Map<String, Object> res = test._sweep("POST", "sweep_from_address", new Gson().toJson(sweepObj));
//        System.out.println(res);
        //new Gson().toJson(Options) Converts maps and Json object to json string
    }
}

