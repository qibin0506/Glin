package org.loader.superglin;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.loader.glin.NetResult;
import org.loader.glin.Result;
import org.loader.glin.helper.Helper;
import org.loader.glin.parser.Parser;

/**
 * Created by qibin on 2016/7/19. <br />
 * 解析json对象到java bean
 */

public class CommParser extends Parser {

    public CommParser(String key) {
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

            result.setCode(baseObject.getIntValue("code"));
            result.setObj(result.getCode());
            result.ok(baseObject.getBooleanValue("ok"));
            if (result.isOK()) { // ok true
                if (baseObject.containsKey(mKey)) {
                    T t = baseObject.getObject(mKey, klass);
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
