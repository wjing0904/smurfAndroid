package com.smurf.app.presenter;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.view.View;

import androidx.annotation.NonNull;

import com.smurf.app.view.ILoginViewInterface;

public class SplashPresenter {
    private static final int MSG_DELAY_TIME_WHAT = 0;
    private ILoginViewInterface loginViewInterface;
    private int time = 3;

    //开始录音倒计时 3s
    private Handler handler = new Handler() {
        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            time--;
            if(loginViewInterface!= null)
                loginViewInterface.startTime(time);
            if (time == 0) {
                handler.removeMessages(MSG_DELAY_TIME_WHAT);
                if(loginViewInterface!= null) {
                    loginViewInterface.hiddenDelayView();
                }
            } else {
                handler.sendEmptyMessageDelayed(MSG_DELAY_TIME_WHAT, 1000);
            }
        }
    };

    public SplashPresenter(ILoginViewInterface iLoginViewInterface){
        this.loginViewInterface = iLoginViewInterface;
    }

    public void startTimeDelay(){
        if(handler!=null)
            handler.sendEmptyMessageDelayed(MSG_DELAY_TIME_WHAT, 1000);
    }

    public void resertTime(){
        time = 3;
    }

}
