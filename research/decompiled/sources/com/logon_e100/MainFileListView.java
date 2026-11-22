package com.logon_e100;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TabHost;
import android.widget.TextView;

/* loaded from: classes.dex */
public class MainFileListView extends TabActivity implements TabHost.OnTabChangeListener {
    public static Button btn_descend;
    public static Context mContext2;
    private String country;
    int button_click_flag2 = 0;
    int button_click_flag3 = 0;
    int button_click_flag4 = 0;
    int button_click_flag5 = 0;
    int up_down_flag = 0;

    @Override // android.app.ActivityGroup, android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_filelistview);
        Resources resources = getResources();
        TabHost tabHost = getTabHost();
        Intent intent = new Intent().setClass(this, MainFileListView1.class);
        String string = resources.getString(R.string.tab_download_file);
        String string2 = resources.getString(R.string.tab_download_blackbox);
        TabHost.TabSpec newTabSpec = tabHost.newTabSpec(string);
        newTabSpec.setIndicator(string, resources.getDrawable(android.R.drawable.ic_menu_agenda));
        newTabSpec.setContent(intent);
        mContext2 = this;
        tabHost.addTab(newTabSpec);
        tabHost.addTab(tabHost.newTabSpec(string2).setIndicator(string2, resources.getDrawable(android.R.drawable.ic_menu_edit)).setContent(new Intent().setClass(this, MainFileListView_blackbox1.class)));
        TextView textView = (TextView) tabHost.getTabWidget().getChildAt(0).findViewById(android.R.id.title);
        TextView textView2 = (TextView) tabHost.getTabWidget().getChildAt(1).findViewById(android.R.id.title);
        textView.setTextSize(14.0f);
        textView2.setTextSize(14.0f);
        int i = ((WifiManager) getApplicationContext().getSystemService("wifi")).getDhcpInfo().gateway;
        String format = String.format("%d.%d.%d.%d", Integer.valueOf(i & 255), Integer.valueOf((i >> 8) & 255), Integer.valueOf((i >> 16) & 255), Integer.valueOf((i >> 24) & 255));
        Log.e("HAN2", "ipAddress =" + format);
        tabHost.setOnTabChangedListener(this);
        if (format.equals(MainProtocol.SERVERIP)) {
            tabHost.getTabWidget().getChildTabViewAt(0).setEnabled(true);
            tabHost.getTabWidget().getChildTabViewAt(1).setClickable(true);
        } else {
            tabHost.getTabWidget().getChildTabViewAt(0).setEnabled(false);
            tabHost.getTabWidget().getChildTabViewAt(1).setClickable(false);
            ((TextView) tabHost.getTabWidget().getChildAt(1).findViewById(android.R.id.title)).setTextColor(Color.parseColor("#999999"));
        }
        btn_descend = (Button) findViewById(R.id.btn_descend);
        btn_descend.setVisibility(8);
        btn_descend.setOnClickListener(new View.OnClickListener() { // from class: com.logon_e100.MainFileListView.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                MainFileListView.this.country = MainFileListView.this.getResources().getConfiguration().locale.getCountry();
                SharedPreferences sharedPreferences = MainFileListView.this.getSharedPreferences("save_edit", 0);
                if (sharedPreferences != null) {
                    MainFileListView.this.up_down_flag = sharedPreferences.getInt("save_edit", 0);
                    Log.e("HAN7", "up_down_flag =" + MainFileListView.this.up_down_flag);
                }
                new MainFileListView_blackbox1();
                Log.e("HAN3", "blackbox1.flag_black =" + MainFileListView_blackbox1.flag_black);
                if (MainFileListView_blackbox1.flag_black == 2) {
                    MainFileListView_blackbox2 mainFileListView_blackbox2 = new MainFileListView_blackbox2();
                    Log.e("HAN3", "button_click_flag2 =" + MainFileListView.this.button_click_flag2);
                    if (MainFileListView.this.up_down_flag == 0) {
                        mainFileListView_blackbox2.FileList(1);
                        MainFileListView mainFileListView = MainFileListView.this;
                        mainFileListView.up_down_flag = 1;
                        if (mainFileListView.country.equals("KR")) {
                            MainFileListView.btn_descend.setBackgroundResource(R.drawable.updown_down);
                        } else {
                            MainFileListView.btn_descend.setBackgroundResource(R.drawable.updown_down_eng);
                        }
                        if (MainFileListView.this.country.equals("HK") || MainFileListView.this.country.equals("TW") || MainFileListView.this.country.equals("CN")) {
                            MainFileListView.btn_descend.setBackgroundResource(R.drawable.updown_down_chn);
                        }
                        Log.e("HAN7", "2222222222button_click_flag2 =" + MainFileListView.this.button_click_flag2);
                        SharedPreferences sharedPreferences2 = MainFileListView.this.getSharedPreferences("save_edit", 0);
                        if (sharedPreferences2 != null) {
                            SharedPreferences.Editor edit = sharedPreferences2.edit();
                            edit.putInt("save_edit", MainFileListView.this.up_down_flag);
                            edit.commit();
                        }
                    } else {
                        MainFileListView.this.up_down_flag = 0;
                        mainFileListView_blackbox2.FileList(0);
                        Log.e("HAN7", "33333333333button_click_flag2 =" + MainFileListView.this.button_click_flag2);
                        if (MainFileListView.this.country.equals("KR")) {
                            MainFileListView.btn_descend.setBackgroundResource(R.drawable.updown_up);
                        } else {
                            MainFileListView.btn_descend.setBackgroundResource(R.drawable.updown_up_eng);
                        }
                        if (MainFileListView.this.country.equals("HK") || MainFileListView.this.country.equals("TW") || MainFileListView.this.country.equals("CN")) {
                            MainFileListView.btn_descend.setBackgroundResource(R.drawable.updown_up_chn);
                        }
                        SharedPreferences sharedPreferences3 = MainFileListView.this.getSharedPreferences("save_edit", 0);
                        if (sharedPreferences3 != null) {
                            SharedPreferences.Editor edit2 = sharedPreferences3.edit();
                            edit2.putInt("save_edit", MainFileListView.this.up_down_flag);
                            edit2.commit();
                        }
                    }
                }
                if (MainFileListView_blackbox1.flag_black == 3) {
                    MainFileListView_blackbox3 mainFileListView_blackbox3 = new MainFileListView_blackbox3();
                    Log.e("HAN3", "button_click_flag3 =" + MainFileListView.this.button_click_flag3);
                    if (MainFileListView.this.up_down_flag == 0) {
                        mainFileListView_blackbox3.FileList(1);
                        MainFileListView mainFileListView2 = MainFileListView.this;
                        mainFileListView2.up_down_flag = 1;
                        if (mainFileListView2.country.equals("KR")) {
                            MainFileListView.btn_descend.setBackgroundResource(R.drawable.updown_down);
                        } else {
                            MainFileListView.btn_descend.setBackgroundResource(R.drawable.updown_down_eng);
                        }
                        if (MainFileListView.this.country.equals("HK") || MainFileListView.this.country.equals("TW") || MainFileListView.this.country.equals("CN")) {
                            MainFileListView.btn_descend.setBackgroundResource(R.drawable.updown_down_chn);
                        }
                        SharedPreferences sharedPreferences4 = MainFileListView.this.getSharedPreferences("save_edit", 0);
                        if (sharedPreferences4 != null) {
                            SharedPreferences.Editor edit3 = sharedPreferences4.edit();
                            edit3.putInt("save_edit", MainFileListView.this.up_down_flag);
                            edit3.commit();
                        }
                    } else {
                        MainFileListView.this.up_down_flag = 0;
                        mainFileListView_blackbox3.FileList(0);
                        if (MainFileListView.this.country.equals("KR")) {
                            MainFileListView.btn_descend.setBackgroundResource(R.drawable.updown_up);
                        } else {
                            MainFileListView.btn_descend.setBackgroundResource(R.drawable.updown_up_eng);
                        }
                        if (MainFileListView.this.country.equals("HK") || MainFileListView.this.country.equals("TW") || MainFileListView.this.country.equals("CN")) {
                            MainFileListView.btn_descend.setBackgroundResource(R.drawable.updown_up_chn);
                        }
                        SharedPreferences sharedPreferences5 = MainFileListView.this.getSharedPreferences("save_edit", 0);
                        if (sharedPreferences5 != null) {
                            SharedPreferences.Editor edit4 = sharedPreferences5.edit();
                            edit4.putInt("save_edit", MainFileListView.this.up_down_flag);
                            edit4.commit();
                        }
                    }
                }
                if (MainFileListView_blackbox1.flag_black == 4) {
                    MainFileListView_blackbox4 mainFileListView_blackbox4 = new MainFileListView_blackbox4();
                    Log.e("HAN3", "button_click_flag4 =" + MainFileListView.this.button_click_flag4);
                    if (MainFileListView.this.up_down_flag == 0) {
                        mainFileListView_blackbox4.FileList(1);
                        MainFileListView mainFileListView3 = MainFileListView.this;
                        mainFileListView3.up_down_flag = 1;
                        if (mainFileListView3.country.equals("KR")) {
                            MainFileListView.btn_descend.setBackgroundResource(R.drawable.updown_down);
                        } else {
                            MainFileListView.btn_descend.setBackgroundResource(R.drawable.updown_down_eng);
                        }
                        if (MainFileListView.this.country.equals("HK") || MainFileListView.this.country.equals("TW") || MainFileListView.this.country.equals("CN")) {
                            MainFileListView.btn_descend.setBackgroundResource(R.drawable.updown_down_chn);
                        }
                        SharedPreferences sharedPreferences6 = MainFileListView.this.getSharedPreferences("save_edit", 0);
                        if (sharedPreferences6 != null) {
                            SharedPreferences.Editor edit5 = sharedPreferences6.edit();
                            edit5.putInt("save_edit", MainFileListView.this.up_down_flag);
                            edit5.commit();
                        }
                    } else {
                        MainFileListView.this.up_down_flag = 0;
                        mainFileListView_blackbox4.FileList(0);
                        if (MainFileListView.this.country.equals("KR")) {
                            MainFileListView.btn_descend.setBackgroundResource(R.drawable.updown_up);
                        } else {
                            MainFileListView.btn_descend.setBackgroundResource(R.drawable.updown_up_eng);
                        }
                        if (MainFileListView.this.country.equals("HK") || MainFileListView.this.country.equals("TW") || MainFileListView.this.country.equals("CN")) {
                            MainFileListView.btn_descend.setBackgroundResource(R.drawable.updown_up_chn);
                        }
                        SharedPreferences sharedPreferences7 = MainFileListView.this.getSharedPreferences("save_edit", 0);
                        if (sharedPreferences7 != null) {
                            SharedPreferences.Editor edit6 = sharedPreferences7.edit();
                            edit6.putInt("save_edit", MainFileListView.this.up_down_flag);
                            edit6.commit();
                        }
                    }
                }
                if (MainFileListView_blackbox1.flag_black == 5) {
                    MainFileListView_blackbox5 mainFileListView_blackbox5 = new MainFileListView_blackbox5();
                    Log.e("HAN3", "button_click_flag5 =" + MainFileListView.this.button_click_flag5);
                    if (MainFileListView.this.up_down_flag == 0) {
                        mainFileListView_blackbox5.FileList(1);
                        MainFileListView mainFileListView4 = MainFileListView.this;
                        mainFileListView4.up_down_flag = 1;
                        if (mainFileListView4.country.equals("KR")) {
                            MainFileListView.btn_descend.setBackgroundResource(R.drawable.updown_down);
                        } else {
                            MainFileListView.btn_descend.setBackgroundResource(R.drawable.updown_down_eng);
                        }
                        if (MainFileListView.this.country.equals("HK") || MainFileListView.this.country.equals("TW") || MainFileListView.this.country.equals("CN")) {
                            MainFileListView.btn_descend.setBackgroundResource(R.drawable.updown_down_chn);
                        }
                        SharedPreferences sharedPreferences8 = MainFileListView.this.getSharedPreferences("save_edit", 0);
                        if (sharedPreferences8 != null) {
                            SharedPreferences.Editor edit7 = sharedPreferences8.edit();
                            edit7.putInt("save_edit", MainFileListView.this.up_down_flag);
                            edit7.commit();
                            return;
                        }
                        return;
                    }
                    MainFileListView.this.up_down_flag = 0;
                    mainFileListView_blackbox5.FileList(0);
                    if (MainFileListView.this.country.equals("KR")) {
                        MainFileListView.btn_descend.setBackgroundResource(R.drawable.updown_up);
                    } else {
                        MainFileListView.btn_descend.setBackgroundResource(R.drawable.updown_up_eng);
                    }
                    if (MainFileListView.this.country.equals("HK") || MainFileListView.this.country.equals("TW") || MainFileListView.this.country.equals("CN")) {
                        MainFileListView.btn_descend.setBackgroundResource(R.drawable.updown_up_chn);
                    }
                    SharedPreferences sharedPreferences9 = MainFileListView.this.getSharedPreferences("save_edit", 0);
                    if (sharedPreferences9 != null) {
                        SharedPreferences.Editor edit8 = sharedPreferences9.edit();
                        edit8.putInt("save_edit", MainFileListView.this.up_down_flag);
                        edit8.commit();
                    }
                }
            }
        });
    }

    @Override // android.widget.TabHost.OnTabChangeListener
    public void onTabChanged(String str) {
        if (str.equals(getResources().getString(R.string.tab_download_blackbox))) {
            Log.e("HAN3", "여기 타니 = " + str);
            ((Button) findViewById(R.id.btn_descend)).setVisibility(0);
            return;
        }
        Log.e("HAN3", "다운로드파일 리스트버튼 클릭");
        btn_descend = (Button) findViewById(R.id.btn_descend);
        btn_descend.setVisibility(8);
    }
}
