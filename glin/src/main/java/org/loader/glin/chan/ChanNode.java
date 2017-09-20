package org.loader.glin.chan;

import org.loader.glin.Context;
import org.loader.glin.call.Call;

/**
 * Created by qibin on 2016/7/13.
 */

public abstract class ChanNode {
    private ChanNode mNext;
    private Context mContext;

    private boolean beforeCall;

    public final void beforeCall(boolean invoke) {
        beforeCall = invoke;
    }

    public final boolean isBeforeCall() {
        return beforeCall;
    }

    public final void exec(Context ctx) {
        mContext = ctx;
        run(ctx);
    }

    public final ChanNode nextChanNode() {
        return mNext;
    }

    public final void nextChanNode(ChanNode chanNode) {
        mNext = chanNode;
    }

    protected final void next() {
        if (mNext != null) {
            mNext.exec(mContext);
            return;
        }

        if (beforeCall) { mContext.getCall().exec();}
    }

    protected final void cancel() {
        cancel(Call.DEF_HTTP_CODE_CHAN_CANCELED);
    }

    protected final void cancel(int code) {
        cancel(code, Call.DEF_MSG_CHAN_CANCELED);
    }

    protected final void cancel(int code, String msg) {
        mNext = null;
        if (beforeCall) {
            mContext.getCall().cancel(code, msg);
        }
    }

    public abstract void run(Context ctx);
}
