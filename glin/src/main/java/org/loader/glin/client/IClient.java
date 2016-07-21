package org.loader.glin.client;

import org.loader.glin.Callback;
import org.loader.glin.Params;
import org.loader.glin.factory.ParserFactory;

import java.util.LinkedHashMap;

/**
 * Created by qibin on 2016/7/13.
 */

public interface IClient {
    <T> void get(final String url, final Object tag, final Callback<T> callback);
    <T> void post(final String url, final Params params, final Object tag, final Callback<T> callback);
    <T> void post(final String url, final String json, final Object tag, final Callback<T> callback);
    <T> void put(final String url, final Params params, final Object tag, final Callback<T> callback);
    <T> void put(final String url, final String json, final Object tag, final Callback<T> callback);
    <T> void delete(final String url, final Object tag, final Callback<T> callback);

    void cancel(final Object tag);
    void parserFactory(ParserFactory factory);
    void timeout(long ms);
    void debugMode(boolean debug);

    LinkedHashMap<String, String> headers();
}
