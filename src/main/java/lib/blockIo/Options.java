package lib.blockIo;

public class Options {
    private String apiUrl;
    private boolean allowNoPin;

    Options(String url){
        apiUrl = url;
    }
    Options(boolean opt){
        allowNoPin = opt;
    }
    Options(String url, boolean opt){
        apiUrl = url;
        allowNoPin = opt;
    }
    Options(){
        apiUrl = "";
        allowNoPin = false;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public boolean isAllowNoPin() {
        return allowNoPin;
    }

    public void setAllowNoPin(boolean allowNoPin) {
        this.allowNoPin = allowNoPin;
    }
}
