package org.loader.glin;

/**
 * Created by qibin on 2016/7/13.
 */

public class Result<T> {
    private boolean ok;
    private String message;
    private T result;
    private Object obj;
    private boolean isCache;

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

    public boolean isOk() {
        return ok;
    }

    public void setOk(boolean ok) {
        this.ok = ok;
    }

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }

    public <V> V assertGetObj() {
        return (V) getObj();
    }

    public boolean isCache() {
        return isCache;
    }

    public void setCache(boolean cache) {
        isCache = cache;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ok: ").append(ok).append("\n");
        sb.append("message: ").append(message).append("\n");
        sb.append("obj: ").append(obj).append("\n");
        sb.append("result: ").append(result);
        sb.append("is_cache: ").append(isCache);

        return sb.toString();
    }
}
