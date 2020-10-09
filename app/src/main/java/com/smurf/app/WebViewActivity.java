package com.smurf.app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.lcw.library.imagepicker.ImagePicker;
import com.smurf.app.presenter.JavaScriptPresenter;
import com.smurf.app.utils.ShareUtil;
import com.smurf.app.view.IWebViewInterface;
import com.smurf.app.webView.X5WebView;
import com.tencent.smtt.export.external.interfaces.ConsoleMessage;
import com.tencent.smtt.sdk.WebChromeClient;

import java.util.ArrayList;
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
    private long exitTime = 0;


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
        JavaScriptInterface javascriptInterface = new JavaScriptInterface(this);
        if (BuildConfig.DEBUG) {
            mWebView.loadUrl(DEBUG_APP_URL);
        } else {
            mWebView.loadUrl(RELEASE_APP_URL);
        }
        mWebView.addJavascriptInterface(javascriptInterface, "JSInterface");
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


   class JavaScriptInterface {
        private Context mContext;
        public JavaScriptInterface(Context context){
            this.mContext = context;
        }

        /**
         *  js 调用原生二维码
         */
        @JavascriptInterface
        public void goSCan(){
            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(((Activity)mContext), new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_CODE);
            } else {
                if(javaScriptPresenter!= null)
                    javaScriptPresenter.openZxing();
            }
        }

        /**
         * js调用原生图片选择
         * @param picNum
         */
        @JavascriptInterface
        public void imageSelected(int picNum){
            if(javaScriptPresenter != null)
                javaScriptPresenter.openImageSelected(picNum);
        }

        /**
         * js调用原生 激励视频
         */
        @JavascriptInterface
        public void openRewarderVideo(){
            Intent intent = new Intent(mContext, RewadeVideoActivity.class);
            mContext.startActivity(intent);
        }
        /**
         * js调用原生 获取当前定位
         */
        @JavascriptInterface
        public void getLocation(){
            LocationClient mLocationClient = new LocationClient( mContext);
            mLocationClient.registerLocationListener(new BDLocationListener() {
                @Override
                public void onReceiveLocation(BDLocation bdLocation) {
                    if(javaScriptPresenter!= null){
                        javaScriptPresenter.notifyLoaction(bdLocation.getCity());
                    }
                    //mTv.setText(arg0.getProvince() + arg0.getCity() + arg0.getStreet());
                }

                @Override
                public void onReceivePoi(BDLocation bdLocation) {

                }
            });
            LocationClientOption option = new LocationClientOption();
            option.setOpenGps(true); //打开gps
            option.setServiceName("com.baidu.location.service_v2.9");
            option.setPoiExtraInfo(true);
            option.setAddrType("all");
            option.setPriority(LocationClientOption.NetWorkFirst);
            option.setPriority(LocationClientOption.GpsFirst);       //gps
            option.setPoiNumber(10);
            option.disableCache(true);
            mLocationClient.setLocOption(option);
            mLocationClient.start();
        }

        /**
         * 图片保存
         */
        @JavascriptInterface
        public void openAndSaveImg(String url){
            Intent intent = new Intent(mContext, ImageActivity.class);
            intent.putExtra("img_url",url);
            mContext.startActivity(intent);
        }

        /**
         * 分享功能
         * shareType 0：分享文本内容，1：分享单张图片，2：分享多张图片
         */
        @JavascriptInterface
        public void share(int shareType, String title, String text, Uri uri, ArrayList<Uri> imageUris){
            switch (shareType){
                case 0:
                    ShareUtil.shareText(mContext,text,title);
                    break;
                case 1:
                    ShareUtil.shareImage(mContext,uri,title);
                    break;
                case 2:
                    ShareUtil.sendMoreImage(mContext,imageUris,title);
                    break;
                default:
                    break;
            }
        }

        /**
         * js调用原生 已签约 && 认证 && 打开第三方链接 && 打开h5
         */
        public void signUp(String signUrl){
            Intent intent = new Intent(mContext, SignUpActivity.class);
            intent.putExtra("sign_url",signUrl);
            mContext.startActivity(intent);
        }

    }



    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        ExitApp();
        return false;
    }
    private void ExitApp() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Toast.makeText(WebViewActivity.this, "再按一次退出蓝晶灵应用", Toast.LENGTH_SHORT)
                    .show();
            exitTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }





}
