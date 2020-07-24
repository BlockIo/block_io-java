package lib;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Input {

    @SerializedName("input_no")
    @Expose
    public Integer inputNo;
    @SerializedName("signatures_needed")
    @Expose
    public Integer signaturesNeeded;
    @SerializedName("data_to_sign")
    @Expose
    public String dataToSign;
    @SerializedName("signers")
    @Expose
    public ArrayList<Signer> signers = null;

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

    public ArrayList<Signer> getSigners() {
        return signers;
    }

    public void setSigners(ArrayList<Signer> signers) {
        this.signers = signers;
    }

}
