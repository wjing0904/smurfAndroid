package com.smurf.app.jsp;

import android.webkit.JavascriptInterface;
//js 调用 原生 方法

public interface JavaScriptInterface {

    @JavascriptInterface
    public void goSCan();


    @JavascriptInterface
    public void imageSelected(int picNum);

    @JavascriptInterface
    public void openRewarderVideo();

}
