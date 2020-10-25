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
                .setWechat("wx1bfb50c54805d042", "e5ba6b7e326639c6970199cf068f4cd5")
                .setQQ("", "")
                .setSinaWeibo("", "", "https://www.jiguang.cn");
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

    }
}
