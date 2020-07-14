package io.block.lib;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Signer {

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
