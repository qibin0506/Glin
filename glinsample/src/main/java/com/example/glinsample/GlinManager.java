package com.example.glinsample;

import org.loader.glin.Glin;

/**
 * Created by qibin on 2016/7/14.
 */

public class GlinManager {
    private static GlinManager sInstance;
    private Glin mGlin;

    public static GlinManager getsInstance() {
        if (sInstance == null) {sInstance = new GlinManager();}
        return sInstance;
    }

    private GlinManager() {
        mGlin = new Glin.Builder()
            .client(new OkClient())
            .baseUrl("http://192.168.201.39")
            .debug(true)
            .parserFactory(new FastJsonParserFactory())
            .cacheProvider(new DefaultCacheProvider(Environment.getExternalStorageDirectory() + "/test/", 2000))
            .timeout(10000)
            .build();
    }

    public Glin get() {
        return mGlin;
    }
}
