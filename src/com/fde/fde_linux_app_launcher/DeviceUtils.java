package com.fde.fde_linux_app_launcher;

import android.Manifest;
import android.app.Instrumentation;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.display.DisplayManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.UserManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.Display;
import androidx.core.content.ContextCompat;
import com.google.gson.Gson;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;
import okhttp3.*;
import java.io.*;

public class DeviceUtils {
    public static final String BASIP = "127.0.0.1";
    public static final String BASEURL = "http://" + BASIP + ":18080";
    public static final String URL_GETALLAPP = "/api/v1/apps";
    public static final String URL_STARTAPP = "/api/v1/vnc";
    public static final String URL_STOPAPP = "/api/v1/vnc";
    public static final String URL_LOGOUT = "/api/v1/power/logout";
    public static final String URL_POWOFF = "/api/v1/power/off";
    public static final String URL_RESTART = "/api/v1/power/restart";
    public static final String URL_LOCK = "/api/v1/power/lock";
    public static final String URL_GET_BRIGHTNESS = "/api/v1/brightness";
    public static final String URL_SET_BRIGHTNESS = "/api/v1/brightness";
    public static final String URL_DETECT_BRIGHTNESS = "/api/v1/brightness/detect";

    public static boolean lockScreen(Context context) {
        DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        try {
            dpm.lockNow();
        } catch (SecurityException e) {
            return false;
        }
        return true;
    }


    public static void gotoPower(String power) {
        if("lock".equals(power)) {
            lock();
        } else if("logout".equals(power)){
            logout();
        }else if("restart".equals(power)){
            restart();
        }else{
            poweroff();
        }
    }  

    public static void logout() {
        NetworkUtils networkUtils = new NetworkUtils();
        FormBody requestBody = new FormBody.Builder().build();
        networkUtils.postRequest(BASEURL + URL_LOGOUT, requestBody);
    }

    public static void poweroff() {
        NetworkUtils networkUtils = new NetworkUtils();
        FormBody requestBody = new FormBody.Builder().build();
        networkUtils.postRequest(BASEURL + URL_POWOFF, requestBody);
    }

    public static void restart() {
        NetworkUtils networkUtils = new NetworkUtils();
        FormBody requestBody = new FormBody.Builder().build();
        networkUtils.postRequest(BASEURL + URL_RESTART, requestBody);
    }

    public static void lock() {
        NetworkUtils networkUtils = new NetworkUtils();
        FormBody requestBody = new FormBody.Builder().build();
        networkUtils.postRequest(BASEURL + URL_LOCK, requestBody);
    }
}
