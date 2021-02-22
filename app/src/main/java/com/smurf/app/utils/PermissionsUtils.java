package com.smurf.app.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;

import com.smurf.app.R;

import static com.smurf.app.base.StaticNum.REQUEST_DIALOG_CODE;

public class PermissionsUtils {

    public static void showNormalDialog(Activity activity, String serviceName){
        /* @setIcon 设置对话框图标
         * @setTitle 设置对话框标题
         * @setMessage 设置对话框消息提示
         * setXXX方法返回Dialog对象，因此可以链式设置属性
         */
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(activity);
        normalDialog.setIcon(R.drawable.logo);
        normalDialog.setTitle("蓝晶灵想要使用"+serviceName);
        normalDialog.setMessage("请在设置-蓝晶灵中开启"+serviceName);
        normalDialog.setPositiveButton("去设置",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri1 = Uri.fromParts("package", activity.getPackageName(), null);
                        intent.setData(uri1);
                        activity.startActivityForResult(intent,REQUEST_DIALOG_CODE);
                    }
                });
        normalDialog.setNegativeButton("知道了",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        // 显示
        normalDialog.show();
    }
}
