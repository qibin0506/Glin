package com.haiersmart.commonbizlib.parser;

import com.alibaba.fastjson.JSON;

import org.loader.glin.NetResult;
import org.loader.glin.Result;
import org.loader.glin.helper.Helper;
import org.loader.glin.parser.Parser;

/**
 * Created by qibin on 2016/10/17.
 */

public class CompleteObjectParser extends Parser {

    public CompleteObjectParser() {}

    @Override
    public <T> Result<T> parse(Class<T> klass, NetResult netResult) {
        Result<T> result = new Result<>();
        result.setObj(netResult.getStatusCode());
        result.setMessage(netResult.getMessage());
        try {
            T res = (T) JSON.parseObject(netResult.getResponse(), Helper.getType(klass));
            result.setResult(res);
            result.ok(true);
        } catch (Exception e) {
            e.printStackTrace();
            result.ok(false);
        }

        return result;
    }
}
