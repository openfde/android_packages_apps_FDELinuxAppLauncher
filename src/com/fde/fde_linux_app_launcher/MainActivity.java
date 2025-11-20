package com.fde.fde_linux_app_launcher;

import android.content.Context;
import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import java.util.List;
import java.util.Map;

import com.fde.fde_linux_app_launcher.R;
import android.content.Intent;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.content.pm.PackageInfo;
import android.util.Log;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import android.os.Looper;
import android.view.Window;


public class MainActivity extends Activity {
    Context context;
    String targetPackage = "com.fde.x11";
    String targetVersion = "1.3.0";
    // String downloadPath = "https://gitee.com/openfde/FDE-X11/releases/download/1.2.3/fde_x11-1.2.3-release.apk";
    String downloadJson = "https://gitee.com/openfde/provision/releases/download/14_2.0.1/14apps.json";

    String name ;
    String exec ;

    boolean isUpdate = false ;
    boolean isAppInstalled = false ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
		setWindowDecorationStatus(Window.WINDOW_DECORATION_FORCE_HIDE);

        try {
            String openParams = getIntent().getStringExtra("openParams");
			 Log.i("FDE","openParams "+openParams);
			if("lock".equals(openParams) || "logout".equals(openParams) || "restart".equals(openParams) || "poweroff".equals(openParams)){               
				new Thread(new Runnable() {
						@Override
						public void run() {
							DeviceUtils.gotoPower(openParams);
							finish();
						}
			    }).start();  
			}else  if(openParams.contains("waydroid")){
                String packageName = openParams.split("###")[1];
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packageName);
                launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(launchIntent);
                finish();
                return;
            }else{
				String[] arrParams = openParams.split("###");
                name = arrParams[0].trim().replaceAll("%[FfUu]", "");
                exec = arrParams[1].trim().replaceAll("%[FfUu]", "");
                Log.i("FDE","name: "+name +",exec: "+exec);

                isAppInstalled = Utils.isAppInstalled(context,targetPackage);
                if(isAppInstalled){
                    PackageManager pm = context.getPackageManager();
                    PackageInfo packageInfo = pm.getPackageInfo(targetPackage, 0);
                    String versionName = packageInfo.versionName; // 
                    int versionCode = packageInfo.versionCode;   // 
                    Log.i("FDE","versionName: "+versionName +",versionCode: "+versionCode);
                    isUpdate = Utils.compareVersionNames(versionName, targetVersion) < 0 ;
                }else{
                    Log.i("FDE","fde x11 is not install ");
                }
        
            
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String result = NetUtils.getFdeMode();
                        if ("shell".equals(result)) {
                            if("open_terminal".equals(exec)){
                                if("".equals(name)){
                                    NetUtils.gotoTerminalApp("0", true,"");
                                }else if(name.contains("volumes")){//linux path
                                    NetUtils.gotoTerminalApp("0", false,name);
                                }else{//android path
                                    NetUtils.gotoTerminalApp("0", true,name);    
                                }
                            }else{
                                NetUtils.gotoLinuxApp(name, exec,"0",MainActivity.this);
                            }        
                            finish();
                        } else {
                            if(!isAppInstalled || isUpdate){
                                parseGitXml( context,downloadJson);
                            }else{
                                // Intent intent = new Intent();
                                // ComponentName componentName = new ComponentName("com.fde.x11", "com.fde.x11.FakeListActivity");
                                // intent.setComponent(componentName);
                                // intent.putExtra("App", name);
                                // intent.putExtra("Path", exec);
                                // intent.putExtra("vnc_activity_name", name);
                                // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                // startActivity(intent);
                                // finish();
                                // if(Utils.isXserviceRunning(context)){
                                String cpuAbiString =Utils.getSystemProperty("ro.product.cpu.abi", ""); 
                                Log.i("FDE","cpuAbiString  "+cpuAbiString);
                                if(cpuAbiString.contains("x86") || cpuAbiString.contains("X86")){
                                    Toast.makeText(context, R.string.x86_tips, Toast.LENGTH_SHORT).show();
                                }else{
                                    if("open_terminal".equals(exec)){
                                        if("".equals(name)){
                                            NetUtils.gotoTerminalApp("1001", true,"");
                                        }else if(name.contains("volumes")){//linux path
                                            NetUtils.gotoTerminalApp("1001", false,name);
                                        }else{//android path
                                            NetUtils.gotoTerminalApp("1001", true,name);    
                                        }
                                    }else{
                                         NetUtils.gotoLinuxApp(name, exec,"1001",MainActivity.this);
                                    } 
                                }
                                    
                                // }else{
                                //     Log.i("FDE","fde x11 is not running... ");
                                //     runOnUiThread(new Runnable() {
                                //         @Override
                                //         public void run() {
                                //             Toast.makeText(context, R.string.x11_not_run, Toast.LENGTH_SHORT).show(); 
                                //         }
                                //     });    
                                // }
                                finish();
                            }
                        }
                    }
                }).start();
                }

            
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, R.string.fde_app_choose, Toast.LENGTH_SHORT).show();
            finish();
        }

    }


    public  void parseGitXml(Context context , String url) { 
        //if not has network
        if (!Utils.isNetworkAvailable(MainActivity.this)) {
            Looper.prepare();
            Toast.makeText(context, R.string.network_tips, Toast.LENGTH_SHORT).show();
            finish();
            Looper.loop();
        }else{
            OkHttpClient client = new OkHttpClient();
            // create Request 
            Request request = new Request.Builder()
                    .url(url)
                    .build();
    
            // send request
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    Type listType = new TypeToken<List<Map<String, Object>>>() {}.getType();
                    // user Gson parse
                    Gson gson = new Gson();
                    List<Map<String, Object>> list = gson.fromJson(responseBody, listType);
                    if(list !=null){
                        String primaryUrl = list.stream()
                                    .filter(map -> map.get("name").toString().contains("FDE x11"))
                                    .map(map -> map.get("primaryUrl").toString())
                                    .findFirst()
                                    .orElse(null);
    
                        Log.i("FDE","primaryUrl: " + primaryUrl + ",isUpdate: "+isUpdate + ", isAppInstalled:"+isAppInstalled);   
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(!isAppInstalled){
                                    DlgUpdate dlgUpdate = new DlgUpdate(MainActivity.this,getString(R.string.install), getString(R.string.install_x11_tips), primaryUrl);
                                    if (!dlgUpdate.isShowing()) {
                                        dlgUpdate.show();
                                    }
                                }else{
                                    DlgUpdate dlgUpdate = new DlgUpdate(MainActivity.this,getString(R.string.update), getString(R.string.verison_need_update_tips), primaryUrl);
                                    if (!dlgUpdate.isShowing()) {
                                        dlgUpdate.show();
                                    }
                                } 
                            }
                        });    
                    }else{
                        finish();
                    }
                } else {
                    Log.e("FDE","Request failed: " + response.code());
                    finish();
                }
            } catch (Exception e) {
                e.printStackTrace();
                finish();
            }
        }
    }

   

}