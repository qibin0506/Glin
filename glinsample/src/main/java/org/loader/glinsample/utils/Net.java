package org.loader.glinsample.utils;

import android.os.Environment;
import android.util.Log;

import org.loader.glin.Glin;
import org.loader.glin.cache.DefaultCacheProvider;
import org.loader.glin.chan.GlobalChanNode;
import org.loader.glin.chan.LogChanNode;
import org.loader.glin.helper.LogHelper;
import org.loader.okclient.OkClient;

public class Net {

    private static final LogHelper.LogPrinter printer = new LogHelper.LogPrinter() {
        @Override
        public void print(String tag, String content) {
            Log.d(tag, content);
        }
    };

    private static Glin glin;

    public static Glin get() {
        if (glin == null) {
            String cachePath = Environment.getExternalStorageDirectory() + "/cache";

            LogChanNode beforeLog = new LogChanNode(true, printer);
            LogChanNode afterLog = new LogChanNode(true, printer);

            GlobalChanNode before = new GlobalChanNode(beforeLog);
            GlobalChanNode after = new GlobalChanNode(afterLog);

            glin = new Glin.Builder()
                    .baseUrl("http://103.50.253.220:8891")
                    .client(new OkClient())
                    .globalChanNode(before, after)
                    .parserFactory(new Parsers())
                    .cacheProvider(new DefaultCacheProvider(cachePath, 1024*1024L))
                    .timeout(5000L)
                    .build();
        }

        return glin;
    }
}
