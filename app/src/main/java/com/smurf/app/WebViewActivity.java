package com.smurf.app;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;
import com.fadada.faceverifysdk.constant.FddCloudFaceConstant;
import com.fadada.faceverifysdk.ui.FaceVerifyHostActivity;
import com.smurf.app.base.StaticURL;
import com.smurf.app.base.event.SignEvent;
import com.smurf.app.base.event.TokenEvent;
import com.smurf.app.base.event.VideoEvent;
import com.smurf.app.base.event.WebViewEvent;
import com.smurf.app.base.event.WxEvent;
import com.smurf.app.login.activity.MainActivity;
import com.smurf.app.presenter.InstallAppPresenter;
import com.smurf.app.signup.SignUpFaceVerify;
import com.smurf.app.splash.ImgFragment;
import com.smurf.app.base.utils.ThreadUtils;
import com.smurf.app.utils.SharedPreferencesHelper;
import com.smurf.app.wxapi.WXLogin;
import com.smurf.app.base.utils.BitmapUtils;
import com.smurf.app.presenter.JavaScriptPresenter;
import com.smurf.app.utils.SaveImageUtils;
import com.smurf.app.base.utils.ShareUtil;
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

import static com.smurf.app.base.StaticNum.REQUEST_CAMERA_CODE;
import static com.smurf.app.base.StaticNum.REQUEST_CAMERA_PERMISSION;
import static com.smurf.app.base.StaticNum.REQUEST_DIALOG_CODE;
import static com.smurf.app.base.StaticNum.REQUEST_EXTERNAL_STORAGE;
import static com.smurf.app.base.StaticNum.REQUEST_LOCAL_CODE;
import static com.smurf.app.base.StaticNum.REQUEST_SELECT_IMAGES_CODE;
import static com.smurf.app.base.StaticNum.REQUEST_SELECT_IMAGES_PERMISSION;

public class WebViewActivity extends AppCompatActivity implements IWebViewInterface {
    private static final String TAG = "WebViewActivity";
    private static final int REQUEST_CODE = 0x0000;

    private static final String DECODED_CONTENT_KEY = "codedContent";
    private String webUrl = null;


    private X5WebView mWebView;
    private JavaScriptPresenter javaScriptPresenter;
    private long exitTime = 0;


    private ImageView logoImg;
    private InstallAppPresenter installAppPresenter;

    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"};

    private FrameLayout fmLayout;

    private boolean isOpenZxing;
    private boolean isOpenSelected;


    /******************************/
    private ConstraintLayout layoutSplash;
    private ViewPager viewPager;
    private ImageView img1_normal,img1_select,img2_normal,img2_select,img3_normal,img3_select;

    private List<Fragment> fragments;
    private Context mContext;

    private SharedPreferencesHelper sharedPreferencesHelper;
    private OnDialogApplyPermissionListener mOnDialogPremission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.layout_webview);
        layoutSplash = findViewById(R.id.layout_splash);
        sharedPreferencesHelper = new SharedPreferencesHelper(
                this, "smurf");
        //啓動效果
        initFragment();
        initSplashView();

        initView();
//        DispUtil.disabledDisplayDpiChange(this.getResources());
        webUrl = getIntent().getStringExtra("web_url");
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

        EventBus.getDefault().register(this);

    }

    private void initView() {
        //logoImg = findViewById(R.id.logo_img);
        //Glide.with(this).load(R.mipmap.logo_start).into(logoImg);
        //fmLayout = findViewById(R.id.delay_layout);
//        verifyStoragePermissions(this);

        //检查APP是否需要更新
        if (installAppPresenter == null) {
            installAppPresenter = new InstallAppPresenter(this, new InstallAppPresenter.PermissionInterface() {
                @Override
                public boolean allowPermission() {
                    ActivityCompat.requestPermissions((Activity) mContext, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
                    int permission = ActivityCompat.checkSelfPermission(mContext,
                            "android.permission.WRITE_EXTERNAL_STORAGE");
                    return permission == PackageManager.PERMISSION_GRANTED;

                }

                @Override
                public void showDialogPermission(String serviceName,OnDialogApplyPermissionListener on,int requestCode) {
                    showDialog(serviceName,on,requestCode);
                }
            });

        }
        installAppPresenter.checkAppInstall();
    }

    private void showDialog(String serviceName,OnDialogApplyPermissionListener onDialogPremission,int requestCode){
        /* @setIcon 设置对话框图标
         * @setTitle 设置对话框标题
         * @setMessage 设置对话框消息提示
         * setXXX方法返回Dialog对象，因此可以链式设置属性
         */
        this.mOnDialogPremission = onDialogPremission;
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(mContext);
        normalDialog.setIcon(R.drawable.logo);
        normalDialog.setTitle("蓝晶灵想要使用"+serviceName);
        normalDialog.setMessage("请在设置-蓝晶灵中开启"+serviceName);
        normalDialog.setPositiveButton("去设置",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri1 = Uri.fromParts("package", mContext.getPackageName(), null);
                        intent.setData(uri1);
                        ((Activity)mContext).startActivityForResult(intent,requestCode);
                    }
                });
        normalDialog.setNegativeButton("知道了",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if(mOnDialogPremission!= null){
                            mOnDialogPremission.isPremission(false);
                        }
                    }
                });
        // 显示
        normalDialog.show();
    }


        @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        webUrl = intent.getStringExtra("web_url");
        if (TextUtils.isEmpty(webUrl)) {
            if (BuildConfig.DEBUG) {
                webUrl = StaticURL.DEBUG_APP_URL;
            } else {
                webUrl = StaticURL.RELEASE_APP_URL;
            }
        }
        mWebView.loadUrl(webUrl);
    }


    /**
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRecordTimeEvent1(TokenEvent event) {
        Log.e(TAG, "onRecordTimeEvent: fdsfdsa" + event.getCode());
        if (mWebView != null) {
            mWebView.loadUrl("javascript:toOnePhoneLogin('" + event.getCode() + "')");
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRecordTimeEvent(WxEvent event) {
        if (mWebView != null) {
            Log.d("smurf","wxLogin token = " + event.token);
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
    public void signBack(SignEvent signEvent) {
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
        if(requestCode == REQUEST_CAMERA_PERMISSION){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sharedPreferencesHelper.put(SharedPreferencesHelper.CAMERA_PERMISSION, 1);
                if (isOpenZxing)
                    javaScriptPresenter.openZxing();
            }else{
                sharedPreferencesHelper.put(SharedPreferencesHelper.CAMERA_PERMISSION, 2);
            }
        }

        if(requestCode == REQUEST_SELECT_IMAGES_PERMISSION){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sharedPreferencesHelper.put(SharedPreferencesHelper.CAMERA_PERMISSION, 1);
                if (isOpenSelected)
                    javaScriptPresenter.openImageSelected(javaScriptPresenter.getPicSelectedNum());
            }else{
                sharedPreferencesHelper.put(SharedPreferencesHelper.CAMERA_PERMISSION, 2);
            }
        }

        if (requestCode == REQUEST_LOCAL_CODE) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == -1) {
                    haspermission = true;
                }
            }
            if (haspermission) {
//                Toast.makeText(this, "你拒绝了权限申请，无法进行定位哦！", Toast.LENGTH_SHORT).show();
                sharedPreferencesHelper.put(SharedPreferencesHelper.WRITE_EXTERNAL_STORAGE,2);

            } else {
                sharedPreferencesHelper.put(SharedPreferencesHelper.WRITE_EXTERNAL_STORAGE,1);
                if (javaScriptPresenter != null)
                    javaScriptPresenter.getLocal();
            }
        }

        if(requestCode == REQUEST_EXTERNAL_STORAGE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
               sharedPreferencesHelper.put(SharedPreferencesHelper.WRITE_EXTERNAL_STORAGE,1);

            }else{
                sharedPreferencesHelper.put(SharedPreferencesHelper.WRITE_EXTERNAL_STORAGE,2);
//                Toast.makeText(this, "你拒绝了权限申请，无法进行软件一键升级！", Toast.LENGTH_SHORT).show();

            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 扫描二维码/条码回传
        if(requestCode == REQUEST_CAMERA_PERMISSION){
//            ActivityCompat.requestPermissions(((Activity) mContext), new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            int permission = ActivityCompat.checkSelfPermission(mContext,Manifest.permission.CAMERA);
            if(permission != PackageManager.PERMISSION_GRANTED){
                if(mOnDialogPremission!= null){
                    mOnDialogPremission.isPremission(false);
                }
            }else{
                if(mOnDialogPremission!= null){
                    mOnDialogPremission.isPremission(true);
                }
            }
        }

        if (requestCode == REQUEST_CAMERA_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                String content = data.getStringExtra(DECODED_CONTENT_KEY);
                if (javaScriptPresenter != null)
                    javaScriptPresenter.notifyScanVue(content);
            }
        }



        if(requestCode == REQUEST_SELECT_IMAGES_PERMISSION){
//            ActivityCompat.requestPermissions(((Activity) mContext), new String[]{Manifest.permission.CAMERA}, REQUEST_SELECT_IMAGES_PERMISSION);
            int permission = ActivityCompat.checkSelfPermission(mContext,Manifest.permission.CAMERA);
            if(permission != PackageManager.PERMISSION_GRANTED){
                if(mOnDialogPremission!= null){
                    mOnDialogPremission.isPremission(false);
                }
            }else{
                if(mOnDialogPremission!= null){
                    mOnDialogPremission.isPremission(true);
                }
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

        if(resultCode == 4){
            if(mWebView!= null){
                mWebView.loadUrl("javascript:openLogin()");
            }
        }

        if(resultCode ==5){
            Bundle bundle = data.getExtras();
            String url = bundle.getString("openAppointPage");
            if(mWebView!= null){
                mWebView.loadUrl("javascript:openPage('" + url + "')");
            }
        }

        if(requestCode == REQUEST_LOCAL_CODE){
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
                if(mOnDialogPremission!= null){
                    mOnDialogPremission.isPremission(true);
                }
            }else{
                if(mOnDialogPremission!= null){
                    mOnDialogPremission.isPremission(false);
                }
            }
        }

        if(requestCode == REQUEST_DIALOG_CODE){
//            ActivityCompat.requestPermissions((Activity) mContext, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
            int permission = ActivityCompat.checkSelfPermission(mContext,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if(permission != PackageManager.PERMISSION_GRANTED){
                if(mOnDialogPremission!= null){
                    mOnDialogPremission.isPremission(false);
                }
            }else{
                if(mOnDialogPremission!= null){
                    mOnDialogPremission.isPremission(true);
                }
            }
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
                int isPermiss = (int) sharedPreferencesHelper.get(SharedPreferencesHelper.CAMERA_PERMISSION,0);
                if(isPermiss == 0){
                    ActivityCompat.requestPermissions(((Activity) mContext), new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                }else if(isPermiss == 2){
                    showDialog("照片，多媒体，存储权限", new OnDialogApplyPermissionListener() {
                        @Override
                        public void isPremission(boolean isAllow) {
                            if(isAllow){
                                if (javaScriptPresenter != null)
                                    javaScriptPresenter.openZxing();
                            }
                        }
                    },REQUEST_CAMERA_PERMISSION);
                }
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

            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                int isPermiss = (int) sharedPreferencesHelper.get(SharedPreferencesHelper.CAMERA_PERMISSION,0);
                if(isPermiss == 0){
                    ActivityCompat.requestPermissions(((Activity) mContext), new String[]{Manifest.permission.CAMERA}, REQUEST_SELECT_IMAGES_PERMISSION);
                }else if(isPermiss == 2){
                    showDialog("照片，多媒体，存储权限", new OnDialogApplyPermissionListener() {
                        @Override
                        public void isPremission(boolean isAllow) {
                            if(isAllow){
                                if (javaScriptPresenter != null)
                                    javaScriptPresenter.openImageSelected(picNum);
                            }
                        }
                    },REQUEST_SELECT_IMAGES_PERMISSION);
                }
            } else {
                if (javaScriptPresenter != null)
                    javaScriptPresenter.openImageSelected(picNum);
            }



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
                int isPermiss = (int) sharedPreferencesHelper.get(SharedPreferencesHelper.LOCAL_PERMISSION,0);
                if(isPermiss == 0){
                    ActivityCompat.requestPermissions(((Activity) mContext), permissions, REQUEST_LOCAL_CODE);
                }else if(isPermiss == 2){
                    showDialog("定位权限", new OnDialogApplyPermissionListener() {
                        @Override
                        public void isPremission(boolean isAllow) {
                            if(isAllow){
                                if (javaScriptPresenter != null) {
                                    javaScriptPresenter.getLocal();
                                }
                            }
                        }
                    },REQUEST_LOCAL_CODE);
                }
            }
        }

        /**
         * 图片保存
         */
        @JavascriptInterface
        public void openAndSaveImg(int type, String strArr[], int index) {
            if (type == 0) {
                Intent intent = new Intent(mContext, ImageActivity.class);
                intent.putExtra("img_url", strArr);
                intent.putExtra("postion", index);
                ((Activity) mContext).startActivityForResult(intent, 9);
            }
            if (type == 1) {
                Glide.with(WebViewActivity.this)
                        .asBitmap()
                        .load(strArr[index])
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
//                                SaveImageUtils.saveImageToGallery(WebViewActivity.this, resource);
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

                    SignUpFaceVerify.getInstance().openFaceVerifySdk(mContext, signUrl);
                }
            });
        }

        @JavascriptInterface
        public void shopSign(String url){
            Bundle bundle = new Bundle();
            bundle.putString(FddCloudFaceConstant.VERIFY_URL, url);
            Intent intent = new Intent(WebViewActivity.this, ShopWebViewActivity.class);
            intent.putExtras(bundle);
            startActivityForResult(intent,4);
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
        if (event.getKeyCode() == 25||event.getKeyCode()==24) {
            return false;
        }
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

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }






    /*****************************************/
    private void initFragment() {
        fragments = new ArrayList<>();
        fragments.add(ImgFragment.getInstance("",1));
        fragments.add(ImgFragment.getInstance("",2));
        fragments.add(ImgFragment.getInstance("",3));
    }

    private void initSplashView() {
        viewPager = findViewById(R.id.viewpager);
        img1_normal = findViewById(R.id.img_1_normal);
        img1_select = findViewById(R.id.img_1_select);
        img2_normal = findViewById(R.id.img_2_normal);
        img2_select = findViewById(R.id.img_2_select);
        img3_normal = findViewById(R.id.img_3_normal);
        img3_select = findViewById(R.id.img_3_select);

        viewPager.setAdapter(new MyAdapter(getSupportFragmentManager()));
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                resetDot();
                if(position == 0){
                    img1_normal.setVisibility(View.GONE);
                    img1_select.setVisibility(View.VISIBLE);
                }else if(position == 1){
                    img2_normal.setVisibility(View.GONE);
                    img2_select.setVisibility(View.VISIBLE);
                }else if(position == 2){
                    img3_normal.setVisibility(View.GONE);
                    img3_select.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        resetDot();
        img1_normal.setVisibility(View.GONE);
        img1_select.setVisibility(View.VISIBLE);

    }

    public void splashOver(){
        layoutSplash.setVisibility(View.GONE);
    }

    /**
     * 重置dot
     */
    private void resetDot(){
        img1_normal.setVisibility(View.VISIBLE);
        img1_select.setVisibility(View.GONE);
        img2_normal.setVisibility(View.VISIBLE);
        img2_select.setVisibility(View.GONE);
        img3_normal.setVisibility(View.VISIBLE);
        img3_select.setVisibility(View.GONE);
    }

    /**
     * viewpager adapter
     */
    class MyAdapter extends FragmentPagerAdapter {

        public MyAdapter(@NonNull FragmentManager fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }






}
