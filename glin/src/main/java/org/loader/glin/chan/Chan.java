/**
 * Copyright 2017,Smart Haier.All rights reserved
 */
package org.loader.glin.chan;

import org.loader.glin.Context;

/**
 * <p class="note">File Note</p>
 * created by qibin at 2017/7/1 
 */
public abstract class Chan {
    private Chan mNext;
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

    public final Chan nextChan() {
        return mNext;
    }

    public final void nextChan(Chan chan) {
        mNext = chan;
    }

    protected final void next() {
        if (mNext != null) {
            mNext.exec(mContext);
            return;
        }

        if (beforeCall) { mContext.getCall().exec();}
    }

    protected final void cancel() {
        mNext = null;
        if (beforeCall) { mContext.getCall().cancel();}
    }

    public abstract void run(Context ctx);
}
