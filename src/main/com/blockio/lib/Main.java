package com.blockio.lib;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Utils;
import org.bouncycastle.util.encoders.Hex;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

public class Main {
    public static void main(String[] args) throws UnsupportedEncodingException {
        JsonObject obj = new JsonObject();
        obj.addProperty("api_key", "2341234");
        obj.addProperty("version", 3);
        obj.addProperty("server", "wasiq.io");
        obj.addProperty("port", "8080");
        obj.addProperty("pin", "was1qwas1q");
        JsonObject options = new JsonObject();
        options.addProperty("option1", 1);
        options.addProperty("options2", 2);
        obj.addProperty("options", new Gson().toJson(options));
//        JsonArray objArr = new JsonArray();
//        objArr.add("this");
//        objArr.add("is");
//        objArr.add("arr");
//        obj.add("param3", objArr);
//        System.out.println(new Gson().toJson(obj));
//        BlockIo test = new BlockIo(new Gson().toJson(obj));
        BlockIo test = new BlockIo(new Gson().toJson(obj));
        //new Gson().toJson(Options) Converts maps and Json object to json string
    }
}

