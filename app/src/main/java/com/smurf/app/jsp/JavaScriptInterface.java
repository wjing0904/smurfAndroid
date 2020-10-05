package com.smurf.app.jsp;

import android.net.Uri;
import android.webkit.JavascriptInterface;

import java.util.ArrayList;
//js 调用 原生 方法

public interface JavaScriptInterface {
    /**
     *  js 调用原生二维码
     */
    @JavascriptInterface
    public void goSCan();

    /**
     * js调用原生图片选择
     * @param picNum
     */
    @JavascriptInterface
    public void imageSelected(int picNum);

    /**
     * js调用原生 激励视频
     */
    @JavascriptInterface
    public void openRewarderVideo();
    /**
     * js调用原生 获取当前定位
     */
    @JavascriptInterface
    public void getLocation();

    /**
     * 图片保存
     */
    @JavascriptInterface
    public void openAndSaveImg(String url);

    /**
     * 分享功能
     * shareType 0：分享文本内容，1：分享单张图片，2：分享多张图片
     */
    @JavascriptInterface
    public void share(int shareType, String title, String text, Uri uri, ArrayList<Uri> imageUris);

    /**
     * js调用原生 已签约 && 认证 && 打开第三方链接 && 打开h5
     */
    public void signUp(String signUrl);

}
