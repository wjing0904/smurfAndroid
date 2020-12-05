package com.smurf.app;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;
import com.smurf.app.base.StaticURL;
import com.smurf.app.login.activity.MainActivity;
import com.smurf.app.signup.SignUpFaceVerify;
import com.smurf.app.utils.DispUtil;
import com.smurf.app.utils.ThreadUtils;
import com.smurf.app.wxapi.WXLogin;
import com.smurf.app.login.utils.BitmapUtils;
import com.smurf.app.presenter.InstallAppPresenter;
import com.smurf.app.presenter.JavaScriptPresenter;
import com.smurf.app.utils.SaveImageUtils;
import com.smurf.app.utils.ShareUtil;
import com.smurf.app.view.IWebViewInterface;
import com.smurf.app.webView.X5WebView;
import com.tencent.smtt.export.external.interfaces.ConsoleMessage;
import com.tencent.smtt.sdk.WebChromeClient;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.smurf.app.base.event.*;

import static com.smurf.app.base.StaticNum.REQUEST_CAMERA_CODE;
import static com.smurf.app.base.StaticNum.REQUEST_LOCAL_CODE;
import static com.smurf.app.base.StaticNum.REQUEST_SELECT_IMAGES_CODE;

public class WebViewActivity extends Activity implements IWebViewInterface {
    private static final String TAG = "WebViewActivity";
    private static final int REQUEST_CODE = 0x0000;

    private static final String DECODED_CONTENT_KEY = "codedContent";
    private String webUrl = null;


    private X5WebView mWebView;
    private JavaScriptPresenter javaScriptPresenter;
    private long exitTime = 0;


    private ImageView logoImg;
    private InstallAppPresenter installAppPresenter;
    //先定义
    private static final int REQUEST_EXTERNAL_STORAGE = 1;

    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"};

    private FrameLayout fmLayout;

    private boolean isOpenZxing;
    private boolean isOpenSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_webview);

        initView();
//        DispUtil.disabledDisplayDpiChange(this.getResources());
        webUrl = getIntent().getStringExtra("web_url");
        Log.d("liuluchao","onCreate" + webUrl);
        if (TextUtils.isEmpty(webUrl)) {
            if (BuildConfig.DEBUG) {
                webUrl = StaticURL.DEBUG_APP_URL;
            } else {
                webUrl = StaticURL.RELEASE_APP_URL;
            }
        }
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
        mWebView.loadUrl(webUrl);
        mWebView.addJavascriptInterface(javascriptInterface, "JSInterface");

        String[] permissions = new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.INTERNET};
        List<String> permissionList = new ArrayList<>();
        for (int i = 0; i < permissions.length; i++) {
            if (ActivityCompat.checkSelfPermission(this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(permissions[i]);
            }
        }
        if (permissionList.size() <= 0) {
            if (javaScriptPresenter != null) {
                javaScriptPresenter.getLocal();
            }
        } else {
            ActivityCompat.requestPermissions(((Activity) this), permissions, REQUEST_LOCAL_CODE);
        }
        EventBus.getDefault().register(this);

    }

    private void initView() {
        logoImg = findViewById(R.id.logo_img);
        Glide.with(this).load(R.mipmap.logo_start).into(logoImg);
        fmLayout = findViewById(R.id.delay_layout);
        verifyStoragePermissions(this);

        //检查APP是否需要更新
        if (installAppPresenter == null) {
            installAppPresenter = new InstallAppPresenter(this);
        }
        installAppPresenter.checkAppInstall();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        webUrl = intent.getStringExtra("web_url");
        Log.d("liuluchao","onNewIntent" + webUrl);
        if (!TextUtils.isEmpty(webUrl)) {
            mWebView.loadUrl(webUrl);
        }
    }

    /**
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRecordTimeEvent(TokenEvent event) {
        if (mWebView != null) {
            mWebView.loadUrl("javascript:toOnePhoneLogin('" + event.getCode() + "')");
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRecordTimeEvent(WxEvent event){
        if (mWebView != null) {
            mWebView.loadUrl("javascript:toWxLogin('" + event.token + "')");
        }
    }



    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRecordTimeEvent(VideoEvent event) {
        if (mWebView != null) {
            if (event.isVideoEnd) {
                mWebView.loadUrl("javascript:videoEnd()");
            } else {
                mWebView.loadUrl("javascript:videoStart()");
            }
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void webViewPageFinished(WebViewEvent webViewEvent) {
        if (fmLayout != null) {
            fmLayout.setVisibility(View.GONE);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void signBack(SignEvent signEvent){
        if (mWebView != null)
            mWebView.loadUrl("javascript:closeSign()");
    }

    /**
     * 动态权限申请
     *
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
                    if (isOpenZxing)
                        javaScriptPresenter.openZxing();
                    break;
                case REQUEST_SELECT_IMAGES_CODE:
                    if (isOpenSelected)
                        javaScriptPresenter.openImageSelected(javaScriptPresenter.getPicSelectedNum());
                    break;
                default:
                    break;
            }
        } else {
            Toast.makeText(this, "你拒绝了权限申请，可能无法打开相机哦！", Toast.LENGTH_SHORT).show();
        }

        if (requestCode == REQUEST_LOCAL_CODE) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == -1) {
                    haspermission = true;
                }
            }
            if (haspermission) {
                Toast.makeText(this, "你拒绝了权限申请，无法进行定位哦！", Toast.LENGTH_SHORT).show();

            } else {
                if (javaScriptPresenter != null)
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
            List<Image> images = ImagePicker.getImages(data);
            if (javaScriptPresenter != null)
                javaScriptPresenter.notifyCamer(getImgInputStream(images), images.get(0).getName());

        }

        if (resultCode == 3) {
            if (mWebView != null)
                mWebView.loadUrl("javascript:closeSign()");
        }
    }

    private String getImgInputStream(List<Image> images) {
        StringBuffer imgInputs = new StringBuffer();
//        BitmapUtils.comPressImg(images.get(0).getPath());
        imgInputs.append(BitmapUtils.fileToBase64(images.get(0).getPath())).append("|");
        return imgInputs.toString().substring(0, imgInputs.toString().length() - 1);
    }


    @Override
    public void notifyZxingValueToJs(String value) {
        if (mWebView != null)
            mWebView.loadUrl("javascript:getInviteInfo('" + value + "')");
    }

    @Override
    public void notifyImageSelectedValueToJs(String value) {
        if (mWebView != null)
            mWebView.loadUrl("javascript:androidUploadImg('" + value + "')");
    }

    @Override
    public void notifyImageName(String name) {
        if (mWebView != null)
            mWebView.loadUrl("javascript:imageName('" + name + "')");
    }

    @Override
    public void notifyLocation(String value) {
        if (mWebView != null)
            mWebView.loadUrl("javascript:localCity('" + value + "')");
    }

    //然后通过一个函数来申请
    private void verifyStoragePermissions(Activity activity) {
        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    class JavaScriptInterface {
        private Context mContext;

        public JavaScriptInterface(Context context) {
            this.mContext = context;
        }

        /**
         * js 调用原生二维码
         */
        @JavascriptInterface
        public void goSCan() {
            isOpenZxing = true;
            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(((Activity) mContext), new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_CODE);
            } else {
                if (javaScriptPresenter != null)
                    javaScriptPresenter.openZxing();
            }
        }

        /**
         * js调用原生图片选择
         *
         * @param picNum
         */
        @JavascriptInterface
        public void imageSelected(int picNum) {
            isOpenSelected = true;
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
            List<String> permissionList = new ArrayList<>();
            for (int i = 0; i < permissions.length; i++) {
                if (ActivityCompat.checkSelfPermission(mContext, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                    permissionList.add(permissions[i]);
                }
            }
            if (permissionList.size() <= 0) {
                if (javaScriptPresenter != null) {
                    javaScriptPresenter.getLocal();
                }
            } else {
                ActivityCompat.requestPermissions(((Activity) mContext), permissions, REQUEST_LOCAL_CODE);
            }
        }

        /**
         * 图片保存
         */
        @JavascriptInterface
        public void openAndSaveImg(int type,String strArr[],int index) {
            if (type == 0) {
                Intent intent = new Intent(mContext, ImageActivity.class);
                intent.putExtra("img_url", strArr);
                intent.putExtra("postion",index);
                mContext.startActivity(intent);
            }
            if (type == 1) {
                Glide.with(WebViewActivity.this)
                        .asBitmap()
                        .load(strArr[index])
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                                SaveImageUtils.saveImageToGallery(WebViewActivity.this, resource);
                                SaveImageUtils.saveImageToGallerys(WebViewActivity.this, resource);
                            }
                        });
            }
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
                    ShareUtil.getInstance(WebViewActivity.this).shareText(type, text);
                    break;
                case 1:
                    ShareUtil.getInstance(WebViewActivity.this).shareImage(type, imgUri);
                    break;
                case 2:
                    ShareUtil.getInstance(WebViewActivity.this).shareWebPage(type, webpageUrl, title, description);
                    break;
                default:
                    break;
            }
        }

        /**
         * js调用原生 已签约 && 认证 && 打开第三方链接 && 打开h5
         */
        @JavascriptInterface
        public void signUp(String signUrl) {
            ThreadUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (signUrl.startsWith("weixin://") || signUrl.startsWith("alipays://")) {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(signUrl));
                        startActivity(intent);
                        return;
                    }

                    if (signUrl.contains("https://wx.tenpay.com")) {
                        Map<String, String> extraHeaders = new HashMap<>();
                        extraHeaders.put("Referer", "http://smurf.langongbao.com");
                        mWebView.loadUrl(signUrl, extraHeaders);
                        return;
                    }

                    SignUpFaceVerify.getInstance().openFaceVerifySdk(mContext,signUrl);
                }
            });
        }

        @JavascriptInterface
        public void openLoginPage() {
            Intent intent = new Intent(mContext, MainActivity.class);
            startActivity(intent);
        }

        @JavascriptInterface
        public void wxLogin() {
            WXLogin wxLogin = new WXLogin(mContext);
            wxLogin.login();
        }

        @JavascriptInterface
        public void exitApp() {
            ExitApp();
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mWebView != null) {
            mWebView.loadUrl("javascript:back()");
        }
        return true;
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        isOpenZxing = false;
        isOpenSelected = false;
    }
    @Override
    public Resources getResources() {//禁止app字体大小跟随系统字体大小调节
        Resources resources = super.getResources();
        if (resources != null && resources.getConfiguration().fontScale != 1.0f) {
            android.content.res.Configuration configuration = resources.getConfiguration();
            configuration.fontScale = 1.0f;
            resources.updateConfiguration(configuration, resources.getDisplayMetrics());
        }
        return resources;
    }
}
