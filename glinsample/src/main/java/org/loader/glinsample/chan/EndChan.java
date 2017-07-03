/**
 * Copyright 2017,Smart Haier.All rights reserved
 */
package org.loader.glinsample.chan;

import android.util.Log;

import org.loader.glin.Context;
import org.loader.glin.chan.Chan;

/**
 * <p class="note">File Note</p>
 * created by qibin at 2017/7/3 
 */
public class EndChan extends Chan {

    @Override
    public void run(Context ctx) {
        // do nothing
        Log.d("Glin", "EndChan do nothing");
        next();
    }
}
