package com.logon_e100;

import android.annotation.TargetApi;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyCharacterMap;
import android.view.ViewConfiguration;
import android.widget.TabHost;
import android.widget.TextView;
import com.logon.e100.setting.ActivitySetting;
import com.logon.e100.utils.Logger;
import com.logon_e100.MainProtocol;
import java.io.IOException;

@TargetApi(19)
/* loaded from: classes.dex */
public class MainActivity extends TabActivity implements TabHost.OnTabChangeListener {
    public static Context context;
    static TabHost tabHost;
    MainProtocol mprotocol;

    @Override // android.app.ActivityGroup, android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getWindow().addFlags(128);
        setTheme(android.R.style.Theme.Holo.Light.NoActionBar);
        setContentView(R.layout.activity_main);
        context = this;
        Resources resources = getResources();
        Log.d("HAN6", "wifi_forward = " + new Intent(getIntent()).getIntExtra("wifi_forward", 0));
        tabHost = getTabHost();
        Intent intent = new Intent().setClass(this, MainFreeView.class);
        intent.putExtra("wifi_forward", 1);
        String string = getResources().getString(R.string.tab_live_stream);
        String string2 = getResources().getString(R.string.tab_file_list);
        String string3 = getResources().getString(R.string.tab_setting);
        TabHost.TabSpec newTabSpec = tabHost.newTabSpec(string);
        newTabSpec.setIndicator(string, resources.getDrawable(android.R.drawable.ic_menu_agenda));
        newTabSpec.setContent(intent);
        tabHost.addTab(newTabSpec);
        tabHost.addTab(tabHost.newTabSpec(string2).setIndicator(string2, resources.getDrawable(android.R.drawable.ic_menu_edit)).setContent(new Intent().setClass(this, MainFileListView.class).addFlags(67108864)));
        tabHost.addTab(tabHost.newTabSpec(string3).setIndicator(string3, resources.getDrawable(android.R.drawable.ic_menu_help)).setContent(new Intent().setClass(this, ActivitySetting.class).addFlags(67108864)));
        tabHost.setOnTabChangedListener(this);
        TextView textView = (TextView) tabHost.getTabWidget().getChildAt(0).findViewById(android.R.id.title);
        TextView textView2 = (TextView) tabHost.getTabWidget().getChildAt(1).findViewById(android.R.id.title);
        TextView textView3 = (TextView) tabHost.getTabWidget().getChildAt(2).findViewById(android.R.id.title);
        textView.setTextSize(1, 12.0f);
        textView2.setTextSize(1, 12.0f);
        textView3.setTextSize(1, 12.0f);
    }

    private int checkLgPhone() {
        int i;
        if (isSoftkeyYn(this)) {
            i = 1;
            Log.i("HAN3", "LGphone");
        } else {
            i = 0;
            Log.i("HAN3", "not LGphone");
        }
        Log.i("HAN3", "notlg = " + i);
        return i;
    }

    public static boolean isSoftkeyYn(Context context2) {
        if (Build.VERSION.SDK_INT < 14) {
            return false;
        }
        boolean hasPermanentMenuKey = ViewConfiguration.get(context2).hasPermanentMenuKey();
        boolean deviceHasKey = KeyCharacterMap.deviceHasKey(4);
        return Build.MANUFACTURER.equalsIgnoreCase("LGE") ? (hasPermanentMenuKey || deviceHasKey) ? false : true : (hasPermanentMenuKey && deviceHasKey) ? false : true;
    }

    public boolean hasSoftKeys() {
        if (Build.VERSION.SDK_INT < 17) {
            return (ViewConfiguration.get(this).hasPermanentMenuKey() || KeyCharacterMap.deviceHasKey(4)) ? false : true;
        }
        Display defaultDisplay = getWindowManager().getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        defaultDisplay.getRealMetrics(displayMetrics);
        int i = displayMetrics.heightPixels;
        int i2 = displayMetrics.widthPixels;
        DisplayMetrics displayMetrics2 = new DisplayMetrics();
        defaultDisplay.getMetrics(displayMetrics2);
        return i2 - displayMetrics2.widthPixels > 0 || i - displayMetrics2.heightPixels > 0;
    }

    class splashhandler implements Runnable {
        splashhandler() {
        }

        @Override // java.lang.Runnable
        public void run() {
            MainActivity.tabHost.getTabWidget().getChildTabViewAt(0).setEnabled(true);
            MainActivity.tabHost.getTabWidget().getChildTabViewAt(0).setClickable(true);
            MainActivity.tabHost.getTabWidget().getChildTabViewAt(1).setEnabled(true);
            MainActivity.tabHost.getTabWidget().getChildTabViewAt(1).setClickable(true);
            MainActivity.tabHost.getTabWidget().getChildTabViewAt(2).setEnabled(true);
            MainActivity.tabHost.getTabWidget().getChildTabViewAt(2).setClickable(true);
        }
    }

    @Override // android.widget.TabHost.OnTabChangeListener
    public void onTabChanged(String str) {
        Log.e("HAN3", "여기 타니 = " + str);
        String string = getResources().getString(R.string.tab_live_stream);
        String string2 = getResources().getString(R.string.tab_file_list);
        String string3 = getResources().getString(R.string.tab_setting);
        if (str.equals(string)) {
            tabHost.getTabWidget().getChildTabViewAt(1).setEnabled(false);
            tabHost.getTabWidget().getChildTabViewAt(1).setClickable(false);
            tabHost.getTabWidget().getChildTabViewAt(2).setEnabled(false);
            tabHost.getTabWidget().getChildTabViewAt(2).setClickable(false);
            new Handler().postDelayed(new splashhandler(), 1000L);
            return;
        }
        if (str.equals(string2)) {
            tabHost.getTabWidget().getChildTabViewAt(0).setEnabled(false);
            tabHost.getTabWidget().getChildTabViewAt(0).setClickable(false);
            tabHost.getTabWidget().getChildTabViewAt(2).setEnabled(false);
            tabHost.getTabWidget().getChildTabViewAt(2).setClickable(false);
            new Handler().postDelayed(new splashhandler(), 1000L);
            return;
        }
        if (str.equals(string3)) {
            tabHost.getTabWidget().getChildTabViewAt(0).setEnabled(false);
            tabHost.getTabWidget().getChildTabViewAt(0).setClickable(false);
            tabHost.getTabWidget().getChildTabViewAt(1).setEnabled(false);
            tabHost.getTabWidget().getChildTabViewAt(1).setClickable(false);
            new Handler().postDelayed(new splashhandler(), 1000L);
        }
    }

    @Override // android.app.Activity
    protected void onStart() {
        super.onStart();
    }

    @Override // android.app.Activity
    protected void onRestart() {
        MainProtocol.OnStopFlag = true;
        super.onRestart();
    }

    @Override // android.app.ActivityGroup, android.app.Activity
    protected void onResume() {
        super.onResume();
    }

    @Override // android.app.ActivityGroup, android.app.Activity
    protected void onPause() {
        super.onPause();
    }

    @Override // android.app.ActivityGroup, android.app.Activity
    protected void onStop() {
        Log.e("HAN5", "#### " + getClass().getName() + "::onStop ####");
        if (MainProtocol.OnStopFlag.booleanValue()) {
            Log.e("HAN5", "죽자");
            MainProtocol.CNSSimpleIndex cNSSimpleIndex = new MainProtocol.CNSSimpleIndex();
            cNSSimpleIndex.setIndex(0);
            MainProtocol.send(MainProtocol.NAT_CMD_NET_CONNECT_STATE3, cNSSimpleIndex.getBytes(), -1);
            try {
                MainProtocol mainProtocol = this.mprotocol;
                if (MainProtocol.mSock != null) {
                    MainProtocol mainProtocol2 = this.mprotocol;
                    MainProtocol.mSock.close();
                    MainProtocol mainProtocol3 = this.mprotocol;
                    MainProtocol.m_out_stream.close();
                    MainProtocol mainProtocol4 = this.mprotocol;
                    MainProtocol.m_in_stream.close();
                }
                Log.d("HAN5", "MainProtocol.WIFI_SSID = " + MainProtocol.WIFI_SSID);
            } catch (IOException e) {
                e.printStackTrace();
            }
            SystemClock.sleep(100L);
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService("wifi");
            if (wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(false);
            }
            if (MainFileListView2.mShareValue_I != 1 && MainFileListView3.mShareValue_E != 1 && MainFileListView4.mShareValue_P != 1) {
                Logger.e("mShareValue 99999_I" + MainFileListView2.mShareValue_I);
                Logger.e("mShareValue 99999_E" + MainFileListView3.mShareValue_E);
                Logger.e("mShareValue 99999_P" + MainFileListView4.mShareValue_P);
                Process.killProcess(Process.myPid());
            } else {
                new Handler().postDelayed(new Runnable() { // from class: com.logon_e100.MainActivity.1
                    @Override // java.lang.Runnable
                    public void run() {
                        Logger.e("mShareValue 101_I" + MainFileListView2.mShareValue_I);
                        Logger.e("mShareValue 101_E" + MainFileListView3.mShareValue_E);
                        Logger.e("mShareValue 101_P" + MainFileListView4.mShareValue_P);
                        Process.killProcess(Process.myPid());
                    }
                }, 1000L);
                finish();
            }
        } else {
            Log.e("HAN5", "죽지말자");
        }
        super.onStop();
    }

    @Override // android.app.ActivityGroup, android.app.Activity
    protected void onDestroy() {
        super.onDestroy();
    }
}
