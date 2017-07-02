package org.loader.glin.cache;

import org.loader.glin.RawResult;
import org.loader.glin.Result;

public interface ICacheProvider {
    /**
     * get cache by key of {@link #getKey(String, String)}
     * @param key key
     * @param  klass type of data struct, see also {@link org.loader.glin.helper.Helper}
     * @param isList is result a List
     * @return cache data
     */
    <T> Result<T> get(String key, Class<T> klass, boolean isList);

    /**
     * cache data
     * @param key see {@link #getKey(String, String)}
     * @param netResult the origin result
     * @param result parsed result
     */
    <T> void put(String key, RawResult netResult, Result<T> result);

    /**
     * provide the key
     * @param url the request url
     * @param params the request params, maybe key1=value1&key2=value2 while your request is POST, maybe null while your request is GET, maybe json string while your request is POST Json
     * @return
     */
    String getKey(String url, String params);
}
