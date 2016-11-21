package org.loader.glin.cache;

import org.loader.glin.NetResult;
import org.loader.glin.Result;
import org.loader.glin.helper.Helper;
import org.loader.glin.helper.SerializeHelper;

import java.io.File;

public class DefaultCacheProvider implements ICacheProvider {

    private static final String SUFFIX = ".gc";

    private String mCachePath;
    private long mMaxCacheSize;

    public DefaultCacheProvider(String cachePath, long maxCacheSize) {
        mCachePath = cachePath.endsWith("/") ? cachePath : cachePath + "/";
        mMaxCacheSize = maxCacheSize;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Result<T> get(String key, Class<T> klass, boolean isList) {
        T cacheResult = SerializeHelper.unSerialize(mCachePath, key + SUFFIX);
        if (cacheResult == null) { return null;}

        Result<T> result = new Result<>();
        result.setOk(true);
        result.setMessage("");
        result.setResult(cacheResult);
        result.setObj(200);
        result.setCache(true);

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> void put(String key, NetResult netResult, Result<T> result) {
        checkCacheSize();
        SerializeHelper.serialize(mCachePath, key + SUFFIX, result.getResult());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getKey(String url, String params) {
        String cacheName = url + (params == null ? "" : params);
        cacheName = Helper.md5(cacheName);
        return cacheName == null ? "" : cacheName;
    }

    private void checkCacheSize() {
        File dir = new File(mCachePath);
        File[] files = dir.listFiles();

        long size = 0L;
        for (File item : files) {
            size += item.length();
        }

        if (mMaxCacheSize <= size) {
            clearAllCache(files);
        }
    }

    private void clearAllCache(File[] files) {
        TAG:
        for (File file : files) {
            for (int i = 0; i < 3; i++) {
                if (file.delete()) {continue TAG;}
            }
        }
    }
}
