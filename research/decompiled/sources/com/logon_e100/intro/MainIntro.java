package com.logon_e100.intro;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.TextView;
import com.logon.e100.utils.Logger;
import com.logon_e100.MainActivity;
import com.logon_e100.Manifest;
import com.logon_e100.R;
import java.lang.reflect.Method;

/* loaded from: classes.dex */
public class MainIntro extends Activity {
    private static final int ACCESS_COARSE_LOCATION_WRITE_EXTERNAL_STORAGE = 2;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 0;
    private static final String TAG = "MainIntro";
    public static int mMobileData;
    private final Handler handler16 = new Handler() { // from class: com.logon_e100.intro.MainIntro.1
        @Override // android.os.Handler
        public void handleMessage(Message message) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainIntro.this);
            builder.setPositiveButton(MainIntro.this.getResources().getString(R.string.action_confirm), new DialogInterface.OnClickListener() { // from class: com.logon_e100.intro.MainIntro.1.1
                @Override // android.content.DialogInterface.OnClickListener
                @TargetApi(23)
                public void onClick(DialogInterface dialogInterface, int i) {
                    Log.i("WAN1", "111111");
                    MainIntro.this.finish();
                    Log.i("WAN1", "222222");
                }
            });
            Log.i("WAN1", "333333");
            builder.setIcon(R.drawable.ic_launcher);
            builder.setTitle(MainIntro.this.getResources().getString(R.string.App_authority));
            builder.setMessage(MainIntro.this.getResources().getString(R.string.App_authority_content));
            builder.show();
        }
    };
    private int m_AppAuthority;

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setTheme(android.R.style.Theme.Holo.Light.NoActionBar);
        setContentView(R.layout.mainintro);
        if (isMobileDataEnabled().booleanValue()) {
            Logger.e("Wanjae Data On");
            mMobileData = 1;
        } else {
            Logger.e("Wanjae Data Off");
            mMobileData = 0;
        }
        if (Build.VERSION.SDK_INT >= 23) {
            checkPermission();
            if (this.m_AppAuthority == 1) {
                GotoActivityWarningAndQuit();
                return;
            }
            return;
        }
        GotoActivityWarningAndQuit();
    }

    private Boolean isMobileDataEnabled() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService("connectivity");
        try {
            Method declaredMethod = Class.forName(connectivityManager.getClass().getName()).getDeclaredMethod("getMobileDataEnabled", new Class[0]);
            declaredMethod.setAccessible(true);
            return (Boolean) declaredMethod.invoke(connectivityManager, new Object[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void GotoActivityWarningAndQuit() {
        new Handler().postDelayed(new Runnable() { // from class: com.logon_e100.intro.MainIntro.2
            @Override // java.lang.Runnable
            public void run() {
                Log.d(MainIntro.TAG, "run: ");
                MainIntro.this.startActivity(new Intent(MainIntro.this, (Class<?>) MainActivity.class));
                MainIntro.this.finish();
            }
        }, 1000L);
        ((TextView) findViewById(R.id.textview_firmware)).setText(getAppVersion());
    }

    public String getAppVersion() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException unused) {
            return "";
        }
    }

    @Override // android.app.Activity
    protected void onStart() {
        super.onStart();
    }

    @Override // android.app.Activity
    protected void onRestart() {
        super.onRestart();
    }

    @Override // android.app.Activity
    protected void onResume() {
        super.onResume();
    }

    @Override // android.app.Activity
    protected void onPause() {
        super.onPause();
    }

    @Override // android.app.Activity
    protected void onStop() {
        super.onStop();
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        super.onDestroy();
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, "android.permission.ACCESS_COARSE_LOCATION"}, 2);
    }

    @TargetApi(23)
    private void checkPermission() {
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == 0 && checkSelfPermission("android.permission.ACCESS_COARSE_LOCATION") == 0) {
            Log.i("WAN1", "WRITE_EXTERNAL_STORAGE 접근 승낙 상태 일때");
            this.m_AppAuthority = 1;
            return;
        }
        requestPermission();
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) && ActivityCompat.shouldShowRequestPermissionRationale(this, "android.permission.ACCESS_COARSE_LOCATION")) {
            Log.i("WAN1", "사용자가 다시 보지 않기에 체크를 하지 않고, 권한 설정을 둘 다 거절한 이력이 있는 경우");
            return;
        }
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Log.i("WAN1", "사용자가 다시 보지 않기에 체크를 하지 않고, WRITE_EXTERNAL_STORAGE 권한 설정을 거절한 이력이 있는 경우");
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, "android.permission.ACCESS_COARSE_LOCATION")) {
            Log.i("WAN1", "사용자가 다시 보지 않기에 체크를 하지 않고, ACCESS_COARSE_LOCATION 권한 설정을 거절한 이력이 있는 경우");
        } else {
            Log.i("WAN1", "사용자가 최초로 사용할 때");
        }
    }

    @Override // android.app.Activity
    public void onRequestPermissionsResult(int i, @NonNull String[] strArr, @NonNull int[] iArr) {
        Log.i("WAN1", "case문  바깥");
        switch (i) {
            case 0:
                if (iArr.length > 0 && iArr[0] == 0) {
                    Log.i("WAN1", "case문  WRITE_EXTERNAL_STORAGE 안 권한 허락");
                    GotoActivityWarningAndQuit();
                    break;
                } else {
                    this.handler16.sendMessage(this.handler16.obtainMessage());
                    Log.i("WAN1", "case문 WRITE_EXTERNAL_STORAGE 안 권한 거부");
                    break;
                }
                break;
            case 1:
                if (iArr.length > 0 && iArr[0] == 0) {
                    Log.i("WAN1", "case문 ACCESS_COARSE_LOCATION 안 권한 허락");
                    GotoActivityWarningAndQuit();
                    break;
                } else {
                    this.handler16.sendMessage(this.handler16.obtainMessage());
                    Log.i("WAN1", "case문 ACCESS_COARSE_LOCATION 안 권한 거부");
                    break;
                }
                break;
            case 2:
                if (iArr.length > 0 && iArr[0] == 0 && iArr[1] == 0) {
                    Log.i("WAN1", "case문  안 권한 허락");
                    GotoActivityWarningAndQuit();
                    break;
                } else {
                    this.handler16.sendMessage(this.handler16.obtainMessage());
                    Log.i("WAN1", "case문 안 권한 거부");
                    break;
                }
        }
    }
}
