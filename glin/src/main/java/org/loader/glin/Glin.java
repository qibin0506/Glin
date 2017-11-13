/**
 * Glin, A retrofit like network framework <br />
 *
 * Usage: <br />
 *  1. write your client and parser, config glin
 *      Glin glin = new Glin.Builder()
             .client(new OkClient())
             .baseUrl("http://192.168.201.39")
             .debug(true)
             .parserFactory(new FastJsonParserFactory())
             .timeout(10000)
             .build();
 *
 *  2. create an interface
 *      public interface UserBiz {
            @POST("/users/list")
            Call<User> list(@Arg("name") String userName);
        }
 *
 *  3. request the network and callback
 *      UserBiz biz = glin.create(UserBiz.class, getClass().getName());
 *      Call<User> call = biz.list("qibin");
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Result<User> result) {
                if (result.isOK()) {
                    Toast.makeText(MainActivity.this, result.getResult().getName(), Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(MainActivity.this, result.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
 */
package org.loader.glin;

import org.loader.glin.annotation.Arg;
import org.loader.glin.annotation.JSON;
import org.loader.glin.annotation.Path;
import org.loader.glin.annotation.ShouldCache;
import org.loader.glin.cache.ICacheProvider;
import org.loader.glin.call.Call;
import org.loader.glin.chan.GlobalChanNode;
import org.loader.glin.chan.LogChanNode;
import org.loader.glin.client.IClient;
import org.loader.glin.factory.CallFactory;
import org.loader.glin.factory.ParserFactory;
import org.loader.glin.interceptor.IResultInterceptor;
import org.loader.glin.utils.Pair;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by qibin on 2016/7/13.
 */

public class Glin {
    private Builder mBuilder;
    private CallFactory mCallFactory;

    private Glin(Builder builder) {
        mBuilder = builder;
        mCallFactory = new CallFactory();
    }

    @SuppressWarnings("unchecked")
    public <T> T create(Class<T> klass, Object tag) {
        return (T) Proxy.newProxyInstance(klass.getClassLoader(),
                new Class<?>[] {klass}, new Handler(tag));
    }

    public void cancel(String tag) {
        mBuilder.client.cancel(tag);
    }

    public void register(Class<? extends Annotation> key, Class<? extends Call> value) {
        mCallFactory.register(key, value);
    }

    class Handler implements InvocationHandler {
        private Object mTag;

        public Handler(Object tag) {
            mTag = tag;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Class<? extends Annotation> key = null;
            String path = null;

            HashMap<Class<? extends Annotation>, Class<? extends Call>> mapping = mCallFactory.get();
            Class<? extends Annotation> item;
            Annotation anno;
            for (Iterator<Class<? extends Annotation>> iterator = mapping.keySet().iterator();
                 iterator.hasNext();) {
                item = iterator.next();
                if (method.isAnnotationPresent(item)) {
                    key = item;
                    anno = method.getAnnotation(item);
                    path = (String) anno.getClass().getDeclaredMethod("value").invoke(anno);
                    break;
                }
            }

            if (key == null) {
                throw new UnsupportedOperationException("cannot find annotations");
            }

            Class<? extends Call> callKlass = mCallFactory.get(key);
            if (callKlass == null) {
                throw new UnsupportedOperationException("cannot find calls");
            }

            boolean shouldCache = method.isAnnotationPresent(ShouldCache.class);

            Pair<String, Params> pair = new Pair<>(justUrl(path), new Params());
            params(pair, method, args);

            Constructor<? extends Call> constructor = callKlass.getConstructor(IClient.class,
                    String.class, Params.class, Object.class, boolean.class);

            Call<?> call = constructor.newInstance(mBuilder.client, pair.first, pair.second,
                    mTag, shouldCache);

            call.setGlobalChanNode(mBuilder.beforeGlobalChanNode, mBuilder.afterGlobalChanNode);

            return call;
        }

        private String justUrl(String path) {
            String url = mBuilder.baseUrl == null ? "" : mBuilder.baseUrl;
            path = path == null ? "" : path;
            if (isFullUrl(path)) { url = path;}
            else { url += path;}
            return url;
        }

        private boolean isFullUrl(String url) {
            if (url == null || url.length() == 0) { return false;}
            if (url.toLowerCase().startsWith("http://")) { return true;}
            if (url.toLowerCase().startsWith("https://")) {return true;}
            return false;
        }

        private void params(Pair<String, Params> pair, Method method, Object[] args) {
            if (args == null || args.length == 0) {
                return;
            }

            // method.getParameterAnnotations.length always equals args.length
            Annotation[][] paramsAnno = method.getParameterAnnotations();
//            if (method.isAnnotationPresent(JSON.class)) {
//                params.add(Params.DEFAULT_JSON_KEY, args[0]);
//                return params;
//            }

            int length = paramsAnno.length;
            for (int i = 0; i < length; i++) {
                if (paramsAnno[i].length == 0) {
                    // there is no annotation on this param,
                    // so, maybe it is a json value when the method is JSON annotation presented
                    if (method.isAnnotationPresent(JSON.class)) {
                        pair.second.add(Params.DEFAULT_JSON_KEY, args[i]);
                    }
                } else {
                    if (paramsAnno[i][0] instanceof Arg) {
                        pair.second.add(((Arg)paramsAnno[i][0]).value(), args[i]);
                    } else if (paramsAnno[i][0] instanceof Path) {
                        pair.first = pair.first.replaceAll("\\{:"+((Path)paramsAnno[i][0]).value()+"\\}",
                                args[i].toString());
                    }
                }
            }
        }
    }

    public static class Builder {

        private IClient client;
        private String baseUrl;

        private GlobalChanNode beforeGlobalChanNode;
        private GlobalChanNode afterGlobalChanNode;

        public Builder() {

        }

        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder client(IClient client) {
            this.client = client;
            return this;
        }

        public Builder globalChanNode(GlobalChanNode before, GlobalChanNode after) {
            this.beforeGlobalChanNode = before;
            this.afterGlobalChanNode = after;
            return this;
        }

        public Builder parserFactory(ParserFactory factory) {
            if (this.client == null) {
                throw new UnsupportedOperationException("invoke client method first");
            }
            this.client.parserFactory(factory);
            return this;
        }

        public Builder cacheProvider(ICacheProvider cacheProvider) {
            if (this.client == null) {
                throw new UnsupportedOperationException("invoke client method first");
            }
            this.client.cacheProvider(cacheProvider);
            return this;
        }

        public Builder timeout(long ms) {
            if (this.client == null) {
                throw new UnsupportedOperationException("invoke client method first");
            }
            this.client.timeout(ms);
            return this;
        }

        public Builder resultInterceptor(IResultInterceptor interceptor) {
            if (this.client == null) {
                throw new UnsupportedOperationException("invoke client method first");
            }
            this.client.resultInterceptor(interceptor);
            return this;
        }

        public Glin build() {
            return new Glin(this);
        }
    }
}
