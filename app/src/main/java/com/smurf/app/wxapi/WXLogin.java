package com.smurf.app.wxapi;

import android.content.Context;
import android.widget.Toast;

import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

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
