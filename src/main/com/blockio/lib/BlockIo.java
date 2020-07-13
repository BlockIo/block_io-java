package com.blockio.lib;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import okhttp3.*;
import org.bitcoinj.core.ECKey;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.blockio.lib.JsonUtils.isJson;
import static com.blockio.lib.JsonUtils.parseJson;

public class BlockIo {
    private OkHttpClient RestClient;
    private String ApiUrl;

    private Map<String, Object> Options;
    private String ApiKey;
    private int Version;
    private String Server;
    private String Port;
    private String Pin;
    private String AesKey;

    private int DefaultVersion = 2;
    private String DefaultServer = "" ;
    private String DefaultPort = "";
    private String Host = "block.io";

    public BlockIo(String config) throws UnsupportedEncodingException {
        this(config, null, 2, "{}");
    }

    public BlockIo(String config, String pin) throws UnsupportedEncodingException {
        this(config, pin, 2, "{}");
    }

    public BlockIo(String config, String pin, int version) throws UnsupportedEncodingException {
        this(config, pin, version, "{}");
    }

    public BlockIo(String config, String pin, int version, String options ) throws UnsupportedEncodingException {
        Options = parseJson(options);
        Options.put("allowNoPin", false);
        Pin = pin.equals("") ? null : pin;
        AesKey = null;
        Map<String, Object> ConfigObj;

        if(isJson(config)){
            ConfigObj = parseJson(config);
            ApiKey = ConfigObj.get("api_key").toString();
            if (ConfigObj.get("version") != null) this.Version = (int) Math.round((double)ConfigObj.get("version")); else this.Version = this.DefaultVersion;
            if (ConfigObj.get("server") != null) this.Server = ConfigObj.get("server").toString(); else this.Server = this.DefaultServer;
            if (ConfigObj.get("port") != null) this.Port = ConfigObj.get("port").toString(); else this.Port = this.DefaultPort;

            if(ConfigObj.get("pin") != null)
            {
                this.Pin = ConfigObj.get("pin").toString();
                this.AesKey = Helper.pinToAesKey(this.Pin);
            }
            if(ConfigObj.get("options") != null)
            {
                this.Options = parseJson(ConfigObj.get("options").toString());
                this.Options.put("allowNoPin", false);
            }

        } else{
            ApiKey = config;
            if(Version == -1) Version = DefaultVersion; else Version = version;
            Server = DefaultServer;
            Port = DefaultPort;
            if(Pin != null){
                Pin = pin;
                AesKey = Helper.pinToAesKey(Pin);
            }
        }
        String serverString = !Server.equals("") ? Server + "." : Server;
        String portString = !Port.equals("") ? ":" + Port : Port;

        ApiUrl = "https://" + serverString + Host + portString + "/api/v" + Version;

        ConnectionSpec requireTls12 = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                .tlsVersions(TlsVersion.TLS_1_2)
                .build();
        RestClient = new OkHttpClient.Builder()
                .connectionSpecs(Arrays.asList(requireTls12))
                .build();
    }

    public Map<String, Object> _withdraw(String method, String path, String args) throws Exception {
        Map<String, Object> res = null;
        Map<String, Object> argsObj = parseJson(args);
        String pin = argsObj.get("pin") != null ? argsObj.get("pin").toString() : Pin;
        argsObj.put("pin", "");
        res = _request(method, path, new Gson().toJson(argsObj));
        JsonElement jsonElement = new Gson().toJsonTree(res);
        SignatureJson pojo = new Gson().fromJson(jsonElement, SignatureJson.class);
        if(pojo.getReferenceId() == null || pojo.getEncryptedPassphrase() == null ||
                pojo.getEncryptedPassphrase().getPassphrase() == null)
            return res;
        if(pin == null) {
            if(Options.get("allowNoPin").toString().equals("true")){
                return res;
            }
            throw new Exception("Public key mismatch. Invalid Secret PIN detected");
        }
        String encrypted_passphrase = pojo.getEncryptedPassphrase().getPassphrase();
        String aesKey = AesKey != null ? AesKey : Helper.pinToAesKey(pin);
        ECKey privKey = Key.extractKeyFromEncryptedPassphrase(encrypted_passphrase, aesKey);
        String pubKey = privKey.getPublicKeyAsHex();
        if(!pubKey.equals(pojo.getEncryptedPassphrase().getSignerPublicKey())){
            throw new Exception("Public key mismatch. Invalid Secret PIN detected.");
        }
        for(Input input : pojo.getInputs()){
            for(Signer signer : input.getSigners()){
                signer.setSignedData(Helper.signInputs(privKey, input.getDataToSign(), pojo.getEncryptedPassphrase().getSignerPublicKey()));
            }
        }
        aesKey = null;
        privKey = null;
        return _request(method, "sign_and_finalize_withdrawal", new Gson().toJson(pojo));
    }

    private Map<String, Object> _request(String method, String path, String args) throws Exception {
        Map<String, Object> res = null;

        if(method.equals("POST")){
            if(path.contains("sign_and_finalize")){
                JsonObject postObj = new JsonObject();
                postObj.addProperty("signature_data", args);
                args = new Gson().toJson(postObj);
            }
            res = parseJson(post(args, path));
        }
        else{
            res = parseJson(get(path));
        }
        if(!res.get("status").equals("success")){
            throw new Exception(res.get("data").toString());
        }
        return parseJson(new Gson().toJson(res.get("data")));
    }

    private String constructUrl(String path){
        return ApiUrl + "/" + path + "?api_key=" + ApiKey;
    }

    private String get(String path) throws IOException {
        Request request = new Request.Builder()
                .url(constructUrl(path))
                .build();

        Response response = RestClient.newCall(request).execute();
        assert response.body() != null;
        String res = response.body().string();
        response.body().close();
        return res;
    }

    private String post(String json, String path) throws IOException {
        final MediaType JSON = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(constructUrl(path))
                .addHeader("Accept", "application/json")
                .post(body)
                .build();
        Response response = RestClient.newCall(request).execute();
        assert response.body() != null;
        String res = response.body().string();
        response.body().close();
        return res;
    }

}
