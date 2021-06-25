package lib;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import okhttp3.*;
import org.bitcoinj.core.*;
import org.bitcoinj.core.Address;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bouncycastle.util.encoders.Hex;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.libdohj.params.DogecoinMainNetParams;
import org.libdohj.params.DogecoinTestNet3Params;
import org.libdohj.params.LitecoinMainNetParams;
import org.libdohj.params.LitecoinTestNet3Params;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class BlockIo {
    private OkHttpClient RestClient;
    private String ApiUrl;

    private Options Opts;
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
    private NetworkParameters networkParams;
    HashMap<String, ECKey> userKeys;

    public BlockIo(String apiKey) throws UnsupportedEncodingException {
        this(apiKey, null, 2, new Options());
    }

    public BlockIo(String apiKey, String pin) throws UnsupportedEncodingException {
        this(apiKey, pin, 2, new Options());
    }

    public BlockIo(String apiKey, String pin, int version) throws UnsupportedEncodingException {
        this(apiKey, pin, version, new Options());
    }

    public BlockIo(String apiKey, String pin, int version, Options opts ) throws UnsupportedEncodingException {
        networkParams = null;
        userKeys = new HashMap<>();

        Opts = opts;
        Pin = pin == null || pin.equals("") ? null : pin;
        AesKey = null;
        ApiUrl = null;

        ApiKey = apiKey;
        Version = version;
        Server = DefaultServer;
        Port = DefaultPort;
        if(Pin != null){
            AesKey = Helper.pinToAesKey(Pin);
        }

        if(!Opts.getApiUrl().equals("")) {
            ApiUrl = Opts.getApiUrl() + "/api/v2";
        }
        String serverString = !Server.equals("") ? Server + "." : Server;
        String portString = !Port.equals("") ? ":" + Port : Port;

        ApiUrl = ApiUrl == null ? "https://" + serverString + Host + portString + "/api/v" + Version : ApiUrl;

        ConnectionSpec requireTls12 = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                .tlsVersions(TlsVersion.TLS_1_2)
                .build();
        RestClient = new OkHttpClient.Builder()
                .connectionSpecs(Arrays.asList(requireTls12, ConnectionSpec.CLEARTEXT))
                .build();
    }

    public JSONObject createAndSignTransaction(JSONObject data) throws Exception {
        return createAndSignTransaction(data, new String[]{});
    }

    public JSONObject createAndSignTransaction(JSONObject data, String [] keys) throws Exception {
        String status = data.containsKey("status") ? data.get("status").toString() : "";
        JSONObject dataObj = (JSONObject) data.get("data");
        String networkString = dataObj.containsKey("network") ? dataObj.get("network").toString() : "";

        if(networkParams == null && !status.equals("")
            && status.equals("success") && !dataObj.isEmpty()
            && !networkString.equals("")) {
            networkParams = getNetwork(networkString);
        }

        JSONArray inputs = (JSONArray) dataObj.get("inputs");
        JSONArray outputs = (JSONArray) dataObj.get("outputs");
        JSONArray inputAddressData = (JSONArray) dataObj.get("input_address_data");

        Transaction tx = new Transaction(networkParams);

        for(Object input : inputs) {
            JSONObject curInput = (JSONObject) input;
            Sha256Hash preTxId = Sha256Hash.wrap(curInput.get("previous_txid").toString());
            int outputIndex = Integer.parseInt(curInput.get("previous_output_index").toString());
            tx.addInput(preTxId, outputIndex, new ScriptBuilder().data(new byte[0]).build()).clearScriptBytes();
        }

        for(Object output : outputs) {
            JSONObject curOutput = (JSONObject) output;
            Address receivingAddr = Address.fromString(networkParams, curOutput.get("receiving_address").toString());
            Coin outputValue = Coin.parseCoin(curOutput.get("output_value").toString());
            tx.addOutput(outputValue, receivingAddr);
        }

        String txHex = Helper.txToHexString(tx);

        HashMap<String, JSONObject> addressDataMap = new HashMap<>();
        HashMap<String, Script> addressScriptMap = new HashMap<>();

        for (Object inputAddressDatum : inputAddressData) {
            JSONObject curInputAddrData = (JSONObject) inputAddressDatum;

            JSONObject curAddrData = new JSONObject();
            String curAddrType = curInputAddrData.get("address_type").toString();
            JSONArray pubkeys = (JSONArray) curInputAddrData.get("public_keys");

            curAddrData.put("required_signatures", curInputAddrData.get("required_signatures"));
            curAddrData.put("public_keys", pubkeys);
            curAddrData.put("address_type", curAddrType);

            addressDataMap.put(curInputAddrData.get("address").toString(), curAddrData);

            ArrayList<ECKey> pubkeyList = new ArrayList<>();
            for(Object pubkey : pubkeys) {
                ECKey curPubKey = ECKey.fromPublicOnly(Hex.decode(pubkey.toString()));
                pubkeyList.add(curPubKey);
            }

            Script redeem;

            if(curAddrType.equals("P2WSH-over-P2SH") || curAddrType.equals("WITNESS_V0") || curAddrType.equals("P2SH")) {
                redeem = ScriptBuilder.createMultiSigOutputScript(2, pubkeyList);
            } else if(curAddrType.equals("P2PKH") || curAddrType.equals("P2WPKH") || curAddrType.equals("P2WPKH-over-P2SH")) {
                redeem = ScriptBuilder.createP2PKHOutputScript(pubkeyList.get(0));
            } else{
                throw new Exception("Unrecognized address type: " + curAddrType);
            }
            addressScriptMap.put(curInputAddrData.get("address").toString(), redeem);
        }

        if(keys.length > 0) {
            for(String key : keys) {
                ECKey userKey = Key.fromHex(key);
                userKeys.put(userKey.getPublicKeyAsHex(), userKey);
            }
        }

        if(dataObj.containsKey("user_key") && !userKeys.containsKey(((JSONObject)dataObj.get("user_key")).get("public_key").toString())){
            if(Pin != null) {
                String encryptionKey = Helper.pinToAesKey(Pin);
                String pubkeyStr = ((JSONObject)dataObj.get("user_key")).get("public_key").toString();
                ECKey key = Key.extractKeyFromEncryptedPassphrase(((JSONObject)dataObj.get("user_key")).get("encrypted_passphrase").toString(), encryptionKey);

                if(!key.getPublicKeyAsHex().equals(pubkeyStr)) {
                    throw new Exception("Fail: Invalid Secret PIN provided.");
                }
                userKeys.put(pubkeyStr, key);
            } else {
                throw new Exception("Fail: No PIN provided to decrypt private key.");
            }
        }

        if(dataObj.containsKey("expected_unsigned_txid") && !dataObj.get("expected_unsigned_txid").toString().equals(tx.getTxId().toString())) {
            throw new Exception("Expected unsigned transaction ID mismatch. Please report this error to support@block.io.");
        }

        boolean isTxFullySigned = true;
        JSONArray signatures = new JSONArray();

        for(Object input: inputs) {
            JSONObject curInput = (JSONObject) input;
            String curAddr = curInput.get("spending_address").toString();
            Script curAddrScript = addressScriptMap.get(curAddr);
            String curAddrType = addressDataMap.get(curAddr).get("address_type").toString();
            int addrRequiredSigs = Integer.parseInt(addressDataMap.get(curAddr).get("required_signatures").toString());
            JSONArray curPubKeys = (JSONArray) addressDataMap.get(curAddr).get("public_keys");
            int curSigCount = 0;
            int inputIte = Integer.parseInt(curInput.get("input_index").toString());
            Coin inputValue = Coin.parseCoin(curInput.get("input_value").toString());

            for(Object pubkey: curPubKeys) {
                String pubkeyStr = pubkey.toString();
                if(userKeys.containsKey(pubkeyStr)) {
                    ECKey key = userKeys.get(pubkeyStr);
                    Sha256Hash sigHash;
                    if(curAddrType.equals("P2WSH-over-P2SH") || curAddrType.equals("WITNESS_V0") || curAddrType.equals("P2WPKH")
                            || curAddrType.equals("P2WPKH-over-P2SH")) {
                        sigHash = tx.hashForWitnessSignature(inputIte, curAddrScript, inputValue, Transaction.SigHash.ALL, false);
                    } else{
                        sigHash = tx.hashForSignature(inputIte, curAddrScript, Transaction.SigHash.ALL, false);
                    }
                    ECKey.ECDSASignature sig = key.sign(sigHash);
                    TransactionSignature txSig = new TransactionSignature(sig, Transaction.SigHash.ALL, false);
                    JSONObject sigObj = new JSONObject();
                    sigObj.put("input_index", inputIte);
                    sigObj.put("public_key", pubkeyStr);
                    sigObj.put("signature", Utils.HEX.encode(txSig.encodeToDER()));
                    signatures.add(sigObj);
                    curSigCount++;
                }
            }
            if(curSigCount < addrRequiredSigs) {
                isTxFullySigned = false;
            }
        }

        JSONObject createAndSignResponse = new JSONObject();

        if(isTxFullySigned) {
            signatures = null;
        }
        createAndSignResponse.put("tx_type", dataObj.get("tx_type").toString());
        createAndSignResponse.put("tx_hex", txHex);
        createAndSignResponse.put("signatures", signatures);

        userKeys.clear();
        System.out.println(createAndSignResponse);
        return createAndSignResponse;
    }

    private NetworkParameters getNetwork(String networkString)
    {
        switch (networkString)
        {
            case "LTC":
                return LitecoinMainNetParams.get();
            case "DOGE":
                return DogecoinMainNetParams.get();
            case "BTCTEST":
                return NetworkParameters.fromID(NetworkParameters.ID_TESTNET);
            case "LTCTEST":
                return LitecoinTestNet3Params.get();
            case "DOGETEST":
                return DogecoinTestNet3Params.get();
            default:
                return NetworkParameters.fromID(NetworkParameters.ID_MAINNET);
        }
    }

    private Map<String, Object> _withdraw(String method, String path, Map<String, Object> args) throws Exception {
        Map<String, Object> res = null;

        String pin = Pin;
        if(args.get("pin") != null) {
            pin = args.get("pin").toString();
            args.put("pin", null);
        }

        res = _request(method, path, new Gson().toJson(args));
        JsonElement jsonElement = new Gson().toJsonTree(res);
        SignatureJson pojo = new Gson().fromJson(jsonElement, SignatureJson.class);
        if(pojo.getReferenceId() == null || pojo.getEncryptedPassphrase() == null ||
                pojo.getEncryptedPassphrase().getPassphrase() == null)
            return res;
        if(pin == null) {
            if(Opts.isAllowNoPin()){
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
        pojo.encryptedPassphrase = null;
        return _request(method, "sign_and_finalize_withdrawal", new Gson().toJson(pojo));
    }

    private Map<String, Object> _sweep(String method, String path, Map<String, Object> args) throws Exception {
        ECKey keyFromWif = null;
        Map<String, Object> res = null;
        Map<String, Object> mutableMap = new HashMap<>(args);

        if(mutableMap.get("to_address") == null){
            throw new Exception("Missing mandatory private_key argument.");
        }
        String privKeyStr = mutableMap.get("private_key").toString();
        keyFromWif = Key.fromWif(privKeyStr);
        mutableMap.put("public_key", keyFromWif.getPublicKeyAsHex());
        mutableMap.put("private_key", "");
        res = _request(method, path, new Gson().toJson(mutableMap));
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

    public Map<String, Object> GetNewAddress(Map<String, Object> args) throws Exception { 
        String argsJson = new Gson().toJson(args);
        return _request("POST", "get_new_address", argsJson);
    }
    public Map<String, Object> GetBalance(Map<String, Object> args) throws Exception { 
        String argsJson = new Gson().toJson(args);
        return _request("GET", "get_balance", argsJson);
    }
    public Map<String, Object> GetMyAddresses(Map<String, Object> args) throws Exception { 
        String argsJson = new Gson().toJson(args);
        return _request("POST", "get_my_addresses", argsJson);
    }
    public Map<String, Object> GetAddressReceived(Map<String, Object> args) throws Exception { 
        String argsJson = new Gson().toJson(args);
        return _request("POST", "get_address_received", argsJson);
    }
    public Map<String, Object> GetAddressByLabel(Map<String, Object> args) throws Exception { 
        String argsJson = new Gson().toJson(args);
        return _request("POST", "get_address_by_label", argsJson);
    }
    public Map<String, Object> GetAddressBalance(Map<String, Object> args) throws Exception { 
        String argsJson = new Gson().toJson(args);
        return _request("POST", "get_address_balance", argsJson);
    }
    public Map<String, Object> CreateUser(Map<String, Object> args) throws Exception { 
        String argsJson = new Gson().toJson(args);
        return _request("POST", "create_user", argsJson);
    }
    public Map<String, Object> GetUsers(Map<String, Object> args) throws Exception { 
        String argsJson = new Gson().toJson(args);
        return _request("POST", "get_users", argsJson);
    }
    public Map<String, Object> GetUserBalance(Map<String, Object> args) throws Exception { 
        String argsJson = new Gson().toJson(args);
        return _request("POST", "get_user_balance", argsJson);
    }
    public Map<String, Object> GetUserAddress(Map<String, Object> args) throws Exception { 
        String argsJson = new Gson().toJson(args);
        return _request("POST", "get_user_address", argsJson);
    }
    public Map<String, Object> GetUserReceived(Map<String, Object> args) throws Exception { 
        String argsJson = new Gson().toJson(args);
        return _request("POST", "get_user_received", argsJson);
    }
    public Map<String, Object> GetTransactions(Map<String, Object> args) throws Exception { 
        String argsJson = new Gson().toJson(args);
        return _request("POST", "get_transactions", argsJson);
    }
    public Map<String, Object> SignAndFinalizeWithdrawal(SignatureJson args) throws Exception {
        String argsJson = new Gson().toJson(args);
        return _request("POST", "sign_and_finalize_withdrawal", argsJson);
    }
    public Map<String, Object> GetNewDtrustAddress(Map<String, Object> args) throws Exception { 
        String argsJson = new Gson().toJson(args);
        return _request("POST", "get_new_dtrust_address", argsJson);
    }
    public Map<String, Object> GetMyDtrustAddresses(Map<String, Object> args) throws Exception { 
        String argsJson = new Gson().toJson(args);
        return _request("POST", "get_my_dtrust_addresses", argsJson);
    }
    public Map<String, Object> GetDtrustAddressByLabel(Map<String, Object> args) throws Exception { 
        String argsJson = new Gson().toJson(args);
        return _request("POST", "get_dtrust_address_by_label", argsJson);
    }
    public Map<String, Object> GetDtrustTransactions(Map<String, Object> args) throws Exception { 
        String argsJson = new Gson().toJson(args);
        return _request("POST", "get_dtrust_transactions", argsJson);
    }
    public Map<String, Object> GetDtrustAddressBalance(Map<String, Object> args) throws Exception { 
        String argsJson = new Gson().toJson(args);
        return _request("POST", "get_dtrust_address_balance", argsJson);
    }
    public Map<String, Object> GetNetworkFeeEstimate(Map<String, Object> args) throws Exception { 
        String argsJson = new Gson().toJson(args);
        return _request("POST", "get_network_fee_estimate", argsJson);
    }
    public Map<String, Object> ArchiveAddress(Map<String, Object> args) throws Exception { 
        String argsJson = new Gson().toJson(args);
        return _request("POST", "archive_address", argsJson);
    }
    public Map<String, Object> UnarchiveAddress(Map<String, Object> args) throws Exception { 
        String argsJson = new Gson().toJson(args);
        return _request("POST", "unarchive_address", argsJson);
    }
    public Map<String, Object> GetMyArchivedAddresses(Map<String, Object> args) throws Exception { 
        String argsJson = new Gson().toJson(args);
        return _request("POST", "get_my_archived_addresses", argsJson);
    }
    public Map<String, Object> ArchiveDtrustAddress(Map<String, Object> args) throws Exception { 
        String argsJson = new Gson().toJson(args);
        return _request("POST", "archive_dtrust_address", argsJson);
    }
    public Map<String, Object> UnarchiveDtrustAddress(Map<String, Object> args) throws Exception { 
        String argsJson = new Gson().toJson(args);
        return _request("POST", "unarchive_dtrust_address", argsJson);
    }
    public Map<String, Object> GetMyArchivedDtrustAddresses(Map<String, Object> args) throws Exception { 
        String argsJson = new Gson().toJson(args);
        return _request("POST", "get_my_archived_dtrust_addresses", argsJson);
    }
    public Map<String, Object> GetDtrustNetworkFeeEstimate(Map<String, Object> args) throws Exception { 
        String argsJson = new Gson().toJson(args);
        return _request("POST", "get_dtrust_network_fee_estimate", argsJson);
    }
    public Map<String, Object> CreateNotification(Map<String, Object> args) throws Exception { 
        String argsJson = new Gson().toJson(args);
        return _request("POST", "create_notification", argsJson);
    }
    public Map<String, Object> DisableNotification(Map<String, Object> args) throws Exception { 
        String argsJson = new Gson().toJson(args);
        return _request("POST", "disable_notification", argsJson);
    }
    public Map<String, Object> EnableNotification(Map<String, Object> args) throws Exception { 
        String argsJson = new Gson().toJson(args);
        return _request("POST", "enable_notification", argsJson);
    }
    public Map<String, Object> GetNotifications(Map<String, Object> args) throws Exception { 
        String argsJson = new Gson().toJson(args);
        return _request("POST", "get_notifications", argsJson);
    }
    public Map<String, Object> GetRecentNotificationEvents(Map<String, Object> args) throws Exception { 
        String argsJson = new Gson().toJson(args);
        return _request("POST", "get_recent_notification_events", argsJson);
    }
    public Map<String, Object> DeleteNotification(Map<String, Object> args) throws Exception { 
        String argsJson = new Gson().toJson(args);
        return _request("POST", "delete_notification", argsJson);
    }
    public Map<String, Object> ValidateApiKey(Map<String, Object> args) throws Exception { 
        String argsJson = new Gson().toJson(args);
        return _request("POST", "validate_api_key", argsJson);
    }
    public Map<String, Object> SignTransation(Map<String, Object> args) throws Exception { 
        String argsJson = new Gson().toJson(args);
        return _request("POST", "sign_transaction", argsJson);
    }
    public Map<String, Object> FinalizeTransaction(Map<String, Object> args) throws Exception { 
        String argsJson = new Gson().toJson(args);
        return _request("POST", "finalize_transaction", argsJson);
    }
    public Map<String, Object> GetMyAddressesWithoutBalances(Map<String, Object> args) throws Exception { 
        String argsJson = new Gson().toJson(args);
        return _request("POST", "get_my_addresses_without_balances", argsJson);
    }
    public Map<String, Object> GetRawTransaction(Map<String, Object> args) throws Exception { 
        String argsJson = new Gson().toJson(args);
        return _request("POST", "get_raw_transaction", argsJson);
    }
    public Map<String, Object> GetDtrustBalance(Map<String, Object> args) throws Exception { 
        String argsJson = new Gson().toJson(args);
        return _request("POST", "get_dtrust_balance", argsJson);
    }
    public Map<String, Object> ArchiveAddresses(Map<String, Object> args) throws Exception { 
        String argsJson = new Gson().toJson(args);
        return _request("POST", "archive_addresses", argsJson);
    }
    public Map<String, Object> UnarchiveAddresses(Map<String, Object> args) throws Exception { 
        String argsJson = new Gson().toJson(args);
        return _request("POST", "unarchive_addresses", argsJson);
    }
    public Map<String, Object> ArchiveDtrustAddresses(Map<String, Object> args) throws Exception { 
        String argsJson = new Gson().toJson(args);
        return _request("POST", "archive_dtrust_addresses", argsJson);
    }
    public Map<String, Object> UnarchiveDtrustAddresses(Map<String, Object> args) throws Exception { 
        String argsJson = new Gson().toJson(args);
        return _request("POST", "unarchive_dtrust_addresses", argsJson);
    }
    public Map<String, Object> IsValidAddress(Map<String, Object> args) throws Exception { 
        String argsJson = new Gson().toJson(args);
        return _request("POST", "is_valid_address", argsJson);
    }
    public Map<String, Object> GetCurrentPrice(Map<String, Object> args) throws Exception { 
        String argsJson = new Gson().toJson(args);
        return _request("POST", "get_current_price", argsJson);
    }
    public Map<String, Object> GetAccountInfo(Map<String, Object> args) throws Exception { 
        String argsJson = new Gson().toJson(args);
        return _request("POST", "get_account_info", argsJson);
    }

    //Withdrawal Methods

    public Map<String, Object> Withdraw(Map<String, Object> args) throws Exception { return _withdraw("POST", "withdraw", args); }
    public Map<String, Object> WithdrawFromUser(Map<String, Object> args) throws Exception { return _withdraw("POST", "withdraw_from_user", args); }
    public Map<String, Object> WithdrawFromLabel(Map<String, Object> args) throws Exception { return _withdraw("POST", "withdraw_from_label", args); }
    public Map<String, Object> WithdrawFromAddress(Map<String, Object> args) throws Exception { return _withdraw("POST", "withdraw_from_address", args); }
    public Map<String, Object> WithdrawFromLabels(Map<String, Object> args) throws Exception { return _withdraw("POST", "withdraw_from_labels", args); }
    public Map<String, Object> WithdrawFromAddresses(Map<String, Object> args) throws Exception { return _withdraw("POST", "withdraw_from_addresses", args); }
    public Map<String, Object> WithdrawFromUsers(Map<String, Object> args) throws Exception { return _withdraw("POST", "withdraw_from_users", args); }
    public Map<String, Object> WithdrawFromDtrustAddress(Map<String, Object> args) throws Exception { return _withdraw("POST", "withdraw_from_dtrust_address", args); }
    public Map<String, Object> WithdrawFromDtrustAddresses(Map<String, Object> args) throws Exception { return _withdraw("POST", "withdraw_from_dtrust_addresses", args); }
    public Map<String, Object> WithdrawFromDtrustLabels(Map<String, Object> args) throws Exception { return _withdraw("POST", "withdraw_from_dtrust_labels", args); }

    //Sweeep Method

    public Map<String, Object> SweepFromAddress(Map<String, Object> args) throws Exception { return _sweep("POST", "sweep_from_address", args); }
}
