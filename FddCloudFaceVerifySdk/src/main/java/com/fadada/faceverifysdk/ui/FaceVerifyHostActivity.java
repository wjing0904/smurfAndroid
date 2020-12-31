package com.fadada.faceverifysdk.ui;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.fadada.faceverifysdk.R;
import com.fadada.faceverifysdk.bean.Sign;
import com.fadada.faceverifysdk.constant.FddCloudFaceConstant;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import static com.smurf.app.base.StaticURL.DEBUG_BASE;
import static com.smurf.app.base.StaticURL.DEBUG_BASE_QY;
import static com.smurf.app.base.StaticURL.RELEASE_BASE;
import static com.smurf.app.base.StaticURL.RELEASE_BASE_QY;

import com.smurf.app.base.BuildConfig;
import com.smurf.app.base.event.*;

public class FaceVerifyHostActivity extends AppCompatActivity {

    final static int REQUEST_PERMISSION = 1;

    private WebView webView;

    private TextView back;
    //拍照图片路径
    private String cameraFielPath;
    //5.0以下使用
    private ValueCallback<Uri> uploadMessage;
    // 5.0及以上使用
    private ValueCallback<Uri[]> uploadMessageAboveL;
    //图片
    private final static int FILE_CHOOSER_RESULT_CODE = 128;
    //拍照
    private final static int FILE_CAMERA_RESULT_CODE = 129;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fddcloudface);

        webView = (WebView) findViewById(R.id.wv_host);


        Bundle bundle = getIntent().getExtras();
        String h5Url = bundle.getString(FddCloudFaceConstant.VERIFY_URL);
        back = findViewById(R.id.web_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                webBack();
            }
        });

        //开启JS调用逻辑
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setJavaScriptEnabled(true);

        //不通过外部浏览器打开
        webView.setWebChromeClient(new WebChromeClient() {
            // For Android < 3.0
            public void openFileChooser(ValueCallback<Uri> valueCallback) {
                uploadMessage = valueCallback;
                openImageChooserActivity();
            }

            // For Android  >= 3.0
            public void openFileChooser(ValueCallback valueCallback, String acceptType) {
                uploadMessage = valueCallback;
                openImageChooserActivity();
            }

            //For Android  >= 4.1
            public void openFileChooser(ValueCallback<Uri> valueCallback, String acceptType, String capture) {
                uploadMessage = valueCallback;
                openImageChooserActivity();
            }

            // For Android >= 5.0
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
                uploadMessageAboveL = filePathCallback;
                openImageChooserActivity();
                return true;
            }
        });
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                String baseUrlQy = null;
                String baseUrl = null;
                if (BuildConfig.DEBUG) {
                    baseUrl = DEBUG_BASE;
                    baseUrlQy = DEBUG_BASE_QY;
                } else {
                    baseUrl = RELEASE_BASE;
                    baseUrlQy = RELEASE_BASE_QY;
                }
                if (url.startsWith(baseUrl) && !url.startsWith(baseUrlQy)) {
                    webBack();
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

        webView.addJavascriptInterface(new FaceVerifyInterface(this, webView), "scanface");

        //区分app和H5调用刷脸的标记
        String ua = webView.getSettings().getUserAgentString();
        webView.getSettings().setUserAgentString(ua + "fdd_authentication_android_v2");
        //区分app和H5调用刷脸的标记
        String ua1 = webView.getSettings().getUserAgentString();
        webView.getSettings().setUserAgentString(ua1 + "fdd_authentication");

        webView.loadUrl(h5Url);
    }

    private void openImageChooserActivity() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, REQUEST_PERMISSION);
        } else {
            new MaterialDialog.Builder(this)
                    .items(R.array.photo)
                    .positiveText("取消")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            if (uploadMessageAboveL != null) {
                                uploadMessageAboveL.onReceiveValue(null);
                                uploadMessageAboveL = null;
                            }
                            if (uploadMessage != null) {
                                uploadMessage.onReceiveValue(null);
                                uploadMessage = null;
                            }
                            dialog.dismiss();
                        }
                    })
                    .cancelable(false)
                    .canceledOnTouchOutside(false)
                    .itemsCallback(new MaterialDialog.ListCallback() {
                        @Override
                        public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                            if (position == 0) {
                                takeCamera();
                            } else if (position == 1) {
                                takePhoto();
                            }
                        }
                    }).show();
        }
    }

    //选择图片
    private void takePhoto() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        startActivityForResult(Intent.createChooser(i, "Image Chooser"), FILE_CHOOSER_RESULT_CODE);
    }

    //拍照
    private void takeCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        String fileName = System.currentTimeMillis() + "upload.jpg";
        cameraFielPath = getExternalFilesDir(Environment.DIRECTORY_PICTURES) + fileName;
        File outputImage = new File(cameraFielPath);
        Uri photoURI = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".fileProvider", outputImage);

//        Uri photoURI = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", outputImage);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
        startActivityForResult(intent, FILE_CAMERA_RESULT_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openImageChooserActivity();
                } else {
                    uploadMessageAboveL.onReceiveValue(null);
                    Toast.makeText(this, "获取权限失败", Toast.LENGTH_LONG).show();
                }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (null == uploadMessage && null == uploadMessageAboveL) return;
        if (resultCode != RESULT_OK) {//同上所说需要回调onReceiveValue方法防止下次无法响应js方法
            if (uploadMessageAboveL != null) {
                uploadMessageAboveL.onReceiveValue(null);
                uploadMessageAboveL = null;
            }
            if (uploadMessage != null) {
                uploadMessage.onReceiveValue(null);
                uploadMessage = null;
            }
            return;
        }
        Uri result = null;
        if (requestCode == FILE_CAMERA_RESULT_CODE) {
            if (null != data && null != data.getData()) {
                result = data.getData();
            }
            if (result == null) {
                File outputImage = new File(cameraFielPath);
                result = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".fileProvider", outputImage);
//                result = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", outputImage);
            }
            if (uploadMessageAboveL != null) {
                uploadMessageAboveL.onReceiveValue(new Uri[]{result});
                uploadMessageAboveL = null;
            } else if (uploadMessage != null) {
                uploadMessage.onReceiveValue(result);
                uploadMessage = null;
            }
        } else if (requestCode == FILE_CHOOSER_RESULT_CODE) {
            if (data != null) {
                result = data.getData();
            }
            if (uploadMessageAboveL != null) {
                onActivityResultAboveL(data);
            } else if (uploadMessage != null) {
                uploadMessage.onReceiveValue(result);
                uploadMessage = null;
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void onActivityResultAboveL(Intent intent) {
        Uri[] results = null;
        if (intent != null) {
            String dataString = intent.getDataString();
            ClipData clipData = intent.getClipData();
            if (clipData != null) {
                results = new Uri[clipData.getItemCount()];
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    ClipData.Item item = clipData.getItemAt(i);
                    results[i] = item.getUri();
                }
            }
            if (dataString != null) {
                results = new Uri[]{Uri.parse(dataString)};
            }
        }
        uploadMessageAboveL.onReceiveValue(results);
        uploadMessageAboveL = null;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        webBack();
    }

    private void webBack(){

        Intent i = new Intent();
        setResult(3, i);
        finish();

//        String baseUrlQy = null;
//        String baseUrl = null;
//        if (BuildConfig.DEBUG) {
//            baseUrl = DEBUG_BASE;
//            baseUrlQy = DEBUG_BASE_QY;
//        } else {
//            baseUrl = RELEASE_BASE;
//            baseUrlQy = RELEASE_BASE_QY;
//        }
//        if (h5Url.startsWith(baseUrl) && !h5Url.startsWith(baseUrlQy)) {
//
//        }else{
//            finish();
//        }
    }
}
