package com.example.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.xuexiang.xqrcode.XQRCode;
import com.xuexiang.xqrcode.ui.CaptureActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddDeviceActivity extends AppCompatActivity {

    @BindView(R.id.left_imgBtn)
    ImageButton leftImgBtn;
    @BindView(R.id.wifiOption)
    TextView wifiOption;
    @BindView(R.id.btn)
    ImageButton btn;
    @BindView(R.id.edit_imsi)
    EditText editImsi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device);
        ButterKnife.bind(this);


    }

    //获取手机相机权限
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivityForResult(new Intent(this, CaptureActivity.class), requestCode);
            } else {
                Toast.makeText(this, "请打开相机权限", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //处理扫描结果
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            handleScanResult(data);
        }
    }

    /**
     * 处理二维码扫描结果
     *
     * @param data
     */
    private void handleScanResult(Intent data) {
        if (data != null) {
            Bundle bundle = data.getExtras();
            if (bundle != null) {
                if (bundle.getInt(XQRCode.RESULT_TYPE) == XQRCode.RESULT_SUCCESS) {
                    String result = bundle.getString(XQRCode.RESULT_DATA);
                    editImsi.setText(result);
                } else if (bundle.getInt(XQRCode.RESULT_TYPE) == XQRCode.RESULT_FAILED) {
                    Toast.makeText(this, "解析二维码失败", Toast.LENGTH_LONG).show();
                }
            }
        }
    }


    @OnClick({R.id.left_imgBtn, R.id.btn})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.left_imgBtn:
                Intent intent = new Intent();
                setResult(18, intent);
                finish();
                break;
            case R.id.btn:
                if (Build.VERSION.SDK_INT > 22) {
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        //先判断有没有权限 ，没有就在这里进行权限的申请
                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.CAMERA}, 1);
                    } else {
                        Intent intent1 = new Intent(this, CaptureActivity.class);
                        startActivityForResult(intent1, 1);

                    }
                } else {
                    //这个说明系统版本在6.0之下，不需要动态获取权限。
                    Intent intent1 = new Intent(this, CaptureActivity.class);
                    startActivityForResult(intent1, 1);
                }
                break;
        }
    }
}