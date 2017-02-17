package org.loader.glin.helper;

import org.loader.glin.Callback;
import org.loader.glin.NetResult;
import org.loader.glin.Result;
import org.loader.glin.cache.ICacheProvider;
import org.loader.glin.factory.ParserFactory;
import org.loader.glin.interceptor.IResultInterceptor;

import java.util.List;

/**
 * ClientHelper, useful when you convert response, cache your data and intercept your callback
 */
public final class ClientHelper {

    private ParserFactory mParserFactory;
    private ICacheProvider mCacheProvider;
    private IResultInterceptor mResultInterceptor;

    public ClientHelper parserFactory(ParserFactory parserFactory) {
        mParserFactory = parserFactory;
        return this;
    }

    public ClientHelper cacheProvider(ICacheProvider cacheProvider) {
        mCacheProvider = cacheProvider;
        return this;
    }

    public ClientHelper resultInterceptor(IResultInterceptor resultInterceptor) {
        mResultInterceptor = resultInterceptor;
        return this;
    }

    /**
     * convert the http response to Result
     * @param callback
     * @param netResult
     * @param <T>
     * @return return the result
     */
    public <T> Result<T> parseResponse(Callback<T> callback, NetResult netResult) {
        final Class<?> callbackKlass = callback.getClass();
        final Class<T> dataKlass = Helper.getType(callbackKlass);
        final boolean resultTypeIsArray = resultTypeIsArray(dataKlass);

        Result<T> result;
        if (resultTypeIsArray) {
            Class<T> klass = Helper.getDeepType(callbackKlass);
            result = mParserFactory.getListParser().parse(klass, netResult);
        } else {
            result = mParserFactory.getParser().parse(dataKlass, netResult);
        }

        return result;
    }

    /**
     * generate cache key
     * @param url
     * @param params
     * @return return key of cache
     */
    public String getCacheKey(String url, String params) {
        return mCacheProvider == null ? null : mCacheProvider.getKey(url, params);
    }

    /**
     * use cache
     * @param shouldCache
     * @param cacheKey
     * @param callback
     * @param <T>
     * @return return the cache Result, if no cache was found return null
     */
    public <T> Result<T> useCache(boolean shouldCache, String cacheKey, Callback<T> callback) {
        final Class<?> callbackKlass = callback.getClass();
        final Class<T> dataKlass = Helper.getType(callbackKlass);
        final boolean resultTypeIsArray = resultTypeIsArray(dataKlass);
        if (shouldCache && mCacheProvider != null) {
            // data struct or List.class
            Result<T> result;
            if (resultTypeIsArray) {
                Class<T> klass = Helper.getDeepType(callbackKlass);
                result = mCacheProvider.get(cacheKey, klass, true);
            }else {
                result = mCacheProvider.get(cacheKey, dataKlass, false);
            }

            if (result != null && result.isOK()) {
                return result;
            }
        }

        return null;
    }

    /**
     * cache your response if should cache
     * @param shouldCache
     * @param cacheKey
     * @param result
     * @param netResult
     * @param <T>
     * @return return true if cache ok, otherwise return false
     */
    public <T> boolean cache(boolean shouldCache, String cacheKey, Result<T> result,
                          NetResult netResult) {
        if (shouldCache && mCacheProvider != null && result.isOK()) {
            mCacheProvider.put(cacheKey, netResult, result);
            return true;
        }
        return false;
    }

    /**
     * testing should intercept callback
     * @param result
     * @param <T>
     * @return return true if should intercept your callback, otherwise return false
     */
    public <T> boolean shouldInterceptResult(final Result<T> result) {
        return mResultInterceptor != null && mResultInterceptor.intercept(result);
    }

    private <T> boolean resultTypeIsArray(Class<T> dataKlass) {
        return List.class.isAssignableFrom(dataKlass);
    }
}
