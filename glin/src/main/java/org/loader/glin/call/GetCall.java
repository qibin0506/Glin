package org.loader.glin.call;

import org.loader.glin.Callback;
import org.loader.glin.Params;
import org.loader.glin.client.IClient;

/**
 * Created by qibin on 2016/7/13.
 */

public class GetCall<T> extends Call<T> {

    public GetCall(IClient client, String url, Params params, Object tag) {
        super(client, url, params, tag);
    }

    @Override
    public void enqueue(final Callback<T> callback) {
        String query = mParams.encode();
        String url =  query == null ? mUrl : mUrl + "?" + mParams.encode();
        mClient.get(url, mHeaders, mTag, callback);
    }
}
