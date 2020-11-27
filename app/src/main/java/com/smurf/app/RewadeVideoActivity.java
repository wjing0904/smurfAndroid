package com.smurf.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Toast;

import com.adhub.ads.RewardedVideoAd;
import com.adhub.ads.RewardedVideoAdListener;
import com.smurf.app.base.event.VideoEvent;

import org.greenrobot.eventbus.EventBus;


public class RewadeVideoActivity extends Activity {
    private static final String ADID = "102903";

    private RewardedVideoAd mRewardedVideoAd;
    private AlertDialog alertDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showLoadingDialog();
        showRewadeVideo();
    }

    private void showRewadeVideo(){
        //TODO 拉去广告的 ADId
        mRewardedVideoAd = new RewardedVideoAd(this, ADID,new RewardedVideoAdListener() {

            /**
             * 获得奖励
             */
            @Override
            public void onRewarded() {
            }

            /**
             * 加载失败
             * @param i 错误码
             */
            @Override
            public void onRewardedVideoAdFailedToLoad(int i) {
                dismissLoadingDialog();
                VideoEvent videoEvent = new VideoEvent();
                videoEvent.isVideoEnd = true;
                EventBus.getDefault().post(videoEvent);
                Toast.makeText(RewadeVideoActivity.this,"视频加载失败，请确定网络环境后重新点击观看",Toast.LENGTH_SHORT).show();
                finish();
            }

            /**
             * 广告加载成功
             */
            @Override
            public void onRewardedVideoAdLoaded() {
                dismissLoadingDialog();
                //展示广告
                if(mRewardedVideoAd.isLoaded()){
                    VideoEvent videoEvent = new VideoEvent();
                    videoEvent.isVideoEnd = false;
                    EventBus.getDefault().post(videoEvent);
                    mRewardedVideoAd.showAd(RewadeVideoActivity.this);
                }
            }

            /**
             * 广告展示
             */
            @Override
            public void onRewardedVideoAdShown() {
            }

            /**
             * 广告关闭
             */
            @Override
            public void onRewardedVideoAdClosed() {
                dismissLoadingDialog();
                VideoEvent videoEvent = new VideoEvent();
                videoEvent.isVideoEnd = true;
                EventBus.getDefault().post(videoEvent);
                finish();
            }

            /**
             * 广告点击
             */
            @Override
            public void onRewardedVideoClick() {
            }
        },5000);////激励视频加载超时时长，建议5秒以上,该参数单位为ms

        if (!mRewardedVideoAd.isLoaded()) {
            mRewardedVideoAd.loadAd();
        }
    }

    @Override
    protected void onDestroy() {
        dismissLoadingDialog();
        if (mRewardedVideoAd != null){
            mRewardedVideoAd.destroy();
        }
        super.onDestroy();
    }

    public void showLoadingDialog() {
        dismissLoadingDialog();
        alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable());
        alertDialog.setCancelable(false);
        alertDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_SEARCH || keyCode == KeyEvent.KEYCODE_BACK)
                    return true;
                return false;
            }
        });
        alertDialog.show();
        alertDialog.setContentView(R.layout.loading_alert);
        alertDialog.setCanceledOnTouchOutside(false);
    }

    public void dismissLoadingDialog() {
        if (null != alertDialog && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
    }

}
