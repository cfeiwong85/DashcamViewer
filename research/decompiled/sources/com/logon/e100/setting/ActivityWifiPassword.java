package com.logon.e100.setting;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.logon_e100.MainActivity;
import com.logon_e100.MainProtocol;
import com.logon_e100.R;
import com.logon_e100.db.WiFiDBHelper;
import java.io.IOException;

/* loaded from: classes.dex */
public class ActivityWifiPassword extends Activity {
    private static final int MAX_STRING_LENGTH = 8;
    private static final int MIN_STRING_LENGTH = 8;
    static final Handler displayAlertHandler = new Handler() { // from class: com.logon.e100.setting.ActivityWifiPassword.3
        @Override // android.os.Handler
        public void handleMessage(Message message) {
            String[] strArr = (String[]) message.obj;
            ActivityWifiPassword.displayInfoAlert(strArr[0], strArr[1]);
        }
    };
    static final Handler editTextUpdateHandler = new Handler() { // from class: com.logon.e100.setting.ActivityWifiPassword.4
        @Override // android.os.Handler
        public void handleMessage(Message message) {
            ActivityWifiPassword.mEditTextPW.setText("");
            ActivityWifiPassword.mEditTextPWConfirm.setText("");
        }
    };
    static final Handler handler = new Handler() { // from class: com.logon.e100.setting.ActivityWifiPassword.5
        @Override // android.os.Handler
        public void handleMessage(Message message) {
            ProgressDialog unused = ActivityWifiPassword.mProgressDialog = ProgressDialog.show(ActivityWifiPassword.mContext, "", ActivityWifiPassword.mContext.getResources().getString(R.string.setting_wifi_password_conplete2), true);
            new Handler().postDelayed(new Runnable() { // from class: com.logon.e100.setting.ActivityWifiPassword.5.1
                @Override // java.lang.Runnable
                public void run() {
                    MainProtocol.CNSSimpleIndex cNSSimpleIndex = new MainProtocol.CNSSimpleIndex();
                    cNSSimpleIndex.setIndex(0);
                    MainProtocol.send(MainProtocol.NAT_CMD_NET_CONNECT_STATE3, cNSSimpleIndex.getBytes(), -1);
                    ActivityWifiPassword.mContext.startActivity(new Intent(ActivityWifiPassword.mContext, (Class<?>) MainActivity.class));
                    ((ActivitySetting) ActivitySetting.mContext).finish();
                    if (MainProtocol.mSock != null) {
                        try {
                            MainProtocol.mSock.close();
                            MainProtocol.m_out_stream.close();
                            MainProtocol.m_in_stream.close();
                            Log.e("HAN", "소켓 클로즈");
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.e("HAN", "소켓 클로즈 = " + e);
                        }
                    }
                    Process.killProcess(Process.myPid());
                }
            }, 10000L);
        }
    };
    private static Context mContext = null;
    private static EditText mEditTextPW = null;
    private static EditText mEditTextPWConfirm = null;
    private static ProgressDialog mProgressDialog = null;
    private static String mSendPW = "";
    private static String mWiFi_Ssid = "";
    private static SQLiteDatabase m_db;
    private static WiFiDBHelper m_db_helper;
    private Button mButtonCancel;
    private Button mButtonConfirm;
    View.OnClickListener mClickListener = new View.OnClickListener() { // from class: com.logon.e100.setting.ActivityWifiPassword.1
        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.button_wifi_password_cancel /* 2131230760 */:
                    Log.d("mhhan", "#### " + getClass().getName() + "::onClick    cancel clicked!! ####");
                    ActivityWifiPassword.this.finish();
                    break;
                case R.id.button_wifi_password_confirm /* 2131230761 */:
                    Log.d("mhhan", "#### " + getClass().getName() + "::onClick    confirm clicked!! ####");
                    if (ActivityWifiPassword.this.checkPassword() >= 0) {
                        MainProtocol.CNSWiFiSetting cNSWiFiSetting = new MainProtocol.CNSWiFiSetting();
                        if (cNSWiFiSetting.setNewPwd(ActivityWifiPassword.mSendPW.getBytes()) < 0) {
                            ActivityWifiPassword.displayInfoAlert(ActivityWifiPassword.this.getResources().getString(R.string.setting_wifi_password_title), ActivityWifiPassword.this.getResources().getString(R.string.setting_wifi_password_error));
                        }
                        if (MainProtocol.send(MainProtocol.NAT_CMD_SET_WIFI_PWD, cNSWiFiSetting.getBytes(), 4) < 0) {
                            ActivityWifiPassword.displayInfoAlert(ActivityWifiPassword.this.getResources().getString(R.string.setting_wifi_password_title), ActivityWifiPassword.this.getResources().getString(R.string.setting_wifi_password_error));
                            break;
                        }
                    }
                    break;
                default:
                    Log.d("mhhan", "what????????");
                    break;
            }
        }
    };
    private Handler mHandler;

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setTheme(android.R.style.Theme.Holo.Light.NoActionBar.TranslucentDecor);
        setContentView(R.layout.activity_wifi_password);
        mContext = this;
        mEditTextPW = (EditText) findViewById(R.id.edittext_wifi_password);
        mEditTextPWConfirm = (EditText) findViewById(R.id.edittext_wifi_password_confirm);
        this.mButtonCancel = (Button) findViewById(R.id.button_wifi_password_cancel);
        this.mButtonConfirm = (Button) findViewById(R.id.button_wifi_password_confirm);
        this.mButtonCancel.setOnClickListener(this.mClickListener);
        this.mButtonConfirm.setOnClickListener(this.mClickListener);
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService("connectivity");
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService("wifi");
        connectivityManager.getActiveNetworkInfo();
        new String();
        String ssid = wifiManager.getConnectionInfo().getSSID();
        if (ssid.contains("\"")) {
            mWiFi_Ssid = ssid.substring(1, ssid.length() - 1);
        }
        Log.d("HAN5", "m_strName = " + mWiFi_Ssid);
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

    public int checkPassword() {
        String obj = mEditTextPW.getText().toString();
        String obj2 = mEditTextPWConfirm.getText().toString();
        if (obj == null || obj.equals("") || obj.length() <= 0) {
            Log.d("mhhan", "#### " + getClass().getName() + "::checkPassword    pw is null!! ####");
            displayInfoAlert(getResources().getString(R.string.setting_wifi_password_title), getResources().getString(R.string.setting_wifi_password_empty));
            return -1;
        }
        if (obj2 == null || obj2.equals("") || obj2.length() <= 0) {
            Log.d("mhhan", "#### " + getClass().getName() + "::checkPassword    pw confirm is null!! ####");
            displayInfoAlert(getResources().getString(R.string.setting_wifi_password_title), getResources().getString(R.string.setting_wifi_password_confirm_empty));
            return -1;
        }
        if (obj.length() < 8 || obj.length() > 8) {
            displayInfoAlert(getResources().getString(R.string.setting_wifi_password_title), getResources().getString(R.string.setting_wifi_password_length));
            return -1;
        }
        if (obj2.length() < 8 || obj2.length() > 8) {
            displayInfoAlert(getResources().getString(R.string.setting_wifi_password_title), getResources().getString(R.string.setting_wifi_password_confirm_length));
            return -1;
        }
        if (spaceCheck(obj)) {
            displayInfoAlert(getResources().getString(R.string.setting_wifi_password_title), getResources().getString(R.string.setting_wifi_password_space));
            return -1;
        }
        if (spaceCheck(obj2)) {
            displayInfoAlert(getResources().getString(R.string.setting_wifi_password_title), getResources().getString(R.string.setting_wifi_password_confirm_space));
            return -1;
        }
        if (!obj.equals(obj2)) {
            displayInfoAlert(getResources().getString(R.string.setting_wifi_password_title), getResources().getString(R.string.setting_wifi_password_not_match));
            return -1;
        }
        mSendPW = obj;
        return 0;
    }

    public boolean spaceCheck(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == ' ') {
                return true;
            }
        }
        return false;
    }

    public static void displayInfoAlert(String str, String str2) {
        String string = mContext.getResources().getString(R.string.action_confirm);
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(str2).setCancelable(false).setPositiveButton(string, new DialogInterface.OnClickListener() { // from class: com.logon.e100.setting.ActivityWifiPassword.2
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        AlertDialog create = builder.create();
        create.setTitle(str);
        create.setIcon(R.drawable.ic_launcher);
        create.show();
    }

    public static void onReceive(int i, byte[] bArr) {
        Log.d("mhhan", "#### onReceive cmd = " + i + " ####");
        if (i == 41009) {
            if (new MainProtocol.CNSResult(bArr).getResult() < 0) {
                String[] strArr = {mContext.getResources().getString(R.string.setting_wifi_password_title), mContext.getResources().getString(R.string.setting_wifi_password_error)};
                Message obtainMessage = displayAlertHandler.obtainMessage();
                obtainMessage.obj = strArr;
                displayAlertHandler.sendMessage(obtainMessage);
                return;
            }
            m_db_helper = new WiFiDBHelper(mContext);
            m_db = m_db_helper.getWritableDatabase();
            Log.d("TCP", "db 삭제");
            m_db = m_db_helper.getWritableDatabase();
            m_db.delete("wifilist", "ssid=?", new String[]{mWiFi_Ssid});
            m_db.execSQL("insert into wifilist(ssid, password) values('" + mWiFi_Ssid + "', '" + mSendPW + "');");
            m_db.close();
            handler.sendMessage(handler.obtainMessage());
            return;
        }
        Log.d("mhhan", "what????????");
    }
}
