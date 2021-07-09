package lib.blockIo;

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
import java.text.DecimalFormat;
import java.text.NumberFormat;
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
    private String UserAgent;
    private NetworkParameters networkParams;
    HashMap<String, ECKey> userKeys;

    public BlockIo(String apiKey) throws Exception {
        this(apiKey, null, 2, new Options());
    }

    public BlockIo(String apiKey, String pin) throws Exception {
        this(apiKey, pin, 2, new Options());
    }

    public BlockIo(String apiKey, String pin, int version) throws Exception {
        this(apiKey, pin, version, new Options());
    }

    public BlockIo(String apiKey, String pin, int version, Options opts ) throws Exception {
        networkParams = null;
        userKeys = new HashMap<>();

        String libVersion = getClass().getPackage().getImplementationVersion();
        UserAgent = String.join(":", "java", "block_io", libVersion);

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
    public JSONObject CreateAndSignTransaction(JSONObject data) throws Exception {
        return CreateAndSignTransaction(data, new String[]{});
    }

    public JSONObject CreateAndSignTransaction(JSONObject data, String [] keys) throws Exception {
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
            tx.addInput(preTxId, outputIndex, ScriptBuilder.createEmpty()).clearScriptBytes();
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
            int requiredSigs = Integer.parseInt(curInputAddrData.get("required_signatures").toString());
            JSONArray pubkeys = (JSONArray) curInputAddrData.get("public_keys");

            curAddrData.put("required_signatures", requiredSigs);
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
                redeem = ScriptBuilder.createMultiSigOutputScript(requiredSigs, pubkeyList);
            } else if(curAddrType.equals("P2PKH") || curAddrType.equals("P2WPKH") || curAddrType.equals("P2WPKH-over-P2SH")) {
                redeem = ScriptBuilder.createP2PKHOutputScript(pubkeyList.get(0));
            } else{
                throw new Exception("Unrecognized address type: " + curAddrType);
            }
            addressScriptMap.put(curInputAddrData.get("address").toString(), redeem);
        }

        if(keys.length > 0) {
             // user provided some keys, let's index them
                for(String key : keys) {
                ECKey userKey = Key.fromHex(key);
                userKeys.put(userKey.getPublicKeyAsHex(), userKey);
            }
        }

        if(dataObj.containsKey("user_key") && !userKeys.containsKey(((JSONObject)dataObj.get("user_key")).get("public_key").toString())){
             // we don't have the key to sign for transaction yet
                if(Pin != null) {
                // use the user_key to extract private key dynamically
                String pubkeyStr = ((JSONObject)dataObj.get("user_key")).get("public_key").toString();
                ECKey key = Key.dynamicExtractKey((JSONObject)dataObj.get("user_key"), Pin);

                if(!key.getPublicKeyAsHex().equals(pubkeyStr)) {
                    throw new Exception("Fail: Invalid Secret PIN provided.");
                }
                // we have the key, let's save it for later use
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
            Sha256Hash sigHash;
            if(curAddrType.equals("P2WSH-over-P2SH") || curAddrType.equals("WITNESS_V0") || curAddrType.equals("P2WPKH")
                    || curAddrType.equals("P2WPKH-over-P2SH")) {
                sigHash = tx.hashForWitnessSignature(inputIte, curAddrScript, inputValue, Transaction.SigHash.ALL, false);
            } else{
                sigHash = tx.hashForSignature(inputIte, curAddrScript, Transaction.SigHash.ALL, false);
            }

            ArrayList<TransactionSignature> txSigList = new ArrayList<>();
            ArrayList<ECKey> curKeys = new ArrayList<>();
            for(Object pubkey: curPubKeys) {
                String pubkeyStr = pubkey.toString();
                if(userKeys.containsKey(pubkeyStr)) {
                    ECKey key = userKeys.get(pubkeyStr);
                    ECKey.ECDSASignature sig = key.sign(sigHash);
                    TransactionSignature txSig = new TransactionSignature(sig, Transaction.SigHash.ALL, false);
                    txSigList.add(txSig);
                    curKeys.add(key);
                    JSONObject sigObj = new JSONObject();
                    sigObj.put("input_index", inputIte);
                    sigObj.put("public_key", pubkeyStr);
                    sigObj.put("signature", Hex.toHexString(txSig.encodeToDER()));
                    signatures.add(sigObj);
                    curSigCount++;
                }
            }

            if(curAddrType.equals("P2WSH-over-P2SH")) {
                tx.getInput(inputIte).setScriptSig(Helper.createBlockIoP2WSHScript(curAddrScript));
                tx.getInput(inputIte).setWitness(Helper.redeemP2WSH(txSigList, curAddrScript));
            } else if(curAddrType.equals("WITNESS_V0")) {
                tx.getInput(inputIte).setWitness(Helper.redeemP2WSH(txSigList, curAddrScript));
            } else if(curAddrType.equals("P2WPKH")) {
                tx.getInput(inputIte).setWitness(TransactionWitness.redeemP2WPKH(txSigList.get(0), curKeys.get(0)));
            } else if(curAddrType.equals("P2WPKH-over-P2SH")) {
                Script redeem = ScriptBuilder.createP2WPKHOutputScript(curKeys.get(0));
                tx.getInput(inputIte).setScriptSig(Helper.createBlockIoP2WPKHScript(redeem));
                tx.getInput(inputIte).setWitness(TransactionWitness.redeemP2WPKH(txSigList.get(0), curKeys.get(0)));
            } else if(curAddrType.equals("P2SH")){
                Script inputScript = ScriptBuilder.createP2SHMultiSigInputScript(txSigList, curAddrScript);
                tx.getInput(inputIte).setScriptSig(inputScript);
            } else {
                // P2PKH
                Script inputScript = ScriptBuilder.createInputScript(txSigList.get(0), curKeys.get(0));
                tx.getInput(inputIte).setScriptSig(inputScript);
            }

            if(curSigCount < addrRequiredSigs) {
                isTxFullySigned = false;
            }
        }

        JSONObject createAndSignResponse = new JSONObject();

        if(isTxFullySigned) {
            txHex = Helper.txToHexString(tx);
            signatures = null;
        }
        createAndSignResponse.put("tx_type", dataObj.get("tx_type").toString());
        createAndSignResponse.put("tx_hex", txHex);
        createAndSignResponse.put("signatures", signatures);

        userKeys.clear();
        return createAndSignResponse;
    }

    public JSONObject SummarizePreparedTransaction(JSONObject data) {

        JSONObject dataObj = (JSONObject) data.get("data");
        JSONArray inputs = (JSONArray) dataObj.get("inputs");
        JSONArray outputs = (JSONArray) dataObj.get("outputs");

        double inputSum = 0;
        double blockIoFee = 0;
        double changeAmount = 0;
        double outputSum = 0;

        for(Object input : inputs) {
            String inputValue = ((JSONObject) input).get("input_value").toString();
            inputSum += Double.parseDouble(inputValue);
        }

        for(Object output: outputs) {
            String outputCategory = ((JSONObject) output).get("output_category").toString();
            String value = ((JSONObject) output).get("output_value").toString();
            if(outputCategory.equals("blockio-fee")) {
                blockIoFee += Double.parseDouble(value);
            } else if(outputCategory.equals("change")) {
                changeAmount += Double.parseDouble(value);
            } else {
                outputSum += Double.parseDouble(value);
            }
        }

        double networkFee = inputSum - outputSum - changeAmount - blockIoFee;

        NumberFormat format = new DecimalFormat();
        format.setMinimumFractionDigits(8);

        JSONObject response = new JSONObject();
        response.put("network", dataObj.get("network"));
        response.put("network_fee", format.format(networkFee));
        response.put("blockio_fee", format.format(blockIoFee));
        response.put("total_amount_to_send", format.format(outputSum));

        return response;
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

    private JSONObject _prepare_sweep_transaction(String method, String path, JSONObject args) throws Exception {
        // handle extraction of public key from given WIF private key, store the key for later use, and return the response for prepare_sweep_transaction
        ECKey keyFromWif;

        if(args.get("private_key") == null){
            throw new Exception("Missing mandatory private_key argument.");
        }
        if(args.get("to_address") == null){
            throw new Exception("Missing mandatory to_address argument.");
        }
        String privKeyStr = args.get("private_key").toString();
        keyFromWif = Key.fromWif(privKeyStr);
        args.put("public_key", keyFromWif.getPublicKeyAsHex());
        args.put("private_key", "");

        userKeys.put(keyFromWif.getPublicKeyAsHex(), keyFromWif);
        return _request(method, path, args.toJSONString());
    }

    private JSONObject _request(String method, String path, String args) throws Exception {

        Request.Builder builder = new Request.Builder()
                .addHeader("User-Agent", UserAgent)
                .url(constructUrl(path));

        Request request;

        if(method.equals("POST")){
            MediaType type = MediaType.get("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(args, type);
            request = builder
                    .addHeader("Accept", "application/json")
                    .post(body)
                    .build();
        }
        else{
            request = builder.build();
        }
        Response response = RestClient.newCall(request).execute();
        assert response.body() != null;
        String res = Objects.requireNonNull(response.body()).string();

        RestClient.dispatcher().executorService().shutdown();
        RestClient.connectionPool().evictAll();

        return JsonUtils.parseJson(res);
    }

    private String constructUrl(String path){
        return ApiUrl + "/" + path + "?api_key=" + ApiKey;
    }

    /**
     * Block Io Methods
     */

    // Passthrough methods

    public JSONObject GetNewAddress(JSONObject args) throws Exception { 
        String argsJson = args != null ? args.toJSONString() : "";
        return _request("POST", "get_new_address", argsJson);
    }
    public JSONObject GetBalance(JSONObject args) throws Exception { 
        String argsJson = args != null ? args.toJSONString() : "";
        return _request("GET", "get_balance", argsJson);
    }
    public JSONObject GetMyAddresses(JSONObject args) throws Exception { 
        String argsJson = args != null ? args.toJSONString() : "";
        return _request("POST", "get_my_addresses", argsJson);
    }
    public JSONObject GetAddressReceived(JSONObject args) throws Exception { 
        String argsJson = args != null ? args.toJSONString() : "";
        return _request("POST", "get_address_received", argsJson);
    }
    public JSONObject GetAddressByLabel(JSONObject args) throws Exception { 
        String argsJson = args != null ? args.toJSONString() : "";
        return _request("POST", "get_address_by_label", argsJson);
    }
    public JSONObject GetAddressBalance(JSONObject args) throws Exception { 
        String argsJson = args != null ? args.toJSONString() : "";
        return _request("POST", "get_address_balance", argsJson);
    }
    public JSONObject CreateUser(JSONObject args) throws Exception { 
        String argsJson = args != null ? args.toJSONString() : "";
        return _request("POST", "create_user", argsJson);
    }
    public JSONObject GetUsers(JSONObject args) throws Exception { 
        String argsJson = args != null ? args.toJSONString() : "";
        return _request("POST", "get_users", argsJson);
    }
    public JSONObject GetUserBalance(JSONObject args) throws Exception { 
        String argsJson = args != null ? args.toJSONString() : "";
        return _request("POST", "get_user_balance", argsJson);
    }
    public JSONObject GetUserAddress(JSONObject args) throws Exception { 
        String argsJson = args != null ? args.toJSONString() : "";
        return _request("POST", "get_user_address", argsJson);
    }
    public JSONObject GetUserReceived(JSONObject args) throws Exception { 
        String argsJson = args != null ? args.toJSONString() : "";
        return _request("POST", "get_user_received", argsJson);
    }
    public JSONObject GetTransactions(JSONObject args) throws Exception { 
        String argsJson = args != null ? args.toJSONString() : "";
        return _request("POST", "get_transactions", argsJson);
    }
    public JSONObject GetNewDtrustAddress(JSONObject args) throws Exception { 
        String argsJson = args != null ? args.toJSONString() : "";
        return _request("POST", "get_new_dtrust_address", argsJson);
    }
    public JSONObject GetMyDtrustAddresses(JSONObject args) throws Exception { 
        String argsJson = args != null ? args.toJSONString() : "";
        return _request("POST", "get_my_dtrust_addresses", argsJson);
    }
    public JSONObject GetDtrustAddressByLabel(JSONObject args) throws Exception { 
        String argsJson = args != null ? args.toJSONString() : "";
        return _request("POST", "get_dtrust_address_by_label", argsJson);
    }
    public JSONObject GetDtrustTransactions(JSONObject args) throws Exception { 
        String argsJson = args != null ? args.toJSONString() : "";
        return _request("POST", "get_dtrust_transactions", argsJson);
    }
    public JSONObject GetDtrustAddressBalance(JSONObject args) throws Exception { 
        String argsJson = args != null ? args.toJSONString() : "";
        return _request("POST", "get_dtrust_address_balance", argsJson);
    }
    public JSONObject GetNetworkFeeEstimate(JSONObject args) throws Exception { 
        String argsJson = args != null ? args.toJSONString() : "";
        return _request("POST", "get_network_fee_estimate", argsJson);
    }
    public JSONObject ArchiveAddress(JSONObject args) throws Exception { 
        String argsJson = args != null ? args.toJSONString() : "";
        return _request("POST", "archive_address", argsJson);
    }
    public JSONObject UnarchiveAddress(JSONObject args) throws Exception { 
        String argsJson = args != null ? args.toJSONString() : "";
        return _request("POST", "unarchive_address", argsJson);
    }
    public JSONObject GetMyArchivedAddresses(JSONObject args) throws Exception { 
        String argsJson = args != null ? args.toJSONString() : "";
        return _request("POST", "get_my_archived_addresses", argsJson);
    }
    public JSONObject ArchiveDtrustAddress(JSONObject args) throws Exception { 
        String argsJson = args != null ? args.toJSONString() : "";
        return _request("POST", "archive_dtrust_address", argsJson);
    }
    public JSONObject UnarchiveDtrustAddress(JSONObject args) throws Exception { 
        String argsJson = args != null ? args.toJSONString() : "";
        return _request("POST", "unarchive_dtrust_address", argsJson);
    }
    public JSONObject GetMyArchivedDtrustAddresses(JSONObject args) throws Exception { 
        String argsJson = args != null ? args.toJSONString() : "";
        return _request("POST", "get_my_archived_dtrust_addresses", argsJson);
    }
    public JSONObject GetDtrustNetworkFeeEstimate(JSONObject args) throws Exception { 
        String argsJson = args != null ? args.toJSONString() : "";
        return _request("POST", "get_dtrust_network_fee_estimate", argsJson);
    }
    public JSONObject CreateNotification(JSONObject args) throws Exception { 
        String argsJson = args != null ? args.toJSONString() : "";
        return _request("POST", "create_notification", argsJson);
    }
    public JSONObject DisableNotification(JSONObject args) throws Exception { 
        String argsJson = args != null ? args.toJSONString() : "";
        return _request("POST", "disable_notification", argsJson);
    }
    public JSONObject EnableNotification(JSONObject args) throws Exception { 
        String argsJson = args != null ? args.toJSONString() : "";
        return _request("POST", "enable_notification", argsJson);
    }
    public JSONObject GetNotifications(JSONObject args) throws Exception { 
        String argsJson = args != null ? args.toJSONString() : "";
        return _request("POST", "get_notifications", argsJson);
    }
    public JSONObject GetRecentNotificationEvents(JSONObject args) throws Exception { 
        String argsJson = args != null ? args.toJSONString() : "";
        return _request("POST", "get_recent_notification_events", argsJson);
    }
    public JSONObject DeleteNotification(JSONObject args) throws Exception { 
        String argsJson = args != null ? args.toJSONString() : "";
        return _request("POST", "delete_notification", argsJson);
    }
    public JSONObject ValidateApiKey(JSONObject args) throws Exception { 
        String argsJson = args != null ? args.toJSONString() : "";
        return _request("POST", "validate_api_key", argsJson);
    }
    public JSONObject SignTransation(JSONObject args) throws Exception { 
        String argsJson = args != null ? args.toJSONString() : "";
        return _request("POST", "sign_transaction", argsJson);
    }
    public JSONObject FinalizeTransaction(JSONObject args) throws Exception { 
        String argsJson = args != null ? args.toJSONString() : "";
        return _request("POST", "finalize_transaction", argsJson);
    }
    public JSONObject GetMyAddressesWithoutBalances(JSONObject args) throws Exception { 
        String argsJson = args != null ? args.toJSONString() : "";
        return _request("POST", "get_my_addresses_without_balances", argsJson);
    }
    public JSONObject GetRawTransaction(JSONObject args) throws Exception { 
        String argsJson = args != null ? args.toJSONString() : "";
        return _request("POST", "get_raw_transaction", argsJson);
    }
    public JSONObject GetDtrustBalance(JSONObject args) throws Exception { 
        String argsJson = args != null ? args.toJSONString() : "";
        return _request("POST", "get_dtrust_balance", argsJson);
    }
    public JSONObject ArchiveAddresses(JSONObject args) throws Exception { 
        String argsJson = args != null ? args.toJSONString() : "";
        return _request("POST", "archive_addresses", argsJson);
    }
    public JSONObject UnarchiveAddresses(JSONObject args) throws Exception { 
        String argsJson = args != null ? args.toJSONString() : "";
        return _request("POST", "unarchive_addresses", argsJson);
    }
    public JSONObject ArchiveDtrustAddresses(JSONObject args) throws Exception { 
        String argsJson = args != null ? args.toJSONString() : "";
        return _request("POST", "archive_dtrust_addresses", argsJson);
    }
    public JSONObject UnarchiveDtrustAddresses(JSONObject args) throws Exception { 
        String argsJson = args != null ? args.toJSONString() : "";
        return _request("POST", "unarchive_dtrust_addresses", argsJson);
    }
    public JSONObject IsValidAddress(JSONObject args) throws Exception { 
        String argsJson = args != null ? args.toJSONString() : "";
        return _request("POST", "is_valid_address", argsJson);
    }
    public JSONObject GetCurrentPrice(JSONObject args) throws Exception { 
        String argsJson = args != null ? args.toJSONString() : "";
        return _request("POST", "get_current_price", argsJson);
    }
    public JSONObject GetAccountInfo(JSONObject args) throws Exception { 
        String argsJson = args != null ? args.toJSONString() : "";
        return _request("POST", "get_account_info", argsJson);
    }
    public JSONObject PrepareTransaction(JSONObject args) throws Exception {
        String argsJson = args != null ? args.toJSONString() : "";
        return _request("POST", "prepare_transaction", argsJson);
    }
    public JSONObject PrepareDtrustTransaction(JSONObject args) throws Exception {
        String argsJson = args != null ? args.toJSONString() : "";
        return _request("POST", "prepare_dtrust_transaction", argsJson);
    }
    public JSONObject SubmitTransaction(JSONObject args) throws Exception {
        String argsJson = args != null ? args.toJSONString() : "";
        return _request("POST", "submit_transaction", argsJson);
    }

    //Sweep Method
    public JSONObject PrepareSweepTransaction(JSONObject args) throws Exception { return _prepare_sweep_transaction("POST", "prepare_sweep_transaction", args); }
}
