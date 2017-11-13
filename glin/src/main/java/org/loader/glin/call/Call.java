package org.loader.glin.call;

import org.loader.glin.Callback;
import org.loader.glin.Context;
import org.loader.glin.Params;
import org.loader.glin.Result;
import org.loader.glin.chan.ChanNode;
import org.loader.glin.chan.GlobalChanNode;
import org.loader.glin.chan.LogChanNode;
import org.loader.glin.client.IClient;

import java.util.LinkedHashMap;
import java.util.LinkedList;

/**
 * Created by qibin on 2016/7/13.
 */

public abstract class Call<T> {

    public static final int DEF_HTTP_CODE_CHAN_CANCELED = 468;
    public static final String DEF_MSG_CHAN_CANCELED = "chan canceled";

    protected String mUrl;
    protected Params mParams;
    protected LinkedHashMap<String, String> mHeaders;

    protected IClient mClient;
    protected Object mTag;

    protected boolean shouldCache;

    private ChanNode mBeforeChanNode;
    private ChanNode mAfterChanNode;

    private GlobalChanNode mBeforeGlobalChanNode;
    private GlobalChanNode mAfterGlobalChanNode;

    private Callback<T> mCallback;

    public Call(IClient client, String url,
                Params params, Object tag,
                boolean cache) {
        mClient = client;
        mUrl = url;
        mParams = params;
        mTag = tag;
        shouldCache = cache;
    }

    public Call<T> before(ChanNode chanNode) {
        if (mBeforeChanNode == null) {
            mBeforeChanNode = chanNode;
            chanNode.beforeCall(true);
        } else {
            next(chanNode, mBeforeChanNode);
        }

        return this;
    }

    public Call<T> after(ChanNode chanNode) {
        if (mAfterChanNode == null) {
            mAfterChanNode = chanNode;
            chanNode.beforeCall(false);
        } else {
            next(chanNode, mAfterChanNode);
        }
        return this;
    }

    public Call<T> next(ChanNode chanNode) {
        ChanNode current = mAfterChanNode == null ? mBeforeChanNode : mAfterChanNode;
        return next(chanNode, current);
    }

    private Call<T> next(ChanNode chanNode, ChanNode header) {
        ChanNode current = header;

        if (current == null) {
            throw new RuntimeException("there is no before chans, please call before() first");
        }

        while(current.nextChanNode() != null) {
            current = current.nextChanNode();
        }

        current.nextChanNode(chanNode);
        chanNode.beforeCall(current.isBeforeCall());

        return this;
    }

    public void enqueue(Callback<T> callback) {
        mCallback = callback;

        if (mBeforeGlobalChanNode != null) {
            before(mBeforeGlobalChanNode);
        }

        if (mAfterGlobalChanNode != null) {
            ChanNode after = mAfterChanNode;
            mAfterChanNode = mAfterGlobalChanNode;
            mAfterChanNode.nextChanNode(after);
        }

        Context ctx = new Context(this);
        callback.attach(ctx, mAfterChanNode);

        if (mBeforeChanNode != null) {
            mBeforeChanNode.exec(ctx);
            return;
        }

        exec(callback);
    }

    public void exec() { exec(mCallback);}

    public abstract void exec(Callback<T> callback);

    public void cancel(int code, String msg) {
        if (mCallback != null) {
            Result<T> result = new Result<>();
            result.ok(false);
            result.setCode(code);
            result.setMessage(msg);
            mCallback.onResponse(result);
        }
    }

    public void setGlobalChanNode(GlobalChanNode before, GlobalChanNode after) {
        mBeforeGlobalChanNode = before;
        mAfterGlobalChanNode = after;

        if (mBeforeGlobalChanNode != null) {
            mBeforeGlobalChanNode.beforeCall(true);
        }

        if (mAfterGlobalChanNode != null) {
            mAfterGlobalChanNode.beforeCall(false);
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

    public LinkedHashMap<String, String> getHeaders() {
        return mHeaders;
    }

    public Params getParams() {
        return mParams;
    }

    public String getUrl() {
        return mUrl;
    }

    public boolean shouldCache() {
        return shouldCache;
    }

    public void rewriteUrl(String url) {
        mUrl = url;
    }
}
