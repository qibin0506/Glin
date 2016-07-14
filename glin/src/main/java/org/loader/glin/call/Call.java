package org.loader.glin.call;

import org.loader.glin.Callback;
import org.loader.glin.Params;
import org.loader.glin.client.IClient;

/**
 * Created by qibin on 2016/7/13.
 */

public abstract class Call<T> {
    protected String mUrl;
    protected Params mParams;
    protected IClient mClient;
    protected Object mTag;

    public Call(IClient client, String url, Params params, Object tag) {
        mClient = client;
        mUrl = url;
        mParams = params;
        mTag = tag;
    }

    public abstract void enqueue(Callback<T> callback);
}
