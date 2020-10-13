package com.smurf.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.smurf.app.login.activity.MainActivity;
import com.smurf.app.presenter.InstallAPPListener;
import com.smurf.app.presenter.InstallAppPresenter;
import com.smurf.app.presenter.SplashPresenter;
import com.smurf.app.view.ILoginViewInterface;

public class SplashActivity extends Activity implements ILoginViewInterface {

    private ImageView logoImg;
    private TextView delayTime;
    private SplashPresenter presenter;
    private InstallAppPresenter installAppPresenter;


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
        Intent intent = new Intent(this, WebViewActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void hiddenTimeTxt() {
        if (delayTime != null)
            delayTime.setVisibility(View.GONE);
    }
}