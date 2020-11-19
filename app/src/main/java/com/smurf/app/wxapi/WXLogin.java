package com.smurf.app.wxapi;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.smurf.app.event.TokenEvent;
import com.smurf.app.event.WebViewEvent;
import com.smurf.app.login.common.Constants;
import com.smurf.app.utils.ThreadUtils;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

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
    private IWXAPI api;

    public WXLogin(Context context){
        mContext = context;
        api = WXAPIFactory.createWXAPI(context, WXEntity.WECHAT_APP_ID, false);
        api.registerApp(WXEntity.WECHAT_APP_ID);
    }


    public void login(){
        if (!api.isWXAppInstalled()) {
            Toast.makeText(mContext, "您的设备未安装微信客户端", Toast.LENGTH_SHORT).show();
        } else {
            final SendAuth.Req req = new SendAuth.Req();
            req.scope = "snsapi_userinfo";
            req.state = "wechat_sdk_demo_test";
            api.sendReq(req);

        }
    }
}
