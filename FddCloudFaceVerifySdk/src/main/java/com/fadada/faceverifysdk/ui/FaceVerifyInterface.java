package com.fadada.faceverifysdk.ui;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.fadada.faceverifysdk.FddFaceVerifySdk;
import com.fadada.faceverifysdk.bean.ResponseData;
import com.fadada.faceverifysdk.bean.Sign;
import com.google.gson.Gson;
import com.webank.facelight.contants.WbCloudFaceContant;
import com.webank.facelight.contants.WbFaceError;
import com.webank.facelight.contants.WbFaceVerifyResult;
import com.webank.facelight.listerners.WbCloudFaceVeirfyLoginListner;
import com.webank.facelight.listerners.WbCloudFaceVeirfyResultListener;
import com.webank.facelight.tools.WbCloudFaceVerifySdk;
import com.webank.facelight.ui.FaceVerifyStatus;
import com.webank.mbank.okhttp3.Call;
import com.webank.mbank.okhttp3.Callback;
import com.webank.mbank.okhttp3.FormBody;
import com.webank.mbank.okhttp3.OkHttpClient;
import com.webank.mbank.okhttp3.Request;
import com.webank.mbank.okhttp3.Response;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class FaceVerifyInterface {
    private Activity mActivity;

    private WebView mWebView;

    private String userId = "fadadaFaceVerify" + System.currentTimeMillis();

    private String nonce = UUID.randomUUID().toString().trim().replaceAll("-", "");

    // TODO: 腾讯云派发的licence，需要根据自己的包名向腾讯云申请，文档地址：https://cloud.tencent.com/document/product/1007/35870
    private final String WB_LICENSE = "";

    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            Toast.makeText(mActivity, (String)msg.obj, Toast.LENGTH_LONG).show();
        }
    };

    public FaceVerifyInterface(Activity activity, WebView webView) {
        mActivity = activity;
        mWebView = webView;
    }

    @JavascriptInterface
    public void startFace(String name, String idCard, String orderId, String url) {
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(name) || TextUtils.isEmpty(name) || TextUtils.isEmpty(url)) {
            FddFaceVerifySdk.getInstance().getVerifyResultListener().onVerifyFailed();
            return;
        }
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        FormBody builder = new FormBody.Builder()
                .add("userId", userId)
                .add("nonceStr", nonce)
                .add("orderId", orderId)
                .add("name", name)
                .add("idcard", idCard)
                .add("summary", Base64.encodeToString((shaEncrypt(nonce + userId + name + idCard) +
                        md5(orderId + name)).getBytes(), Base64.DEFAULT).replaceAll("\n",""))
                .build();

        Request request = new Request.Builder()
                .url(url + "/fddAuthenticationService/v2/sdk/generateSDKSign.action")
                .post(builder)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.v("FaceVerifyInterface", "API调用失败");
                FddFaceVerifySdk.getInstance().getVerifyResultListener().onVerifyFailed();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.v("FaceVerifyInterface", "API调用成功");

                Gson gson = new Gson();
                ResponseData responseData = gson.fromJson(response.body().string(), ResponseData.class);
                if (responseData.isSuccess()) {
                    //WbCloudFaceContant.NONE           仅活体检测不需要faceId，直接拉起sdk
                    //WbCloudFaceContant.REFLECTION     光线活体
                    //WbCloudFaceContant.ACT            动作活体
                    callWbCloudFaceVerifySdk(responseData);
                } else {
                    FddFaceVerifySdk.getInstance().getVerifyResultListener().onVerifyFailed();
                }
            }
        });
    }

    @JavascriptInterface
    public void gohome(){
        mActivity.finish();
    }

    private void callWbCloudFaceVerifySdk(ResponseData responseData) {
        Bundle data = new Bundle();
        final Sign signData = responseData.getData();
        WbCloudFaceVerifySdk.InputData inputData = new WbCloudFaceVerifySdk.InputData(
                signData.getH5faceId(),
                signData.getOrderId(),
                signData.getWebankAppId(),
                "1.0.0",
                nonce,
                userId,
                signData.getSign(),
                FaceVerifyStatus.Mode.REFLECTION,
                WB_LICENSE);

        data.putSerializable(WbCloudFaceContant.INPUT_DATA, inputData);
        //是否展示刷脸成功页面，默认展示
        data.putBoolean(WbCloudFaceContant.SHOW_SUCCESS_PAGE, false);
        //是否展示刷脸失败页面，默认展示
        data.putBoolean(WbCloudFaceContant.SHOW_FAIL_PAGE, false);
        //颜色设置，默认黑色
        data.putString(WbCloudFaceContant.COLOR_MODE, WbCloudFaceContant.WHITE);
        //是否需要录制上传视频 默认需要
        data.putBoolean(WbCloudFaceContant.VIDEO_UPLOAD, true);
        //是否开启闭眼检测，默认不开启
        data.putBoolean(WbCloudFaceContant.ENABLE_CLOSE_EYES, false);
        //是否播放提示音，默认播放
        data.putBoolean(WbCloudFaceContant.PLAY_VOICE, true);
        //设置选择的比对类型  默认为公安网纹图片对比
        //公安网纹图片比对 WbCloudFaceContant.ID_CRAD
        //自带比对源比对  WbCloudFaceContant.SRC_IMG
        //仅活体检测  WbCloudFaceContant.NONE
        //默认公安网纹图片比对
        data.putString(WbCloudFaceContant.COMPARE_TYPE, WbCloudFaceContant.ID_CARD);

        WbCloudFaceVerifySdk.getInstance().initSdk(mActivity, data, new WbCloudFaceVeirfyLoginListner(){

            @Override
            public void onLoginSuccess() {
                //拉起刷脸页面
                WbCloudFaceVerifySdk.getInstance().startWbFaceVeirifySdk(mActivity, new WbCloudFaceVeirfyResultListener() {

                    @Override
                    public void onFinish(WbFaceVerifyResult wbFaceVerifyResult) {
                        //得到刷脸结果
                        if (wbFaceVerifyResult != null) {
                            if (wbFaceVerifyResult.isSuccess()) {
                                Log.d("FaceVerifyInterface", "成功");
                                Message message = new Message();
                                message.obj = "成功";
                                handler.sendMessage(message);
                                String callbackS = "javascript:startFaceCallback('" + signData.getOrderId() + "','" + 0 + "')";
                                Log.v("FaceVerifyInterface", callbackS);
                                mWebView.loadUrl(callbackS);
                                FddFaceVerifySdk.getInstance().getVerifyResultListener().onVerifySuccess();
                                return;
                            } else {
                                WbFaceError error = wbFaceVerifyResult.getError();
                                if (error != null) {
                                    Message message = new Message();
                                    message.obj = error.getReason();
                                    handler.sendMessage(message);

                                    Log.e("FaceVerifyInterface", error.getReason());
                                } else {
                                    Message message = new Message();
                                    message.obj = "sdk返回error为空！";
                                    handler.sendMessage(message);

                                    Log.e("FaceVerifyInterface", "sdk返回error为空！");
                                }
                            }
                        } else {
                            Message message = new Message();
                            message.obj = "sdk返回结果为空！";
                            handler.sendMessage(message);

                            Log.e("FaceVerifyInterface", "sdk返回结果为空！");
                        }
                        mWebView.loadUrl("javascript:startFaceCallback('" + signData.getOrderId() + "','" + 1 + "')");
                        FddFaceVerifySdk.getInstance().getVerifyResultListener().onVerifyFailed();
                    }
                });
            }

            @Override
            public void onLoginFailed(WbFaceError wbFaceError) {
                if (wbFaceError != null) {
                    Message message = new Message();
                    message.obj = "登录失败！domain=" + wbFaceError.getDomain() + " ;code= " + wbFaceError.getCode()
                            + " ;desc=" + wbFaceError.getDesc() + ";reason=" + wbFaceError.getReason();
                    handler.sendMessage(message);

                    Log.d("FaceVerifyInterface", "登录失败！domain=" + wbFaceError.getDomain() + " ;code= " + wbFaceError.getCode()
                            + " ;desc=" + wbFaceError.getDesc() + ";reason=" + wbFaceError.getReason());
                } else {
                    Message message = new Message();
                    message.obj = "登录失败, sdk返回error为空";
                    handler.sendMessage(message);
                    Log.e("FaceVerifyInterface", "sdk返回error为空！");
                }

                FddFaceVerifySdk.getInstance().getVerifyResultListener().onVerifyFailed();
            }
        });
    }

    private static String shaEncrypt(String strSrc) {
        MessageDigest md = null;
        String strDes = null;
        byte[] bt = strSrc.getBytes();
        try {
            // 将此换成SHA-1、SHA-512、SHA-384等参数
            md = MessageDigest.getInstance("SHA-1");
            md.update(bt);
            // to HexString
            strDes = bytes2Hex(md.digest());
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
        return strDes.toUpperCase();


    }

    private static String md5(String string) {
        if (TextUtils.isEmpty(string)) {
            return "";
        }
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(string.getBytes());
            String result = "";
            for (byte b : bytes) {
                String temp = Integer.toHexString(b & 0xff);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                result += temp;
            }
            return result.toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * byte数组转换为16进制字符串
     *
     * @param bts
     *            数据源
     * @return 16进制字符串
     */
    private static String bytes2Hex(byte[] bts) {
        String des = "";
        String tmp = null;
        for (int i = 0; i < bts.length; i++) {
            tmp = (Integer.toHexString(bts[i] & 0xFF));
            if (tmp.length() == 1) {
                des += "0";
            }
            des += tmp;
        }
        return des;
    }
}
