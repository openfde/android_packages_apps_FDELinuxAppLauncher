package com.fde.fde_linux_app_launcher;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.util.Log;
import android.content.pm.PackageManager;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import androidx.core.content.FileProvider;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;

public class Utils {
    public static int compareVersionNames(String version1, String version2) {
        String[] parts1 = version1.split("\\.");
        String[] parts2 = version2.split("\\.");
    
        int length = Math.max(parts1.length, parts2.length);
        for (int i = 0; i < length; i++) {
            int v1 = i < parts1.length ? Integer.parseInt(parts1[i]) : 0;
            int v2 = i < parts2.length ? Integer.parseInt(parts2[i]) : 0;
    
            if (v1 != v2) {
                return v1 - v2; // 如果 v1 > v2 返回正数，v1 < v2 返回负数
            }
        }
    
        return 0; // 相等
    }

    public static  boolean isAppInstalled(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        try {
            packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true; // app installed
        } catch (PackageManager.NameNotFoundException e) {
            return false; // app not install
        }
    }

    public static void installApk(Activity activity, String downloadApk) {
        // File apkFile = new File(context.getFilesDir(), downloadApk);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        File file = new File(downloadApk);
        Uri apkUri = FileProvider.getUriForFile(activity, activity.getPackageName() + ".fileprovider", file);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        activity.startActivity(intent);
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
            return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        }
        return false;
    }
    
}
