package com.xyoye.core.net.okhttp;


import android.support.annotation.NonNull;

import com.xyoye.core.BaseApplication;
import com.xyoye.core.interf.Engine;
import com.xyoye.core.utils.TLog;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * okHttp3引擎
 * Created by Administrator on 2016/1/11.
 */
public final class OkHttpEngine implements Engine {

    private static OkHttpEngine okHttpEngine;
    private OkHttpClient mOkHttpClient;
    private static CookiesManager mCookiesManager;

    public static void setCookieStore(CookiesManager cookiesManager) {
        mCookiesManager = cookiesManager;
    }

    public CookiesManager getCookieStore() {
        if (mCookiesManager == null && BaseApplication.get_context() != null) {
            mCookiesManager = new CookiesManager(BaseApplication.get_context());
        }
        return mCookiesManager;
    }

    public static OkHttpEngine getInstance() {
        if (okHttpEngine == null) {
            okHttpEngine = new OkHttpEngine();
        }
        return okHttpEngine;
    }

    private OkHttpEngine() {
        mOkHttpClient = initOkHttpConfig();
    }

    private OkHttpClient initOkHttpConfig() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(30, TimeUnit.SECONDS);
        builder.readTimeout(30, TimeUnit.SECONDS);
        builder.writeTimeout(30, TimeUnit.SECONDS);
        builder.retryOnConnectionFailure(true);
        File cacheDir = new File(BaseApplication.get_context().getCacheDir(), "HttpResponseCache");
        builder.cache(new Cache(cacheDir, 10 * 1024 * 1024));
        builder.cookieJar(BaseApplication.getCookiesManager());
        if (BaseApplication.isDebug()) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(interceptor);
        }
        return builder.build();
    }

    public void setHttpClient(OkHttpClient client) {
        this.mOkHttpClient = client;
    }

    public OkHttpClient getOkHttpClient() {
        return mOkHttpClient;
    }

    public String get(String url) throws Exception {
        Request request = new Request.Builder().url(url).get().build();
        Response response = mOkHttpClient.newCall(request).execute();
        if (response.code() == 200) {
           return response.body().string();
        }
        return "";
    }

    public void getAsync(String url, Callback responseCallback) {
        Request request = new Request.Builder().url(url).get().build();
        mOkHttpClient.newCall(request).enqueue(responseCallback);
    }

    public void postAsync(String url, Map<String, String> paramRequestParams, Callback responseCallback) {
        postAsync(url, getRequestBody(paramRequestParams), responseCallback);
    }

    public void postAsync(String url, Callback responseCallback) {
        postAsync(url, new FormBody.Builder().build(), responseCallback);
    }

    public void postAsync(String url, RequestBody paramRequestParams, Callback responseCallback) {
        Request request = new Request.Builder()
                .url(url)
                .addHeader(CONTENT_TYPE_LABEL, CONTENT_TYPE_VALUE_JSON)
                .post(paramRequestParams)
                .build();
        mOkHttpClient.newCall(request).enqueue(responseCallback);
    }

    public void postAsync(String url, RequestBody paramRequestParams, Callback responseCallback, String tag) {
        Request request = new Request.Builder()
                .url(url)
                .addHeader(CONTENT_TYPE_LABEL, CONTENT_TYPE_VALUE_JSON)
                .post(paramRequestParams)
                .tag(tag)
                .build();
        mOkHttpClient.newCall(request).enqueue(responseCallback);
    }

    public void postAsync(Request request, Callback responseCallback) {
        mOkHttpClient.newCall(request).enqueue(responseCallback);
    }

    public Request createRequest(String url, RequestBody paramRequestParams) {
        return new Request.Builder()
                .url(url)
                .addHeader(CONTENT_TYPE_LABEL, CONTENT_TYPE_VALUE_JSON)
                .post(paramRequestParams)
                .build();
    }

    public Request setCookie(@NonNull Request request) {
        if (getOkHttpClient().cookieJar() != null) {
            List<Cookie> cookies = getOkHttpClient().cookieJar().loadForRequest(request.url());
            if (!cookies.isEmpty()) {
                return request.newBuilder().addHeader("Cookie", cookieHeader(cookies)).build();
            }
        }
        return request;
    }

    /**
     * 不会开启异步线程。
     * param request
     * return Response
     * throws IOException
     */
    public Response execute(Request request) throws IOException {
        return mOkHttpClient.newCall(request).execute();
    }

    /**
     * 开启异步线程访问网络
     * param request
     * param responseCallback
     */
    public void enqueue(Request request, Callback responseCallback) {
        mOkHttpClient.newCall(request).enqueue(responseCallback);
    }

    /**
     * 开启异步线程访问网络, 且不在意返回结果（实现空callback）
     * param request
     */
    public void enqueue(Request request) {
        mOkHttpClient.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }

        });
    }

    public String getStringFromServer(String url) throws IOException {
        Request request = new Request.Builder().url(url).build();
        Response response = execute(request);
        if (response.isSuccessful()) {
            return response.body().string();
        } else {
            throw new IOException("Unexpected code " + response);
        }
    }

    /**
     * 参数转换
     *
     * @param params
     * @return
     */
    public RequestBody getRequestBody(Map<String, String> params) {
        FormBody.Builder body = new FormBody.Builder();
        if (params == null) return body.build();
        Iterator<Map.Entry<String, String>> iter = params.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, String> entry = iter.next();
            body.add(entry.getKey(), entry.getValue());
        }
        return body.build();
    }

    public void post(String url, RequestBody paramRequestParams) {
    }

    @Override
    public String post(String method, Map<String, String> paramsMap) throws IOException {
        return postHttp(method, joinParams(paramsMap));
    }

    @Override
    public String post(String method, String[] paramKeys, String[] paramValues) throws IOException {
        return postHttp(method, joinParams(paramKeys, paramValues));
    }

    // respoonse.body().string()只能调用一次，调用完后会自动关闭流
    public String postHttp(String url, RequestBody body) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Response response = mOkHttpClient.newCall(request).execute();
        if (response.isSuccessful()) {
            String entity = response.body().string();
            TLog.i(entity);
            return entity;
        }
        TLog.i(response.body().string());
        return null;
    }

    public RequestBody joinParams(Map<String, String> paramsMap) {
        if (paramsMap == null) return new FormBody.Builder().build();
        FormBody.Builder builder = new FormBody.Builder();
        for (String key : paramsMap.keySet()) {
            TLog.i(key + "========>" + paramsMap.get(key));
            builder.add(key, paramsMap.get(key));
        }
        return builder.build();
    }

    public RequestBody joinParams(String[] paramKeys, String[] paramValues) throws IOException {
        if (paramKeys == null || paramValues == null) return new FormBody.Builder().build();
        if (paramKeys.length != paramValues.length) throw new IOException("参数长度不匹配");
        FormBody.Builder builder = new FormBody.Builder();
        for (int i = 0; i < paramKeys.length; i++) {
            builder.add(paramKeys[i], paramValues[i]);
        }
        return builder.build();
    }

    public void arrayParams(@NonNull FormBody.Builder body, @NonNull List<String> value, @NonNull String key) {
        if (value.size() == 0) return;
        StringBuilder val = new StringBuilder("[");
        for (String s : value) {
            val.append(s).append(",");
        }
        String strVal = val.deleteCharAt(val.lastIndexOf(",")).append("]").toString();
        TLog.i("key-value", "key--value===>" + String.format("%s[]", key) + "--" + strVal);
        body.add(String.format("%s[]", key), strVal);
    }

    public String cookieHeader(List<Cookie> cookies) {
        StringBuilder cookieHeader = new StringBuilder();
        for (int i = 0, size = cookies.size(); i < size; i++) {
            if (i > 0) {
                cookieHeader.append("; ");
            }
            Cookie cookie = cookies.get(i);
            cookieHeader.append(cookie.name()).append('=').append(cookie.value());
        }
        TLog.i("cookies", cookieHeader.toString());
        return cookieHeader.toString();
    }

    public void receiveHeaders(Headers headers, Request userRequest) throws IOException {
        if (mOkHttpClient.cookieJar() == CookieJar.NO_COOKIES) return;

        List<Cookie> cookies = Cookie.parseAll(userRequest.url(), headers);
        if (cookies.isEmpty()) return;

        mOkHttpClient.cookieJar().saveFromResponse(userRequest.url(), cookies);
    }
}
