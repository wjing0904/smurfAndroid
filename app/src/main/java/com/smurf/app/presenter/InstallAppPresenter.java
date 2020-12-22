package com.smurf.app.presenter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import com.google.gson.Gson;
import com.smurf.app.BuildConfig;
import com.smurf.app.base.StaticURL;
import com.smurf.app.upgrade.CouponBean;
import com.smurf.app.upgrade.UpgradeDialog;
import com.smurf.app.upgrade.UpgradeUtils;
import com.smurf.app.utils.FileUtils;
import com.smurf.app.utils.ThreadUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class InstallAppPresenter {


    private static final int MSG_PROGRESS = 1;
    private static final int MSG_INSTALL = 2;
    private static final int NOT_DOWN = 3;
    private String apkName;
    private Context mContext;
    private String versionName = "";
    private int versioncode;
    private UpgradeDialog upgradeDialog;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_PROGRESS:
                    // 设置进度条
                    upgradeDialog.setProgress(mProgress + "%");
                    break;
                case MSG_INSTALL:
                    // 隐藏当前下载对话框
                    if (upgradeDialog != null)
                        upgradeDialog.dismiss();

                    openFile(new File(mSavePath+"/"+apkName));
            }

        }
    };
    private int mProgress;
    private String mSavePath;


    public InstallAppPresenter(Context context) {
        this.mContext = context;
    }

    /**
     * 检查APK是否需要更新
     */
    public void checkAppInstall() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                JSONObject json = new JSONObject();
                try {
                    json.put("vno", getAppVersionName(mContext));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //1 . 拿到OkHttpClient对象
                OkHttpClient client = new OkHttpClient();
                //创建一个RequestBody(参数1：数据类型 参数2传递的json串)
                RequestBody requestBody = RequestBody.create(JSON, String.valueOf(json));
                //3 . 构建Request,将FormBody作为Post方法的参数传入
                String url = null;
                if (BuildConfig.DEBUG) {
                    url = StaticURL.DEBUG_APK_INSTALL_URL;
                } else {
                    url = StaticURL.RELEASE_APK_INSTALL_URL;
                }
                Request request = new Request.Builder()
                        .url(url)
                        .post(requestBody)
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        ThreadUtils.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mContext, "网路异常，请检查网络", Toast.LENGTH_SHORT).show();
                            }
                        });
                        handler.sendEmptyMessage(NOT_DOWN);
                        return;
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response == null) {
                            handler.sendEmptyMessage(NOT_DOWN);
                            return;
                        }
                        try {
                            Gson gson = new Gson();
                            CouponBean couponBean = gson.fromJson(response.body().string(), CouponBean.class);
                            String versionCode = getAppVersionCode(mContext);
                            if (couponBean.isSuccess() && couponBean.getCode() == 0 && couponBean.getData().isInstallApp() && !versionCode.equals(couponBean.getData().getVno())) {
                                //弹窗，升级
                                ThreadUtils.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        getAppVersionName(mContext);
                                        upgradeDialog = new UpgradeDialog(mContext, couponBean, getAppVersionName(mContext));
                                        upgradeDialog.setUpgradeNormalListener(new UpgradeDialog.UpgradeNormalListener() {
                                            @Override
                                            public void upgradeForce(String installUrl) {
                                                //下载并通知升级
                                                downloadAPK(installUrl);
                                            }
                                        });
                                        upgradeDialog.show();
                                        upgradeDialog.setCanceledOnTouchOutside(false);//可选，点击dialog其它地方dismiss无效
                                        upgradeDialog.setCancelable(false);//可选,点击返回键无效

                                    }
                                });

                            } else {
                                handler.sendEmptyMessage(NOT_DOWN);
                                return;
                            }
                        } catch (Exception e) {
                            handler.sendEmptyMessage(NOT_DOWN);
                        }
                    }
                });
            }
        });
        thread.start();
    }

    public static String getAppVersionCode(Context context) {
        long appVersionCode = 0;
        try {
            PackageInfo packageInfo = context.getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                appVersionCode = packageInfo.getLongVersionCode();
            } else {
                appVersionCode = packageInfo.versionCode;
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("", e.getMessage());
        }
        return appVersionCode + "";
    }

    /**
     * 下载APk
     */
    private void downloadAPK(final String loadApkUrl) {
        new Thread(new Runnable() {



            @Override
            public void run() {
                File apkFile = null;
                try {
                    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

                        String sdPath = Environment.getExternalStorageDirectory() + "/";

                        //文件保存路径
                        mSavePath = sdPath + "Download";

                        File dir = new File(mSavePath);
                        if (!dir.exists()) {
                            dir.mkdir();
                        }

                        apkName = "smurf_" + getAppVersionCode(mContext) + ".apk";

                        // 下载文件
                        HttpURLConnection conn = (HttpURLConnection) new URL(loadApkUrl).openConnection();
                        conn.connect();
                        InputStream is = conn.getInputStream();
                        int length = conn.getContentLength();
                        apkFile = new File(mSavePath, apkName);
                        FileOutputStream fos = new FileOutputStream(apkFile);
                        int count = 0;
                        int len = -1;

                        byte[] buffer = new byte[1024];
                        while ((len = is.read(buffer)) != -1) {
                            fos.write(buffer, 0, len);
//                            numread = is.read(buffer);
                            count += len;
                            mProgress = (int) ((((float) count / length) * 100));
                            // 更新进度条
                            handler.sendEmptyMessage(MSG_PROGRESS);


                        }
                        fos.close();
                        is.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                handler.sendEmptyMessage(MSG_INSTALL);
            }
        }).start();
    }

    /**
     * 返回当前程序版本名  build.gradle里的
     */
    private String getAppVersionName(Context context) {
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

    //打开APK程序代码
    private void openFile(File file) {
        Log.e("OpenFile", file.getName());

        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(mContext,
                    mContext.getApplicationContext().getPackageName() + ".fileProvider",
                    file);
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
        } else {
            uri = Uri.fromFile(file);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
        }
        mContext.startActivity(intent);
//        这个必须添加，不然无法提示打开应用操作
        android.os.Process.killProcess(android.os.Process.myPid());

    }
}
