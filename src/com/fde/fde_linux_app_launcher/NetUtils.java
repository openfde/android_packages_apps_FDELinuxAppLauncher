package com.fde.fde_linux_app_launcher;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import android.widget.Toast;
import android.app.Activity;
import java.io.OutputStreamWriter;


public class NetUtils {
    private static final String ADDRESS = "http://127.0.0.1:18080";

    public static String getLinuxApp() {
        try {
            URL url = new URL(
                    ADDRESS+"/api/v1/apps?page=" + 1 + "&page_size=" + 100);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();

            connection.setDoOutput(false);
            connection.setDoInput(true);
            connection.setRequestMethod("GET");
            connection.setUseCaches(true);
            connection.setInstanceFollowRedirects(true);
            connection.setConnectTimeout(3000);
            connection.connect();
            int code = connection.getResponseCode();
            String res = "";
            if (code == 200) { // 
                // 
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));
                String line = null;

                while ((line = reader.readLine()) != null) {
                    res += line + "\n";
                }
                reader.close();
            }
            connection.disconnect();

            // Log.i("bella","getLinuxApp res "+res);
            Map<String, Object> mpRes = new Gson().fromJson(res, new TypeToken<Map<String, Object>>() {
            }.getType());
            Map<String, Object> mpData = (Map<String, Object>) mpRes.get("data");
            List<Map<String, Object>> responseList = (List<Map<String, Object>>) mpData.get("data");
            if (responseList != null) {
                Log.i("bella", "getLinuxApp responseList " + responseList.size());
                for (Map<String, Object> mp : responseList) {
                    // Log.i("bella","getLinuxApp IconPath " + mp.get("IconPath") + " ,Path : "+mp.get("Path")+ " ,IconType : "+mp.get("IconType")+ " ,Name : "+mp.get("Name"));
                }
            }


            return res;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static String gotoLinuxApp(String name, String exec,String display,Activity activity) {
        try {
            // Target URL
            String targetURL = ADDRESS+"/api/v1/xserver";
            // Create URL Object
            URL url = new URL(targetURL);
            // open a connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Set the request method to POST
            connection.setRequestMethod("POST");

            // Set to allow output
            connection.setDoOutput(true);

            // Set request properties，eg Content-Type
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            // POST parameters
            String postParameters = "App=" + name + "&Path=" + exec + "&Display=:"+display;
            Log.i("bella", "gotoLinuxApp postParameters: " + postParameters);
            // Retrieve output stream and write parameters
            try (OutputStream os = connection.getOutputStream()) {
                os.write(postParameters.getBytes(StandardCharsets.UTF_8));
            }

            // Obtain response code
            int responseCode = connection.getResponseCode();
            Log.i("bella", "gotoLinuxApp Response Code: " + responseCode);
            // Process response content as needed
            // ...

            // Close connection
            connection.disconnect();
            if(responseCode == 428){
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(activity, R.string.x11_not_run, Toast.LENGTH_SHORT).show(); 
                    }
                });    
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void gotoTerminalApp(String Display, boolean IsAndroidFS,String WorkingPath) {
        try {
            // Target URL
            String targetURL = ADDRESS+"/api/v1/xserver/terminal";
            // Create URL Object
            URL url = new URL(targetURL);
            // open a connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Set the request method to POST
            connection.setRequestMethod("POST");

            // Set to allow output
            connection.setDoOutput(true);

            // Set the request method to POST，eg Content-Type
            connection.setRequestProperty("Content-Type", "application/json");

            String jsonBody = String.format("""
                {
                    "Display": ":%s",
                    "IsAndroidFS": %s,
                    "WorkingPath": "%s"
                }
                """, Display, IsAndroidFS, WorkingPath);

            Log.i("FDE", "gotoTerminalApp targetURL: " + targetURL + ",jsonBody: "+jsonBody);
             // 发送 JSON 数据
            try (OutputStream os = connection.getOutputStream();
                OutputStreamWriter osw = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
                osw.write(jsonBody);
                osw.flush();
            }

            // Obtain response code
            int responseCode = connection.getResponseCode();
            String message = connection.getResponseMessage();
            Log.i("FDE", "gotoTerminalApp Response Code: " + responseCode + ",message: "+message);
            // Process response content as needed
            // ...
            // Close connection
            connection.disconnect();
            // if(responseCode == 428){
            //     activity.runOnUiThread(new Runnable() {
            //         @Override
            //         public void run() {
            //             Toast.makeText(activity, R.string.x11_not_run, Toast.LENGTH_SHORT).show(); 
            //         }
            //     });    
            // }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getFdeMode() {
        try {
            URL url = new URL(
                    ADDRESS+"/api/v1/fde_mode");
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();

            connection.setDoOutput(false);
            connection.setDoInput(true);
            connection.setRequestMethod("GET");
            connection.setUseCaches(true);
            connection.setInstanceFollowRedirects(true);
            connection.setConnectTimeout(3000);
            connection.connect();
            int code = connection.getResponseCode();
            String res = "";
            if (code == 200) { //
                //
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));
                String line = null;

                while ((line = reader.readLine()) != null) {
                    res += line + "\n";
                }
                reader.close();
            }
            connection.disconnect();

            Log.i("bella", "getFdeMode res " + res);
            Map<String, Object> mpRes = new Gson().fromJson(res, new TypeToken<Map<String, Object>>() {
            }.getType());
            Map<String, Object> mpData = (Map<String, Object>) mpRes.get("Data");
            return  mpData.get("FDEMode").toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
