package com.smurf.app.jsp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.bumptech.glide.Glide;
import com.smurf.app.ImageActivity;
import com.smurf.app.RewadeVideoActivity;
import com.smurf.app.SignUpActivity;
import com.smurf.app.presenter.JavaScriptPresenter;
import com.smurf.app.utils.SaveImageUtils;
import com.smurf.app.utils.ShareUtil;

import java.util.ArrayList;

import static com.smurf.app.StaticNum.REQUEST_CAMERA_CODE;

public class JavaScriptInterfaceImpl implements JavaScriptInterface {

    private Context mContext;
    private JavaScriptPresenter javaScriptPresenter;
    public JavaScriptInterfaceImpl(Context context, JavaScriptPresenter javaScriptPresenter) {
        mContext = context;
        this.javaScriptPresenter = javaScriptPresenter;
    }

    @Override
    public void goSCan() {
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(((Activity)mContext), new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_CODE);
        } else {
            if(javaScriptPresenter!= null)
                javaScriptPresenter.openZxing();
        }
    }

    @Override
    public void imageSelected(int picNum) {
        if(javaScriptPresenter != null)
            javaScriptPresenter.openImageSelected(picNum);
    }

    @Override
    public void openRewarderVideo() {
        Intent intent = new Intent(mContext, RewadeVideoActivity.class);
        mContext.startActivity(intent);
    }

    //TODO LocationClient.stop()
    @Override
    public void getLocation() {
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

    @Override
    public void openAndSaveImg(String url) {
        Intent intent = new Intent(mContext, ImageActivity.class);
        intent.putExtra("img_url",url);
        mContext.startActivity(intent);
    }
    /**
     * 分享功能
     * shareType 0：分享文本内容，1：分享单张图片，2：分享多张图片
     */
    @Override
    public void share(int shareType, String title, String text, Uri uri, ArrayList<Uri> imageUris) {
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

    @Override
    public void signUp(String signUrl) {
        Intent intent = new Intent(mContext, SignUpActivity.class);
        intent.putExtra("sign_url",signUrl);
        mContext.startActivity(intent);
    }

}
