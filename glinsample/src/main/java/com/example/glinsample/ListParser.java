package com.example.glinsample;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.loader.glin.Result;
import org.loader.glin.helper.Helper;

/**
 * Created by qibin on 2016/7/13.
 */

public class ListParser extends org.loader.glin.parser.Parser {

    public ListParser() {

    }

    public ListParser(String key) {
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
                klass = Helper.getDeepType(klass);
                JSONArray arr = baseObject.getJSONArray(mKey);
                T t = (T) baseObject.parseArray(arr.toString(), klass);
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
