package com.smurf.app;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.smurf.app.login.activity.MainActivity;
import com.smurf.app.presenter.InstallAPPListener;
import com.smurf.app.presenter.InstallAppPresenter;
import com.smurf.app.presenter.SplashPresenter;
import com.smurf.app.view.ILoginViewInterface;

public class SplashActivity extends Activity implements ILoginViewInterface {


    private static final String DEBUG_APP_URL = "http://39.107.84.57:8091/#/home";
    private static final String RELEASE_APP_URL = "";

    private ImageView logoImg;
    private TextView delayTime;
    private SplashPresenter presenter;
    private InstallAppPresenter installAppPresenter;
    //先定义
    private static final int REQUEST_EXTERNAL_STORAGE = 1;

    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE" };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_main);
        initView();
    }

    private void initView() {
        logoImg = findViewById(R.id.logo_img);
        Glide.with(this).load(R.mipmap.logo_start).into(logoImg);
        delayTime = findViewById(R.id.record_time_txt);
        verifyStoragePermissions(this);

        //检查APP是否需要更新
        if(installAppPresenter == null){
            installAppPresenter = new InstallAppPresenter(this);
        }
        installAppPresenter.setInstallAppListener(new InstallAPPListener() {
            @Override
            public void updateNotify() {
                if (presenter == null) {
                    presenter = new SplashPresenter(SplashActivity.this);
                }
                presenter.startTimeDelay();
            }
        });
        installAppPresenter.checkAppInstall();

    }

    @Override
    public void startTime(int time) {
        if (delayTime != null)
            delayTime.setText(time + "s");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (delayTime.getVisibility() == View.GONE && presenter != null) {
            delayTime.setVisibility(View.VISIBLE);
            presenter.resertTime();
            presenter.startTimeDelay();
        }
    }

    @Override
    public void openLoginActivity() {
        String url = null;
        if (BuildConfig.DEBUG) {
            url = DEBUG_APP_URL;
        } else {
            url = RELEASE_APP_URL;
        }
        Intent intent = new Intent(this, WebViewActivity.class);
        intent.putExtra("web_url",url);
        startActivity(intent);
        finish();
    }

    @Override
    public void hiddenTimeTxt() {
        if (delayTime != null)
            delayTime.setVisibility(View.GONE);
    }

    //然后通过一个函数来申请
    public static void verifyStoragePermissions(Activity activity) {
        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}