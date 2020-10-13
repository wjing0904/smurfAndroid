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
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class InstallAppPresenter {

    private static final int MSG_UPDATE = 0;
    private static final int MSG_PROGRESS = 1;
    private static final int MSG_DELAYTIME = 2;

    private static final String APK_INSTALL_URL = "http://39.107.84.57:8090/api/sys/vno/detect";
    private Context mContext;
    private String versionName = "";
    private int versioncode;
    private InstallAPPListener installAPPListener;
    private UpgradeDialog upgradeDialog;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_PROGRESS:
                    String num = (String) msg.obj;
                    if (upgradeDialog != null)
                        upgradeDialog.setProgress(num);
                    break;
                case MSG_UPDATE:
                    String apkPath = (String) msg.obj;
                    UpgradeUtils.getInstance().installAPK(mContext, apkPath);
                    if (installAPPListener != null)
                        installAPPListener.updateNotify();
                    break;
                case MSG_DELAYTIME:
                    if (installAPPListener != null)
                        installAPPListener.updateNotify();
                    break;
            }

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
                Request request = new Request.Builder()
                        .url(APK_INSTALL_URL)
                        .post(requestBody)
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Toast.makeText(mContext, "网路异常，请检查网络", Toast.LENGTH_SHORT).show();
                        handler.sendEmptyMessage(MSG_DELAYTIME);
                        return;
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response == null) {
                            handler.sendEmptyMessage(MSG_DELAYTIME);
                            return;
                        }

                        try {
                            Gson gson = new Gson();
                            CouponBean couponBean = gson.fromJson(response.body().string(), CouponBean.class);
                            if (couponBean.isSuccess() || couponBean.getCode() == 0 && couponBean.getData().isInstallApp()) {
                                //弹窗，升级
                                ThreadUtils.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        getAppVersionName(mContext);
                                        upgradeDialog = new UpgradeDialog(mContext, couponBean, versioncode);
                                        upgradeDialog.setUpgradeNormalListener(new UpgradeDialog.UpgradeNormalListener() {
                                            @Override
                                            public void upgradeForce(String installUrl) {
                                                //下载并通知升级
                                                downApk(installUrl);
                                            }
                                        });
                                        upgradeDialog.show();
                                    }
                                });

                            } else {
                                handler.sendEmptyMessage(MSG_DELAYTIME);
                                return;
                            }
                        } catch (Exception e) {
                            handler.sendEmptyMessage(MSG_DELAYTIME);
                        }
                    }
                });
            }
        });
        thread.start();
    }


    private void downApk(String loadApkUrl) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
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
                    float count = 0;
                    DecimalFormat df = new DecimalFormat("#.##");
                    // 缓存
                    byte buf[] = new byte[1024];
                    // 写入到文件中
                    int numread;
                    while((numread=is.read(buf)) !=-1){
                        fos.write(buf, 0, numread);
                        count += numread;
                        Message message = handler.obtainMessage();
                        message.what = MSG_PROGRESS;
                        message.obj = String.valueOf(df.format((count / length)*100) + "%");
                        handler.sendMessage(message);
                    }
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
                }
                Message message = handler.obtainMessage();
                message.what = MSG_UPDATE;
                message.obj = apkFile.getAbsolutePath();
                handler.sendMessage(message);
            }
        });
        thread.start();
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
