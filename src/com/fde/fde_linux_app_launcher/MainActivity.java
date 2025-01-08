package com.fde.fde_linux_app_launcher;

import android.content.Context;
import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;
import com.fde.fde_linux_app_launcher.R;

public class MainActivity extends Activity {
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;

        try {
            String openParams = getIntent().getStringExtra("openParams");
            String[] arrParams = openParams.split("###");
            String name = arrParams[0].trim().replaceAll("%[FfUu]", "");
            String exec = arrParams[1].trim().replaceAll("%[FfUu]", "");

            new Thread(new Runnable() {
                @Override
                public void run() {
                    String result = NetUtils.getFdeMode();
                    if ("shell".equals(result)) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                NetUtils.gotoLinuxApp(name, exec);
                            }
                        }).start();
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, R.string.fde_app_choose, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                    finish();
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, R.string.fde_app_choose, Toast.LENGTH_LONG).show();
            finish();
        }

    }
}