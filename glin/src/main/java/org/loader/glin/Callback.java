package org.loader.glin;

import org.loader.glin.chan.ChanNode;

/**
 * Created by qibin on 2016/7/13.
 */

public abstract class Callback<T> {

    private Context mContext;
    private ChanNode mAfterChanNode;

    public final void attach(Context ctx, ChanNode afterChanNode) {
        mContext = ctx;
        mAfterChanNode = afterChanNode;
    }

    public final void afterResponse(Result<T> result, RawResult rawResult) {
        mContext.setRawResult(rawResult);
        mContext.setResult(result);

        if (mAfterChanNode != null) {
            mAfterChanNode.exec(mContext);
        }
    }

    public abstract void onResponse(Result<T> result);
}

