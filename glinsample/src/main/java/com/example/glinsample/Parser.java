package com.example.glinsample;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import org.loader.glin.Result;
import org.loader.glin.helper.Helper;

/**
 * Created by qibin on 2016/7/13.
 */

public class Parser extends org.loader.glin.parser.Parser {

    public Parser() {

    }

    public Parser(String key) {
        super(key);
    }

    @Override
    public <T> Result<T> parse(Class<T> klass, String response) {
        Result<T> result = new Result<>();
        try {
            JSONObject baseObject = JSON.parseObject(response);
            if(!baseObject.getBooleanValue("ok")) {
                result.setMessage(baseObject.getString("message"));
            }else {
                klass = Helper.getType(klass);
                T t = baseObject.getObject(mKey, klass);
                result.ok(true);
                result.setResult(t);
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.setMessage("error parse");
        }

        result.ok(false);
        return result;
    }
}
