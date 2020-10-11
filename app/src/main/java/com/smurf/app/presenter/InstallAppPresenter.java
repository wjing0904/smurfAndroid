package com.smurf.app.presenter;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.smurf.app.upgrade.CouponBean;
import com.smurf.app.upgrade.UpgradeDialog;
import com.smurf.app.utils.ThreadUtils;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class InstallAppPresenter {

    private static final String APK_INSTALL_URL = "http://39.107.84.57:8090/api/sys/vno/detect";
    private Context mContext;
    private  String versionName = "";
    private  int versioncode;


    public InstallAppPresenter(Context context){
        this.mContext = context;
    }

//    public void test(){
//        UpgradeDialog upgradeDialog = new UpgradeDialog(mContext,null,versioncode);
//        upgradeDialog.setUpgradeNormalListener(new UpgradeDialog.UpgradeNormalListener() {
//            @Override
//            public void upgradeForce() {
//                //下载并通知升级
//            }
//        });
//        upgradeDialog.show();
//
//    }

    /**
     * 检查APK是否需要更新
     */
    public void checkAppInstall(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client =  new OkHttpClient();
                FormBody.Builder builder = new FormBody.Builder();
                builder.add("vuo","");
                Request request = new Request.Builder().url(APK_INSTALL_URL).build();
                Call call = client.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        ThreadUtils.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mContext,"网路异常，请检查网络",Toast.LENGTH_SHORT).show();
                                return;
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        Gson gson = new Gson();
                        CouponBean couponBean = gson.fromJson(response.toString(),CouponBean.class);
                        if(couponBean.isSuccess() || couponBean.getCode() ==0 && couponBean.getData().isIsInstallAppX()){
                            //弹窗，升级
                            getAppVersionName(mContext);
                            UpgradeDialog upgradeDialog = new UpgradeDialog(mContext,null,versioncode);
                            upgradeDialog.setUpgradeNormalListener(new UpgradeDialog.UpgradeNormalListener() {
                                @Override
                                public void upgradeForce(String installUrl) {
                                    //下载并通知升级
                                }
                            });
                            upgradeDialog.show();
                        }else{
                            return;
                        }
                    }
                });
            }
        });
        thread.start();
    }

    /**
     * 返回当前程序版本名  build.gradle里的
     */
    private  String getAppVersionName(Context context) {
        try {
            // ---get the package info---
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName;
            versioncode = pi.versionCode;
            if (versionName == null || versionName.length() <= 0) {
                return "";
            }
        } catch (Exception e) {
            Log.e("VersionInfo", "Exception", e);
        }
        return versionName;
    }
}
