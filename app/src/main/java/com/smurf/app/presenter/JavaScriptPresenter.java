package com.smurf.app.presenter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.webkit.JavascriptInterface;

import com.lcw.library.imagepicker.ImagePicker;
import com.smurf.app.view.IWebViewInterface;
import com.smurf.app.zxing.android.CaptureActivity;

import java.util.List;

import static com.smurf.app.StaticNum.REQUEST_CAMERA_CODE;
import static com.smurf.app.StaticNum.REQUEST_SELECT_IMAGES_CODE;

public class JavaScriptPresenter {
    private Context mContext;
    private IWebViewInterface webViewInterface;
    private int picSelectedNum;
    public JavaScriptPresenter(Context context, IWebViewInterface webViewInterface){
        this.mContext = context;
        this.webViewInterface = webViewInterface;
    }

    public int getPicSelectedNum(){
        return picSelectedNum;
    }

    public void openImageSelected(int picNum) {
        this.picSelectedNum = picNum;
        ImagePicker.getInstance()
                .setTitle("标题")//设置标题
                .showCamera(true)//设置是否显示拍照按钮
                .showImage(true)//设置是否展示图片
                .showVideo(true)//设置是否展示视频
                .setSingleType(true)//设置图片视频不能同时选择
                .setMaxCount(picNum ==0? 1:picNum)//设置最大选择图片数目(默认为1，单选)
                .start(((Activity)mContext), REQUEST_SELECT_IMAGES_CODE);//REQEST_SELECT_IMAGES_CODE为Intent调用的requestCode
    }

    /**
     * 跳转到扫码界面扫码
     */
    public void openZxing() {
        Intent intent = new Intent(mContext, CaptureActivity.class);
        ((Activity)mContext).startActivityForResult(intent, REQUEST_CAMERA_CODE);
    }

    public void notifyScanVue(String content){
        if(webViewInterface!= null)
            webViewInterface.notifyZxingValueToJs(content);
    }
    public void notifyCamer(List<String> paths){
        StringBuffer sb = new StringBuffer();
        for(String s:paths){
            sb.append(s).append(",");
        }
        if(webViewInterface!= null)
            webViewInterface.notifyImageSelectedValueToJs(sb.toString().substring(0,sb.toString().length()-1));
    }

    public void notifyLoaction(String value){
        if(webViewInterface!= null){
            webViewInterface.notifyLocation(value);
        }
    }
}
