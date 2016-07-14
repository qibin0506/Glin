package org.loader.glin.parser;

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

    public abstract <T> Result<T> parse(Class<T> klass, String response);
}
