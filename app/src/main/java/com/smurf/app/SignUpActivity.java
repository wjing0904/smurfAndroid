package com.smurf.app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;


/**
 * 签约&& 认证
 */

public class SignUpActivity extends Activity {
    private WebView mWebView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_webview);
        String signUrl = getIntent().getStringExtra("sign_url");
        mWebView = (WebView) findViewById(R.id.webview);
        mWebView.getSettings();
        mWebView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                String referer = "http://smurf.langongbao.com";
                try {
                    if (url.startsWith("weixin://") || url.startsWith("alipays://")) {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(url));
                        startActivity(intent);
                        return true;
                    }
                }catch (Exception e){
                    return false;
                }

                if(url.contains("https://wx.tenpay.com")){
                    Map<String,String> extraHeaders = new HashMap<>();
                    extraHeaders.put("Referer",referer);
                    view.loadUrl(url,extraHeaders);
                    referer = url;
                    return true;
                }
                return super.shouldOverrideUrlLoading(view, url);
            }
        });
        mWebView.loadUrl(signUrl);
    }
}
