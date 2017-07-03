/**
 * Copyright 2017,Smart Haier.All rights reserved
 */
package org.loader.glinsample;

import android.app.Application;

/**
 * <p class="note">File Note</p>
 * created by qibin at 2017/7/3 
 */
public class App extends Application {

    private static App app;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
    }

    public static App get() {
        return app;
    }
}
