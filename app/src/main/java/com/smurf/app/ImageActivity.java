package com.smurf.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.smurf.app.login.activity.MainActivity;
import com.smurf.app.utils.SaveImageUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import cn.bingoogolapple.bgabanner.BGABanner;

public class ImageActivity extends Activity {
    private ImageView imageView;
//    private String imgUrl;
    private AlertDialog.Builder builder;
    private String[] imgUrls;
    private BGABanner bgaBanner;
    private int postion;
    private ImageView backIV;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_activity);
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
                        .centerCrop()
                        .dontAnimate()
                        .into(itemView);
            }
        });
//        Log.e("TAG", "onCreate: "+imgUrl);
        init();

//        Glide.with(this).load(imgUrl).into(imageView);



    }

    private void init() {
//        imageView.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View view) {
//                builder = new AlertDialog.Builder(ImageActivity.this);
//                builder.setItems(new String[]{getResources().getString(R.string.save_picture)}, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {

//                    }
//                });
//                builder.show();
//                return true;
//            }
//        });
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
                saveCroppedImage(((BitmapDrawable) bannerItemView.getDrawable()).getBitmap());
            }
        });
        backIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    //保存图片
    private void saveCroppedImage(Bitmap bmp) {
//        SaveImageUtils.saveImageToGallery(ImageActivity.this, bmp);
        SaveImageUtils.saveImageToGallerys(ImageActivity.this, bmp);
        finish();
    }

}
