package com.logon.e100.setting;

import android.R;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

/* loaded from: classes.dex */
public class ActivityFirmware extends Activity {
    private Context mContext;
    private String mFWVersion;
    private TextView mTextViewOnlineManual;
    private TextView mTextViewVWVersion;

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setTheme(R.style.Theme.Holo.Light.NoActionBar.TranslucentDecor);
        setContentView(com.logon_e100.R.layout.activity_firmware);
        this.mContext = this;
        this.mFWVersion = getIntent().getStringExtra("logon.e100.firmware.version");
        this.mTextViewVWVersion = (TextView) findViewById(com.logon_e100.R.id.textview_firmware);
        this.mTextViewVWVersion.setText(this.mFWVersion);
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
