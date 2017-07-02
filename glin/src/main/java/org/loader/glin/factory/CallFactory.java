package org.loader.glin.factory;

import org.loader.glin.annotation.DEL;
import org.loader.glin.annotation.GET;
import org.loader.glin.annotation.JSON;
import org.loader.glin.annotation.POST;
import org.loader.glin.annotation.PUT;
import org.loader.glin.call.Call;
import org.loader.glin.call.DelCall;
import org.loader.glin.call.GetCall;
import org.loader.glin.call.JsonCall;
import org.loader.glin.call.PostCall;
import org.loader.glin.call.PutCall;

import java.lang.annotation.Annotation;
import java.util.HashMap;

/**
 * Created by qibin on 2016/7/13.
 */

public class CallFactory {

    private HashMap<Class<? extends Annotation>, Class<? extends Call>> mMapping = new HashMap<>();

    public CallFactory() {
        autoRegist();
    }

    private void autoRegist() {
        register(JSON.class, JsonCall.class);
        register(GET.class, GetCall.class);
        register(POST.class, PostCall.class);
        register(PUT.class, PutCall.class);
        register(DEL.class, DelCall.class);
    }

    public void register(Class<? extends Annotation> key, Class<? extends Call> value) {
        mMapping.put(key, value);
    }

    public Class<? extends Call> get(Class<? extends Annotation> key) {
        return mMapping.get(key);
    }

    public HashMap<Class<? extends Annotation>, Class<? extends Call>> get() {
        return mMapping;
    }
}
