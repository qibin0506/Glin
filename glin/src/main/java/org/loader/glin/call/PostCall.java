package org.loader.glin.call;

import org.loader.glin.Callback;
import org.loader.glin.Params;
import org.loader.glin.client.IClient;

/**
 * Created by qibin on 2016/7/13.
 */

public class PostCall<T> extends Call<T> {

    public PostCall(IClient client, String url, Params params, Object tag) {
        super(client, url, params, tag);
    }

    @Override
    public void enqueue(final Callback<T> callback) {
        mClient.post(mUrl, mHeaders, mParams, mTag, callback);
    }
}
