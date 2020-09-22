package com.smurf.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.smurf.app.presenter.SplashPresenter;
import com.smurf.app.view.ILoginViewInterface;

public class SplashActivity extends BaseActivity implements ILoginViewInterface {

    private ImageView logoImg;
    private TextView delayTime;
    private SplashPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        logoImg = findViewById(R.id.logo_img);
        Glide.with(this).load(R.mipmap.logo_start).into(logoImg);
        delayTime = findViewById(R.id.record_time_txt);
        if (presenter == null) {
            presenter = new SplashPresenter(this);
        }
        presenter.startTimeDelay();
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
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void hiddenTimeTxt() {
        if (delayTime != null)
            delayTime.setVisibility(View.GONE);
    }
}