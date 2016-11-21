package org.loader.superglin;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import org.loader.glin.Callback;
import org.loader.glin.NetResult;
import org.loader.glin.Params;
import org.loader.glin.Result;
import org.loader.glin.cache.DefaultCacheProvider;
import org.loader.glin.cache.ICacheProvider;
import org.loader.glin.client.IClient;
import org.loader.glin.factory.ParserFactory;
import org.loader.glin.helper.Helper;
import org.loader.glin.helper.SerializeHelper;
import org.loader.glin.interceptor.IResultInterceptor;
import org.loader.glin.parser.Parser;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by qibin on 2016/7/13.
 */

public class OkClient implements IClient {
    public static final String MSG_ERROR_HTTP = "msg_error_http:okhttp";
    private static final long DEFAULT_TIME_OUT = 5000;

    private OkHttpClient mClient;
    private Handler mHandler;
    private ParserFactory mParserFactory;
    private IResultInterceptor mResultInterceptor;
    private ICacheProvider mCacheProvider;

    private long mTimeOut = DEFAULT_TIME_OUT;
    private boolean isDebug;

    public OkClient() {
        mClient = buildClient();
    }

    private OkHttpClient buildClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, new TrustManager[]{new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    X509Certificate[] x509Certificates = new X509Certificate[0];
                    return x509Certificates;
                }
            }}, new SecureRandom());

            builder.sslSocketFactory(sc.getSocketFactory());
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        return builder.build();
    }

    @Override
    public <T> void get(String url, final LinkedHashMap<String, String> header,
                        Object tag, boolean shouldCache, Callback<T> callback) {
        final Request request = new Request.Builder().url(url).build();
        call(request, header, null, callback, tag, shouldCache, new StringBuilder());
    }

    @Override
    public <T> void post(String url, final LinkedHashMap<String, String> header,
                         Params params, Object tag, boolean shouldCache, Callback<T> callback) {
        StringBuilder debugInfo = new StringBuilder();
        MultipartBody builder = createRequestBody(params, debugInfo);
        Request request = new Request.Builder().url(url).post(builder).build();
        call(request, header, params.encode(), callback, tag, shouldCache, debugInfo);
    }

    @Override
    public <T> void post(String url, final LinkedHashMap<String, String> header,
                         String json, Object tag, boolean shouldCache, final Callback<T> callback) {
        StringBuilder debugInfo = new StringBuilder();
        Request request = new Request.Builder().url(url).post(createJsonBody(json, debugInfo)).build();
        call(request, header, json, callback, tag, shouldCache, debugInfo);
    }

    @Override
    public <T> void put(String url, final LinkedHashMap<String, String> header,
                        Params params, Object tag, boolean shouldCache, Callback<T> callback) {
        StringBuilder debugInfo = new StringBuilder();
        MultipartBody builder = createRequestBody(params, debugInfo);
        Request request = new Request.Builder().url(url).put(builder).build();
        call(request, header, params.encode(), callback, tag, shouldCache, debugInfo);
    }

    @Override
    public <T> void put(String url, final LinkedHashMap<String, String> header,
                        String json, Object tag, boolean shouldCache, Callback<T> callback) {
        StringBuilder debugInfo = new StringBuilder();
        Request request = new Request.Builder().url(url).put(createJsonBody(json, debugInfo)).build();
        call(request, header, json, callback, tag, shouldCache, debugInfo);
    }

    @Override
    public <T> void delete(String url, final LinkedHashMap<String, String> header,
                           Object tag, boolean shouldCache, Callback<T> callback) {
        StringBuilder debugInfo = new StringBuilder();
        final Request request = new Request.Builder().url(url).delete().build();
        call(request, header, null, callback, tag, shouldCache, debugInfo);
    }

    @Override
    public void cancel(Object tag) {
        for (Call call : mClient.dispatcher().queuedCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }

        for (Call call : mClient.dispatcher().runningCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
    }

    @Override
    public LinkedHashMap<String, String> headers() {
        return null;
    }

    @Override
    public void parserFactory(ParserFactory factory) {
        mParserFactory = factory;
    }

    @Override
    public void timeout(long ms) {
        mTimeOut = ms;
    }

    @Override
    public void debugMode(boolean debug) {
        isDebug = debug;
    }

    @Override
    public void cacheProvider(ICacheProvider provider) {
        mCacheProvider = provider;
    }

    private OkHttpClient cloneClient() {
        return mClient.newBuilder()
                .connectTimeout(mTimeOut, TimeUnit.MILLISECONDS)
//                .readTimeout(mTimeOut, TimeUnit.MILLISECONDS)
//                .writeTimeout(mTimeOut, TimeUnit.MILLISECONDS)
                .build();
    }

    @SuppressWarnings("unchecked")
    private <T> void call(Request request, final LinkedHashMap<String, String> header,
                          final String params, final Callback<T> callback, final Object tag,
                          final boolean shouldCache, StringBuilder debugInfo) {
        final String cacheKey = mCacheProvider == null ? null :
                mCacheProvider.getKey(request.url().toString(), params);

        if (shouldCache && mCacheProvider != null) {
            T cacheResult = mCacheProvider.get(cacheKey);
            if (cacheResult != null) {
                prntInfo("ReadCache->" + cacheKey);
                Result<T> res = new Result<>();
                res.setOk(true);
                res.setMessage("");
                res.setResult(cacheResult);
                res.setObj(200);
                res.setCache(true);
                callback.onResponse(res);
            }
        }

        String info = debugInfo.toString();
        debugInfo.delete(0, debugInfo.length());

        debugInfo.append("URL->").append(request.url().toString()).append("\n");
        debugInfo.append("Method->").append(request.method()).append("\n");

        LinkedHashMap<String, String> map = (header != null && !header.isEmpty())
                ? header : headers();
        Request.Builder builder = request.newBuilder();
        builder.tag(tag);
        if (map != null && !map.isEmpty()) {
            for (Iterator<String> iterator = map.keySet().iterator(); iterator.hasNext();) {
                String key = iterator.next();
                String value = map.get(key);
                if(value == null) continue;
                builder.addHeader(key, value);
            }
        }
        request = builder.build();
        final Call call = cloneClient().newCall(request);
        call.enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                prntInfo("Error->" + e.getMessage());
                Result<T> result = new Result<>();
                result.ok(false);
                result.setObj(0);
                result.setMessage(MSG_ERROR_HTTP);
                callback(call, callback, result);
            }

            @Override
            public void onResponse(final Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    prntInfo("Response->" + response.code() + ":" + response.message());
                    Result<T> res = new Result<>();
                    res.ok(false);
                    res.setObj(response.code());
                    res.setMessage(MSG_ERROR_HTTP);
                    callback(call, callback, res);
                    return;
                }

                String resp = response.body().string();
                prntInfo("Response->" + replaceBlank(resp));
                NetResult netResult = new NetResult(response.code(), response.message(), resp);
                Result<T> res = (Result<T>) getParser(callback.getClass()).parse(callback.getClass(), netResult);

                if (shouldCache && mCacheProvider != null && res.getResult() != null) {
                    prntInfo("CacheResult->" + cacheKey);
                    mCacheProvider.put(cacheKey, netResult, res);
                }
                callback(call, callback, res);
            }
        });

        String debugHeader = request.headers().toString();
        if(!TextUtils.isEmpty(debugHeader)) {
            debugInfo.append("Header->").append(debugHeader).append("\n");
        }
        debugInfo.append("\n");

        debugInfo.append(info);
        prntInfo(debugInfo.toString());
    }

    private <T> Parser getParser(Class<T> klass) {
        Class<?> type = Helper.getType(klass);
        if (List.class.isAssignableFrom(type)) {
            return mParserFactory.getListParser();
        }
        return mParserFactory.getParser();
    }

    private static String replaceBlank(String str) {
        String dest = str;
        if (dest != null) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = p.matcher(str);
            dest = m.replaceAll("");
        }
        return dest;
    }

    private void prntInfo(String info) {
        if (!isDebug) { return;}

        Log.d("Glin", "*******************--BEGIN--*******************");
        Log.d("Glin", info);
        Log.d("Glin", "********************--END--********************");
    }

    private <T> void callback(final Call call, final Callback<T> callback, final Result<T> result) {
        if(Looper.myLooper() == Looper.getMainLooper()) {
            realCallback(call, callback, result);
        }else {
            getHandler().post(new Runnable() {
                @Override
                public void run() {
                    realCallback(call, callback, result);
                }
            });
        }
    }

    private <T> void realCallback(Call call, Callback<T> callback, Result<T> result) {
//        if (isDebug) { Log.d("Glin", "call is canceled ? " + call.isCanceled());}
        if (call.isCanceled()) { return;}
        if (!intercept(result)) { callback.onResponse(result);}
    }

    private <T> boolean intercept(final Result<T> result) {
        if (mResultInterceptor != null && mResultInterceptor.intercept(result)) {
            return true;
        }
        return false;
    }

    private MultipartBody createRequestBody(Params params, StringBuilder debugInfo) {
        debugInfo.append("Params->");
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);

        LinkedHashMap<String, String> map = params.get();
        for(Iterator<String> iterator=map.keySet().iterator();iterator.hasNext();) {
            String key = iterator.next();
            String value = map.get(key);
            if(value == null) continue;
            debugInfo.append(key).append(":").append(value).append(";");
            builder.addPart(Headers.of("Content-Disposition",
                    "form-data; name=\""+ key +"\""),
                    RequestBody.create(null, value));
        }

        debugInfo.append("\nFiles->");

        LinkedHashMap<String, File> files = params.files();
        for(Iterator<String> iterator=files.keySet().iterator();iterator.hasNext();) {
            String key = iterator.next();
            File file = files.get(key);
            if(file == null) continue;
            debugInfo.append(key).append(":").append(file.getName());
            builder.addPart(Headers.of("Content-Disposition",
                    "form-data; name=\"" + key + "\";filename=\""+ file.getName() +"\""),
                    RequestBody.create(MediaType.parse("application/octet-stream"), file));
        }

        debugInfo.append("\n");
        return builder.build();
    }

    private RequestBody createJsonBody(String json, StringBuilder debugInfo) {
        debugInfo.append("RequestJson->").append(json);
        RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), json);
        return body;
    }

    private Handler getHandler() {
        if(mHandler == null) { mHandler = new Handler(Looper.getMainLooper());}
        return mHandler;
    }

    @Override
    public void resultInterceptor(IResultInterceptor interceptor) {
        mResultInterceptor = interceptor;
    }
}