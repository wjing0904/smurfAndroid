package com.smurf.app;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.smurf.app.base.OnDialogApplyPermissionListener;
import com.smurf.app.base.utils.SharedPreferencesHelper;
import com.smurf.app.login.activity.MainActivity;
import com.smurf.app.utils.SaveImageUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import cn.bingoogolapple.bgabanner.BGABanner;

import static com.smurf.app.base.StaticNum.REQUEST_FDD_CODE;

public class ImageActivity extends Activity {
    private ImageView imageView;
//    private String imgUrl;
    private AlertDialog.Builder builder;
    private String[] imgUrls;
    private BGABanner bgaBanner;
    private int postion;
    private ImageView backIV;
    private Context mContext;
    private SharedPreferencesHelper sharedPreferencesHelper;
    private OnDialogApplyPermissionListener mOnDialogPremission;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_activity);
        mContext = this;
        sharedPreferencesHelper = new SharedPreferencesHelper(
                this, "smurf");
        bgaBanner = (BGABanner) findViewById(R.id.banner_guide_content);
        imageView = (ImageView) findViewById(R.id.image);
        backIV = (ImageView) findViewById(R.id.back_iv);
        imgUrls = getIntent().getStringArrayExtra("img_url");
        postion = getIntent().getIntExtra("postion",0);
        bgaBanner.setData(Arrays.asList(imgUrls), Arrays.asList("", "", ""));
        bgaBanner.setCurrentItem(postion);
        bgaBanner.setAdapter(new BGABanner.Adapter<ImageView, String>() {
            @Override
            public void fillBannerItem(BGABanner banner, ImageView itemView, String model, int position) {
                Glide.with(ImageActivity.this)
                        .load(model)
                        .dontAnimate()
                        .into(itemView);
            }
        });
        init();
    }

    private void init() {
        List<? extends View> bgaBannerViews = bgaBanner.getViews();
        for (int i = 0; i < bgaBannerViews.size(); i++) {
            View view = bgaBannerViews.get(i);
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    builder = new AlertDialog.Builder(ImageActivity.this);
                    builder.setItems(new String[]{getResources().getString(R.string.save_picture)}, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ImageView bannerItemView = (ImageView) bgaBanner.getItemView(bgaBanner.getCurrentItem());
                            saveCroppedImage(((BitmapDrawable) bannerItemView.getDrawable()).getBitmap());
                        }
                    });
                    builder.show();
                    return false;
                }
            });
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });
        }
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageView bannerItemView =(ImageView)bgaBanner.getItemView(bgaBanner.getCurrentItem());

                if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(mContext,
                        Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    int isPermiss = (int) sharedPreferencesHelper.get(SharedPreferencesHelper.FDD_PERMISSION, 0);
                    if (isPermiss == 0) {
                        ActivityCompat.requestPermissions((Activity) mContext,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, REQUEST_FDD_CODE);
                    } else if (isPermiss == 2) {
                        showDialog("照片，多媒体，存储权限", new OnDialogApplyPermissionListener() {
                            @Override
                            public void isPremission(boolean isAllow) {
                                if (isAllow) {
                                    saveCroppedImage(((BitmapDrawable) bannerItemView.getDrawable()).getBitmap());
                                }
                            }
                        }, REQUEST_FDD_CODE);
                    } else {
                        sharedPreferencesHelper.put(SharedPreferencesHelper.FDD_PERMISSION, 0);
                        ActivityCompat.requestPermissions((Activity) mContext,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, REQUEST_FDD_CODE);
                    }
                } else {
                    saveCroppedImage(((BitmapDrawable) bannerItemView.getDrawable()).getBitmap());
                }


            }
        });
        backIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                setResult(9,intent);
                finish();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_FDD_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sharedPreferencesHelper.put(SharedPreferencesHelper.FDD_PERMISSION, 1);
            } else {
                sharedPreferencesHelper.put(SharedPreferencesHelper.FDD_PERMISSION, 2);

            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_FDD_CODE){
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                if (mOnDialogPremission != null) {
                    mOnDialogPremission.isPremission(false);
                }
                sharedPreferencesHelper.put(SharedPreferencesHelper.FDD_PERMISSION, 2);
            }else{
                if (mOnDialogPremission != null) {
                    mOnDialogPremission.isPremission(true);
                }
                sharedPreferencesHelper.put(SharedPreferencesHelper.FDD_PERMISSION, 1);
            }
        }
    }

    private void showDialog(String serviceName, OnDialogApplyPermissionListener onDialogPremission, int requestCode) {
        /* @setIcon 设置对话框图标
         * @setTitle 设置对话框标题
         * @setMessage 设置对话框消息提示
         * setXXX方法返回Dialog对象，因此可以链式设置属性
         */
        this.mOnDialogPremission = onDialogPremission;
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(mContext);
        normalDialog.setIcon(R.drawable.logo);
        normalDialog.setTitle("蓝晶灵想要使用" + serviceName);
        normalDialog.setMessage("请在设置-蓝晶灵中开启" + serviceName);
        normalDialog.setPositiveButton("去设置",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri1 = Uri.fromParts("package", mContext.getPackageName(), null);
                        intent.setData(uri1);
                        ((Activity) mContext).startActivityForResult(intent, requestCode);
                    }
                });
        normalDialog.setNegativeButton("知道了",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (mOnDialogPremission != null) {
                            mOnDialogPremission.isPremission(false);
                        }
                    }
                });
        // 显示
        normalDialog.show();
    }


    //保存图片
    private void saveCroppedImage(Bitmap bmp) {
//        SaveImageUtils.saveImageToGallery(ImageActivity.this, bmp);
        SaveImageUtils.saveImageToGallerys(ImageActivity.this, bmp);
//        finish();
    }

}
