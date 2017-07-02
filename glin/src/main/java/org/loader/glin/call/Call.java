package org.loader.glin.call;

import org.loader.glin.Callback;
import org.loader.glin.Context;
import org.loader.glin.Params;
import org.loader.glin.Result;
import org.loader.glin.chan.Chan;
import org.loader.glin.chan.LogChan;
import org.loader.glin.client.IClient;

import java.util.LinkedHashMap;

/**
 * Created by qibin on 2016/7/13.
 */

public abstract class Call<T> {

    public static final int HTTP_CODE_CHAN_CANCELED = 468;
    public static final String MSG_CHAN_CANCELED = "chan canceled";

    protected String mUrl;
    protected Params mParams;
    protected LinkedHashMap<String, String> mHeaders;

    protected IClient mClient;
    protected Object mTag;

    protected boolean shouldCache;

    private Chan mBeforeChan;
    private Chan mAfterChan;

    private Chan mRequestLogChan;
    private Chan mResponseLogChan;

    private Callback<T> mCallback;

    public Call(IClient client, String url,
                Params params, Object tag,
                boolean cache) {
        mClient = client; mUrl = url;
        mParams = params; mTag = tag;

        shouldCache = cache;
    }

    public void setLogChan(LogChan logChan) {
        mRequestLogChan = logChan;
        mRequestLogChan.beforeCall(true);

        try {
            mResponseLogChan = logChan.clone();
            mResponseLogChan.beforeCall(false);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }

    public Call<T> header(LinkedHashMap<String, String> headers) {
        mHeaders = headers;
        return this;
    }

    public Call<T> shouldCache(boolean cache) {
        shouldCache = cache;
        return this;
    }

    public Call<T> before(Chan chan) {
        if (mBeforeChan == null) {
            mBeforeChan = chan;
            chan.beforeCall(true);
        } else {
            next(chan, mBeforeChan);
        }

        return this;
    }

    public Call<T> after(Chan chan) {
        if (mAfterChan == null) {
            mAfterChan = chan;
            chan.beforeCall(false);
        } else {
            next(chan, mAfterChan);
        }
        return this;
    }

    public Call<T> next(Chan chan) {
        Chan current = mAfterChan == null ? mBeforeChan : mAfterChan;
        return next(chan, current);
    }

    private Call<T> next(Chan chan, Chan header) {
        Chan current = header;

        if (current == null) {
            throw new RuntimeException("there is no before chans, please call before() first");
        }

        while(current.nextChan() != null) {
            current = current.nextChan();
        }

        current.nextChan(chan);
        chan.beforeCall(current.isBeforeCall());

        return this;
    }

    public void enqueue(Callback<T> callback) {
        mCallback = callback;

        if (mRequestLogChan != null) {
            before(mRequestLogChan);
        }

        if (mResponseLogChan != null) {
            Chan after = mAfterChan;
            mAfterChan = mResponseLogChan;
            mAfterChan.nextChan(after);
        }

        Context ctx = new Context(this);
        callback.attach(ctx, mAfterChan);

        if (mBeforeChan != null) {
            mBeforeChan.exec(ctx);
            return;
        }

        exec(callback);
    }

    public void exec() {
        exec(mCallback);
    }

    public abstract void exec(Callback<T> callback);

    public void cancel() {
        if (mCallback != null) {
            Result<T> result = new Result<>();
            result.ok(false);
            result.setCode(HTTP_CODE_CHAN_CANCELED);
            result.setMessage(MSG_CHAN_CANCELED);
            mCallback.onResponse(result);
        }
    }

    public LinkedHashMap<String, String> getHeaders() {
        return mHeaders;
    }

    public Params getParams() {
        return mParams;
    }

    public String getUrl() {
        return mUrl;
    }
}
