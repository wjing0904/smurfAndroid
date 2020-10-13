package com.smurf.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.smurf.app.utils.SaveImageUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageActivity extends Activity {
    private ImageView imageView;
    private String imgUrl;
    private AlertDialog.Builder builder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_activity);
        imgUrl = getIntent().getStringExtra("img_url");
        init();

        Glide.with(this).load(imgUrl).into(imageView);



    }

    private void init() {
        imageView = (ImageView) findViewById(R.id.image);
        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                builder = new AlertDialog.Builder(ImageActivity.this);
                builder.setItems(new String[]{getResources().getString(R.string.save_picture)}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveCroppedImage(((BitmapDrawable) imageView.getDrawable()).getBitmap());
                    }
                });
                builder.show();
                return true;
            }
        });

//        imageView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                builder.dismiss();
//            }
//        });
    }

    //保存图片
    private void saveCroppedImage(Bitmap bmp) {
        SaveImageUtils.saveImageToGallery(ImageActivity.this, bmp);
        SaveImageUtils.saveImageToGallerys(ImageActivity.this, bmp);
        finish();
    }

}
