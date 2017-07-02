package org.loader.glin;

import org.loader.glin.chan.Chan;

/**
 * Created by qibin on 2016/7/13.
 */

public abstract class Callback<T> {

    private Context mContext;
    private Chan mAfterChan;

    public final void attach(Context ctx, Chan afterChan) {
        mContext = ctx;
        mAfterChan = afterChan;
    }

    public final void afterResponse(Result<T> result, RawResult rawResult) {
        mContext.setRawResult(rawResult);
        mContext.setResult(result);

        if (mAfterChan != null) {
            mAfterChan.exec(mContext);
        }
    }

    public abstract void onResponse(Result<T> result);
}

