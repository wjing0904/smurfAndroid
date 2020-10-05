package com.smurf.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.lcw.library.imagepicker.ImagePicker;
import com.smurf.app.jsp.JavaScriptInterface;
import com.smurf.app.jsp.JavaScriptInterfaceImpl;
import com.smurf.app.presenter.JavaScriptPresenter;
import com.smurf.app.view.IWebViewInterface;
import com.smurf.app.webView.X5WebView;
import com.tencent.smtt.export.external.interfaces.ConsoleMessage;
import com.tencent.smtt.sdk.WebChromeClient;

import java.util.List;

import static com.smurf.app.StaticNum.REQUEST_CAMERA_CODE;
import static com.smurf.app.StaticNum.REQUEST_SELECT_IMAGES_CODE;

public class WebViewActivity extends Activity implements IWebViewInterface {
    private static final String TAG = "WebViewActivity";

    private static final String DECODED_CONTENT_KEY = "codedContent";

    private static final String DEBUG_APP_URL = "http://39.107.84.57:8091/#/home";
    private static final String RELEASE_APP_URL = "";
    private X5WebView mWebView;
    private JavaScriptPresenter javaScriptPresenter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_webview);

        mWebView = (X5WebView) findViewById(R.id.webview);
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.i("consoleMessage", consoleMessage.message());
                return super.onConsoleMessage(consoleMessage);
            }
        });
        mWebView.getSettings();
        if(javaScriptPresenter == null){
            javaScriptPresenter = new JavaScriptPresenter(this,this);
        }
        JavaScriptInterface javascriptInterface = new JavaScriptInterfaceImpl(this,javaScriptPresenter);
        mWebView.addJavascriptInterface(javascriptInterface, "JSInterface");
        if (BuildConfig.DEBUG) {
            mWebView.loadUrl(DEBUG_APP_URL);
        } else {
            mWebView.loadUrl(RELEASE_APP_URL);
        }
    }

    /**
     * 动态权限申请
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            switch (requestCode){
                case REQUEST_CAMERA_CODE:
                    javaScriptPresenter.openZxing();
                    break;
                case REQUEST_SELECT_IMAGES_CODE:
                    javaScriptPresenter.openImageSelected(javaScriptPresenter.getPicSelectedNum());
                    break;
                default:
                    break;
            }
        }else{
            Toast.makeText(this, "你拒绝了权限申请，可能无法打开相机哦！", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 扫描二维码/条码回传
        if (requestCode == REQUEST_CAMERA_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                String content = data.getStringExtra(DECODED_CONTENT_KEY);
                if(javaScriptPresenter!= null)
                    javaScriptPresenter.notifyScanVue(content);
            }
        }
        //多图选择
        if (requestCode == REQUEST_SELECT_IMAGES_CODE && resultCode == RESULT_OK) {
            List<String> imagePaths = data.getStringArrayListExtra(ImagePicker.EXTRA_SELECT_IMAGES);
            if(javaScriptPresenter!= null)
                javaScriptPresenter.notifyCamer(imagePaths);
        }
    }

    @Override
    public void notifyZxingValueToJs(String value) {
        mWebView.loadUrl("javascript:getInviteInfo('"+value+"')");
    }

    @Override
    public void notifyImageSelectedValueToJs(String value) {
        mWebView.loadUrl("javascript:androidUploadImg('"+value+"')");
    }
    //TODO 需要跟H5 确定接收city的方法
    @Override
    public void notifyLocation(String value) {
        mWebView.loadUrl("javascript:localCity('"+value+"')");
    }
}
