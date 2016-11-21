package org.loader.glin.parser;

import org.loader.glin.NetResult;
import org.loader.glin.Result;

/**
 * Created by qibin on 2016/7/13.
 */

public abstract class Parser {
    public String mKey;

    public Parser() {

    }

    public Parser(String key) {
        mKey = key;
    }

    /**
     *
     * @param klass the class of data struct
     * @param netResult
     * @param <T>
     * @return
     */
    public abstract <T> Result<T> parse(Class<T> klass, NetResult netResult);
}
