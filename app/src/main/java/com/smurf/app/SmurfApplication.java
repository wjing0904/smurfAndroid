package com.smurf.app;

import android.app.Application;
import android.util.Log;

import com.adhub.ads.AdHubs;
import com.tencent.smtt.sdk.QbSdk;

import cn.jiguang.share.android.api.JShareInterface;
import cn.jiguang.share.android.api.PlatformConfig;
import cn.jiguang.verifysdk.api.JVerificationInterface;
import cn.jiguang.verifysdk.api.RequestCallback;

public class SmurfApplication  extends Application {

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
                .setWechat("wx4a58c62d258121ac", "6f43e3fca5b2c3996c20faf5c2a08729")
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
        AdHubs.init(this, "277");

    }
}
