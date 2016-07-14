package com.example.glinsample;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import org.loader.glin.Callback;
import org.loader.glin.Params;
import org.loader.glin.Result;
import org.loader.glin.client.IClient;
import org.loader.glin.helper.Helper;
import org.loader.glin.parser.*;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
    private static final long DEFAULT_TIME_OUT = 5000;

    private OkHttpClient mClient;
    private Handler mHandler;
    private ParserFactory mParserFactory;
    private long mTimeOut = DEFAULT_TIME_OUT;
    private boolean isDebug;

    public OkClient() {
        mClient = new OkHttpClient();
    }

    @Override
    public <T> void get(String url, Object tag, Callback<T> callback) {
        final Request request = new Request.Builder()
                .url(url).build();
        call(request, callback, tag, new StringBuilder());
    }

    @Override
    public <T> void post(String url, Params params, Object tag, Callback<T> callback) {
        StringBuilder debugInfo = new StringBuilder();
        MultipartBody builder = createRequestBody(params, debugInfo);
        Request request = new Request.Builder()
                .url(url).post(builder).build();
        call(request, callback, tag, debugInfo);
    }

    @Override
    public <T> void post(String url, String json, Object tag, final Callback<T> callback) {
        StringBuilder debugInfo = new StringBuilder();
        Request request = new Request.Builder().url(url)
                .post(createJsonBody(json, debugInfo)).build();
        call(request, callback, tag, debugInfo);
    }

    @Override
    public <T> void put(String url, Params params, Object tag, Callback<T> callback) {
        StringBuilder debugInfo = new StringBuilder();
        MultipartBody builder = createRequestBody(params, debugInfo);
        Request request = new Request.Builder()
                .url(url).put(builder).build();
        call(request, callback, tag, debugInfo);
    }

    @Override
    public <T> void put(String url, String json, Object tag, Callback<T> callback) {
        StringBuilder debugInfo = new StringBuilder();
        Request request = new Request.Builder()
                .url(url).put(createJsonBody(json, debugInfo)).build();
        call(request, callback, tag, debugInfo);
    }

    @Override
    public <T> void delete(String url, Object tag, Callback<T> callback) {
        StringBuilder debugInfo = new StringBuilder();
        final Request request = new Request.Builder()
                .url(url).delete().build();
        call(request, callback, tag, debugInfo);
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

    private OkHttpClient cloneClient() {
        return mClient.newBuilder()
                .connectTimeout(mTimeOut, TimeUnit.MILLISECONDS)
                .readTimeout(mTimeOut, TimeUnit.MILLISECONDS)
                .writeTimeout(mTimeOut, TimeUnit.MILLISECONDS)
                .build();
    }

    private <T> void call(Request request, final Callback<T> callback,
                          final Object tag, StringBuilder debugInfo) {
        String info = debugInfo.toString();
        debugInfo.delete(0, debugInfo.length());

        debugInfo.append("URL->" + request.url().toString() + "\n");
        debugInfo.append("Method->" + request.method() + "\n");
        debugInfo.append("Header->");

        Request.Builder builder = request.newBuilder();
        builder.tag(tag);
        LinkedHashMap<String, String> map = headers();
        if(map != null && !map.isEmpty()) {
            for(Iterator<String> iterator = map.keySet().iterator(); iterator.hasNext();) {
                String key = iterator.next();
                String value = map.get(key);
                if(value == null) continue;
                debugInfo.append(key + ":" + value + ";");
                builder.addHeader(key, value);
            }
            request = builder.build();
        }
        debugInfo.append("\n");

        final Call call = cloneClient().newCall(request);
        call.enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                prntInfo("Error->" + e.getMessage());
                Result<T> result = new Result<T>();
                result.ok(false);
                result.setMessage(e.getMessage());
                callback(callback, result);
            }

            @Override
            public void onResponse(final Call call, Response response) throws IOException {
                String resp = response.body().string();
                prntInfo("Response->" + resp);
                callback(callback, (Result<T>) getParser(callback.getClass()).parse(callback.getClass(), resp));
            }
        });

        String header = request.headers().toString();
        if(!TextUtils.isEmpty(header)) {
            debugInfo.append("Header->" + header + "\n");
        }

        debugInfo.append(info);
        prntInfo(debugInfo.toString());
    }

    private <T> org.loader.glin.parser.Parser getParser(Class<T> klass) {
        Class<?> type = Helper.getType(klass);
        if (type.isAssignableFrom(List.class)) {
            return mParserFactory.getListParser();
        }
        return mParserFactory.getParser();
    }

    private void prntInfo(String info) {
        if (!isDebug) { return;}
        Log.d("Glin", "*******************--BEGIN--*******************");
        Log.d("Glin", info);
        Log.d("Glin", "********************--END--********************");
    }

    private <T> void callback(final Callback<T> callback, final Result<T> result) {
        if(Looper.myLooper() == Looper.getMainLooper()) {
            callback.onResponse(result);
        }else {
            getHandler().post(new Runnable() {
                @Override
                public void run() {
                    callback.onResponse(result);
                }
            });
        }
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
            debugInfo.append(key + ":" + value + ";");
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
            debugInfo.append(key + ":" + file.getName());
            builder.addPart(Headers.of("Content-Disposition",
                    "form-data; name=\"" + key + "\";filename=\""+ file.getName() +"\""),
                    RequestBody.create(MediaType.parse("application/octet-stream"), file));
        }

        debugInfo.append("\n");
        return builder.build();
    }

    private RequestBody createJsonBody(String json, StringBuilder debugInfo) {
        debugInfo.append("RequestJson->" + json);
        RequestBody body = RequestBody.create(MediaType
                .parse("application/json;charset=utf-8"), json);
        return body;
    }

    private Handler getHandler() {
        if(mHandler == null) { mHandler = new Handler(Looper.getMainLooper());}
        return mHandler;
    }
}
