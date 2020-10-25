package com.smurf.app.login.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.smurf.app.BuildConfig;
import com.smurf.app.R;
import com.smurf.app.StaticURL;
import com.smurf.app.WebViewActivity;
import com.smurf.app.event.TokenEvent;
import com.smurf.app.login.common.Constants;
import com.smurf.app.login.common.PermissionConstants;
import com.smurf.app.login.utils.PermissionUtils;

import org.greenrobot.eventbus.EventBus;

import cn.jiguang.share.android.api.AuthListener;
import cn.jiguang.share.android.api.JShareInterface;
import cn.jiguang.share.android.api.Platform;
import cn.jiguang.share.android.model.AccessTokenInfo;
import cn.jiguang.share.android.model.BaseResponseInfo;
import cn.jiguang.share.android.utils.Logger;
import cn.jiguang.share.wechat.Wechat;
import cn.jiguang.verifysdk.api.JVerificationInterface;
import cn.jiguang.verifysdk.api.JVerifyUIClickCallback;
import cn.jiguang.verifysdk.api.JVerifyUIConfig;
import cn.jiguang.verifysdk.api.VerifyListener;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    private String mNumStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initPermission();
        finish();
//        initShowLoginPage();
    }


    @SuppressLint("WrongConstant")
    private void initPermission() {
        PermissionUtils.permission(PermissionConstants.STORAGE,PermissionConstants.PHONE)
        .callback(new PermissionUtils.SimpleCallback() {
            @Override
            public void onGranted() {
                initShowLoginPage();
            }

            @Override
            public void onDenied() {
                Toast.makeText(MainActivity.this,"未开启读取手机状态权限",Toast.LENGTH_SHORT).show();

            }
        }).request();
    }

    private void initShowLoginPage(){
        JVerificationInterface.setCustomUIWithConfig(getFullScreenPortraitConfig(),null);
        JVerificationInterface.loginAuth(this, new VerifyListener() {
            @Override
            public void onResult(final int code, final String token, String operator) {
                Log.e(TAG, "onResult: code=" + code + ",token=" + token + ",operator=" + operator);
                final String errorMsg = "operator=" + operator + ",code=" + code + "\ncontent=" + token;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (code == Constants.CODE_LOGIN_SUCCESS) {
                            toSuccessActivity(Constants.ACTION_LOGIN_SUCCESS,token,0);
                            Log.e(TAG, "onResult: loginSuccess");
                        } else if(code != Constants.CODE_LOGIN_CANCELD){
                            Log.e(TAG, "onResult: loginError");
                            toFailedActivigy(code,token);
                        }
                    }
                });
            }
        });
    }


    private JVerifyUIConfig getFullScreenPortraitConfig(){
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
        uiConfigBuilder.setAppPrivacyColor(0xFFBBBCC5,0xFF8998FF);
//        uiConfigBuilder.setPrivacyTopOffsetY(310);
        uiConfigBuilder.setPrivacyText("登录即同意《","","","》并使用本机号码登录");
        uiConfigBuilder.setPrivacyCheckboxHidden(false);
        uiConfigBuilder.setPrivacyTextCenterGravity(true);
        uiConfigBuilder.setPrivacyTextSize(12);
//        uiConfigBuilder.setPrivacyOffsetX(52-15);

        // 手机登录按钮
        RelativeLayout.LayoutParams layoutParamPhoneLogin = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParamPhoneLogin.setMargins(0, dp2Pix(this,360.0f),0,0);
        layoutParamPhoneLogin.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        layoutParamPhoneLogin.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        TextView tvPhoneLogin = new TextView(this);
        tvPhoneLogin.setText("其他手机号登录");
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
                JShareInterface.authorize(Wechat.Name, mAuthListener);
            }
        });

        btnWechat.setImageResource(R.drawable.o_wechat);

        LinearLayout.LayoutParams btnParam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        btnParam.setMargins(25,0,25,0);

        layoutLoginGroup.addView(btnWechat,btnParam);
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
    protected void onActivityResult(int requestCode, int resultCode,Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if(resultCode!=0||intent==null){
            return;
        }
        mNumStr = intent.getStringExtra(Constants.KEY_NUM);
    }


    private void toSuccessActivity(int action, String token,int type) {
        TokenEvent codeEvent = new TokenEvent();
        codeEvent.setCode(token);
        codeEvent.setType(type);
        EventBus.getDefault().post(codeEvent);
        finish();

    }
    private void toFailedActivigy(int code, String errorMsg){
        String msg = errorMsg;
        if (code == 2003){
            msg = "网络连接不通";
            Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();

        }else if (code == 2005){
            msg = "请求超时";
            Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();

        }else if (code == 2016){
            msg = "当前网络环境不支持认证";
            Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();

        }else if (code == 2010){
            msg = "未开启读取手机状态权限";
            Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();

        }else if (code == 6001){
            msg = "获取loginToken失败";
            Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();

        }else if (code == 6006){
            msg = "预取号结果超时，需要重新预取号";
            Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
        }

        String url = null;
        if(BuildConfig.DEBUG) {
            url = StaticURL.DEBUG_PHONE_LOGIN;
        }else{
            url = StaticURL.RELEASE_PHONE_LOGIN;
        }
        Intent intent = new Intent(this, WebViewActivity.class);
        intent.putExtra("web_url",url);
        startActivity(intent);
        finish();
    }

    private void toFailedActivityThird(int code, String errorMsg) {
        String msg = errorMsg;
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
    }

    private void toNativeVerifyActivity() {
        String url = null;
        if(BuildConfig.DEBUG) {
            url = StaticURL.DEBUG_PHONE_LOGIN;
        }else{
            url = StaticURL.RELEASE_PHONE_LOGIN;
        }
        Intent intent = new Intent(this, WebViewActivity.class);
        intent.putExtra("web_url",url);
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

    private AuthListener mAuthListener = new AuthListener() {
        @Override
        public void onComplete(Platform platform, int action, BaseResponseInfo data) {
            Logger.dd(TAG, "onComplete:" + platform + ",action:" + action + ",data:" + data);
            String toastMsg = null;
            switch (action) {
                case Platform.ACTION_AUTHORIZING:
                    if (data instanceof AccessTokenInfo) {        //授权信息
                        JVerificationInterface.dismissLoginAuthActivity();
                        String token = ((AccessTokenInfo) data).getToken();//token
                        long expiration = ((AccessTokenInfo) data).getExpiresIn();//token有效时间，时间戳
                        String refresh_token = ((AccessTokenInfo) data).getRefeshToken();//refresh_token
                        String openid = ((AccessTokenInfo) data).getOpenid();//openid
                        //授权原始数据，开发者可自行处理
                        String originData = data.getOriginData();
                        toastMsg = "授权成功:" + data.toString();
                        Logger.dd(TAG, "openid:" + openid + ",token:" + token + ",expiration:" + expiration + ",refresh_token:" + refresh_token);
                        Logger.dd(TAG, "originData:" + originData);
                        toSuccessActivity(Constants.ACTION_THIRD_AUTHORIZED_SUCCESS, token,1);
                        Log.e(TAG, "onResult: loginSuccess");
                    }
                    break;
            }
            JShareInterface.removeAuthorize(platform.getName(),null);
        }

        @Override
        public void onError(Platform platform, int action, int errorCode, Throwable error) {
            Logger.dd(TAG, "onError:" + platform + ",action:" + action + ",error:" + error);
            switch (action) {
                case Platform.ACTION_AUTHORIZING:
                    JVerificationInterface.dismissLoginAuthActivity();
                    Log.e(TAG, "onResult: loginError:"+errorCode);
                    toFailedActivityThird(errorCode, "授权失败" + (error != null ? error.getMessage() : "") + "---" + errorCode);
                    break;
            }
        }

        @Override
        public void onCancel(Platform platform, int action) {
            Logger.dd(TAG, "onCancel:" + platform + ",action:" + action);
            String toastMsg = null;
            switch (action) {
                case Platform.ACTION_AUTHORIZING:
                    toastMsg = "取消授权";
                    break;
            }
        }
    };
}
