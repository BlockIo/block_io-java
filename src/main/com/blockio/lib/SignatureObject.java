package com.blockio.lib;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

class SignatureJson {

    @SerializedName("reference_id")
    @Expose
    private String referenceId;
    @SerializedName("inputs")
    @Expose
    private List<Input> inputs = null;
    @SerializedName("encrypted_passphrase")
    @Expose
    private EncryptedPassphrase encryptedPassphrase;
    @SerializedName("unsigned_tx_hex")
    @Expose
    private String unsignedTxHex;

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public List<Input> getInputs() {
        return inputs;
    }

    public void setInputs(List<Input> inputs) {
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

class EncryptedPassphrase {

    @SerializedName("signer_public_key")
    @Expose
    private String signerPublicKey;
    @SerializedName("passphrase")
    @Expose
    private String passphrase;

    public String getSignerPublicKey() {
        return signerPublicKey;
    }

    public void setSignerPublicKey(String signerPublicKey) {
        this.signerPublicKey = signerPublicKey;
    }

    public String getPassphrase() {
        return passphrase;
    }

    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }

}
class Input {

    @SerializedName("input_no")
    @Expose
    private Integer inputNo;
    @SerializedName("signatures_needed")
    @Expose
    private Integer signaturesNeeded;
    @SerializedName("data_to_sign")
    @Expose
    private String dataToSign;
    @SerializedName("signers")
    @Expose
    private List<Signer> signers = null;

    public Integer getInputNo() {
        return inputNo;
    }

    public void setInputNo(Integer inputNo) {
        this.inputNo = inputNo;
    }

    public Integer getSignaturesNeeded() {
        return signaturesNeeded;
    }

    public void setSignaturesNeeded(Integer signaturesNeeded) {
        this.signaturesNeeded = signaturesNeeded;
    }

    public String getDataToSign() {
        return dataToSign;
    }

    public void setDataToSign(String dataToSign) {
        this.dataToSign = dataToSign;
    }

    public List<Signer> getSigners() {
        return signers;
    }

    public void setSigners(List<Signer> signers) {
        this.signers = signers;
    }

}

class Signer {

    @SerializedName("signer_public_key")
    @Expose
    private String signerPublicKey;
    @SerializedName("signed_data")
    @Expose
    private String signedData;

    public String getSignerPublicKey() {
        return signerPublicKey;
    }

    public void setSignerPublicKey(String signerPublicKey) {
        this.signerPublicKey = signerPublicKey;
    }

    public String getSignedData() {
        return signedData;
    }

    public void setSignedData(String signedData) {
        this.signedData = signedData;
    }

}