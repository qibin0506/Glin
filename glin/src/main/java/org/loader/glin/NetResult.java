package org.loader.glin;

/**
 * Created by qibin on 2016/10/17.
 */

public class NetResult {
    private int statusCode;
    private String message;
    private String response;

    public NetResult() {}

    public NetResult(int statusCode, String message, String response) {
        this.statusCode = statusCode;
        this.message = message;
        this.response = response;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}
