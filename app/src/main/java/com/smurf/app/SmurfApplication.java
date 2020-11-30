package com.smurf.app;

import android.app.Application;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.Log;

import com.adhub.ads.AdHubs;
import com.smurf.app.wxapi.WXEntity;
import com.tencent.smtt.sdk.QbSdk;

import cn.jiguang.share.android.api.JShareInterface;
import cn.jiguang.share.android.api.PlatformConfig;
import cn.jiguang.verifysdk.api.JVerificationInterface;
import cn.jiguang.verifysdk.api.RequestCallback;

public class SmurfApplication  extends Application {

    private static final String APPID = "20018";

    private static SmurfApplication SApp;

    public static SmurfApplication getApp() {
        return SApp;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SApp = this;
        JVerificationInterface.setDebugMode(true);
        JVerificationInterface.init(this, new RequestCallback<String>() {
            @Override
            public void onResult(int code, String result) {
                Log.d("MainActivity", "[init] code = " + code + " result = " + result);
            }
        });

        JVerificationInterface.setDebugMode(true);
        JVerificationInterface.init(this);

        JShareInterface.setDebugMode(true);
        PlatformConfig platformConfig = new PlatformConfig()
                .setWechat("101789350", "8bd761ec8be03a0c75477ad1d4eb2a03")
                .setQQ("101789350", "8bd761ec8be03a0c75477ad1d4eb2a03")
                .setSinaWeibo("2906641376", "b495eedd2ac836895eb06c971e521073", "https://www.jiguang.cn");
        JShareInterface.init(this, platformConfig);

        QbSdk.PreInitCallback cb = new QbSdk.PreInitCallback() {

            @Override
            public void onViewInitFinished(boolean arg0) {
            }

            @Override
            public void onCoreInitFinished() {
            }
        };
        //x5内核初始化接口
        QbSdk.initX5Environment(getApplicationContext(), cb);

        //TODO 申请广告的APP ID
        AdHubs.init(this, APPID);
        Resources res = super.getResources();
        Configuration config = new Configuration();
        config.setToDefaults();
        res.updateConfiguration(config, res.getDisplayMetrics());

    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (newConfig.fontScale != 1)//非默认值
            getResources();
        super.onConfigurationChanged(newConfig);
    }


    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        if (res.getConfiguration().fontScale != 1) {//非默认值
            Configuration newConfig = new Configuration();
            newConfig.setToDefaults();//设置默认
            res.updateConfiguration(newConfig, res.getDisplayMetrics());
        }
        return res;
    }
}
