package org.loader.glinsample.chan;

import android.util.Log;

import org.loader.glin.Context;
import org.loader.glin.chan.ChanNode;

public class EndChanNode extends ChanNode {

    @Override
    public void run(Context ctx) {
        // do nothing
        Log.d("Glin", "EndChanNode do nothing");
        next();
    }
}
