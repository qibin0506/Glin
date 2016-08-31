package org.loader.glin.call;

import org.loader.glin.Callback;
import org.loader.glin.Params;
import org.loader.glin.client.IClient;

/**
 * Created by qibin on 2016/7/13.
 */

public class JsonCall<T> extends Call<T> {

    public JsonCall(IClient client, String url, Params params, Object tag) {
        super(client, url, params, tag);
    }

    @Override
    public void enqueue(final Callback<T> callback) {
        String json = mParams.getParams(Params.DEFAULT_JSON_KEY);
        if (json == null) {
            throw new UnsupportedOperationException("cannot find json");
        }
        mClient.post(mUrl, mHeaders, mParams.getParams(Params.DEFAULT_JSON_KEY), mTag, callback);
    }
}
