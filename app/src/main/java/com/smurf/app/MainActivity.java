package com.smurf.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

public class MainActivity extends BaseActivity {

    private static final int MSG_DELAY_TIME_WHAT = 0;
    private ImageView logoImg;
    private int time = 3;
    private TextView delayTime;

    //开始录音倒计时 3s
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            time--;
            delayTime.setText(String.valueOf(time));
            if (time == 0) {
                handler.removeMessages(MSG_DELAY_TIME_WHAT);
                delayTime.setVisibility(View.GONE);
                startLoginActivity();
            } else {
                handler.sendEmptyMessageDelayed(MSG_DELAY_TIME_WHAT, 1000);
            }
        }
    };

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
        handler.sendEmptyMessageDelayed(MSG_DELAY_TIME_WHAT, 1000);
    }

    private void startLoginActivity(){
        Intent intent = new Intent(this,LoginActivity.class);
        startActivity(intent);
    }
}