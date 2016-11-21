package org.loader.superglin;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.loader.glin.NetResult;
import org.loader.glin.Result;
import org.loader.glin.helper.Helper;
import org.loader.glin.parser.Parser;

/**
 * Created by qibin on 2016/7/13. <br />
 * 解析json数组到List
 */

public class ListParser extends Parser {

    public ListParser(String key) {
        super(key);
    }

    @Override
    public <T> Result<T> parse(Class<T> klass, NetResult netResult) {
        Result<T> result = new Result<>();
        try {
            JSONObject baseObject = JSON.parseObject(netResult.getResponse());
            if (baseObject.containsKey("message")) {
                result.setMessage(baseObject.getString("message"));// message always get
            }

            result.setObj(baseObject.getIntValue("code"));
            result.ok(baseObject.getBooleanValue("ok"));
            if (result.isOK()) { // ok true
                if (baseObject.containsKey(mKey)) {
                    JSONArray arr = baseObject.getJSONArray(mKey);
                    T t = (T) baseObject.parseArray(arr.toString(), klass);
                    result.setResult(t);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.setMessage("数据获取失败");
        }

        return result;
    }
}