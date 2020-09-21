package com.smurf.app;

import android.app.Application;

import cn.jiguang.verifysdk.api.JVerificationInterface;

public class SmurfApplication  extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        JVerificationInterface.init(this);
    }
}
