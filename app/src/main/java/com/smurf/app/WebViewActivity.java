package com.smurf.app;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.widget.Button;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.adhub.ads.RewardedVideoAd;
import com.adhub.ads.RewardedVideoAdListener;
import com.lcw.library.imagepicker.ImagePicker;
import com.smurf.app.webView.X5WebView;
import com.smurf.app.zxing.android.CaptureActivity;
import com.tencent.smtt.export.external.interfaces.ConsoleMessage;
import com.tencent.smtt.sdk.WebChromeClient;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class WebViewActivity extends Activity {
    private static final String TAG = "WebViewActivity";

    private X5WebView mWebView;
    private static final int REQUEST_CODE_SCAN = 0x0000;
    private static final int CAMERA_PERMISSION = 1;
    private static final int CAMERA_PERMISSION_SCAN = 2;

    private static final int REQUEST_CODE_TAKE = 3;

    private static final int REQUEST_SELECT_IMAGES_CODE = 4;

    private Button btn1, btn2, btn3,btn4;

    /**
     * H5中调用
     * @param savedInstanceState
     *
     * <html>
     * <head>
     * <script type="text/javascript">
     * function displaymessage()
     * {
     * JSInterface.changeActivity();
     * }
     * </script>
     * </head>
     *
     * <body>
     * <form>
     * <input type="button" value="Click me!" onclick="displaymessage()" />
     * </form>
     * </body>
     * </html>
     */


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
        JavaScriptInterface javascriptInterface = new JavaScriptInterface(this);
        mWebView.addJavascriptInterface(javascriptInterface,"JSInterface");
        mWebView.loadUrl(" http://39.107.84.57:8091/#/home");

        initView();
    }

    public class JavaScriptInterface{
        Context mContext;
        public JavaScriptInterface(Context context){
            mContext = context;
        }

        @JavascriptInterface
        public void goSCan(Context context){

        }

        @JavascriptInterface
        public void takePicture(){

        }

        @JavascriptInterface
        public void imageSelected(){

        }

        @JavascriptInterface
        public void openRewarderVideo(){

        }
    }

    private void initView() {
        btn1 = findViewById(R.id.btn1);
        btn2 = findViewById(R.id.btn2);
        btn3 = findViewById(R.id.btn3);
        btn4 = findViewById(R.id.btn4);

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //动态权限申请
                if (ContextCompat.checkSelfPermission(WebViewActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(WebViewActivity.this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_SCAN);
                } else {
                    goScan();
                }
            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(WebViewActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(WebViewActivity.this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION);
                } else {
                    takePicture(REQUEST_CODE_TAKE);
                }
            }
        });

        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WebViewActivity.this,RewadeVideoActivity.class);
                startActivity(intent);
            }
        });

        btn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImagePicker.getInstance()
                        .setTitle("标题")//设置标题
                        .showCamera(true)//设置是否显示拍照按钮
                        .showImage(true)//设置是否展示图片
                        .showVideo(true)//设置是否展示视频
                        .setSingleType(true)//设置图片视频不能同时选择
                        .setMaxCount(9)//设置最大选择图片数目(默认为1，单选)
                        .start(WebViewActivity.this, REQUEST_SELECT_IMAGES_CODE);//REQEST_SELECT_IMAGES_CODE为Intent调用的requestCode

            }
        });
    }

    /**
     * 跳转到扫码界面扫码
     */
    private void goScan() {
        Intent intent = new Intent(this, CaptureActivity.class);
        startActivityForResult(intent, REQUEST_CODE_SCAN);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case CAMERA_PERMISSION_SCAN:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    goScan();
                } else {
                    Toast.makeText(this, "你拒绝了权限申请，可能无法打开相机扫码哟！", Toast.LENGTH_SHORT).show();
                }
                break;
            case CAMERA_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takePicture(REQUEST_CODE_TAKE);
                } else {
                    Toast.makeText(this, "你拒绝了权限申请，可能无法打开相机扫码哟！", Toast.LENGTH_SHORT).show();
                }
                break;

            default:
        }
    }

    /**
     * 拍照的方法
     */
    private void takePicture(int requestCode) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CODE_TAKE);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 扫描二维码/条码回传
        if (requestCode == REQUEST_CODE_SCAN && resultCode == RESULT_OK) {
            if (data != null) {

            }
        }
        if (requestCode == REQUEST_CODE_TAKE) {
// 解析返回的图片成bitmap
            Bitmap bmp = (Bitmap) data.getExtras().get("data");
            String f = System.currentTimeMillis() + ".jpg";
            File file = new File("/sdcard/pic/" + f);
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(file);
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            } catch (Exception x) {
                Log.e(TAG, "save Bitmap error=" + x);
            } finally {
                try {
                    fos.flush();
                    fos.close();
                } catch (Exception x) {
                    Log.e(TAG, "save Bitmap error=" + x);
                }
            }
            // mWebview.loadUrl("javascript:方法名(参数)");
            //TODO webview 将处理完的值 传递到 javascripte

            mWebView.loadUrl("javascripte:WriteX");
        }

        if (requestCode == REQUEST_SELECT_IMAGES_CODE && resultCode == RESULT_OK) {
            List<String> imagePaths = data.getStringArrayListExtra(ImagePicker.EXTRA_SELECT_IMAGES);
        }
    }
}
