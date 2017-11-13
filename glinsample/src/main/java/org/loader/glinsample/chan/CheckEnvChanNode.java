package org.loader.glinsample.chan;

import android.util.Log;

import org.loader.glin.Context;
import org.loader.glin.chan.ChanNode;

public class CheckEnvChanNode extends ChanNode {

    @Override
    public void run(Context ctx) {
//        ctx.getCall().rewriteUrl("http://baidu.com");
        Log.d("Glin", "CheckEnvChanNode");
        next();
    }
}
