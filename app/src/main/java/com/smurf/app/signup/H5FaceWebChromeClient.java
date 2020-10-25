package com.smurf.app.signup;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;


import com.esafirm.imagepicker.features.ImagePicker;

import java.io.File;

import static com.smurf.app.StaticNum.REQUEST_SELECT_IMAGES_CODE;


public class H5FaceWebChromeClient extends WebChromeClient {
    private static final String TAG = "H5FaceWebChromeClient";
    private Activity activity;

    public H5FaceWebChromeClient(Activity mActivity) {
        this.activity = mActivity;
    }

    @Override
    public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
        return super.onJsPrompt(view, url, message, defaultValue, result);
    }

    @Override
    public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
        return super.onJsConfirm(view, url, message, result);
    }

    @TargetApi(8)
    @Override
    public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
        return super.onConsoleMessage(consoleMessage);
    }

    // For Android >= 3.0
    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
        toOpenImgChoose();
//        if (WBH5FaceVerifySDK.getInstance().recordVideoForApiBelow21(uploadMsg, acceptType, activity)) {
//            Log.d("liuluchao","receiver h5 info");
//
//            return;
//        }
        return;

    }

    // For Android >= 4.1
    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
        toOpenImgChoose();
//        if (WBH5FaceVerifySDK.getInstance().recordVideoForApiBelow21(uploadMsg, acceptType, activity)) {
//            Log.d("liuluchao","receiver h5 info");
//
//            return;
//        }
        return;
    }

    // For Lollipop 5.0+ Devices
    @TargetApi(21)
    @Override
    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
        toOpenImgChoose();
//        if (WBH5FaceVerifySDK.getInstance().recordVideoForApi21(webView, filePathCallback, activity, fileChooserParams)) {
//            Log.d("liuluchao","receiver h5 info");
//
//            return true;
//        }
        return true;
    }

    //跳转相机
    private void toOpenImgChoose() {
        ImagePicker.create(activity).limit(1) // Activity or Fragment
                .start();


    }

}
