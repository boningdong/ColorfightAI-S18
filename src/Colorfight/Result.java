package Colorfight;

import org.json.simple.JSONObject;

public class Result {

    public boolean isSuccessful = false;
    public int errorCode = -1;
    public String errorMessage = "";

    Result(JSONObject result) {
        errorCode = Math.toIntExact((long)result.get("err_code"));
        isSuccessful = errorCode == 0;
        errorMessage = isSuccessful ? "None" : result.get("err_msg").toString();
    }

    Result(boolean isSuccessful, int errorCode, String errorMessage) {
        this.isSuccessful = isSuccessful;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Status: " + (isSuccessful ? "Succeed" : "Failed") + " ");
        sb.append("ErrCode: " + errorCode + " ");
        sb.append("ErrMsg: " + errorMessage);
        return sb.toString();
    }
}
