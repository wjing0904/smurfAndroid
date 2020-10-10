package com.smurf.app.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.smurf.app.share.WechatShareManager;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.ArrayList;

import static android.util.Patterns.EMAIL_ADDRESS;

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

    public void shareText(Context context, String text, String title) {
        WechatShareManager.ShareContentText mShareContentText = (WechatShareManager.ShareContentText) mShareManager.getShareContentText(text);
        mShareManager.shareByWebchat(mShareContentText, WechatShareManager.WECHAT_SHARE_TYPE_FRENDS);
    }

    public void shareImage(Context context, String uri, String title) {
        WechatShareManager.ShareContentPicture mShareContentPicture = (WechatShareManager.ShareContentPicture) mShareManager.getShareContentPicture(uri);
        mShareManager.shareByWebchat(mShareContentPicture, WechatShareManager.WECHAT_SHARE_TYPE_FRENDS);
    }

    public void shareWebPage(Context context, String uri, String title) {
        WechatShareManager.ShareContentWebpage mShareContentWebPaget = (WechatShareManager.ShareContentWebpage) mShareManager.getShareContentWebpag(title,"",uri,-1);
        mShareManager.shareByWebchat(mShareContentWebPaget, WechatShareManager.WECHAT_SHARE_TYPE_FRENDS);
    }

}
