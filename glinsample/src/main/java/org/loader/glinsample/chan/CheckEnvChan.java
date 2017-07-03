package org.loader.glinsample.chan;

import android.util.Log;

import org.loader.glin.Context;
import org.loader.glin.chan.Chan;

public class CheckEnvChan extends Chan {

    @Override
    public void run(Context ctx) {
        Log.d("Glin", "CheckEnvChan");
        next();
    }
}
