package com.smurf.app.presenter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.webkit.JavascriptInterface;

import androidx.core.app.ActivityCompat;

import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.features.ReturnMode;
import com.smurf.app.view.IWebViewInterface;
import com.smurf.app.zxing.android.CaptureActivity;

import java.util.List;

import static com.smurf.app.StaticNum.REQUEST_CAMERA_CODE;
import static com.smurf.app.StaticNum.REQUEST_SELECT_IMAGES_CODE;

public class JavaScriptPresenter {
    private static final String TAG = "JavaScriptPresenter";
    private Context mContext;
    private IWebViewInterface webViewInterface;
    private int picSelectedNum;
    private LocationManager locationManager;

    public JavaScriptPresenter(Context context, IWebViewInterface webViewInterface){
        this.mContext = context;
        this.webViewInterface = webViewInterface;
    }

    public int getPicSelectedNum(){
        return picSelectedNum;
    }

    public void openImageSelected(int picNum) {
        this.picSelectedNum = picNum;
        ImagePicker.create((Activity)mContext).limit(picNum <=0 ?1:picNum) // Activity or Fragment
                .start(REQUEST_SELECT_IMAGES_CODE);

    }
    public void getLocal(){
        locationManager = (LocationManager)(((Activity)mContext).getSystemService(Context.LOCATION_SERVICE));
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            getLocation();
            //gps已打开
        } else {
            toggleGPS();
            new Handler() {
            }.postDelayed(new Runnable() {
                @Override
                public void run() {
                    getLocation();
                }
            }, 2000);

        }
    }

    private void toggleGPS() {
        Intent gpsIntent = new Intent();
        gpsIntent.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
        gpsIntent.addCategory("android.intent.category.ALTERNATIVE");
        gpsIntent.setData(Uri.parse("custom:3"));
        try {
            PendingIntent.getBroadcast(mContext, 0, gpsIntent, 0).send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener);
            Location location1 = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location1 != null && webViewInterface!= null) {
//                latitude = location1.getLatitude(); // 经度
//                longitude = location1.getLongitude(); // 纬度
                webViewInterface.notifyLocation(location1.getLatitude() + "," + location1.getLongitude());
            }
        }
    }

    private void getLocation() {
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null && webViewInterface != null) {
            webViewInterface.notifyLocation(location.getLatitude() + "," + location.getLongitude());
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
        }
    }


    LocationListener locationListener = new LocationListener() {
        // Provider的状态在可用、暂时不可用和无服务三个状态直接切换时触发此函数
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        // Provider被enable时触发此函数，比如GPS被打开
        @Override
        public void onProviderEnabled(String provider) {
            Log.e(TAG, provider);
        }

        // Provider被disable时触发此函数，比如GPS被关闭
        @Override
        public void onProviderDisabled(String provider) {
            Log.e(TAG, provider);
        }

        // 当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发
        @Override
        public void onLocationChanged(Location location) {
            if (location != null && webViewInterface != null) {
                Log.e("Map", "Location changed : Lat: " + location.getLatitude() + " Lng: " + location.getLongitude());
//                latitude = location.getLatitude(); // 经度
//                longitude = location.getLongitude(); // 纬度
                webViewInterface.notifyLocation(location.getLatitude() + "," + location.getLongitude());
            }
        }
    };


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
    public void notifyCamer(String imageInput,String name){
        if(webViewInterface!= null)
            webViewInterface.notifyImageSelectedValueToJs(imageInput,name);
    }

    public void notifyLoaction(String value){
        if(webViewInterface!= null){
            webViewInterface.notifyLocation(value);
        }
    }
}
