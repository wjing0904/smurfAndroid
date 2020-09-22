package com.smurf.app;

import android.app.Application;
import android.util.Log;

import com.tencent.smtt.sdk.QbSdk;

import cn.jiguang.verifysdk.api.JVerificationInterface;

public class SmurfApplication  extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        JVerificationInterface.setDebugMode(true);
        JVerificationInterface.init(this);

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
    }
}
