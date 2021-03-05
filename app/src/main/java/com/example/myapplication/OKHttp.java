package com.example.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OKHttp {
    //单例模式
    private volatile  static OKHttp manager = null;
    OkHttpClient client = new OkHttpClient.Builder()
            .cookieJar(new CookieJar() {
                private final HashMap<String, List<Cookie>> cookieStore = new HashMap<>();
                @Override
                public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                    cookieStore.put(url.host(), cookies);
                }

                @Override
                public List<Cookie> loadForRequest(HttpUrl url) {
                    List<Cookie> cookies = cookieStore.get(url.host());
                    return cookies != null ? cookies : new ArrayList<Cookie>();
                }
            })
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build();

    public static OKHttp getInstance(){
        if(manager == null){
            synchronized (OKHttp.class) {
                if (manager == null) {
                    manager = new OKHttp();
                }
            }
        }
        return manager;
    }

    /**
     * post请求
     * @param mContext
     * @param url
     * @param requestBody
     * @param msg
     * @param requestCallback
     */
    public void post(final Context mContext, String url, final RequestBody requestBody, final IRequestCallback requestCallback){
        //OKHttp post()
        if(!NetWorkState.netWorkState(mContext)){
            Common.toast(mContext.getString(R.string.networkalter));
            return;
        }
        final AlertDialog builder = new AlertDialog.Builder(mContext).create();


        builder.setCancelable(true);
        builder.setCanceledOnTouchOutside(false);

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        final Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                builder.dismiss();
                requestCallback.onFailure(ConnetFailMessage.connetFailMessage(mContext, e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                builder.dismiss();
                String result = new String(response.body().bytes());
                requestCallback.onSuccess(result);
            }
        });
    }



    public interface IRequestCallback{
        public void onSuccess(String res);
        public void onFailure(String err);
    }

}
