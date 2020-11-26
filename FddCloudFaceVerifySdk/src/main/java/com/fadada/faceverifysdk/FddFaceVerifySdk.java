package com.fadada.faceverifysdk;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.fadada.faceverifysdk.listeners.FddFaceVerifyResultListener;
import com.fadada.faceverifysdk.ui.FaceVerifyHostActivity;

public class FddFaceVerifySdk {
    private static volatile FddFaceVerifySdk instance;

    private FddFaceVerifyResultListener verifyResultListener;

    public static FddFaceVerifySdk getInstance() {
        if (instance == null) {
            synchronized(FddFaceVerifySdk.class) {
                if (instance == null) {
                    instance = new FddFaceVerifySdk();
                }
            }
        }
        return instance;
    }

    public void startFddFaceVerifySdk(Context context, Bundle data, FddFaceVerifyResultListener listener) {
        // TODO: 2020/9/24 Log
        if (context == null || data == null || listener == null) {
            // TODO: 2020/9/27 Log 
            throw new NullPointerException("参数不能为空");
        }
        verifyResultListener = listener;
        Intent intent = new Intent(context, FaceVerifyHostActivity.class);
        intent.putExtras(data);
        context.startActivity(intent);
    }

    public FddFaceVerifyResultListener getVerifyResultListener() {
        return verifyResultListener;
    }
}
