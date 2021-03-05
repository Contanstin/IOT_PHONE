package com.example.myapplication;

import android.content.Context;

public class ConnetFailMessage {
    /**
     * 获取连接失败原因
     * @param msg
     * @return
     */
    public static String connetFailMessage(Context mContext, String msg) {
        if (msg.contains("ConnectTimeoutException") || msg.contains("SocketTimeoutException")) {
            return mContext.getString(R.string.network_timeout);
        }
        else if (msg.contains("500")){
            return mContext.getString(R.string.network_server_error);
        }
        else{
            return mContext.getString(R.string.network_error);
        }

    }
}
