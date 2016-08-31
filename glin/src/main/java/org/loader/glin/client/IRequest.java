package org.loader.glin.client;

import org.loader.glin.Callback;
import org.loader.glin.Params;

import java.util.LinkedHashMap;

/**
 * Created by qibin on 2016/8/29.
 */

public interface IRequest {
    <T> void get(final String url, final LinkedHashMap<String, String> header, final Object tag, final Callback<T> callback);
    <T> void post(final String url, final LinkedHashMap<String, String> header, final Params params, final Object tag, final Callback<T> callback);
    <T> void post(final String url, final LinkedHashMap<String, String> header, final String json, final Object tag, final Callback<T> callback);
    <T> void put(final String url, final LinkedHashMap<String, String> header, final Params params, final Object tag, final Callback<T> callback);
    <T> void put(final String url, final LinkedHashMap<String, String> header, final String json, final Object tag, final Callback<T> callback);
    <T> void delete(final String url, final LinkedHashMap<String, String> header, final Object tag, final Callback<T> callback);
}
