package lib;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import okhttp3.*;
import org.bitcoinj.core.ECKey;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Map;

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
        Options = JsonUtils.parseJson(options);
        Options.put("allowNoPin", false);
        Pin = pin == null || pin.equals("") ? null : pin;
        AesKey = null;
        Map<String, Object> ConfigObj;

        if(JsonUtils.isJson(config)){
            ConfigObj = JsonUtils.parseJson(config);
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
                this.Options = JsonUtils.parseJson(ConfigObj.get("options").toString());
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

    private Map<String, Object> _withdraw(String method, String path, String args) throws Exception {
        Map<String, Object> res = null;
        Map<String, Object> argsObj = JsonUtils.parseJson(args);

        String pin = argsObj.get("pin") != null ? argsObj.get("pin").toString() : Pin;
        argsObj.put("pin", null);

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

    private Map<String, Object> _sweep(String method, String path, String args) throws Exception {
        ECKey keyFromWif = null;
        Map<String, Object> res = null;
        Map<String, Object> argsObj = JsonUtils.parseJson(args);

        if(argsObj.get("to_address") == null){
            throw new Exception("Missing mandatory private_key argument.");
        }
        String privKeyStr = argsObj.get("private_key").toString();
        keyFromWif = Key.fromWif(privKeyStr);
        argsObj.put("public_key", keyFromWif.getPublicKeyAsHex());
        argsObj.put("private_key", "");
        res = _request(method, path, new Gson().toJson(argsObj));
        JsonElement jsonElement = new Gson().toJsonTree(res);
        SignatureJson pojo = new Gson().fromJson(jsonElement, SignatureJson.class);
        if(pojo.getReferenceId() == null) {
            return res;
        }
        for(Input input : pojo.getInputs()){
            for(Signer signer : input.getSigners()){
                signer.setSignedData(Helper.signInputs(keyFromWif, input.getDataToSign(), signer.getSignerPublicKey()));
            }
        }
        keyFromWif = null;
        return _request(method, "sign_and_finalize_sweep", new Gson().toJson(pojo));
    }

    private Map<String, Object> _request(String method, String path, String args) throws Exception {
        Map<String, Object> res = null;

        if(method.equals("POST")){
            if(path.contains("sign_and_finalize")){
                JsonObject postObj = new JsonObject();
                postObj.addProperty("signature_data", args);
                args = new Gson().toJson(postObj);
            }
            res = JsonUtils.parseJson(post(args, path));
        }
        else{
            res = JsonUtils.parseJson(get(path));
        }

        if(!res.get("status").equals("success")){
            return res;
        }
        return JsonUtils.parseJson(new Gson().toJson(res.get("data")));
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
        RestClient.dispatcher().executorService().shutdown();
        RestClient.connectionPool().evictAll();

        return res;
    }

    private String post(String json, String path) throws IOException {
        MediaType type = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(type, json);
        Request request = new Request.Builder()
                .url(constructUrl(path))
                .addHeader("Accept", "application/json")
                .post(body)
                .build();
        Response response = RestClient.newCall(request).execute();
        assert response.body() != null;
        String res = response.body().string();
        RestClient.dispatcher().executorService().shutdown();
        RestClient.connectionPool().evictAll();

        return res;
    }

    /**
     * Block Io Methods
     */

    // Passthrough methods

    public Map<String, Object> GetNewAddress(String args) throws Exception { return _request("POST", "get_new_address", args); }
    public Map<String, Object> GetBalance(String args) throws Exception { return _request("GET", "get_balance", args); }
    public Map<String, Object> GetMyAddresses(String args) throws Exception { return _request("POST", "get_my_addresses", args); }
    public Map<String, Object> GetAddressReceived(String args) throws Exception { return _request("POST", "get_address_received", args); }
    public Map<String, Object> GetAddressByLabel(String args) throws Exception { return _request("POST", "get_address_by_label", args); }
    public Map<String, Object> GetAddressBalance(String args) throws Exception { return _request("POST", "get_address_balance", args); }
    public Map<String, Object> CreateUser(String args) throws Exception { return _request("POST", "create_user", args); }
    public Map<String, Object> GetUsers(String args) throws Exception { return _request("POST", "get_users", args); }
    public Map<String, Object> GetUserBalance(String args) throws Exception { return _request("POST", "get_user_balance", args); }
    public Map<String, Object> GetUserAddress(String args) throws Exception { return _request("POST", "get_user_address", args); }
    public Map<String, Object> GetUserReceived(String args) throws Exception { return _request("POST", "get_user_received", args); }
    public Map<String, Object> GetTransactions(String args) throws Exception { return _request("POST", "get_transactions", args); }
    public Map<String, Object> SignAndFinalizeWithdrawal(String args) throws Exception { return _request("POST", "sign_and_finalize_withdrawal", args); }
    public Map<String, Object> GetNewDtrustAddress(String args) throws Exception { return _request("POST", "get_new_dtrust_address", args); }
    public Map<String, Object> GetMyDtrustAddresses(String args) throws Exception { return _request("POST", "get_my_dtrust_addresses", args); }
    public Map<String, Object> GetDtrustAddressByLabel(String args) throws Exception { return _request("POST", "get_dtrust_address_by_label", args); }
    public Map<String, Object> GetDtrustTransactions(String args) throws Exception { return _request("POST", "get_dtrust_transactions", args); }
    public Map<String, Object> GetDtrustAddressBalance(String args) throws Exception { return _request("POST", "get_dtrust_address_balance", args); }
    public Map<String, Object> GetNetworkFeeEstimate(String args) throws Exception { return _request("POST", "get_network_fee_estimate", args); }
    public Map<String, Object> ArchiveAddress(String args) throws Exception { return _request("POST", "archive_address", args); }
    public Map<String, Object> UnarchiveAddress(String args) throws Exception { return _request("POST", "unarchive_address", args); }
    public Map<String, Object> GetMyArchivedAddresses(String args) throws Exception { return _request("POST", "get_my_archived_addresses", args); }
    public Map<String, Object> ArchiveDtrustAddress(String args) throws Exception { return _request("POST", "archive_dtrust_address", args); }
    public Map<String, Object> UnarchiveDtrustAddress(String args) throws Exception { return _request("POST", "unarchive_dtrust_address", args); }
    public Map<String, Object> GetMyArchivedDtrustAddresses(String args) throws Exception { return _request("POST", "get_my_archived_dtrust_addresses", args); }
    public Map<String, Object> GetDtrustNetworkFeeEstimate(String args) throws Exception { return _request("POST", "get_dtrust_network_fee_estimate", args); }
    public Map<String, Object> CreateNotification(String args) throws Exception { return _request("POST", "create_notification", args); }
    public Map<String, Object> DisableNotification(String args) throws Exception { return _request("POST", "disable_notification", args); }
    public Map<String, Object> EnableNotification(String args) throws Exception { return _request("POST", "enable_notification", args); }
    public Map<String, Object> GetNotifications(String args) throws Exception { return _request("POST", "get_notifications", args); }
    public Map<String, Object> GetRecentNotificationEvents(String args) throws Exception { return _request("POST", "get_recent_notification_events", args); }
    public Map<String, Object> DeleteNotification(String args) throws Exception { return _request("POST", "delete_notification", args); }
    public Map<String, Object> ValidateApiKey(String args) throws Exception { return _request("POST", "validate_api_key", args); }
    public Map<String, Object> SignTransation(String args) throws Exception { return _request("POST", "sign_transaction", args); }
    public Map<String, Object> FinalizeTransaction(String args) throws Exception { return _request("POST", "finalize_transaction", args); }
    public Map<String, Object> GetMyAddressesWithoutBalances(String args) throws Exception { return _request("POST", "get_my_addresses_without_balances", args); }
    public Map<String, Object> GetRawTransaction(String args) throws Exception { return _request("POST", "get_raw_transaction", args); }
    public Map<String, Object> GetDtrustBalance(String args) throws Exception { return _request("POST", "get_dtrust_balance", args); }
    public Map<String, Object> ArchiveAddresses(String args) throws Exception { return _request("POST", "archive_addresses", args); }
    public Map<String, Object> UnarchiveAddresses(String args) throws Exception { return _request("POST", "unarchive_addresses", args); }
    public Map<String, Object> ArchiveDtrustAddresses(String args) throws Exception { return _request("POST", "archive_dtrust_addresses", args); }
    public Map<String, Object> UnarchiveDtrustAddresses(String args) throws Exception { return _request("POST", "unarchive_dtrust_addresses", args); }
    public Map<String, Object> IsValidAddress(String args) throws Exception { return _request("POST", "is_valid_address", args); }
    public Map<String, Object> GetCurrentPrice(String args) throws Exception { return _request("POST", "get_current_price", args); }
    public Map<String, Object> GetAccountInfo(String args) throws Exception { return _request("POST", "get_account_info", args) ; }

    //Withdrawal Methods

    public Map<String, Object> Withdraw(String args) throws Exception { return _withdraw("POST", "withdraw", args); }
    public Map<String, Object> WithdrawFromUser(String args) throws Exception { return _withdraw("POST", "withdraw_from_user", args); }
    public Map<String, Object> WithdrawFromLabel(String args) throws Exception { return _withdraw("POST", "withdraw_from_label", args); }
    public Map<String, Object> WithdrawFromAddress(String args) throws Exception { return _withdraw("POST", "withdraw_from_address", args); }
    public Map<String, Object> WithdrawFromLabels(String args) throws Exception { return _withdraw("POST", "withdraw_from_labels", args); }
    public Map<String, Object> WithdrawFromAddresses(String args) throws Exception { return _withdraw("POST", "withdraw_from_addresses", args); }
    public Map<String, Object> WithdrawFromUsers(String args) throws Exception { return _withdraw("POST", "withdraw_from_users", args); }
    public Map<String, Object> WithdrawFromDtrustAddress(String args) throws Exception { return _withdraw("POST", "withdraw_from_dtrust_address", args); }
    public Map<String, Object> WithdrawFromDtrustAddresses(String args) throws Exception { return _withdraw("POST", "withdraw_from_dtrust_addresses", args); }
    public Map<String, Object> WithdrawFromDtrustLabels(String args) throws Exception { return _withdraw("POST", "withdraw_from_dtrust_labels", args); }

    //Sweeep Method

    public Map<String, Object> SweepFromAddress(String args) throws Exception { return _sweep("POST", "sweep_from_address", args); }
}
