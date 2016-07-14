package org.loader.glin;

/**
 * Created by qibin on 2016/7/13.
 */

public class Result<T> {
    private boolean ok;
    private String message;
    private T result;

    public boolean isOK() {
        return ok;
    }

    public void ok(boolean ok) {
        this.ok = ok;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String mMessage) {
        this.message = mMessage;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T mResult) {
        this.result = mResult;
    }
}
