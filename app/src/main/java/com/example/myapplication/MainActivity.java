package com.example.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.xuexiang.xqrcode.XQRCode;
import com.xuexiang.xqrcode.ui.CaptureActivity;

import org.json.JSONObject;

import java.util.Timer;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.FormBody;

/**
 *
 */
public class MainActivity extends AppCompatActivity {

    @BindView(R.id.web)
    WebView web;


    public LocationClient mLocationClient = null;

    public BDAbstractLocationListener myListener = new MyLocationListener();

    public LocationClientOption option;

    //单击时间间隔
    private long clickInterval = 0;


    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            double latitude = location.getLatitude();    //获取纬度信息
            double longitude = location.getLongitude();    //获取经度信息
            String coordinate = longitude + "," + latitude;

            String country = location.getCountry();    //获取国家
            String province = location.getProvince();    //获取省份
            String city = location.getCity();    //获取城市
            String district = location.getDistrict();    //获取区县
            String street = location.getStreet();    //获取街道信息
            String adr = country + "/" + province + "/" + city + "/" + district + "/" + street;

            //调用js前端项目的coor函数，将经纬度返回给前端
            String method = "javascript:coor('" + coordinate + "')";
            web.loadUrl(method);

            //调用js前端项目的detailAdr函数，将具体地址返回给前端
            String method1 = "javascript:detailAdr('" + adr + "')";
            web.loadUrl(method1);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        init();
    }

    //调用百度地图api
    public void doLocation() {
        mLocationClient = new LocationClient(getApplicationContext());
        //声明LocationClient类
        mLocationClient.registerLocationListener(myListener);
        //注册监听函数
        initLocation();
//        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED) {//未开启定位权限
//            // 开启定位权限,200是标识码
//            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);
//        } else {
//            Toast.makeText(MainActivity.this, "已开启定位权限", Toast.LENGTH_SHORT).show();
//            mLocationClient.start();//开始定位
//        }
    }

    private void initLocation() {
        option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setIsNeedAddress(true);
        //可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);
        //可选，默认false,设置是否使用gps
        mLocationClient.setLocOption(option);
    }


    private void init() {

        // 自适应屏幕
//        web.getSettings()
//                .setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
//        web.getSettings().setLoadWithOverviewMode(true);
        // 支持javascript
        web.getSettings().setJavaScriptEnabled(true);
        web.setWebViewClient(new WebViewClient());
        String url = "http://192.168.2.229:8080/#/phone_dns";
        web.setVerticalScrollBarEnabled(false);
        web.loadUrl(url);
        web.addJavascriptInterface(this, "webview");

        CookieManager cookieManager = CookieManager.getInstance();
        String CookieStr = cookieManager.getCookie(url);
        Log.e(CookieStr, "CookieStr= ");

    }

    //前端调用android原生的函数，根据传的参数来处理不同的事情
    @JavascriptInterface
    public String startActivity(String name) {
        //进入wifi设置页面（EspTouchActivity)
        if (name.equals("wifiOption")) {
            Intent intent = new Intent();
            intent.setClass(this, EspTouchActivity.class);
            startActivity(intent);
            return "1";
            //添加设备的扫一扫图标，返回imsi码
        } else if (name.equals("addDevice")) {
            if (Build.VERSION.SDK_INT > 22) {
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    //先判断有没有权限 ，没有就在这里进行权限的申请
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.CAMERA}, 1);
                } else {
                    Intent intent1 = new Intent(this, CaptureActivity.class);
                    startActivityForResult(intent1, 1);
                    return "2";
                }
            } else {

                //这个说明系统版本在6.0之下，不需要动态获取权限。
                Intent intent1 = new Intent(this, CaptureActivity.class);
                startActivityForResult(intent1, 1);
                return "2";
            }

            //点击添加设备时，自动获取经纬度
        } else if (name.equals("getLocation")) {
            if (Build.VERSION.SDK_INT > 22) {
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    //先判断有没有权限 ，没有就在这里进行权限的申请
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);
                } else {
                    doLocation();
                    mLocationClient.start();
                    return "4";
                }
            } else {
                doLocation();
                mLocationClient.start();
            }
        }
        return "3";
    }

    //获取手机权限
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivityForResult(new Intent(this, CaptureActivity.class), requestCode);
            } else {
                Toast.makeText(this, getResources().getString(R.string.camera_permission), Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == 2) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                doLocation();//用户同意权限,执行我们的操作
                mLocationClient.start();//开始定位
            } else {
                // 用户拒绝之后
                Toast.makeText(MainActivity.this, getResources().getString(R.string.location_permission), Toast.LENGTH_LONG).show();
            }
        }

    }


    //处理扫描结果，根据mac地址得到imsi
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            String imsi = "";
            imsi = handleScanResult(data);
            String tmp = "";
            if (imsi.length() == 12) {
                tmp = MacToImsi(imsi);
            } else if (imsi.length() == 16) {
                tmp = MacToImsi(imsi.substring(4, 16));
            } else if (imsi.length() == 18) {
                tmp = MacToImsi(imsi.substring(6, 18));
            } else {
                tmp = imsi;
            }
            String method = "javascript:scanCallBack('" + tmp + "')";
            web.loadUrl(method);
        }
    }

    //Mac转imsi
    public static String MacToImsi(String a) {
        String str = a;
        String reg = "^[A-Fa-f0-9]+$";
        String regis = "";
        boolean r = str.matches(reg);
        if (r == false && str != "") {
            return "错误(error)";
        }
        if (str != null && str.length() == 12) {
            String sub1 = str.substring(0, 6);
            int st1 = Integer.parseInt(sub1, 16);
            String sub2 = str.substring(6, 12);
            int st2 = Integer.parseInt(sub2, 16);
            regis = frontCompWithZore(st1 % 10000000, 7) + "" + frontCompWithZore(st2, 8);
            // regis= String.valueOf(st1)+String.valueOf(st2);
            if (regis.length() == 15) {
                return regis;

            }
        }
        return regis;
    }

    public static String frontCompWithZore(int sourceDate, int formatLength) {

        String newString = String.format("%0" + formatLength + "d", sourceDate);
        return newString;
    }

    /**
     * 处理二维码扫描结果
     *
     * @param data
     */
    private String handleScanResult(Intent data) {
        String result = "";
        if (data != null) {
            Bundle bundle = data.getExtras();
            if (bundle != null) {
                if (bundle.getInt(XQRCode.RESULT_TYPE) == XQRCode.RESULT_SUCCESS) {
                    result = bundle.getString(XQRCode.RESULT_DATA);
                } else if (bundle.getInt(XQRCode.RESULT_TYPE) == XQRCode.RESULT_FAILED) {
                    Toast.makeText(this, getResources().getString(R.string.qr_code_fail), Toast.LENGTH_LONG).show();
                }
            }
        }
        return result;
    }


    /**
     * 连续双击退出软件
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - clickInterval) > 2000) {
                Common.toast(getResources().getString(R.string.click_exit));
                clickInterval = System.currentTimeMillis();
            }
            else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

//    @Override
//    public void onBackPressed() {
//        if (web.canGoBack()) {
//            web.goBack();
//            web.removeAllViews();
//        } else {
//            super.onBackPressed();
//        }
//    }

    //调用接口
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                FormBody body = new FormBody.Builder().add("isClick", "1").build();
                OKHttp.getInstance().post(MainActivity.this, "http://192.168.2.229:8080/huangMingJie/app/wifiOption", body, new OKHttp.IRequestCallback() {
                    @Override
                    public void onSuccess(String res) {
                        try {
                            JSONObject object = new JSONObject(res);
                            String msg = JsonUtils.getJSONString(object, "msg");
                            if (msg.equals("成功")) {
                                String data = JsonUtils.getJSONString(object, "data");
                                if (data.equals("html")) {
                                    Intent intent = new Intent(MainActivity.this, text.class);
                                    startActivity(intent);
                                }
                            }
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(String err) {
                        Common.toast(err);
                    }
                });

            }
        }
    };

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK && web.canGoBack()&&event.getAction() == KeyEvent.ACTION_DOWN) {
//            if ((System.currentTimeMillis() - click) > 2000) {
//                Toast.makeText(this,"再点一次就退出了",Toast.LENGTH_SHORT).show();
//                click = System.currentTimeMillis();
//            }
//            else {
//                web.removeAllViews();//删除webview中所有进程
//                finish();
//            }
//            web.goBack();// activityBaseWebAddWebView.reload();
//
//            return true;
//        }
//        return super.onKeyDown(keyCode, event);
//    }


}