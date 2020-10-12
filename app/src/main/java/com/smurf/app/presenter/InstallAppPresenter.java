package com.smurf.app.presenter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.smurf.app.upgrade.CouponBean;
import com.smurf.app.upgrade.UpgradeDialog;
import com.smurf.app.upgrade.UpgradeUtils;
import com.smurf.app.utils.FileUtils;
import com.smurf.app.utils.ThreadUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class InstallAppPresenter {

    private static final String APK_INSTALL_URL = "http://39.107.84.57:8090/api/sys/vno/detect";
    private Context mContext;
    private String versionName = "";
    private int versioncode;
    private InstallAPPListener installAPPListener;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            String apkPath = (String) msg.obj;
            UpgradeUtils.getInstance().installAPK(mContext, apkPath);
            if (installAPPListener != null)
                installAPPListener.updateNotify();
        }
    };


    public InstallAppPresenter(Context context) {
        this.mContext = context;
    }

    public void setInstallAppListener(InstallAPPListener installAppListener) {
        this.installAPPListener = installAppListener;
    }

    /**
     * 检查APK是否需要更新
     */
    public void checkAppInstall() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                FormBody.Builder builder = new FormBody.Builder();
                builder.add("vuo", "");
                Request request = new Request.Builder().url(APK_INSTALL_URL).build();
                Call call = client.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        ThreadUtils.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mContext, "网路异常，请检查网络", Toast.LENGTH_SHORT).show();
                                if (installAPPListener != null)
                                    installAPPListener.updateNotify();
                                return;
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response == null) {
                            if (installAPPListener != null)
                                installAPPListener.updateNotify();
                            return;
                        }
                        Gson gson = new Gson();
                        CouponBean couponBean = gson.fromJson(response.toString(), CouponBean.class);
                        if (couponBean.isSuccess() || couponBean.getCode() == 0 && couponBean.getData().isIsInstallAppX()) {
                            //弹窗，升级
                            getAppVersionName(mContext);
                            UpgradeDialog upgradeDialog = new UpgradeDialog(mContext, null, versioncode);
                            upgradeDialog.setUpgradeNormalListener(new UpgradeDialog.UpgradeNormalListener() {
                                @Override
                                public void upgradeForce(String installUrl) {
                                    //下载并通知升级
                                    downApk(installUrl);
                                }
                            });
                            upgradeDialog.show();
                        } else {
                            if (installAPPListener != null)
                                installAPPListener.updateNotify();
                            return;
                        }
                    }
                });
            }
        });
        thread.start();
    }


    private void downApk(String loadApkUrl) {
        InputStream is = null;
        FileOutputStream fos = null;
        File apkFile = null;
        try {
            // 获得存储卡的路径
            String sdpath = FileUtils.getAppPath() + "/";
            String mSavePath = sdpath + "download";
            URL url = new URL(loadApkUrl);
            // 创建连接
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            // 获取文件大小
            int length = conn.getContentLength();
            // 创建输入流
            is = conn.getInputStream();

            File file = new File(mSavePath);
            // 判断文件目录是否存在
            if (!file.exists()) {
                file.mkdir();
            }
            apkFile = new File(mSavePath, "smurf");
            fos = new FileOutputStream(apkFile);
            int count = 0;
            // 缓存
            byte buf[] = new byte[1024];
            // 写入到文件中
            do {
                int numread = is.read(buf);
                // 写入文件
                fos.write(buf, 0, numread);
            } while (true);// 点击取消就停止下载.

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            Message message = handler.obtainMessage();
            message.obj = apkFile.getAbsolutePath();
            handler.sendMessage(message);
        }

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
}
