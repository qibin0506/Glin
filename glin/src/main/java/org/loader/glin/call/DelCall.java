package org.loader.glin.call;

import org.loader.glin.Callback;
import org.loader.glin.Params;
import org.loader.glin.chan.LogChan;
import org.loader.glin.client.IClient;

/**
 * Created by qibin on 2016/7/14.
 */

public class DelCall<T> extends Call<T> {

    public DelCall(IClient client, String url,
                   Params params, Object tag,
                   boolean cache, LogChan logChan) {
        super(client, url, params, tag, cache, logChan);
    }

    @Override
    public void exec(Callback<T> callback) {
        mClient.delete(mUrl, mHeaders, mTag, shouldCache, callback);
    }
}
