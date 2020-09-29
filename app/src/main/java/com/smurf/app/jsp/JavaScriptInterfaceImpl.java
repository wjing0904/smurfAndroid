package com.smurf.app.jsp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.smurf.app.RewadeVideoActivity;
import com.smurf.app.presenter.JavaScriptPresenter;
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
}
