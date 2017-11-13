package org.loader.glin.chan;

import org.loader.glin.Context;

/**
 * <p class="note">File Note</p>
 * Created by qibin on 2017/11/13.
 */

public class GlobalChanNode extends ChanNode {

    private ChanNode[] mGlobalChanNodes;

    public GlobalChanNode(ChanNode ...chanNodes) {
        mGlobalChanNodes = chanNodes;
    }

    @Override
    public void run(Context ctx) {
        ChanNode[] globalChanNodes = mGlobalChanNodes;

        if (globalChanNodes == null) { return;}
        for (ChanNode item : globalChanNodes) {
            if (item == null) { continue;}

            item.beforeCall(isBeforeCall());
            item.exec(ctx);
        }
    }
}
