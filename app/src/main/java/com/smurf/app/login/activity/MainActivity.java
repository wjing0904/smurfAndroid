package com.smurf.app.login.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.smurf.app.BuildConfig;
import com.smurf.app.OnDialogApplyPermissionListener;
import com.smurf.app.R;
import com.smurf.app.WebViewActivity;
import com.smurf.app.base.StaticURL;
import com.smurf.app.login.common.Constants;
import com.smurf.app.login.common.PermissionConstants;
import com.smurf.app.login.utils.PermissionUtils;
import com.smurf.app.utils.PermissionsUtils;
import com.smurf.app.utils.SharedPreferencesHelper;
import com.smurf.app.wxapi.WXLogin;

import org.greenrobot.eventbus.EventBus;

import cn.jiguang.verifysdk.api.JVerificationInterface;
import cn.jiguang.verifysdk.api.JVerifyUIClickCallback;
import cn.jiguang.verifysdk.api.JVerifyUIConfig;
import cn.jiguang.verifysdk.api.VerifyListener;

import com.smurf.app.base.event.*;

import java.util.ArrayList;
import java.util.List;

import static com.smurf.app.base.StaticNum.REQUEST_LOGIN_STORAGE;


public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    private SharedPreferencesHelper sharedPreferencesHelper;
    private OnDialogApplyPermissionListener mOnDialogPremission;
    private Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        sharedPreferencesHelper = new SharedPreferencesHelper(
                this, "smurf");
        initPermission();
//        finish();
//        initShowLoginPage();
    }


    @SuppressLint("WrongConstant")
    private void initPermission() {
        Log.d("smurf", "request storage, phone");

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission_group.STORAGE) != PackageManager.PERMISSION_GRANTED) {
            int isPermiss = (int) sharedPreferencesHelper.get(SharedPreferencesHelper.LOGIN_STORAGE_PERMISSION, 0);
            if (isPermiss == 0) {
                PermissionUtils.permission(PermissionConstants.STORAGE)
                        .callback(new PermissionUtils.SimpleCallback() {
                            @Override
                            public void onGranted() {
                                Log.d("smurf", "request storage, phone allow");
                                initShowLoginPage();
                                sharedPreferencesHelper.put(SharedPreferencesHelper.LOGIN_STORAGE_PERMISSION, 1);

                            }

                            @Override
                            public void onDenied() {
                                Log.d("smurf", "request storage, phone denied");
//                Toast.makeText(MainActivity.this,"未开启读取手机状态权限",Toast.LENGTH_SHORT).show();
                                sharedPreferencesHelper.put(SharedPreferencesHelper.LOGIN_STORAGE_PERMISSION, 2);
//登陆拒绝权限 开启H5 页面登陆
                                String url = null;
                                if (BuildConfig.DEBUG) {
                                    url = StaticURL.DEBUG_PHONE_LOGIN;
                                } else {
                                    url = StaticURL.RELEASE_PHONE_LOGIN;
                                }
                                Intent intent = new Intent(context, WebViewActivity.class);
                                intent.putExtra("web_url", url);
                                startActivity(intent);

                            }
                        }).request();
            } else if (isPermiss == 2) {
                showDialog("照片、媒体内容和文件读写，电话权限", new OnDialogApplyPermissionListener() {
                    @Override
                    public void isPremission(boolean isAllow) {
                        if (isAllow) {
                            initShowLoginPage();
                        }else{
                            String url = null;
                            if (BuildConfig.DEBUG) {
                                url = StaticURL.DEBUG_PHONE_LOGIN;
                            } else {
                                url = StaticURL.RELEASE_PHONE_LOGIN;
                            }
                            Intent intent = new Intent(context, WebViewActivity.class);
                            intent.putExtra("web_url", url);
                            startActivity(intent);
                        }
                    }
                }, REQUEST_LOGIN_STORAGE);
            } else {
                initShowLoginPage();
            }
        }

    }


    private void showDialog(String serviceName, OnDialogApplyPermissionListener onDialogPremission, int requestCode) {
        /* @setIcon 设置对话框图标
         * @setTitle 设置对话框标题
         * @setMessage 设置对话框消息提示
         * setXXX方法返回Dialog对象，因此可以链式设置属性
         */
        this.mOnDialogPremission = onDialogPremission;
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(this);
        normalDialog.setIcon(R.drawable.logo);
        normalDialog.setTitle("蓝晶灵想要使用" + serviceName);
        normalDialog.setMessage("请在设置-蓝晶灵中开启" + serviceName);
        normalDialog.setPositiveButton("去设置",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri1 = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri1);
                        startActivityForResult(intent, requestCode);
                    }
                });
        normalDialog.setNegativeButton("知道了",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (mOnDialogPremission != null) {
                            mOnDialogPremission.isPremission(false);
                        }
                    }
                });
        // 显示
        normalDialog.show();
    }

    private void initShowLoginPage() {
        JVerificationInterface.getToken(this, new VerifyListener() {
            @Override
            public void onResult(int i, String s, String s1) {
                Log.e(TAG, "onResult: .getToken" + i + " " + s + " " + s1);
            }
        });
        JVerificationInterface.clearPreLoginCache();
        JVerificationInterface.setCustomUIWithConfig(getFullScreenPortraitConfig(), null);
        JVerificationInterface.loginAuth(this, new VerifyListener() {
            @Override
            public void onResult(final int code, final String token, String operator) {
                Log.e(TAG, "onResult: code=" + code + ",token=" + token + ",operator=" + operator);
                final String errorMsg = "operator=" + operator + ",code=" + code + "\ncontent=" + token;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (code == Constants.CODE_LOGIN_SUCCESS) {
                            toSuccessActivity(Constants.ACTION_LOGIN_SUCCESS, token, 0);
                            Log.e(TAG, "onResult: loginSuccess");
                        } else if (code != Constants.CODE_LOGIN_CANCELD) {
                            Log.e(TAG, "onResult: loginError");
                            toFailedActivigy(code, token);
                        }
                        finish();
                    }
                });
            }
        });
    }


    private JVerifyUIConfig getFullScreenPortraitConfig() {
        JVerifyUIConfig.Builder uiConfigBuilder = new JVerifyUIConfig.Builder();
        uiConfigBuilder.setSloganTextColor(0xFFD0D0D9);
        uiConfigBuilder.setLogoOffsetY(103);
        uiConfigBuilder.setNumFieldOffsetY(190);
        uiConfigBuilder.setPrivacyState(true);
        uiConfigBuilder.setLogoImgPath("logo");
        uiConfigBuilder.setNavTransparent(true);
        uiConfigBuilder.setNavReturnImgPath("btn_back");
        uiConfigBuilder.setCheckedImgPath(null);
        uiConfigBuilder.setNumberColor(0xFF222328);
        uiConfigBuilder.setLogBtnImgPath("login_btn");
        uiConfigBuilder.setLogBtnTextColor(0xFFFFFFFF);
        uiConfigBuilder.setLogBtnText("一键登录");
        uiConfigBuilder.setLogBtnOffsetY(255);
        uiConfigBuilder.setLogBtnWidth(300);
        uiConfigBuilder.setLogBtnHeight(45);
        uiConfigBuilder.setAppPrivacyColor(0xFFBBBCC5, 0xFF8998FF);
//        uiConfigBuilder.setPrivacyTopOffsetY(310);
        uiConfigBuilder.setPrivacyText("登录即同意《", "", "", "》并使用本机号码登录");
        uiConfigBuilder.setPrivacyCheckboxHidden(false);
        uiConfigBuilder.setPrivacyTextCenterGravity(true);
        uiConfigBuilder.setPrivacyTextSize(12);
//        uiConfigBuilder.setPrivacyOffsetX(52-15);

        // 手机登录按钮
        RelativeLayout.LayoutParams layoutParamPhoneLogin = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParamPhoneLogin.setMargins(0, dp2Pix(this, 360.0f), 0, 0);
        layoutParamPhoneLogin.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        layoutParamPhoneLogin.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        TextView tvPhoneLogin = new TextView(this);
        tvPhoneLogin.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
        tvPhoneLogin.setTextColor(android.graphics.Color.RED);
        tvPhoneLogin.setText("手机号登陆");
        tvPhoneLogin.setLayoutParams(layoutParamPhoneLogin);
        uiConfigBuilder.addCustomView(tvPhoneLogin, false, new JVerifyUIClickCallback() {
            @Override
            public void onClicked(Context context, View view) {
                toNativeVerifyActivity();
            }
        });

        // 微信qq新浪登录

        LinearLayout layoutLoginGroup = new LinearLayout(this);
        RelativeLayout.LayoutParams layoutLoginGroupParam = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutLoginGroupParam.setMargins(0, dp2Pix(this, 450.0f), 0, 0);
        layoutLoginGroupParam.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        layoutLoginGroupParam.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        layoutLoginGroupParam.setLayoutDirection(LinearLayout.HORIZONTAL);
        layoutLoginGroup.setLayoutParams(layoutLoginGroupParam);

        ImageView btnWechat = new ImageView(this);

        btnWechat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                JShareInterface.authorize(Wechat.Name, mAuthListener);
                WXLogin wxLogin = new WXLogin(MainActivity.this);
                wxLogin.login();
            }
        });

        btnWechat.setImageResource(R.drawable.o_wechat);

        LinearLayout.LayoutParams btnParam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        btnParam.setMargins(25, 0, 25, 0);

        layoutLoginGroup.addView(btnWechat, btnParam);
        uiConfigBuilder.addCustomView(layoutLoginGroup, false, new JVerifyUIClickCallback() {
            @Override
            public void onClicked(Context context, View view) {
//                ToastUtil.showToast(MainActivity.this, "功能未实现", 1000);
            }
        });


//        final View dialogViewTitle = LayoutInflater.from(getApplicationContext()).inflate(R.layout.dialog_login_title,null, false);
//
//        uiConfigBuilder.addNavControlView(dialogViewTitle, new JVerifyUIClickCallback() {
//            @Override
//            public void onClicked(Context context, View view) {
//
//            }
//        });

//        final View dialogView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.dialog_login_agreement,null, false);
//
//        dialogView.findViewById(R.id.dialog_login_no).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                JVerificationInterface.dismissLoginAuthActivity();
//            }
//        });
//
//        dialogView.findViewById(R.id.dialog_login_yes).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dialogView.setVisibility(View.GONE);
//                dialogViewTitle.setVisibility(View.GONE);
//            }
//        });
//
//
//        uiConfigBuilder.addCustomView(dialogView, false, new JVerifyUIClickCallback() {
//            @Override
//            public void onClicked(Context context, View view) {
////                ToastUtil.showToast(MainActivity.this, "功能未实现", 1000);
//            }
//        });


        return uiConfigBuilder.build();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == REQUEST_LOGIN_STORAGE) {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_SELECT_IMAGES_PERMISSION);
//            int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission_group.STORAGE);

            String[] permissions = new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE};
            List<String> permissionList = new ArrayList<>();
            for (int i = 0; i < permissions.length; i++) {
                if (ActivityCompat.checkSelfPermission(this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                    permissionList.add(permissions[i]);
                }
            }
            if (permissionList.size() <= 0) {
                if (mOnDialogPremission != null) {
                    mOnDialogPremission.isPremission(true);
                }
            }else{
                if (mOnDialogPremission != null) {
                    mOnDialogPremission.isPremission(false);
                }
            }

//            if (permission != PackageManager.PERMISSION_GRANTED) {
//                if (mOnDialogPremission != null) {
//                    mOnDialogPremission.isPremission(false);
//                }
//            } else {
//                if (mOnDialogPremission != null) {
//                    mOnDialogPremission.isPremission(true);
//                }
//            }
        }
    }


    private void toSuccessActivity(int action, String token, int type) {
        TokenEvent codeEvent = new TokenEvent();
        Log.e(TAG, "toSuccessActivity: 12343" + token);
        codeEvent.setCode(token);
        EventBus.getDefault().post(codeEvent);
        finish();

    }

    private void toFailedActivigy(int code, String errorMsg) {
        String msg = errorMsg;
        if (code == 2003) {
            msg = "网络连接不通";
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

        }
//        else if (code == 2005){
//            msg = "请求超时";
//            Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
//
//        }
//        else if (code == 2016){
//            msg = "当前网络环境不支持认证";
//            Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
//
//        }
//        else if (code == 2010){
//            msg = "未开启读取手机状态权限";
//            Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
//
//        }else if (code == 6001){
//            msg = "获取loginToken失败";
//            Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
//
//        }else if (code == 6006){
//            msg = "预取号结果超时，需要重新预取号";
//            Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
//        }

        String url = null;
        if (BuildConfig.DEBUG) {
            url = StaticURL.DEBUG_PHONE_LOGIN;
        } else {
            url = StaticURL.RELEASE_PHONE_LOGIN;
        }
        Intent intent = new Intent(this, WebViewActivity.class);
        intent.putExtra("web_url", url);
        startActivity(intent);
        finish();
    }

    private void toFailedActivityThird(int code, String errorMsg) {
        String msg = errorMsg;
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void toNativeVerifyActivity() {
        String url = null;
        if (BuildConfig.DEBUG) {
            url = StaticURL.DEBUG_PHONE_LOGIN;
        } else {
            url = StaticURL.RELEASE_PHONE_LOGIN;
        }
        Intent intent = new Intent(this, WebViewActivity.class);
        intent.putExtra("web_url", url);
        startActivity(intent);
        finish();
    }

    private int dp2Pix(Context context, float dp) {
        try {
            float density = context.getResources().getDisplayMetrics().density;
            return (int) (dp * density + 0.5F);
        } catch (Exception e) {
            return (int) dp;
        }
    }

}
