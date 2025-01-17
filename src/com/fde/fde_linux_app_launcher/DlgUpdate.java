package com.fde.fde_linux_app_launcher;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.fde.fde_linux_app_launcher.DlgUpdate.MyThread;
import com.fde.fde_linux_app_launcher.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import android.widget.LinearLayout;
import android.util.Log;

public class DlgUpdate extends Dialog {
    String packageUrl;
    ProgressBar progressBar;
    TextView txtProgress;
    TextView txtInstall;
    TextView txtCancel ;
    TextView txtDesc;
    Activity activity;
    LinearLayout  layoutProgress;
    int downloadStatus = 0;
    String downPath = "";
    String desc ;
    String tips ;
    private boolean stopDownload = false;
    private static final OkHttpClient client = new OkHttpClient();


    public DlgUpdate(@NonNull Activity activity,String desc,String tips, String packageUrl) {
        super(activity);
        this.packageUrl = packageUrl;
        this.activity = activity;
        this.desc = desc ;
        this.tips = tips;
    }

    Handler mHander = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                Log.i("FDE","msg.arg1:"+msg.arg1 + ",isNetworkAvailable: "+Utils.isNetworkAvailable(activity));
                progressBar.setProgress(msg.arg1);
                txtProgress.setText(msg.arg1 + "%");
            } else {
                Log.i("FDE","finish app download....  downloadStatus:"+downloadStatus);
               // txtInstall.setTextColor(R.color.dark_text);
                txtInstall.setText(R.string.install);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_update);

        layoutProgress = (LinearLayout)findViewById(R.id.layoutProgress);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        txtProgress = (TextView) findViewById(R.id.txtProgress);
        txtInstall = (TextView) findViewById(R.id.txtInstall);
        txtCancel = (TextView) findViewById(R.id.txtCancel);
        txtDesc = (TextView) findViewById(R.id.txtDesc);
        txtInstall.setText(desc);
        txtDesc.setText(tips);

        txtCancel.setOnClickListener(view ->{
            downloadStatus = 0;
            dismiss();
        });

        txtInstall.setOnClickListener(view ->{
            if (downloadStatus == 0) {
                new Thread(new MyThread()).start();
                downloadStatus = 1;
                txtInstall.setText(activity.getString(R.string.install));
              //  txtInstall.setTextColor(R.color.grep_text);
                txtInstall.setText(R.string.downloading);
                layoutProgress.setVisibility(View.VISIBLE);
                txtDesc.setVisibility(View.GONE);
            } else if(downloadStatus == 2 ) {
                Utils.installApk(activity, downPath);
                downloadStatus = 0 ;
                dismiss();
            }
        });      
    }

    class MyThread implements Runnable {

        @Override
        public void run() {
            String savePath = getContext().getFilesDir().getAbsolutePath();
            String fileName = packageUrl.substring(packageUrl.lastIndexOf("/") + 1);
            downPath = savePath +"/"+fileName;
            Log.i("FDE","downPath:"+downPath + ",savePath "+savePath);
            File file = new File(downPath);
            try {
                if(file.exists()){
                    file.delete();
                }
                file.createNewFile();
                downloadFile(packageUrl, file);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void dismiss() {
        if(downloadStatus == 0){
            super.dismiss();
            activity.finish();
        }
    }

    
    public void downloadFile(String url, File destinationFile) {
        long downloadedBytes = destinationFile.exists() ? destinationFile.length() : 0;

        while (!stopDownload) {
            if (!Utils.isNetworkAvailable(getContext())) {
                Log.i("FDE","Network unavailable. Waiting to retry...");
                File file = new File(downPath);
                try {
                    if(file.exists()){
                        file.delete();
                    }
                    file.createNewFile();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                sleep(5000); // 
                continue;
            }

            try {
                Request request = new Request.Builder()
                        .url(url)
                        .header("Range", "bytes=" + downloadedBytes + "-")
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful() || response.code() == 206) {
                        long total = response.body().contentLength();
                        try (InputStream inputStream = response.body().byteStream();
                             RandomAccessFile raf = new RandomAccessFile(destinationFile, "rw")) {
                            raf.seek(downloadedBytes);
                            byte[] buffer = new byte[8192];
                            int bytesRead;
                            long sum = 0;
                            while ((bytesRead = inputStream.read(buffer)) != -1) {
                                raf.write(buffer, 0, bytesRead);
                                sum += bytesRead;

                                int progress = (int) (sum * 1.0f / total * 100);
                                Message msg = new Message();
                                msg.what = 1;
                                msg.arg1 = progress;
                                mHander.sendMessage(msg);

                                if (!Utils.isNetworkAvailable(getContext())) {
                                    throw new IOException("Network lost during download");
                                }
                            }
                        }
                        Log.i("FDE","Download completed!");
                        downloadStatus = 2;
                        Message msg = new Message();
                        msg.what = 2;
                        mHander.sendMessage(msg);
                        return; // 下载完成后退出
                    } else {
                        throw new IOException("Unexpected response code: " + response.code());
                    }
                }
            } catch (IOException e) {
                System.out.println("Download failed: " + e.getMessage());
                System.out.println("Retrying...");
                sleep(5000); // 等待 5 秒后重试
            }
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void stopDownload() {
        stopDownload = true;
    }
}
