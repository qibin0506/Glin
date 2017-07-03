package org.loader.glinsample.chan;

import android.util.Log;

import org.loader.glin.Context;
import org.loader.glin.chan.Chan;

public class EndChan extends Chan {

    @Override
    public void run(Context ctx) {
        // do nothing
        Log.d("Glin", "EndChan do nothing");
        next();
    }
}
