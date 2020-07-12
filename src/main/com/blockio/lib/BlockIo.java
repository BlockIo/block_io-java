package com.blockio.lib;

import okhttp3.OkHttpClient;

import java.io.UnsupportedEncodingException;
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
        Pin = pin;
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

        RestClient = new OkHttpClient();
//        System.out.println(new Gson().toJson(Options));
    }

}
