package com.smurf.app.signup;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.fadada.faceverifysdk.FddFaceVerifySdk;
import com.fadada.faceverifysdk.constant.FddCloudFaceConstant;
import com.fadada.faceverifysdk.listeners.FddFaceVerifyResultListener;

public class SignUpFaceVerify {
    public static volatile  SignUpFaceVerify sInstance ;
    private SignUpFaceVerify(){

    }

    public static SignUpFaceVerify getInstance(){
        if(sInstance == null){
            synchronized (SignUpFaceVerify.class){
                if(sInstance == null){
                    sInstance = new SignUpFaceVerify();
                }
            }
        }
        return sInstance;
    }

    public void openFaceVerifySdk(Context context, String url){
        if(TextUtils.isEmpty(url)){
            Toast.makeText(context, "链接地址不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putString(FddCloudFaceConstant.VERIFY_URL, url);
        FddFaceVerifySdk.getInstance().startFddFaceVerifySdk(context, bundle, new FddFaceVerifyResultListener() {
            @Override
            public void onVerifySuccess() {
                Log.v("FddFaceVerifySdk", "刷脸验证成功");
            }

            @Override
            public void onVerifyFailed() {
                Log.v("FddFaceVerifySdk", "刷脸验证失败");
            }

            @Override
            public void onVerifyCancel() {
                Log.v("FddFaceVerifySdk", "刷脸验证取消");
            }
        });
    }
}
