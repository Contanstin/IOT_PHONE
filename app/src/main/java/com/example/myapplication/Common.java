package com.example.myapplication;

import android.os.Looper;
import android.widget.Toast;

public class Common {
    /**
     * 消息提示框
     * @param text 文本内容
     */
    public static void toast(String text){
        try{
            if(text == null || text.length() == 0){
                return;
            }
            Toast.makeText(MyApplication.getInstance(), text, Toast.LENGTH_SHORT).show();
        }
        catch (Exception e){
            // TODO Auto-generated catch block
            e.printStackTrace();
            Looper.prepare();
            Toast.makeText(MyApplication.getInstance(), text, Toast.LENGTH_SHORT).show();
            Looper.loop();
        }
    }
}
