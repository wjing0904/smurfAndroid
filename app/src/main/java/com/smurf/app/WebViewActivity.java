package com.smurf.app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.lcw.library.imagepicker.ImagePicker;
import com.smurf.app.presenter.JavaScriptPresenter;
import com.smurf.app.share.ShareWeChatListener;
import com.smurf.app.share.Shareboard;
import com.smurf.app.utils.ShareUtil;
import com.smurf.app.view.IWebViewInterface;
import com.smurf.app.webView.X5WebView;
import com.tencent.smtt.export.external.interfaces.ConsoleMessage;
import com.tencent.smtt.sdk.WebChromeClient;

import java.util.ArrayList;
import java.util.List;

import static com.smurf.app.StaticNum.REQUEST_CAMERA_CODE;
import static com.smurf.app.StaticNum.REQUEST_LOCAL_CODE;
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
//        ShareUtil.getInstance(WebViewActivity.this).shareText(0,"text");

        String url = "https://avatar.csdn.net/2/C/8/1_small_and_smallworld.jpg";
        ShareUtil.getInstance(WebViewActivity.this).shareImage(0,url);

//        ShareUtil.getInstance(WebViewActivity.this).shareWebPage(0,"https://www.baidu.com", "title","description");


        mWebView = (X5WebView) findViewById(R.id.webview);
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.i("consoleMessage", consoleMessage.message());
                return super.onConsoleMessage(consoleMessage);
            }
        });
        mWebView.getSettings();
        if (javaScriptPresenter == null) {
            javaScriptPresenter = new JavaScriptPresenter(this, this);
        }
        JavaScriptInterface javascriptInterface = new JavaScriptInterface(this);
        if (BuildConfig.DEBUG) {
            mWebView.loadUrl(DEBUG_APP_URL);
        } else {
            mWebView.loadUrl(RELEASE_APP_URL);
        }
        mWebView.addJavascriptInterface(javascriptInterface, "JSInterface");

        String[] permissions = new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.INTERNET};
        List<String>  permissionList = new ArrayList<>();
        for(int i=0;i<permissions.length;i++){
            if(ActivityCompat.checkSelfPermission(this, permissions[i]) != PackageManager.PERMISSION_GRANTED){
                permissionList.add(permissions[i]);
            }
        }
        if(permissionList.size() <=0){
            if(javaScriptPresenter!= null){
                javaScriptPresenter.getLocal();
            }
        }else{
            ActivityCompat.requestPermissions(((Activity) this), permissions, REQUEST_LOCAL_CODE);
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
        boolean haspermission = false;

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case REQUEST_CAMERA_CODE:
                    javaScriptPresenter.openZxing();
                    break;
                case REQUEST_SELECT_IMAGES_CODE:
                    javaScriptPresenter.openImageSelected(javaScriptPresenter.getPicSelectedNum());
                    break;
                default:
                    break;
            }
        } else {
            Toast.makeText(this, "你拒绝了权限申请，可能无法打开相机哦！", Toast.LENGTH_SHORT).show();
        }

        if(requestCode == REQUEST_LOCAL_CODE){
            for(int i=0;i<grantResults.length;i++){
                if(grantResults[i] == -1){
                    haspermission = true;
                }
            }
            if(haspermission){
                Toast.makeText(this, "你拒绝了权限申请，无法进行定位哦！", Toast.LENGTH_SHORT).show();

            }else{
                if(javaScriptPresenter != null)
                    javaScriptPresenter.getLocal();
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 扫描二维码/条码回传
        if (requestCode == REQUEST_CAMERA_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                String content = data.getStringExtra(DECODED_CONTENT_KEY);
                if (javaScriptPresenter != null)
                    javaScriptPresenter.notifyScanVue(content);
            }
        }
        //多图选择
        if (requestCode == REQUEST_SELECT_IMAGES_CODE && resultCode == RESULT_OK) {
            List<String> imagePaths = data.getStringArrayListExtra(ImagePicker.EXTRA_SELECT_IMAGES);
            if (javaScriptPresenter != null)
                javaScriptPresenter.notifyCamer(imagePaths);
        }
    }

    @Override
    public void notifyZxingValueToJs(String value) {
        mWebView.loadUrl("javascript:getInviteInfo('" + value + "')");
    }

    @Override
    public void notifyImageSelectedValueToJs(String value) {
        mWebView.loadUrl("javascript:androidUploadImg('" + value + "')");
    }

    @Override
    public void notifyLocation(String value) {
        Log.d("test",value);
        mWebView.loadUrl("javascript:localCity('" + value + "')");
    }


    class JavaScriptInterface {
        private Context mContext;

        public JavaScriptInterface(Context context) {
            this.mContext = context;
        }

        /**
         *  js 调用原生二维码
         */
        @JavascriptInterface
        public void goSCan() {
            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(((Activity) mContext), new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_CODE);
            } else {
                if (javaScriptPresenter != null)
                    javaScriptPresenter.openZxing();
            }
        }

        /**
         * js调用原生图片选择
         * @param picNum
         */
        @JavascriptInterface
        public void imageSelected(int picNum) {
            if (javaScriptPresenter != null)
                javaScriptPresenter.openImageSelected(picNum);
        }

        /**
         * js调用原生 激励视频
         */
        @JavascriptInterface
        public void openRewarderVideo() {
            Intent intent = new Intent(mContext, RewadeVideoActivity.class);
            mContext.startActivity(intent);
        }

        /**
         * js调用原生 获取当前定位
         */
        @JavascriptInterface
        public void getLocation() {
            String[] permissions = new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.INTERNET};
            List<String>  permissionList = new ArrayList<>();
            for(int i=0;i<permissions.length;i++){
                if(ActivityCompat.checkSelfPermission(mContext, permissions[i]) != PackageManager.PERMISSION_GRANTED){
                    permissionList.add(permissions[i]);
                }
            }
            if(permissionList.size() <=0){
                if(javaScriptPresenter!= null){
                    javaScriptPresenter.getLocal();
                }
            }else{
                ActivityCompat.requestPermissions(((Activity) mContext), permissions, REQUEST_LOCAL_CODE);
            }
        }

        /**
         * 图片保存
         */
        @JavascriptInterface
        public void openAndSaveImg(String url) {
            Intent intent = new Intent(mContext, ImageActivity.class);
            intent.putExtra("img_url", url);
            mContext.startActivity(intent);
        }

        /**
         * 分享功能
         * shareType 0：分享文本内容，1：分享单张图片，2：网页分享
         * 分享文本内容：分享内容  text
         * 分享图片 imageUrl
         * 分享网页 ：title 标题，description 描述信息 webpageUrl 网页链接
         */
        @JavascriptInterface
        public void share(int shareType, String title, String text, String imgUri, String description,String webpageUrl) {
            if(isWeixinAvilible(mContext)) {

            }else {
                Toast.makeText(mContext, "您还没有安装微信，请先安装微信客户端", Toast.LENGTH_SHORT).show();
            }

            Shareboard shareboard = new Shareboard(mContext);
            shareboard.setShareWeChatListener(new ShareWeChatListener() {
                @Override
                public void shareWeChat(int type) {
                    switch (shareType) {
                        case 0:
                            ShareUtil.getInstance(WebViewActivity.this).shareText(type,text);
                            break;
                        case 1:
                            ShareUtil.getInstance(WebViewActivity.this).shareImage(type,imgUri);
                            break;
                        case 2:
                            ShareUtil.getInstance(WebViewActivity.this).shareWebPage(type,webpageUrl, title,description);
                            break;
                        default:
                            break;
                    }
                }
            });
            shareboard.show();

        }

        /**
         * js调用原生 已签约 && 认证 && 打开第三方链接 && 打开h5
         */
        @JavascriptInterface
        public void signUp(String signUrl) {
            Intent intent = new Intent(mContext, SignUpActivity.class);
            intent.putExtra("sign_url", signUrl);
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
}
