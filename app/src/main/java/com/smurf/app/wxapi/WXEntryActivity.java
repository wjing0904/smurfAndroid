/*
 * 官网地站:http://www.mob.com
 * 技术支持QQ: 4006852216
 * 官方微信:ShareSDK   （如果发布新版本的话，我们将会第一时间通过微信将版本更新内容推送给您。如果使用过程中有任何问题，也可以通过微信与我们取得联系，我们将会在24小时内给予回复）
 *
 * Copyright (c) 2013年 mob.com. All rights reserved.
 */

package com.smurf.app.wxapi;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ProgressBar;

import com.smurf.app.base.event.WxEvent;
import com.smurf.app.base.wx.WXEntity;
import com.smurf.app.login.utils.ToastUtil;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.greenrobot.eventbus.EventBus;

import cn.jiguang.share.wechat.WeChatHandleActivity;

/** 微信客户端回调activity示例 */
public class WXEntryActivity extends WeChatHandleActivity implements IWXAPIEventHandler {
    private IWXAPI iwxapi;
    private String unionid;
    private String openid;
    private ProgressBar progressBar;
    private WXEntryActivity mContext;
    private ProgressDialog mProgressDialog;
    private String code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getSupportActionBar().hide();
        Log.d("smurf","onCreate() ");

        // 隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Log.d("smurf","onCreate() 111 ");
        //接收到分享以及登录的intent传递handleIntent方法，处理结果
        iwxapi = WXAPIFactory.createWXAPI(this, WXEntity.WECHAT_APP_ID, false);

        Log.d("smurf","onCreate() iwxapi ");
        iwxapi.handleIntent(getIntent(), this);

        Log.d("smurf","onCreate() handleIntent ");

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        iwxapi.handleIntent(intent, this);
    }
    private void createProgressDialog() {
        mContext=this;
        mProgressDialog=new ProgressDialog(mContext);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);//转盘
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setTitle("提示");
        mProgressDialog.setMessage("登录中，请稍后");
        mProgressDialog.show();
    }

    @Override
    public void onReq(BaseReq baseReq) {
    }

    //请求回调结果处理
    @Override
    public void onResp(BaseResp baseResp) {
        //登录回调
        switch (baseResp.errCode) {
            case BaseResp.ErrCode.ERR_OK:
                Log.d("smurf","BaseResp.ErrCode.ERR_OK ");

                code = ((SendAuth.Resp) baseResp).code;
//                getAccessToken(code); //用户同意授权
                Log.e("smurf", "onResp: "+code);
                WxEvent wxEvent = new WxEvent();
                wxEvent.token = code;
                EventBus.getDefault().post(wxEvent);
                Log.d("smurf","EventBus.getDefault().post(wxEvent)");
                finish();
                Log.d("smurf","finish ");
                break;
            case BaseResp.ErrCode.ERR_AUTH_DENIED://用户拒绝授权
                Log.d("smurf","BaseResp.ErrCode.ERR_AUTH_DENIED");
                finish();
                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL://用户取消
                Log.d("smurf","BaseResp.ErrCode.ERR_USER_CANCEL");
                finish();
                break;
            default:
                Log.d("smurf","default");
                finish();
                break;
        }
    }

//    private void getAccessToken(String code) {
//        createProgressDialog();
//        //获取授权
//        StringBuffer loginUrl = new StringBuffer();
//        loginUrl.append("https://api.weixin.qq.com/sns/oauth2/access_token")
//                .append("?appid=")
//                .append(WXEntity.WECHAT_APP_ID)
//                .append("&secret=")
//                .append(WXEntity.APP_SECRET)
//                .append("&code=")
//                .append(code)
//                .append("&grant_type=authorization_code");
//        Log.d("urlurl", loginUrl.toString());
//
//        OkHttpClient okHttpClient = new OkHttpClient();
//        final Request request = new Request.Builder()
//                .url(loginUrl.toString())
//                .get()//默认就是GET请求，可以不写
//                .build();
//        Call call = okHttpClient.newCall(request);
//        call.enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                mProgressDialog.dismiss();
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                String responseInfo= response.body().string();
//                String access = null;
//                String openId = null;
//                try {
//                    JSONObject jsonObject = new JSONObject(responseInfo);
//                    access = jsonObject.getString("access_token");
//                    openId = jsonObject.getString("openid");
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//                getUserInfo(access, openId);
//            }
//        });
//
//
//    }
//
//    private void getUserInfo(String access, String openid) {
//        String getUserInfoUrl = "https://api.weixin.qq.com/sns/userinfo?access_token=" + access + "&openid=" + openid;
//
//        OkHttpClient okHttpClient = new OkHttpClient();
//        final Request request = new Request.Builder()
//                .url(getUserInfoUrl)
//                .get()//默认就是GET请求，可以不写
//                .build();
//        Call call = okHttpClient.newCall(request);
//        call.enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                mProgressDialog.dismiss();
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                String responseInfo= response.body().string();
//                if (!responseInfo.isEmpty()){
//                    try {
//                        JSONObject jsonObject = new JSONObject(responseInfo);
//                        WxUserBean wxUserBean = new WxUserBean();
//                        wxUserBean.icon = jsonObject.getString("headimgurl");
//                        wxUserBean.nickName = jsonObject.getString("nickname");
//                        wxUserBean.openId = code;
//
//                        Intent intent = new Intent(WXEntryActivity.this,WxResultActivity.class);
//                        intent.putExtra("wx_userInfo",wxUserBean);
//                        startActivity(intent);
//                        mProgressDialog.dismiss();
//                        finish();
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        });
//    }
}
