package org.loader.glin;

/**
 * Created by qibin on 2016/7/13.
 */

public abstract class Callback<T> {
    public abstract void onResponse(Result<T> result);
}

