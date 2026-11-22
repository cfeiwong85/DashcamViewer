package com.logon_e100;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TabHost;
import android.widget.TextView;

/* loaded from: classes.dex */
public class MainFileListView_blackbox1 extends TabActivity implements TabHost.OnTabChangeListener {
    public static int flag_black = 2;
    public static int flag_image_button;
    TabHost tabHost;

    @Override // android.app.ActivityGroup, android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setTheme(android.R.style.Theme.Holo.Light.NoActionBar.TranslucentDecor);
        setContentView(R.layout.activity_filelistviewblackbox1);
        Resources resources = getResources();
        this.tabHost = getTabHost();
        Intent intent = new Intent().setClass(this, MainFileListView_blackbox2.class);
        String string = getResources().getString(R.string.tab_normal);
        String string2 = getResources().getString(R.string.tab_event);
        String string3 = getResources().getString(R.string.tab_park);
        String string4 = getResources().getString(R.string.tab_photo);
        TabHost.TabSpec newTabSpec = this.tabHost.newTabSpec(string);
        newTabSpec.setIndicator(string, resources.getDrawable(android.R.drawable.ic_menu_agenda));
        newTabSpec.setContent(intent.addFlags(67108864));
        this.tabHost.addTab(newTabSpec);
        Log.e("HAN3", "여기 타니?");
        this.tabHost.addTab(this.tabHost.newTabSpec(string2).setIndicator(string2, resources.getDrawable(android.R.drawable.ic_menu_edit)).setContent(new Intent().setClass(this, MainFileListView_blackbox3.class).addFlags(67108864)));
        this.tabHost.addTab(this.tabHost.newTabSpec(string3).setIndicator(string3, resources.getDrawable(android.R.drawable.ic_menu_edit)).setContent(new Intent().setClass(this, MainFileListView_blackbox4.class).addFlags(67108864)));
        this.tabHost.addTab(this.tabHost.newTabSpec(string4).setIndicator(string4, resources.getDrawable(android.R.drawable.ic_menu_edit)).setContent(new Intent().setClass(this, MainFileListView_blackbox5.class).addFlags(67108864)));
        this.tabHost.setOnTabChangedListener(this);
        TextView textView = (TextView) this.tabHost.getTabWidget().getChildAt(0).findViewById(android.R.id.title);
        TextView textView2 = (TextView) this.tabHost.getTabWidget().getChildAt(1).findViewById(android.R.id.title);
        TextView textView3 = (TextView) this.tabHost.getTabWidget().getChildAt(2).findViewById(android.R.id.title);
        TextView textView4 = (TextView) this.tabHost.getTabWidget().getChildAt(3).findViewById(android.R.id.title);
        textView.setTextSize(14.0f);
        textView2.setTextSize(14.0f);
        textView3.setTextSize(14.0f);
        textView4.setTextSize(14.0f);
    }

    class splashhandler implements Runnable {
        splashhandler() {
        }

        @Override // java.lang.Runnable
        public void run() {
            MainFileListView_blackbox1.this.tabHost.getTabWidget().getChildTabViewAt(1).setEnabled(true);
            MainFileListView_blackbox1.this.tabHost.getTabWidget().getChildTabViewAt(1).setClickable(true);
            MainFileListView_blackbox1.this.tabHost.getTabWidget().getChildTabViewAt(2).setEnabled(true);
            MainFileListView_blackbox1.this.tabHost.getTabWidget().getChildTabViewAt(2).setClickable(true);
            MainFileListView_blackbox1.this.tabHost.getTabWidget().getChildTabViewAt(3).setEnabled(true);
            MainFileListView_blackbox1.this.tabHost.getTabWidget().getChildTabViewAt(3).setClickable(true);
            MainFileListView_blackbox1.this.tabHost.getTabWidget().getChildTabViewAt(0).setEnabled(true);
            MainFileListView_blackbox1.this.tabHost.getTabWidget().getChildTabViewAt(0).setClickable(true);
        }
    }

    @Override // android.widget.TabHost.OnTabChangeListener
    public void onTabChanged(String str) {
        Log.e("HAN3", "여기 타니 = " + str);
        String string = getResources().getString(R.string.tab_normal);
        String string2 = getResources().getString(R.string.tab_event);
        String string3 = getResources().getString(R.string.tab_park);
        String string4 = getResources().getString(R.string.tab_photo);
        flag_image_button = 1;
        if (str.equals(string)) {
            this.tabHost.getTabWidget().getChildTabViewAt(1).setEnabled(false);
            this.tabHost.getTabWidget().getChildTabViewAt(1).setClickable(false);
            this.tabHost.getTabWidget().getChildTabViewAt(2).setEnabled(false);
            this.tabHost.getTabWidget().getChildTabViewAt(2).setClickable(false);
            this.tabHost.getTabWidget().getChildTabViewAt(3).setEnabled(false);
            this.tabHost.getTabWidget().getChildTabViewAt(3).setClickable(false);
            new Handler().postDelayed(new splashhandler(), 1000L);
            flag_black = 2;
            return;
        }
        if (str.equals(string2)) {
            this.tabHost.getTabWidget().getChildTabViewAt(0).setEnabled(false);
            this.tabHost.getTabWidget().getChildTabViewAt(0).setClickable(false);
            this.tabHost.getTabWidget().getChildTabViewAt(2).setEnabled(false);
            this.tabHost.getTabWidget().getChildTabViewAt(2).setClickable(false);
            this.tabHost.getTabWidget().getChildTabViewAt(3).setEnabled(false);
            this.tabHost.getTabWidget().getChildTabViewAt(3).setClickable(false);
            new Handler().postDelayed(new splashhandler(), 1000L);
            flag_black = 3;
            return;
        }
        if (str.equals(string3)) {
            this.tabHost.getTabWidget().getChildTabViewAt(0).setEnabled(false);
            this.tabHost.getTabWidget().getChildTabViewAt(0).setClickable(false);
            this.tabHost.getTabWidget().getChildTabViewAt(1).setEnabled(false);
            this.tabHost.getTabWidget().getChildTabViewAt(1).setClickable(false);
            this.tabHost.getTabWidget().getChildTabViewAt(3).setEnabled(false);
            this.tabHost.getTabWidget().getChildTabViewAt(3).setClickable(false);
            new Handler().postDelayed(new splashhandler(), 1000L);
            flag_black = 4;
            return;
        }
        if (str.equals(string4)) {
            this.tabHost.getTabWidget().getChildTabViewAt(0).setEnabled(false);
            this.tabHost.getTabWidget().getChildTabViewAt(0).setClickable(false);
            this.tabHost.getTabWidget().getChildTabViewAt(1).setEnabled(false);
            this.tabHost.getTabWidget().getChildTabViewAt(1).setClickable(false);
            this.tabHost.getTabWidget().getChildTabViewAt(2).setEnabled(false);
            this.tabHost.getTabWidget().getChildTabViewAt(2).setClickable(false);
            new Handler().postDelayed(new splashhandler(), 1000L);
            flag_black = 5;
        }
    }
}
