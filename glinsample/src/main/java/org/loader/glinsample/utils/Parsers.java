package org.loader.glinsample.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.loader.glin.RawResult;
import org.loader.glin.Result;
import org.loader.glin.factory.ParserFactory;
import org.loader.glin.parser.Parser;

public class Parsers implements ParserFactory {

    @Override
    public Parser getParser() {
        return new Parser() {
            @Override
            public <T> Result<T> parse(Class<T> klass, RawResult netResult) {
                Result<T> result = new Result<>();
                try {
                    JSONObject baseObject = JSON.parseObject(netResult.getResponse());
                    if (baseObject.getBoolean("ok")) {
                        if (baseObject.containsKey("data")) {
                            T t = baseObject.getObject("data", klass);
                            result.setResult(t);
                            result.ok(true);
                            return result;
                        }
                    }

                    throw new Exception();
                } catch (Exception e) {
                    e.printStackTrace();
                    result.ok(false);
                    result.setMessage("error");
                }
                return result;
            }
        };
    }

    @Override
    public Parser getListParser() {
        // empty
        return null;
    }
}
