package com.smurf.app.base.utils;

import android.content.Context;

import com.smurf.app.base.wx.WechatShareManager;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;

public class ShareUtil {
    private static volatile ShareUtil mInstance;
    private WechatShareManager mShareManager;

    private ShareUtil(Context context) {
        mShareManager = WechatShareManager.getInstance(context);

    }

    public static ShareUtil getInstance(Context context) {
        if (mInstance == null) {
            synchronized (ShareUtil.class) {
                if (mInstance == null) {
                    mInstance = new ShareUtil(context);
                }
            }
        }
        return mInstance;
    }

    public void shareText(int shareType, String text) {
        if(shareType ==0){
            WechatShareManager.ShareContentText mShareContentText = (WechatShareManager.ShareContentText) mShareManager.getShareContentText(text);
            mShareManager.shareByWebchat(mShareContentText, WechatShareManager.WECHAT_SHARE_TYPE_TALK);
        }
        if(shareType == 1){
            WechatShareManager.ShareContentText mShareContentText = (WechatShareManager.ShareContentText) mShareManager.getShareContentText(text);
            mShareManager.shareByWebchat(mShareContentText, WechatShareManager.WECHAT_SHARE_TYPE_FRENDS);
        }
    }

    public void shareImage(int shareType, String uri) {
        if(shareType ==0){
            WechatShareManager.ShareContentPicture mShareContentPicture = (WechatShareManager.ShareContentPicture) mShareManager.getShareContentPicture(uri);
            mShareManager.shareByWebchat(mShareContentPicture, WechatShareManager.WECHAT_SHARE_TYPE_TALK);
        }
        if(shareType == 1){
            WechatShareManager.ShareContentPicture mShareContentPicture = (WechatShareManager.ShareContentPicture) mShareManager.getShareContentPicture(uri);
            mShareManager.shareByWebchat(mShareContentPicture, WechatShareManager.WECHAT_SHARE_TYPE_FRENDS);
        }
    }

    public void shareWebPage(int shareType, String uri, String title,String description) {
        if(shareType ==0){
            WechatShareManager.ShareContentWebpage mShareContentWebPaget = (WechatShareManager.ShareContentWebpage) mShareManager.getShareContentWebpag(title,description,uri);
            mShareManager.shareByWebchat(mShareContentWebPaget, WechatShareManager.WECHAT_SHARE_TYPE_TALK);
        }
        if(shareType ==1){
            WechatShareManager.ShareContentWebpage mShareContentWebPaget = (WechatShareManager.ShareContentWebpage) mShareManager.getShareContentWebpag(title,description,uri);
            mShareManager.shareByWebchat(mShareContentWebPaget, WechatShareManager.WECHAT_SHARE_TYPE_FRENDS);
        }

    }

}
