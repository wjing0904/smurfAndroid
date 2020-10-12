package com.smurf.app;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.smurf.app.signup.H5FaceWebChromeClient;
import com.smurf.app.signup.WBH5FaceVerifySDK;

/**
 * 签约&& 认证
 */

public class SignUpActivity extends Activity {
     private static final String TAG= "SignUpActivity";
    private WebView mWebView;
    private static final int PERMISSION_QUEST_FACE_VERIFY = 12;
    private AlertDialog.Builder builder = null;
    private AlertDialog dialog;
    private String signUrl;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_layout_webview);
        signUrl = getIntent().getStringExtra("sign_url");
        mWebView = findViewById(R.id.webview);
        if (Build.VERSION.SDK_INT >= 23) {
            askForPermission();
        }
    }

    private void askForPermission() {
        Log.d(TAG, "askForPermission()");
        //检测权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "checkSelfPermission is not granted");
            //如果权限没打开而用户之前又拒绝过权限，则弹窗解释
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)) {
                if (builder == null) {
                    Log.d(TAG, "shouldShowRequestPermissionRationale is true");
                    builder = new AlertDialog.Builder(SignUpActivity.this);
                }
                builder.setTitle("温馨提示").setMessage("完成H5刷脸需要相应权限哦").setPositiveButton("去设置", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(SignUpActivity.this,
                                new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE},
                                PERMISSION_QUEST_FACE_VERIFY);
                        if (dialog != null) {
                            dialog.dismiss();
                            dialog = null;
                        }
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (dialog != null) {
                            dialog.dismiss();
                            dialog = null;
                        }
                        if (!SignUpActivity.this.isFinishing())
                            SignUpActivity.this.finish();
                    }
                });
                dialog = builder.show();
            } else {
                Log.d(TAG, "shouldShowRequestPermissionRationale is false");
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE},
                        PERMISSION_QUEST_FACE_VERIFY);
            }
        } else {
            mWebView.setWebViewClient(new WebViewClient());
            mWebView.setWebChromeClient(new H5FaceWebChromeClient(SignUpActivity.this));
            WBH5FaceVerifySDK.getInstance().setWebViewSettings(mWebView, getApplicationContext());
            mWebView.loadUrl(signUrl);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_QUEST_FACE_VERIFY:
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        if (grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                            if (grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                                if (grantResults[3] == PackageManager.PERMISSION_GRANTED) {

                                    mWebView.setWebViewClient(new WebViewClient());
                                    mWebView.setWebChromeClient(new H5FaceWebChromeClient(SignUpActivity.this));
                                    WBH5FaceVerifySDK.getInstance().setWebViewSettings(mWebView, getApplicationContext());
                                    mWebView.loadUrl(signUrl);

                                } else {
                                    askPermissionError();
                                }
                            } else {
                                askPermissionError();
                            }
                        } else {
                            askPermissionError();
                        }
                    } else {
                        askPermissionError();
                    }
                }
                break;
        }
    }

    private void askPermissionError() {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
        if (!isFinishing())
            finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (WBH5FaceVerifySDK.getInstance().receiveH5FaceVerifyResult(requestCode, resultCode, data))
            Log.d("liuluchao","receiver h5 info");
        return;
    }

}
