package com.smurf.app.wxapi;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.smurf.app.R;
import com.smurf.app.base.event.WxEvent;

import org.greenrobot.eventbus.EventBus;

public class WxResultActivity extends Activity implements View.OnClickListener{

    private ImageView userLogo;
    private TextView usetName;
    private TextView sureBtn;
    private WxUserBean wxUserBean;
    private TextView closeBtn;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wx_result);
        wxUserBean = (WxUserBean) getIntent().getSerializableExtra("wx_userInfo");
        initView();
        initData();
    }

    private void initView(){
        this.userLogo = findViewById(R.id.user_icon);
        this.usetName = findViewById(R.id.user_name);
        sureBtn = findViewById(R.id.user_sure);
        sureBtn.setOnClickListener(this);
        closeBtn = findViewById(R.id.back);
        closeBtn.setOnClickListener(this);
    }

    private void initData(){
        Glide.with(this).load(wxUserBean.icon).into(userLogo);
        usetName.setText(wxUserBean.nickName);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.user_sure) {
            WxEvent wxEvent = new WxEvent();
            wxEvent.token = wxUserBean.openId;
            EventBus.getDefault().post(wxEvent);
        }
        finish();
    }
}
