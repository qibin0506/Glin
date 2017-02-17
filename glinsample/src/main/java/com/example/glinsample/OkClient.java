package com.example.glinsample;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import org.loader.glin.Callback;
import org.loader.glin.NetResult;
import org.loader.glin.Params;
import org.loader.glin.Result;
import org.loader.glin.cache.ICacheProvider;
import org.loader.glin.client.IClient;
import org.loader.glin.factory.ParserFactory;
import org.loader.glin.helper.ClientHelper;
import org.loader.glin.helper.LogHelper;
import org.loader.glin.interceptor.IResultInterceptor;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.LinkedHashMap;
import java.util.Map;
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

    private ClientHelper mClientHelper;

    private long mTimeOut = DEFAULT_TIME_OUT;
    private boolean isDebugMode;

    public OkClient() {
        mClient = buildClient();
        mClientHelper = new ClientHelper();
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
        call(request, header, null, callback, tag, shouldCache, getLogHelper());
    }

    @Override
    public <T> void post(String url, final LinkedHashMap<String, String> header,
                         Params params, Object tag, boolean shouldCache, Callback<T> callback) {
        LogHelper logHelper = getLogHelper();
        MultipartBody builder = createRequestBody(params, logHelper);
        Request request = new Request.Builder().url(url).post(builder).build();
        call(request, header, params.encode(), callback, tag, shouldCache, logHelper);
    }

    @Override
    public <T> void post(String url, final LinkedHashMap<String, String> header,
                         String json, Object tag, boolean shouldCache, final Callback<T> callback) {
        LogHelper logHelper = getLogHelper();
        Request request = new Request.Builder().url(url).post(createJsonBody(json, logHelper)).build();
        call(request, header, json, callback, tag, shouldCache, logHelper);
    }

    @Override
    public <T> void put(String url, final LinkedHashMap<String, String> header,
                        Params params, Object tag, boolean shouldCache, Callback<T> callback) {
        LogHelper logHelper = getLogHelper();
        MultipartBody builder = createRequestBody(params, logHelper);
        Request request = new Request.Builder().url(url).put(builder).build();
        call(request, header, params.encode(), callback, tag, shouldCache, logHelper);
    }

    @Override
    public <T> void put(String url, final LinkedHashMap<String, String> header,
                        String json, Object tag, boolean shouldCache, Callback<T> callback) {
        LogHelper logHelper = getLogHelper();
        Request request = new Request.Builder().url(url).put(createJsonBody(json, logHelper)).build();
        call(request, header, json, callback, tag, shouldCache, logHelper);
    }

    @Override
    public <T> void delete(String url, final LinkedHashMap<String, String> header,
                           Object tag, boolean shouldCache, Callback<T> callback) {
        final Request request = new Request.Builder().url(url).delete().build();
        call(request, header, null, callback, tag, shouldCache, getLogHelper());
    }

    @SuppressWarnings("unchecked")
    private <T> void call(Request request, final LinkedHashMap<String, String> header,
                          final String params, final Callback<T> callback, final Object tag,
                          final boolean shouldCache, LogHelper helper) {
        final String cacheKey = mClientHelper.getCacheKey(request.url().toString(), params);

        Result<T> result = mClientHelper.useCache(shouldCache, cacheKey, callback);
        if (result != null) {
            helper.getLogAppender().append("\nUseCache->").append(cacheKey).append("\n");
            callback.onResponse(result);
        }

        String info = helper.getLogAppender().toString();
        helper.getLogAppender().delete(0, helper.getLogAppender().length());

        helper.getLogAppender().append("URL->").append(request.url().toString()).append("\n");
        helper.getLogAppender().append("Method->").append(request.method()).append("\n");

        LinkedHashMap<String, String> map = (header != null && !header.isEmpty())
                ? header : headers();
        Request.Builder builder = request.newBuilder();
        builder.tag(tag);
        if (map != null && !map.isEmpty()) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String value = entry.getValue();
                if(value == null) { continue;}
                builder.addHeader(entry.getKey(), value);
            }
        }
        request = builder.build();
        final Call call = cloneClient().newCall(request);
        call.enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getLogHelper().print("Error->" + e.getMessage());

                Result<T> result = new Result<>();
                result.ok(false);
                result.setCode(0);
                result.setObj(0);
                result.setMessage(MSG_ERROR_HTTP);

                callback(call, callback, result);
            }

            @Override
            public void onResponse(final Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    getLogHelper().print("Response->" + response.code() + ":" + response.message());

                    Result<T> res = new Result<>();
                    res.ok(false);
                    res.setCode(response.code());
                    res.setObj(response.code());
                    res.setMessage(MSG_ERROR_HTTP);

                    callback(call, callback, res);
                    return;
                }

                String resp = "";
                try {
                    resp = response.body().string();
                } catch (Exception e) {
                    getLogHelper().print("response.body->" + replaceBlank(e.getMessage()));
                }

                getLogHelper().print("Response->" + replaceBlank(resp));

                NetResult netResult = new NetResult(response.code(), response.message(), resp);
                Result<T> res = mClientHelper.parseResponse(callback, netResult);
                callback(call, callback, res);

                if (mClientHelper.cache(shouldCache, cacheKey, res, netResult)) {
                    getLogHelper().print("CacheResult->" + cacheKey);
                }
            }
        });

        String debugHeader = request.headers().toString();
        if(!TextUtils.isEmpty(debugHeader)) {
            helper.getLogAppender().append("Header->").append(debugHeader).append("\n");
        }

        helper.getLogAppender().append("\n");
        helper.getLogAppender().append(info);
        helper.print();
    }

    private <T> void callback(final Call call, final Callback<T> callback, final Result<T> result) {
        if(Looper.myLooper() == Looper.getMainLooper()) {
            realCallback(call, callback, result);
        }else {
            getHandler().post(new Runnable() {
                @Override
                public void run() { realCallback(call, callback, result);}
            });
        }
    }

    private <T> void realCallback(Call call, Callback<T> callback, Result<T> result) {
        if (call.isCanceled()) { return;}
        if (!mClientHelper.shouldInterceptResult(result)) { callback.onResponse(result);}
    }

    private MultipartBody createRequestBody(Params params, LogHelper logHelper) {
        logHelper.getLogAppender().append("Params->");
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);

        LinkedHashMap<String, String> map = params.get();
        for(Map.Entry<String, String> entry : map.entrySet()) {
            String value = entry.getValue();
            if(value == null) { continue;}

            logHelper.getLogAppender().append(entry.getKey()).append(":").append(value).append(";");
            builder.addPart(Headers.of("Content-Disposition",
                    "form-data; name=\""+ entry.getKey() +"\""),
                    RequestBody.create(null, value));
        }

        logHelper.getLogAppender().append("\nFiles->");

        LinkedHashMap<String, File> files = params.files();
        for(Map.Entry<String, File> entry : files.entrySet()) {
            File file = entry.getValue();
            if(file == null) { continue;}

            logHelper.getLogAppender().append(entry.getKey()).append(":").append(file.getName());
            builder.addPart(Headers.of("Content-Disposition",
                    "form-data; name=\"" + entry.getKey() + "\";filename=\""+ file.getName() +"\""),
                    RequestBody.create(MediaType.parse("application/octet-stream"), file));
        }

        logHelper.getLogAppender().append("\n");
        return builder.build();
    }

    private RequestBody createJsonBody(String json, LogHelper logHelper) {
        logHelper.getLogAppender().append("RequestJson->").append(json);
        return RequestBody.create(MediaType.parse("application/json;charset=utf-8"), json);
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
    public void parserFactory(ParserFactory factory) {
        mClientHelper.parserFactory(factory);
    }

    @Override
    public void cacheProvider(ICacheProvider provider) {
        mClientHelper.cacheProvider(provider);
    }

    @Override
    public void resultInterceptor(IResultInterceptor interceptor) {
        mClientHelper.resultInterceptor(interceptor);
    }

    @Override
    public void timeout(long ms) {
        mTimeOut = ms;
    }

    @Override
    public void debugMode(boolean debug) {
        isDebugMode = debug;
    }

    @Override
    public LinkedHashMap<String, String> headers() {
        return null;
    }

    private Handler getHandler() {
        if(mHandler == null) { mHandler = new Handler(Looper.getMainLooper());}
        return mHandler;
    }

    private LogHelper getLogHelper() {
        return LogHelper.get(isDebugMode, mLogPrinter);
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

    private OkHttpClient cloneClient() {
        return mClient.newBuilder()
                .connectTimeout(mTimeOut, TimeUnit.MILLISECONDS)
//                .readTimeout(mTimeOut, TimeUnit.MILLISECONDS)
//                .writeTimeout(mTimeOut, TimeUnit.MILLISECONDS)
                .build();
    }

    private LogHelper.LogPrinter mLogPrinter = new LogHelper.LogPrinter() {
        @Override
        public void print(String tag, String content) {
            Log.d(tag, content);
        }
    };
}
