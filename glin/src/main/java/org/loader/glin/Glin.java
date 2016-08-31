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
import org.loader.glin.annotation.POST;
import org.loader.glin.call.Call;
import org.loader.glin.factory.CallFactory;
import org.loader.glin.client.IClient;
import org.loader.glin.factory.ParserFactory;
import org.loader.glin.interceptor.IResultInterceptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.plaf.TextUI;

/**
 * Created by qibin on 2016/7/13.
 */

public class Glin {
    private IClient mClient;
    private String mBaseUrl;
    private CallFactory mCallFactory;

    private Glin(IClient client, String baseUrl) {
        mClient = client;
        mBaseUrl = baseUrl;
        mCallFactory = new CallFactory();
    }

    @SuppressWarnings("unchecked")
    public <T> T create(Class<T> klass, Object tag) {
        return (T) Proxy.newProxyInstance(klass.getClassLoader(),
                new Class<?>[] {klass}, new Handler(tag));
    }

    public void cancel(String tag) {
        mClient.cancel(tag);
    }

    public void regist(Class<? extends Annotation> key, Class<? extends Call> value) {
        mCallFactory.regist(key, value);
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

//            if (method.isAnnotationPresent(JSON.class)) {
//                if(!method.isAnnotationPresent(POST.class)) {
//                    throw new UnsupportedOperationException("cannot find POST annotation");
//                }
//                key = JSON.class;
//                path = method.getAnnotation(POST.class).value();
//            } else {
//                HashMap<Class<? extends Annotation>, Class<? extends Call>> mapping = mCallFactory.get();
//                Class<? extends Annotation> item;
//                Annotation anno;
//                for (Iterator<Class<? extends Annotation>> iterator = mapping.keySet().iterator();
//                    iterator.hasNext();) {
//                    item = iterator.next();
//                    if (method.isAnnotationPresent(item)) {
//                        key = item;
//                        anno = method.getAnnotation(item);
//                        path = (String) anno.getClass().getDeclaredMethod("value").invoke(anno);
//                        break;
//                    }
//                }
//            }

            if (key == null) {
                throw new UnsupportedOperationException("cannot find annotations");
            }

            Class<? extends Call> callKlass = mCallFactory.get(key);
            if (callKlass == null) {
                throw new UnsupportedOperationException("cannot find calls");
            }

            Constructor<? extends Call> constructor = callKlass.getConstructor(IClient.class, String.class, Params.class, Object.class);
            Call<?> call = constructor.newInstance(mClient, justUrl(path), params(method, args), mTag);
            return call;
        }

        private String justUrl(String path) {
            String url = mBaseUrl == null ? "" : mBaseUrl;
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

        private Params params(Method method, Object[] args) {
            Params params = new Params();
            if (args == null || args.length == 0) {
                return params;
            }

            // method.getParameterAnnotations.length always equals args.length
            Annotation[][] paramsAnno = method.getParameterAnnotations();
            if (method.isAnnotationPresent(JSON.class)) {
                params.add(Params.DEFAULT_JSON_KEY, args[0]);
                return params;
            }

            int length = paramsAnno.length;
            for (int i = 0; i < length; i++) {
                if (paramsAnno[i].length == 0) { params.add(Params.DEFAULT_JSON_KEY, args[i]);}
                else { params.add(((Arg)paramsAnno[i][0]).value(), args[i]);}
            }

            return params;
        }
    }

    public static class Builder {
        private IClient mClient;
        private String mBaseUrl;

        public Builder() {

        }

        public Builder baseUrl(String baseUrl) {
            mBaseUrl = baseUrl;
            return this;
        }

        public Builder client(IClient client) {
            mClient = client;
            return this;
        }

        public Builder parserFactory(ParserFactory factory) {
            if (mClient == null) {
                throw new UnsupportedOperationException("invoke client method first");
            }
            mClient.parserFactory(factory);
            return this;
        }

        public Builder timeout(long ms) {
            if (mClient == null) {
                throw new UnsupportedOperationException("invoke client method first");
            }
            mClient.timeout(ms);
            return this;
        }

        public Builder resultInterceptor(IResultInterceptor interceptor) {
            if (mClient == null) {
                throw new UnsupportedOperationException("invoke client method first");
            }
            mClient.resultInterceptor(interceptor);
            return this;
        }

        public Builder debug(boolean debugMode) {
            if (mClient == null) {
                throw new UnsupportedOperationException("invoke client method first");
            }
            mClient.debugMode(debugMode);
            return this;
        }

        public Glin build() {
            return new Glin(mClient, mBaseUrl);
        }
    }
}
