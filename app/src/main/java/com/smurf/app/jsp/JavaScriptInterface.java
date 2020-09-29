package com.smurf.app.jsp;

import android.webkit.JavascriptInterface;
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

}
