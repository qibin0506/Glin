package org.loader.glin.call;

import org.loader.glin.Callback;
import org.loader.glin.Params;
import org.loader.glin.client.IClient;

/**
 * Created by qibin on 2016/7/14.
 */

public class PutCall<T> extends Call<T> {

    public PutCall(IClient client, String url, Params params, Object tag) {
        super(client, url, params, tag);
    }

    @Override
    public void enqueue(Callback<T> callback) {
        mClient.put(mUrl, mHeaders, mParams, mTag, callback);
    }
}
