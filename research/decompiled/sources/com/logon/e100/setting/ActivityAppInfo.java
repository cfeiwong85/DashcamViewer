package com.logon.e100.setting;

import android.R;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;

/* loaded from: classes.dex */
public class ActivityAppInfo extends Activity {
    private TextView mTextViewAppInfo;

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setTheme(R.style.Theme.Holo.Light.NoActionBar.TranslucentDecor);
        setContentView(com.logon_e100.R.layout.activity_app_info);
        this.mTextViewAppInfo = (TextView) findViewById(com.logon_e100.R.id.textview_app_info);
        String string = getResources().getString(com.logon_e100.R.string.app_name);
        String appVersion = getAppVersion();
        this.mTextViewAppInfo.setText(string + " (V" + appVersion + ")");
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
}
