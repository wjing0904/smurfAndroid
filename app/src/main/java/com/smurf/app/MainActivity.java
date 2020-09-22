package com.smurf.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.smurf.app.presenter.LoginPresenter;
import com.smurf.app.view.ILoginViewInterface;

import cn.jiguang.verifysdk.api.AuthPageEventListener;
import cn.jiguang.verifysdk.api.JVerificationInterface;
import cn.jiguang.verifysdk.api.VerifyListener;

public class MainActivity extends BaseActivity implements ILoginViewInterface {

    private ImageView logoImg;
    private TextView delayTime;
    private LoginPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView(){
        logoImg = findViewById(R.id.logo_img);
        Glide.with(this).load(R.mipmap.logo_start).into(logoImg);
        delayTime = findViewById(R.id.record_time_txt);
        if(presenter == null){
            presenter = new LoginPresenter(this);
        }
        presenter.startTimeDelay();
    }

    @Override
    public void startTime(int time) {
        if(delayTime!=null)
            delayTime.setText(time + "s");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(delayTime.getVisibility() == View.GONE && presenter!= null) {
            delayTime.setVisibility(View.VISIBLE);
            presenter.resertTime();
            presenter.startTimeDelay();
        }
    }

    @Override
    public void openLoginActivity() {
//        JVerificationInterface.loginAuth(this, true, new VerifyListener() {
//            @Override
//            public void onResult(final int code, final String content, final String operator) {
//                //登陆成功 打开APP主页面
//            }
//        }, new AuthPageEventListener() {
//            @Override
//            public void onEvent(int cmd, String msg) {
//            }
//        });
        Intent intent = new Intent(this,WebViewActivity.class);
        startActivity(intent);
    }

    @Override
    public void hiddenTimeTxt() {
        if(delayTime!=null)
            delayTime.setVisibility(View.GONE);
    }
}