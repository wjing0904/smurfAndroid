package com.smurf.app.login.activity;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.smurf.app.event.TokenEvent;
import com.smurf.app.login.common.Constants;
import com.smurf.app.utils.ThreadUtils;

import org.greenrobot.eventbus.EventBus;

import cn.jiguang.share.android.api.AuthListener;
import cn.jiguang.share.android.api.JShareInterface;
import cn.jiguang.share.android.api.Platform;
import cn.jiguang.share.android.model.AccessTokenInfo;
import cn.jiguang.share.android.model.BaseResponseInfo;
import cn.jiguang.share.android.utils.Logger;
import cn.jiguang.share.wechat.Wechat;
import cn.jiguang.verifysdk.api.JVerificationInterface;

public class WXLogin {
    private Context mContext;
    public WXLogin(Context context){
        mContext = context;
    }


    public void login(){
        JShareInterface.authorize(Wechat.Name, mAuthListener);
    }

    private AuthListener mAuthListener = new AuthListener() {
        @Override
        public void onComplete(Platform platform, int action, BaseResponseInfo data) {
            String toastMsg = null;
            switch (action) {
                case Platform.ACTION_AUTHORIZING:
                    if (data instanceof AccessTokenInfo) {        //授权信息
                        JVerificationInterface.dismissLoginAuthActivity();
                        String token = ((AccessTokenInfo) data).getToken();//token
                        long expiration = ((AccessTokenInfo) data).getExpiresIn();//token有效时间，时间戳
                        String refresh_token = ((AccessTokenInfo) data).getRefeshToken();//refresh_token
                        String openid = ((AccessTokenInfo) data).getOpenid();//openid
                        //授权原始数据，开发者可自行处理
                        String originData = data.getOriginData();
                        toastMsg = "授权成功:" + data.toString();
                        TokenEvent codeEvent = new TokenEvent();
                        codeEvent.setCode(token);
                        codeEvent.setType(1);
                        EventBus.getDefault().post(codeEvent);
                    }
                    break;
            }
            JShareInterface.removeAuthorize(platform.getName(),null);
        }

        @Override
        public void onError(Platform platform, int action, int errorCode, Throwable error) {
            switch (action) {
                case Platform.ACTION_AUTHORIZING:
                    JVerificationInterface.dismissLoginAuthActivity();
                    ThreadUtils.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mContext,"授权失败" + (error != null ? error.getMessage() : "") + "---" + errorCode,Toast.LENGTH_SHORT).show();
                        }
                    });
                    break;
            }
        }

        @Override
        public void onCancel(Platform platform, int action) {
            String toastMsg = null;
            switch (action) {
                case Platform.ACTION_AUTHORIZING:
                    toastMsg = "取消授权";
                    break;
            }
        }
    };
}
