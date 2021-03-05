package com.example.myapplication;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Build;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;

public class MyApplication extends Application {
    private MutableLiveData<String> mBroadcastData;
    private static MyApplication instance;
    private static Context context;
    @Override
    public void onCreate(){
        // TODO Auto-generated method stub
        super.onCreate();
        instance = this;
        context=getApplicationContext();
        mBroadcastData = new MutableLiveData<>();
        IntentFilter filter = new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        if (Build.VERSION.SDK_INT >= 28) {
            filter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);
        }
        registerReceiver(mReceiver, filter);
    }

    public static Context getContext(){
        return context;
    }

    //创建广播
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) {
                return;
            }

            switch (action) {
                case WifiManager.NETWORK_STATE_CHANGED_ACTION:
                case LocationManager.PROVIDERS_CHANGED_ACTION:
                    mBroadcastData.setValue(action);
                    break;
            }
        }
    };

    public static MyApplication getInstance(){
        return instance;
    }

    public void observeBroadcast(LifecycleOwner owner, androidx.lifecycle.Observer<String> observer) {
        mBroadcastData.observe(owner, observer);
    }

}
