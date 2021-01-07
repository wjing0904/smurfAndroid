package com.smurf.app;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.fadada.faceverifysdk.constant.FddCloudFaceConstant;
import com.smurf.app.base.utils.ShareUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ShopWebViewActivity extends AppCompatActivity {

    private WebView webView;

    private TextView back;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.fadada.faceverifysdk.R.layout.activity_fddcloudface);

        webView = (WebView) findViewById(com.fadada.faceverifysdk.R.id.wv_host);


        Bundle bundle = getIntent().getExtras();
        String h5Url = bundle.getString(FddCloudFaceConstant.VERIFY_URL);
        back = findViewById(com.fadada.faceverifysdk.R.id.web_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (webView != null) {
                    webView.loadUrl("javascript:back()");
                }
            }
        });

        //开启JS调用逻辑
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setJavaScriptEnabled(true);

        //不通过外部浏览器打开
        webView.setWebChromeClient(new WebChromeClient() {
            // For Android < 3.0
            public void openFileChooser(ValueCallback<Uri> valueCallback) {
            }

            // For Android  >= 3.0
            public void openFileChooser(ValueCallback valueCallback, String acceptType) {
            }

            //For Android  >= 4.1
            public void openFileChooser(ValueCallback<Uri> valueCallback, String acceptType, String capture) {
            }

            // For Android >= 5.0
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
                return true;
            }
        });
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                try {
                    if (url.startsWith("weixin://") || url.startsWith("alipays://")) {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(url));
                        startActivity(intent);
                        return true;
                    }
                } catch (Exception e) {
                    return false;
                }
                if (url.contains("https://wx.tenpay.com")) {
                    Map<String, String> extraHeaders = new HashMap<>();
                    extraHeaders.put("Referer", "http://shop.langongbao.com");
                    view.loadUrl(url, extraHeaders);
                    return true;
                }

                if (url.startsWith("http://") || url.startsWith("https://")) { //加载的url是http/https协议地址
                    view.loadUrl(url);
                    return false; //返回false表示此url默认由系统处理,url未加载完成，会继续往下走
                } else {
                    return true;
                }
//                return true;
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }
        });

        //区分app和H5调用刷脸的标记
        String ua = webView.getSettings().getUserAgentString();
        webView.getSettings().setUserAgentString(ua+";  SMURF_APP /");

        ShopWebViewActivity.JavaScriptInterface javascriptInterface = new ShopWebViewActivity.JavaScriptInterface(this);
        webView.addJavascriptInterface(javascriptInterface, "JSInterface");
        webView.loadUrl(h5Url);
    }


    /**
     * 判断 用户是否安装微信客户端
     */
    public static boolean isWeixinAvilible(Context context) {
        final PackageManager packageManager = context.getPackageManager();// 获取packagemanager
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);// 获取所有已安装程序的包信息
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName;
                if (pn.equals("com.tencent.mm")) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == 25||event.getKeyCode()==24) {
            return false;
        }
        if (webView != null) {
            webView.loadUrl("javascript:back()");
        }
        return true;
    }

    private void exitShopPage() {
        Intent i = new Intent();
        setResult(4, i);
        finish();
    }



    class JavaScriptInterface {
        private Context mContext;

        public JavaScriptInterface(Context context) {
            this.mContext = context;
        }

        /**
         * 分享功能
         * type 0:朋友 1；朋友圈
         * shareType 0：分享文本内容，1：分享单张图片，2：网页分享
         * 分享文本内容：分享内容  text
         * 分享图片 imageUrl
         * 分享网页 ：title 标题，description 描述信息 webpageUrl 网页链接
         */
        @JavascriptInterface
        public void share(int type, int shareType, String title, String text, String imgUri, String description, String webpageUrl) {
            if (isWeixinAvilible(mContext)) {

            } else {
                Toast.makeText(mContext, "您还没有安装微信，请先安装微信客户端", Toast.LENGTH_SHORT).show();
            }
            switch (shareType) {
                case 0:
                    ShareUtil.getInstance(ShopWebViewActivity.this).shareText(type, text);
                    break;
                case 1:
                    ShareUtil.getInstance(ShopWebViewActivity.this).shareImage(type, imgUri);
                    break;
                case 2:
                    ShareUtil.getInstance(ShopWebViewActivity.this).shareWebPage(type, webpageUrl, title, description);
                    break;
                default:
                    break;
            }
        }

        @JavascriptInterface
        public void exitApp() {
            exitShopPage();
        }

    }


}
