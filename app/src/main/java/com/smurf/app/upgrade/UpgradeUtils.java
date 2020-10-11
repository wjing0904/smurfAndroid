package com.smurf.app.upgrade;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.core.content.FileProvider;

import java.io.File;


public class UpgradeUtils {

    private UpgradeUtils() {
    }

    private static final class UpgradeUtilsHolder {
        private static final UpgradeUtils INTANCE = new UpgradeUtils();
    }

    public static UpgradeUtils getInstance() {
        return UpgradeUtilsHolder.INTANCE;
    }

    public void installAPK(Context context, String apkPath) {
        // 判断是否8.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            boolean hasInstallPermission = context.getPackageManager().canRequestPackageInstalls();
            if (!hasInstallPermission) {
                Intent intent8 = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                        Uri.parse("package:" + context.getPackageName()));
                intent8.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent8);
                startInstallAPK(context, apkPath);
                return;
            } else {
                startInstallAPK(context, apkPath);
            }
        } else {
            startInstallAPK(context, apkPath);
        }
    }


    private void startInstallAPK(Context context, String apkPath) {
        File file = new File(apkPath);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        // 如果不加这行代码，安装成功后，不会打开新版本APP
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri apkUri;
        //判断版本是否在 7.0 以上
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            String packageName = context.getPackageName();
            apkUri = FileProvider.getUriForFile(context, packageName + ".fileProvider", file);
        } else {
            apkUri = Uri.fromFile(file);
        }

        if (context.getPackageManager().queryIntentActivities(intent, 0).size() > 0) {
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            context.startActivity(intent);
        }
    }
}
