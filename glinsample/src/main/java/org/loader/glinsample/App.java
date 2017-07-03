package org.loader.glinsample;

import android.app.Application;


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
