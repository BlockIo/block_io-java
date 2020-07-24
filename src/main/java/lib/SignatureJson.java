package lib;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class SignatureJson {

    @SerializedName("reference_id")
    @Expose
    public String referenceId;
    @SerializedName("inputs")
    @Expose
    public ArrayList<Input> inputs = null;
    @SerializedName("encrypted_passphrase")
    @Expose
    public EncryptedPassphrase encryptedPassphrase;
    @SerializedName("unsigned_tx_hex")
    @Expose
    public String unsignedTxHex;

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public ArrayList<Input> getInputs() {
        return inputs;
    }

    public void setInputs(ArrayList<Input> inputs) {
        this.inputs = inputs;
    }

    public EncryptedPassphrase getEncryptedPassphrase() {
        return encryptedPassphrase;
    }

    public void setEncryptedPassphrase(EncryptedPassphrase encryptedPassphrase) {
        this.encryptedPassphrase = encryptedPassphrase;
    }

    public String getUnsignedTxHex() {
        return unsignedTxHex;
    }

    public void setUnsignedTxHex(String unsignedTxHex) {
        this.unsignedTxHex = unsignedTxHex;
    }

}

