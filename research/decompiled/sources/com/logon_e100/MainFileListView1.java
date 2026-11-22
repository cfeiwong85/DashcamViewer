package com.logon_e100;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TextView;

/* loaded from: classes.dex */
public class MainFileListView1 extends TabActivity implements TabHost.OnTabChangeListener {
    @Override // android.widget.TabHost.OnTabChangeListener
    public void onTabChanged(String str) {
    }

    @Override // android.app.ActivityGroup, android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setTheme(android.R.style.Theme.Holo.Light.NoActionBar.TranslucentDecor);
        setContentView(R.layout.activity_filelistview1);
        Resources resources = getResources();
        TabHost tabHost = getTabHost();
        Intent intent = new Intent().setClass(this, MainFileListView2.class);
        String string = getResources().getString(R.string.tab_normal);
        String string2 = getResources().getString(R.string.tab_event);
        String string3 = getResources().getString(R.string.tab_park);
        String string4 = getResources().getString(R.string.tab_photo);
        TabHost.TabSpec newTabSpec = tabHost.newTabSpec(string);
        newTabSpec.setIndicator(string, resources.getDrawable(android.R.drawable.ic_menu_agenda));
        newTabSpec.setContent(intent.addFlags(67108864));
        tabHost.addTab(newTabSpec);
        tabHost.addTab(tabHost.newTabSpec(string2).setIndicator(string2, resources.getDrawable(android.R.drawable.ic_menu_edit)).setContent(new Intent().setClass(this, MainFileListView3.class).addFlags(67108864)));
        tabHost.addTab(tabHost.newTabSpec(string3).setIndicator(string3, resources.getDrawable(android.R.drawable.ic_menu_edit)).setContent(new Intent().setClass(this, MainFileListView4.class).addFlags(67108864)));
        tabHost.addTab(tabHost.newTabSpec(string4).setIndicator(string4, resources.getDrawable(android.R.drawable.ic_menu_edit)).setContent(new Intent().setClass(this, MainFileListView5.class).addFlags(67108864)));
        TextView textView = (TextView) tabHost.getTabWidget().getChildAt(0).findViewById(android.R.id.title);
        TextView textView2 = (TextView) tabHost.getTabWidget().getChildAt(1).findViewById(android.R.id.title);
        TextView textView3 = (TextView) tabHost.getTabWidget().getChildAt(2).findViewById(android.R.id.title);
        TextView textView4 = (TextView) tabHost.getTabWidget().getChildAt(3).findViewById(android.R.id.title);
        textView.setTextSize(14.0f);
        textView2.setTextSize(14.0f);
        textView3.setTextSize(14.0f);
        textView4.setTextSize(14.0f);
    }
}
