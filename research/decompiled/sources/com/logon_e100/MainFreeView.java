package com.logon_e100;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.os.StrictMode;
import android.os.SystemClock;
import android.support.graphics.drawable.PathInterpolatorCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.logon.e100.setting.ActivityDateAndTime;
import com.logon.e100.setting.ActivityPartitionSelect;
import com.logon.e100.setting.ActivitySelect;
import com.logon.e100.setting.ActivitySetting;
import com.logon.e100.setting.ActivityWifiPassword;
import com.logon.e100.setting.CNSConfig;
import com.logon.e100.utils.Logger;
import com.logon.e100.utils.Utils;
import com.logon_e100.MainProtocol;
import com.logon_e100.intro.MainIntro;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Calendar;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import uk.co.senab.photoview.PhotoViewAttacher;

@SuppressLint({"NewApi", "DefaultLocale", "HandlerLeak", "DrawAllocation"})
@TargetApi(9)
/* loaded from: classes.dex */
public class MainFreeView extends Activity implements View.OnClickListener {
    public static Activity AActivity = null;
    static final Handler buttonReversedUpdateHandler;
    static final Handler buttonUpdateHandler;
    static final Handler displayAlertHandler;
    static final Handler displayErrorAlertHandler;
    static final Handler displayErrorSocketAlertHandler;
    static final Handler displayRenderTimeoutAlertHandler;
    public static String filechannele = null;
    public static String filename2 = null;
    public static int flag_CH = 1;
    public static int flag_all;
    public static int freeviewstop_flag;
    public static int freeviewstop_flag2;
    private static Button mButtonCamReversed;
    private static Button mButtonFullScreen;
    private static Button mButtonPhoto;
    private static Button mButtonVoiceRecord;
    private static Context mContext;
    static final Handler textViewUpdateHandler;
    static TextView tvCh;
    static WifiManager wifiManager2;
    AlertDialog.Builder alert;
    String file_stae;
    int height;
    float heightScaledRatio;
    String ipAddress;
    ImageView iv_rec;
    int mAnotherPhone;
    PhotoViewAttacher mAttacher;
    private LinearLayout mBottomLinearLayout;
    private FrameLayout mFrameLayout;
    private Handler mHandler;
    private boolean mIsTimeSaved;
    BufferedReader mReader;
    private RelativeLayout mRelativeLayout;
    private SurfaceView mSurfaceView;
    private RelativeLayout mTempRelativeLayout;
    private RelativeLayout mTopRelativeLayout;
    private boolean mVoiceRecordState;
    BufferedWriter mWriter;
    int m_photo;
    private ProgressDialog main_ProgressDialog44;
    MainProtocol mprotocol;
    int[] res;
    int[] screenRes;
    Matrix sideInversion;
    Bitmap sideInversionImg;
    StringTokenizer st;
    Thread t;
    public ImageView tmpIv;
    TCPclient tp;
    TextView tvREC;
    TextView tvSsid;
    int width;
    float widthScaledRatio;
    String mRecvData = "";
    public Bitmap backbit = null;
    byte[] readsize = new byte[1024];
    byte[] readchannle = new byte[1024];
    int data_size = 0;
    int db_ch = 0;
    boolean thread_start_flag = true;
    boolean wifi_connecterror_flag = true;
    boolean wifi_level_flag = false;
    Handler WiFiLevelUpdateHandler = null;
    DialogInterface mPopupDlg = null;
    int count = 0;
    int count_filedonw = 0;
    int camreversed_flag = 1;
    int ORIENTATION_FLAG = 1;
    int first_flag = 1;
    int first_flag2 = 0;
    private BroadcastReceiver rssiReceiver = new BroadcastReceiver() { // from class: com.logon_e100.MainFreeView.1
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            ((WifiManager) context.getApplicationContext().getSystemService("wifi")).getConnectionInfo().getRssi();
        }
    };
    Handler mReceiver = new Handler() { // from class: com.logon_e100.MainFreeView.3
        @Override // android.os.Handler
        public void handleMessage(Message message) {
            Logger.d("MainFreeView# mReceiver 리드 핸들러");
            MainFreeView.this.freeviewcon();
        }
    };
    private Thread mCheckRecv = new Thread() { // from class: com.logon_e100.MainFreeView.4
        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            while (MainProtocol.m_in_stream.read(MainProtocol.readdata, 0, 7) > 0) {
                try {
                    if (MainProtocol.readdata[0] == 1) {
                        byte[] bArr = new byte[2];
                        byte[] bArr2 = new byte[4];
                        byte[] bArr3 = new byte[2];
                        System.arraycopy(MainProtocol.readdata, 1, bArr, 0, 2);
                        System.arraycopy(MainProtocol.readdata, 3, bArr2, 0, 4);
                        int byteArrayToInt_Big = Utils.byteArrayToInt_Big(bArr);
                        int byteArrayToInt_Little = Utils.byteArrayToInt_Little(bArr2);
                        Logger.d("MainFreeView# cmd = " + byteArrayToInt_Big + ", size = " + byteArrayToInt_Little + " ####");
                        if (byteArrayToInt_Little <= 100000) {
                            if (byteArrayToInt_Little <= 0) {
                                MainProtocol.m_in_stream.read(bArr3, 0, 2);
                            } else {
                                byte[] bArr4 = new byte[byteArrayToInt_Little];
                                int length = bArr4.length;
                                int i = 0;
                                while (i < bArr4.length) {
                                    int read = MainProtocol.m_in_stream.read(bArr4, i, length);
                                    i += read;
                                    length -= read;
                                    if (i < bArr4.length) {
                                        sleep(25L);
                                    }
                                }
                                MainProtocol.m_in_stream.read(bArr3, 0, 2);
                                switch (byteArrayToInt_Big) {
                                    case 8193:
                                        MainFreeView.this.mReceiver.sendEmptyMessage(0);
                                        break;
                                    case MainProtocol.AW_IOTYPE_USER_IPCAM_FILE_TRANS_START_RESP /* 8201 */:
                                        if (MainProtocol.targetActivity != 10) {
                                            break;
                                        } else {
                                            MainFileFreeView.onReceive(byteArrayToInt_Big, bArr4);
                                            break;
                                        }
                                    case MainProtocol.NAT_CMD_SET_NORMAL_IMPACT_SENSITIVITY_RESP /* 40865 */:
                                    case MainProtocol.NAT_CMD_SET_VOICE_VOLUME_RESP /* 40867 */:
                                    case MainProtocol.NAT_CMD_SET_LCD_BRIGHTNESS_RESP /* 40869 */:
                                    case MainProtocol.NAT_CMD_SET_CONTRAST_RESP /* 40871 */:
                                    case MainProtocol.NAT_CMD_SET_SWITCH_SCREEN_RESP /* 40873 */:
                                    case MainProtocol.NAT_CMD_SET_PARKING_LOW_POWER_RESP /* 40875 */:
                                    case MainProtocol.NAT_CMD_SET_PARKING_OPTIME_RESP /* 40877 */:
                                    case MainProtocol.NAT_CMD_SET_TIMEZONE_RESP /* 40879 */:
                                    case MainProtocol.NAT_CMD_SET_VOICE_GUIDE_RESP /* 40881 */:
                                    case MainProtocol.NAT_CMD_SET_CRASH_EVENT_ALARM_RESP /* 40885 */:
                                    case MainProtocol.NAT_CMD_SET_USE_LED_RESP /* 40893 */:
                                    case MainProtocol.NAT_CMD_SET_RECORDER_QUALITY_RESP /* 40962 */:
                                    case MainProtocol.NAT_CMD_SET_WHITE_BALANCE_RESP /* 40968 */:
                                    case MainProtocol.NAT_CMD_SET_EXPOSURE_RESP /* 40970 */:
                                    case MainProtocol.NAT_CMD_SET_PARK_IMPACT_SENSITIVITY_RESP /* 40986 */:
                                        ActivitySelect.onReceive(byteArrayToInt_Big, bArr4);
                                        break;
                                    case MainProtocol.NAT_CMD_SET_PARTITION_RESP /* 40883 */:
                                        ActivityPartitionSelect.onReceive(byteArrayToInt_Big, bArr4);
                                        break;
                                    case MainProtocol.NAT_CMD_NET_CONNECT_HOT /* 40886 */:
                                        Logger.d("MainFreeView# HEART_BIT");
                                        MainProtocol.send(MainProtocol.NAT_CMD_NET_CONNECT_HOT_RESP, null, -1);
                                        break;
                                    case MainProtocol.NAT_CMD_NET_CONNECT_STOP /* 40888 */:
                                        if (MainProtocol.send(MainProtocol.NAT_CMD_NET_CONNECT_STOP_RESP, null, 0) < 0 && MainFreeView.this.main_ProgressDialog44 != null) {
                                            MainFreeView.this.main_ProgressDialog44.dismiss();
                                        }
                                        Logger.d("MainFreeView# NAT_CMD_NET_CONNECT_STOP");
                                        String[] strArr = {MainFreeView.mContext.getResources().getString(R.string.blackbox_error_title), MainFreeView.mContext.getResources().getString(R.string.blackbox_error_stop)};
                                        Message obtainMessage = MainFreeView.displayErrorAlertHandler.obtainMessage();
                                        obtainMessage.obj = strArr;
                                        MainFreeView.displayErrorAlertHandler.sendMessage(obtainMessage);
                                        MainFreeView.freeviewstop_flag = 1;
                                        break;
                                    case MainProtocol.NAT_CMD_SET_PHOTO_RESP /* 40905 */:
                                        MainFreeView.this.onReceive(byteArrayToInt_Big, bArr4);
                                        break;
                                    case MainProtocol.NAT_CMD_SET_MUTE_RESP /* 40974 */:
                                        if (MainProtocol.targetActivity == 0) {
                                            MainFreeView.this.onReceive(byteArrayToInt_Big, bArr4);
                                            break;
                                        } else if (MainProtocol.targetActivity != 3) {
                                            break;
                                        } else {
                                            ActivitySelect.onReceive(byteArrayToInt_Big, bArr4);
                                            break;
                                        }
                                    case MainProtocol.NAT_CMD_SET_TIME_RESP /* 40978 */:
                                        if (MainProtocol.targetActivity == 5) {
                                            ActivityDateAndTime.onReceive(byteArrayToInt_Big, bArr4);
                                            break;
                                        } else {
                                            Logger.d("MainFreeView# pass set time resp from preview");
                                            break;
                                        }
                                    case MainProtocol.NAT_CMD_FORMAT_TF_CARD_RESP /* 40992 */:
                                    case MainProtocol.NAT_CMD_FACTORY_RESET_RESP /* 41045 */:
                                        ActivitySetting.onReceive(byteArrayToInt_Big, bArr4);
                                        break;
                                    case 40998:
                                        MainFreeView.this.onReceive(byteArrayToInt_Big, bArr4);
                                        break;
                                    case MainProtocol.NAT_CMD_SET_WIFI_PWD /* 41009 */:
                                        ActivityWifiPassword.onReceive(byteArrayToInt_Big, bArr4);
                                        break;
                                    case MainProtocol.NAT_CMD_GET_CONFIG_RESP /* 41013 */:
                                        if (MainProtocol.targetActivity == 0) {
                                            Logger.e("activity id = preview");
                                            MainFreeView.this.onReceive(byteArrayToInt_Big, bArr4);
                                            break;
                                        } else if (MainProtocol.targetActivity != 2) {
                                            break;
                                        } else {
                                            ActivitySetting.onReceive(byteArrayToInt_Big, bArr4);
                                            break;
                                        }
                                    case 41015:
                                        MainFreeView.this.onReceive(byteArrayToInt_Big, bArr4);
                                        break;
                                    case MainProtocol.NAT_CMD_NET_CONNECT_STATE_RESP /* 41023 */:
                                        if (MainProtocol.targetActivity != 0) {
                                            break;
                                        } else {
                                            Log.d("mhhan", "activity id22 = preview");
                                            MainFreeView.this.onReceive(byteArrayToInt_Big, bArr4);
                                            break;
                                        }
                                }
                            }
                        } else {
                            Logger.d("MainFreeView# eroor?");
                            MainProtocol.readdata = new byte[8192];
                        }
                    }
                } catch (Exception e) {
                    Logger.d("MainFreeView# Receive error = " + e);
                    String[] strArr2 = {MainFreeView.mContext.getResources().getString(R.string.file_list_title), MainFreeView.mContext.getResources().getString(R.string.file_list_title2)};
                    Message obtainMessage2 = MainFreeView.this.displayAlertFileDownFailHandler.obtainMessage();
                    obtainMessage2.obj = strArr2;
                    MainFreeView.this.displayAlertFileDownFailHandler.sendMessage(obtainMessage2);
                    try {
                        MainProtocol mainProtocol = MainFreeView.this.mprotocol;
                        if (MainProtocol.mSock != null) {
                            MainProtocol mainProtocol2 = MainFreeView.this.mprotocol;
                            MainProtocol.mSock.close();
                            MainProtocol mainProtocol3 = MainFreeView.this.mprotocol;
                            MainProtocol.m_out_stream.close();
                            MainProtocol mainProtocol4 = MainFreeView.this.mprotocol;
                            MainProtocol.m_in_stream.close();
                            return;
                        }
                        return;
                    } catch (IOException e2) {
                        e2.printStackTrace();
                        return;
                    }
                }
            }
        }
    };
    Handler displayAlertFileDownFailHandler = new Handler() { // from class: com.logon_e100.MainFreeView.5
        @Override // android.os.Handler
        public void handleMessage(Message message) {
            String[] strArr = (String[]) message.obj;
            MainFreeView.this.displayFileDownAlert(strArr[0], strArr[1]);
        }
    };
    private Handler handler = new Handler() { // from class: com.logon_e100.MainFreeView.10
        @Override // android.os.Handler
        public void handleMessage(Message message) {
            if (MainFreeView.this.main_ProgressDialog44 != null && MainFreeView.this.main_ProgressDialog44.isShowing()) {
                MainFreeView.this.main_ProgressDialog44.dismiss();
                MainFreeView.this.wifi_level_flag = true;
                Logger.d("MainFreeView# onFreeView WiFiLevelCheck true");
            }
            if (MainFreeView.this.backbit != null) {
                if (MainFreeView.flag_CH == 2) {
                    if (MainFreeView.this.camreversed_flag == 2) {
                        if (MainFreeView.this.ORIENTATION_FLAG == 1) {
                            MainFreeView mainFreeView = MainFreeView.this;
                            mainFreeView.sideInversionImg = Bitmap.createBitmap(mainFreeView.backbit, 0, 0, 720, 640, MainFreeView.this.sideInversion, false);
                            MainFreeView.this.tmpIv.setImageBitmap(MainFreeView.this.sideInversionImg);
                            MainFreeView.this.tmpIv.invalidate();
                            MainFreeView.this.camreversed_flag = 2;
                            return;
                        }
                        MainFreeView mainFreeView2 = MainFreeView.this;
                        mainFreeView2.sideInversionImg = Bitmap.createBitmap(mainFreeView2.backbit, 0, 0, MainFreeView.this.backbit.getWidth(), MainFreeView.this.backbit.getHeight(), MainFreeView.this.sideInversion, false);
                        MainFreeView.this.tmpIv.setImageBitmap(MainFreeView.this.sideInversionImg);
                        MainFreeView.this.tmpIv.invalidate();
                        MainFreeView.this.camreversed_flag = 2;
                        return;
                    }
                    MainFreeView.this.tmpIv.setImageBitmap(MainFreeView.this.backbit);
                    MainFreeView.this.tmpIv.invalidate();
                    return;
                }
                MainFreeView.this.tmpIv.setImageBitmap(MainFreeView.this.backbit);
                MainFreeView.this.tmpIv.invalidate();
            }
        }
    };
    private Handler handler2 = new Handler() { // from class: com.logon_e100.MainFreeView.11
        @Override // android.os.Handler
        public void handleMessage(Message message) {
            String str = (String) message.obj;
            int i = message.arg1;
            if (str != null) {
                if (MainProtocol.targetActivity == 6) {
                    MainFreeView.this.count_filedonw++;
                    MainFileListView_blackbox2.liststart(str, i, MainFreeView.this.count_filedonw);
                    return;
                }
                if (MainProtocol.targetActivity == 7) {
                    MainFreeView.this.count_filedonw++;
                    MainFileListView_blackbox3.liststart(str, i, MainFreeView.this.count_filedonw);
                } else if (MainProtocol.targetActivity == 8) {
                    MainFreeView.this.count_filedonw++;
                    MainFileListView_blackbox4.liststart(str, i, MainFreeView.this.count_filedonw);
                } else if (MainProtocol.targetActivity == 9) {
                    MainFreeView.this.count_filedonw++;
                    MainFileListView_blackbox5.liststart(str, i, MainFreeView.this.count_filedonw);
                }
            }
        }
    };
    final Handler handler88 = new Handler() { // from class: com.logon_e100.MainFreeView.13
        @Override // android.os.Handler
        public void handleMessage(Message message) {
            String string = MainFreeView.this.getResources().getString(R.string.preview_video_preparing);
            MainFreeView.this.main_ProgressDialog44 = ProgressDialog.show(MainFreeView.mContext, "", string, true);
        }
    };
    Boolean bOrientation = false;
    SurfaceHolder.Callback m_cbSurfaceHolder = new SurfaceHolder.Callback() { // from class: com.logon_e100.MainFreeView.28
        @Override // android.view.SurfaceHolder.Callback
        public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
        }

        @Override // android.view.SurfaceHolder.Callback
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
        }

        @Override // android.view.SurfaceHolder.Callback
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        }
    };
    View.OnClickListener mClickListener = new View.OnClickListener() { // from class: com.logon_e100.MainFreeView.32
        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            if (view.getId() != R.id.imageViewDialog) {
                return;
            }
            MainFreeView.this.alert.setNegativeButton(android.R.string.cancel, (DialogInterface.OnClickListener) null);
            WifiManager wifiManager = (WifiManager) MainFreeView.this.getApplicationContext().getSystemService("wifi");
            if (wifiManager != null && !wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(true);
            }
            MainProtocol.OnStopFlag = false;
            Intent intent = new Intent("android.settings.WIFI_SETTINGS");
            intent.setFlags(268435456);
            MainFreeView.this.startActivity(intent);
        }
    };
    Runnable mWiFiLeVelUpdate = new Runnable() { // from class: com.logon_e100.MainFreeView.33
        @Override // java.lang.Runnable
        public void run() {
            MainFreeView.this.WiFiLevelCheck();
        }
    };

    private static native int naFileAvi();

    private static native int[] naGetVideoRes();

    private static native int naInit(String str, String str2, String str3, int i);

    private static native int naIsFirstFrameReceived();

    private static native int naPlay();

    private static native int naPrepare(String str, String str2, int i);

    private static native int naSetSurface(Surface surface);

    /* JADX INFO: Access modifiers changed from: private */
    public static native int naSetup(int i, int i2);

    /* JADX INFO: Access modifiers changed from: private */
    public static native void naStop();

    /* JADX INFO: Access modifiers changed from: private */
    public static native int renderFrame(Bitmap bitmap);

    @Override // android.app.Activity
    @SuppressLint({"InlinedApi"})
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getWindow().addFlags(128);
        setTheme(android.R.style.Theme.Holo.Light.NoActionBar.TranslucentDecor);
        if (hasSoftKeys()) {
            setContentView(R.layout.activity_freeview_lg);
        } else {
            setContentView(R.layout.activity_freeview);
        }
        getWindow().setFlags(16777216, 16777216);
        if (Build.VERSION.SDK_INT > 9) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
        }
        mContext = this;
        mButtonVoiceRecord = (Button) findViewById(R.id.edit_button);
        mButtonVoiceRecord.setOnClickListener(this);
        mButtonVoiceRecord.setEnabled(false);
        this.mVoiceRecordState = true;
        this.mTopRelativeLayout = (RelativeLayout) findViewById(R.id.main_top_relativelayout);
        this.mTempRelativeLayout = (RelativeLayout) findViewById(R.id.main_temp_relativelayout);
        this.mBottomLinearLayout = (LinearLayout) findViewById(R.id.main_bottom_linearlayout);
        this.mRelativeLayout = (RelativeLayout) findViewById(R.id.FrameLayout01);
        this.mIsTimeSaved = false;
        this.WiFiLevelUpdateHandler = new Handler();
        ((Button) findViewById(R.id.edit_button2)).setOnClickListener(this);
        mButtonCamReversed = (Button) findViewById(R.id.edit_button3);
        mButtonCamReversed.setOnClickListener(this);
        mButtonFullScreen = (Button) findViewById(R.id.btn_full_screen);
        mButtonFullScreen.setOnClickListener(this);
        mButtonPhoto = (Button) findViewById(R.id.edit_button4);
        mButtonPhoto.setOnClickListener(this);
        this.iv_rec = (ImageView) findViewById(R.id.live_rec);
        this.tvSsid = (TextView) findViewById(R.id.main_ssid);
        tvCh = (TextView) findViewById(R.id.main_ch);
        this.tvREC = (TextView) findViewById(R.id.main_REC);
        wifiManager2 = (WifiManager) getApplicationContext().getSystemService("wifi");
        this.sideInversion = new Matrix();
        this.sideInversion.setScale(-1.0f, 1.0f);
        SharedPreferences sharedPreferences = getSharedPreferences("seve_camreversed", 0);
        if (sharedPreferences != null) {
            this.camreversed_flag = sharedPreferences.getInt("seve_camreversed", 1);
        }
    }

    public boolean hasSoftKeys() {
        Logger.e("wanjae_has: ");
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

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        int id = view.getId();
        if (Utils.duplicateClickCheck()) {
            return;
        }
        if (id == R.id.edit_button2) {
            if (this.ipAddress.equals(MainProtocol.SERVERIP)) {
                CamChange();
            } else {
                Toast.makeText(this, getResources().getString(R.string.wifi_connection_required), 1).show();
            }
        }
        if (id == R.id.edit_button4) {
            if (this.ipAddress.equals(MainProtocol.SERVERIP)) {
                MainProtocol.CNSSimpleIndex cNSSimpleIndex = new MainProtocol.CNSSimpleIndex();
                cNSSimpleIndex.setIndex(4);
                MainProtocol.CNSSimpleIndex cNSSimpleIndex2 = new MainProtocol.CNSSimpleIndex();
                cNSSimpleIndex2.setIndex(0);
                MainProtocol.sendData(MainProtocol.NAT_CMD_SET_PHOTO2, cNSSimpleIndex.getBytes(), cNSSimpleIndex2.getBytes(), 0);
                new Timer().schedule(new TimerTask() { // from class: com.logon_e100.MainFreeView.2
                    @Override // java.util.TimerTask, java.lang.Runnable
                    public void run() {
                        MainFreeView.this.runOnUiThread(new Runnable() { // from class: com.logon_e100.MainFreeView.2.1
                            @Override // java.lang.Runnable
                            public void run() {
                                if (MainFreeView.this.m_photo == 1) {
                                    Logger.e("m_photo ok: " + MainFreeView.this.m_photo);
                                    return;
                                }
                                Logger.e("m_photo nonononononononono: " + MainFreeView.this.m_photo);
                                Toast.makeText(MainFreeView.mContext, MainFreeView.this.getResources().getString(R.string.setting_error_ver), 1).show();
                            }
                        });
                    }
                }, 1000L);
            } else {
                Toast.makeText(mContext, getResources().getString(R.string.wifi_connection_required), 1).show();
            }
        }
        if (id == R.id.edit_button3) {
            if (flag_CH == 1) {
                Toast.makeText(this, getResources().getString(R.string.preview_cam_revers), 1).show();
                Message obtainMessage = buttonReversedUpdateHandler.obtainMessage();
                obtainMessage.obj = true;
                buttonReversedUpdateHandler.sendMessage(obtainMessage);
                return;
            }
            if (this.camreversed_flag == 1) {
                this.camreversed_flag = 2;
                Message obtainMessage2 = buttonReversedUpdateHandler.obtainMessage();
                obtainMessage2.obj = false;
                buttonReversedUpdateHandler.sendMessage(obtainMessage2);
                SharedPreferences sharedPreferences = getSharedPreferences("seve_camreversed", 0);
                if (sharedPreferences != null) {
                    SharedPreferences.Editor edit = sharedPreferences.edit();
                    edit.putInt("seve_camreversed", 2);
                    edit.commit();
                    return;
                }
                return;
            }
            this.camreversed_flag = 1;
            Message obtainMessage3 = buttonReversedUpdateHandler.obtainMessage();
            obtainMessage3.obj = true;
            buttonReversedUpdateHandler.sendMessage(obtainMessage3);
            SharedPreferences sharedPreferences2 = getSharedPreferences("seve_camreversed", 0);
            if (sharedPreferences2 != null) {
                SharedPreferences.Editor edit2 = sharedPreferences2.edit();
                edit2.putInt("seve_camreversed", 1);
                edit2.commit();
                return;
            }
            return;
        }
        if (id != R.id.btn_full_screen) {
            if (id == R.id.edit_button) {
                if (this.ipAddress.equals(MainProtocol.SERVERIP)) {
                    mButtonVoiceRecord.setEnabled(false);
                    MainProtocol.CNSSimpleIndex cNSSimpleIndex3 = new MainProtocol.CNSSimpleIndex();
                    if (this.mVoiceRecordState) {
                        cNSSimpleIndex3.setIndex(0);
                    } else {
                        cNSSimpleIndex3.setIndex(1);
                    }
                    if (MainProtocol.send(MainProtocol.NAT_CMD_SET_MUTE, cNSSimpleIndex3.getBytes(), 0) < 0) {
                        displayInfoAlert(getResources().getString(R.string.preview_voice_record_title), getResources().getString(R.string.preview_voice_record_error));
                        return;
                    }
                    return;
                }
                Toast.makeText(this, getResources().getString(R.string.wifi_connection_required), 1).show();
                return;
            }
            return;
        }
        if (this.ipAddress.equals(MainProtocol.SERVERIP)) {
            int i = this.first_flag2;
            if (i == 0) {
                Logger.d("#### 5555");
                if (freeviewstop_flag == 0) {
                    Logger.d("#### 6666");
                    setRequestedOrientation(0);
                    this.mTempRelativeLayout.setVisibility(8);
                    Logger.d("#### 777");
                    this.mTopRelativeLayout.setVisibility(8);
                    Logger.d("#### 8888");
                    this.mBottomLinearLayout.setVisibility(8);
                    Logger.d("#### 9999");
                    MainActivity.tabHost.getTabWidget().setVisibility(8);
                    Logger.d("#### 121212");
                    Logger.d("#### 1313");
                    this.tmpIv.setLayoutParams(new RelativeLayout.LayoutParams(-1, -1));
                    int[] iArr = this.screenRes;
                    int[] iArr2 = this.res;
                    this.widthScaledRatio = (iArr[0] * 1.0f) / iArr2[0];
                    this.heightScaledRatio = (iArr[1] * 1.0f) / iArr2[1];
                    this.height = (int) (iArr2[0] * this.widthScaledRatio);
                    this.width = (int) (iArr2[1] * this.heightScaledRatio);
                    this.ORIENTATION_FLAG = 2;
                    this.width = 1280;
                    this.height = 720;
                    this.bOrientation = true;
                    viewzoom(0);
                    this.first_flag2 = 1;
                    return;
                }
                return;
            }
            if (i == 1 && freeviewstop_flag == 0) {
                setRequestedOrientation(1);
                this.mTopRelativeLayout.setVisibility(0);
                this.mBottomLinearLayout.setVisibility(0);
                this.mTempRelativeLayout.setVisibility(8);
                MainActivity.tabHost.getTabWidget().setVisibility(0);
                this.tmpIv.setLayoutParams(new RelativeLayout.LayoutParams(-1, -2));
                int[] iArr3 = this.screenRes;
                int[] iArr4 = this.res;
                this.widthScaledRatio = (iArr3[0] * 1.0f) / iArr4[0];
                this.heightScaledRatio = ((iArr3[1] * 1.0f) / iArr4[1]) / 2.0f;
                this.width = (int) (iArr4[0] * this.widthScaledRatio);
                this.height = (int) (iArr4[1] * this.heightScaledRatio);
                this.width = 720;
                this.height = 640;
                this.bOrientation = true;
                this.ORIENTATION_FLAG = 1;
                viewzoom(1);
                this.first_flag2 = 0;
            }
        }
    }

    public void SocKetConnect() {
        try {
            int ipAddress = ((WifiManager) getApplicationContext().getSystemService("wifi")).getConnectionInfo().getIpAddress();
            String.format("%d.%d.%d.%d", Integer.valueOf(ipAddress & 255), Integer.valueOf((ipAddress >> 8) & 255), Integer.valueOf((ipAddress >> 16) & 255), Integer.valueOf((ipAddress >> 24) & 255));
            MainProtocol mainProtocol = this.mprotocol;
            if (MainProtocol.mSock != null) {
                MainProtocol mainProtocol2 = this.mprotocol;
                if (MainProtocol.mSock.isConnected()) {
                    Log.e("HAN5", "서버 접속 했어요");
                }
            }
            MainProtocol mainProtocol3 = this.mprotocol;
            MainProtocol.mSock = new Socket();
            MainProtocol mainProtocol4 = this.mprotocol;
            Socket socket = MainProtocol.mSock;
            MainProtocol mainProtocol5 = this.mprotocol;
            String str = MainProtocol.SERVERIP;
            MainProtocol mainProtocol6 = this.mprotocol;
            socket.connect(new InetSocketAddress(str, MainProtocol.serverPort), PathInterpolatorCompat.MAX_NUM_POINTS);
            MainProtocol mainProtocol7 = this.mprotocol;
            MainProtocol mainProtocol8 = this.mprotocol;
            MainProtocol.m_out_stream = new BufferedOutputStream(MainProtocol.mSock.getOutputStream());
            MainProtocol mainProtocol9 = this.mprotocol;
            MainProtocol mainProtocol10 = this.mprotocol;
            MainProtocol.m_in_stream = MainProtocol.mSock.getInputStream();
            this.mCheckRecv.start();
            this.wifi_connecterror_flag = true;
        } catch (Exception e) {
            Logger.d("MainFreeView# Socket connect error = " + e);
            this.wifi_connecterror_flag = false;
            if (MainIntro.mMobileData == 1) {
                String[] strArr = {mContext.getResources().getString(R.string.wifi_connect_error7), mContext.getResources().getString(R.string.wifi_connect_error8)};
                Message obtainMessage = displayErrorSocketAlertHandler.obtainMessage();
                obtainMessage.obj = strArr;
                displayErrorSocketAlertHandler.sendMessage(obtainMessage);
                return;
            }
            String[] strArr2 = {mContext.getResources().getString(R.string.wifi_connect_error5), mContext.getResources().getString(R.string.wifi_connect_error6)};
            Message obtainMessage2 = displayErrorSocketAlertHandler.obtainMessage();
            obtainMessage2.obj = strArr2;
            displayErrorSocketAlertHandler.sendMessage(obtainMessage2);
        }
    }

    public void displayFileDownAlert(String str, String str2) {
        String string = MainActivity.context.getResources().getString(R.string.action_confirm);
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.context);
        builder.setMessage(str2).setCancelable(false).setPositiveButton(string, new DialogInterface.OnClickListener() { // from class: com.logon_e100.MainFreeView.6
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                MainFreeView.mContext.startActivity(new Intent(MainFreeView.mContext, (Class<?>) MainActivity.class));
                ((MainFreeView) MainFreeView.mContext).finish();
                if (MainProtocol.mSock != null) {
                    try {
                        MainProtocol.mSock.close();
                        MainProtocol.m_out_stream.close();
                        MainProtocol.m_in_stream.close();
                        Logger.d("MainFreeView# socketclose");
                    } catch (IOException e) {
                        e.printStackTrace();
                        Logger.d("MainFreeView# socketclose = " + e);
                    }
                }
                Process.killProcess(Process.myPid());
            }
        });
        AlertDialog create = builder.create();
        create.setTitle(str);
        create.setIcon(R.drawable.ic_launcher);
        create.show();
    }

    @SuppressLint({"NewApi"})
    private int[] getScreenRes() {
        int[] iArr = new int[2];
        Display defaultDisplay = getWindowManager().getDefaultDisplay();
        if (Build.VERSION.SDK_INT >= 13) {
            Point point = new Point();
            defaultDisplay.getSize(point);
            iArr[0] = point.x;
            iArr[1] = point.y;
        } else {
            iArr[0] = defaultDisplay.getWidth();
            iArr[1] = defaultDisplay.getHeight();
        }
        return iArr;
    }

    public void screen_mode() {
        Logger.d("MainFreeView# ffmpeg start");
        int naInit = naInit("abc", "abc", "a", 0);
        if (naInit < 0) {
            SystemClock.sleep(1000L);
            naInit("abc", "abc", "a", 0);
            Logger.d("MainFreeView# nainit 재시작");
        }
        Logger.d("MainFreeView# ret = " + naInit);
        if (naInit < 0) {
            Logger.d("MainFreeView# nainit failed!!");
            ProgressDialog progressDialog = this.main_ProgressDialog44;
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
            displayPlayFailedAlert(mContext.getResources().getString(R.string.preview_error_title), mContext.getResources().getString(R.string.preview_error_play_failed));
            return;
        }
        this.res = naGetVideoRes();
        this.screenRes = getScreenRes();
        int[] iArr = this.screenRes;
        int[] iArr2 = this.res;
        this.widthScaledRatio = (iArr[0] * 1.0f) / iArr2[0];
        this.heightScaledRatio = ((iArr[1] * 1.0f) / iArr2[1]) / 2.0f;
        this.width = (int) (iArr2[0] * this.widthScaledRatio);
        this.height = (int) (iArr2[1] * this.heightScaledRatio);
        this.width = 720;
        this.height = 640;
        this.tmpIv = (ImageView) findViewById(R.id.musicimage);
        Log.d("LogOnT", "screen_mode: " + this.screenRes[0] + "," + this.screenRes[1] + "," + this.res[0] + "," + this.res[1]);
        if (this.screenRes[0] == 720) {
            this.width = 780;
            this.height = 560;
            this.mAttacher = new PhotoViewAttacher(this.tmpIv);
            this.mAttacher.setScaleType(ImageView.ScaleType.FIT_XY);
        } else {
            this.width = this.tmpIv.getWidth();
            this.height = this.tmpIv.getHeight();
        }
        Log.d("LogOnT", "screen_mode: " + this.width + "," + this.height);
        this.backbit = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888);
        naSetup(this.width, this.height);
        freeviewstop_flag2 = 1;
        freeviewstop_flag = 0;
        onFreeView();
        if (MainProtocol.FHD_CHECK == 1 || MainProtocol.FHD_CHECK == 3) {
            AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
            alphaAnimation.setDuration(1000L);
            alphaAnimation.setInterpolator(new LinearInterpolator());
            alphaAnimation.setRepeatCount(-1);
            alphaAnimation.setRepeatMode(1);
            this.iv_rec.startAnimation(alphaAnimation);
            this.tvREC = (TextView) findViewById(R.id.main_REC);
            return;
        }
        this.iv_rec.setVisibility(4);
        this.tvREC = (TextView) findViewById(R.id.main_REC);
        this.tvREC.setVisibility(8);
    }

    static {
        System.loadLibrary("avutil-54");
        System.loadLibrary("avcodec-56");
        System.loadLibrary("avformat-56");
        System.loadLibrary("swresample-1");
        System.loadLibrary("swscale-3");
        System.loadLibrary("tutorial02");
        buttonUpdateHandler = new Handler() { // from class: com.logon_e100.MainFreeView.15
            @Override // android.os.Handler
            public void handleMessage(Message message) {
                if (((Boolean) message.obj).booleanValue()) {
                    MainFreeView.mButtonVoiceRecord.setBackground(MainFreeView.mContext.getResources().getDrawable(R.drawable.icon_video_sound_off_normal));
                } else {
                    MainFreeView.mButtonVoiceRecord.setBackground(MainFreeView.mContext.getResources().getDrawable(R.drawable.icon_video_sound_on_normal2));
                }
                MainFreeView.mButtonVoiceRecord.setEnabled(true);
            }
        };
        buttonReversedUpdateHandler = new Handler() { // from class: com.logon_e100.MainFreeView.16
            @Override // android.os.Handler
            public void handleMessage(Message message) {
                if (((Boolean) message.obj).booleanValue()) {
                    MainFreeView.mButtonCamReversed.setBackground(MainFreeView.mContext.getResources().getDrawable(R.drawable.icon_cam_leftreversed));
                } else {
                    MainFreeView.mButtonCamReversed.setBackground(MainFreeView.mContext.getResources().getDrawable(R.drawable.icon_cam_right_reversed));
                }
            }
        };
        displayAlertHandler = new Handler() { // from class: com.logon_e100.MainFreeView.17
            @Override // android.os.Handler
            public void handleMessage(Message message) {
                String[] strArr = (String[]) message.obj;
                MainFreeView.displayInfoAlert(strArr[0], strArr[1]);
            }
        };
        displayErrorAlertHandler = new Handler() { // from class: com.logon_e100.MainFreeView.18
            @Override // android.os.Handler
            public void handleMessage(Message message) {
                String[] strArr = (String[]) message.obj;
                MainFreeView.displayErrorAlert(strArr[0], strArr[1]);
            }
        };
        displayErrorSocketAlertHandler = new Handler() { // from class: com.logon_e100.MainFreeView.19
            @Override // android.os.Handler
            public void handleMessage(Message message) {
                String[] strArr = (String[]) message.obj;
                MainFreeView.displayErrorSocketAlert(strArr[0], strArr[1]);
            }
        };
        textViewUpdateHandler = new Handler() { // from class: com.logon_e100.MainFreeView.20
            @Override // android.os.Handler
            public void handleMessage(Message message) {
                MainFreeView.tvCh.setText((String) message.obj);
            }
        };
        displayRenderTimeoutAlertHandler = new Handler() { // from class: com.logon_e100.MainFreeView.21
            @Override // android.os.Handler
            public void handleMessage(Message message) {
                MainFreeView.displayRenderTimeoutAlert();
            }
        };
    }

    public void WiFiConnect() {
        byte[] bArr = new byte[256];
        bArr[0] = 2;
        bArr[1] = -96;
        bArr[2] = 62;
        bArr[3] = (byte) 4;
        byte b = (byte) 0;
        bArr[4] = b;
        bArr[5] = b;
        bArr[6] = b;
        bArr[7] = 1;
        bArr[8] = 0;
        bArr[9] = 0;
        bArr[10] = 0;
        bArr[11] = 3;
        bArr[12] = bArr[0];
        for (int i = 1; i < 12; i++) {
            bArr[12] = (byte) (bArr[12] ^ bArr[i]);
        }
        MainProtocol.targetActivity = 0;
        try {
            MainProtocol mainProtocol = this.mprotocol;
            MainProtocol.m_out_stream.write(bArr, 0, 13);
            MainProtocol mainProtocol2 = this.mprotocol;
            MainProtocol.m_out_stream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Logger.d("MainFreeView# 실시간 wifi접속 데이터 보내기");
    }

    public void IpCamStart() {
        byte[] bArr = new byte[256];
        bArr[0] = 2;
        bArr[1] = 32;
        bArr[2] = 1;
        bArr[3] = (byte) 8;
        byte b = (byte) 0;
        bArr[4] = b;
        bArr[5] = b;
        bArr[6] = b;
        bArr[7] = 1;
        bArr[8] = 0;
        bArr[9] = 0;
        bArr[10] = 0;
        bArr[11] = 0;
        bArr[12] = 0;
        bArr[13] = 0;
        bArr[14] = 0;
        bArr[15] = 3;
        bArr[16] = bArr[0];
        for (int i = 1; i < 16; i++) {
            bArr[16] = (byte) (bArr[16] ^ bArr[i]);
        }
        Logger.d("MainFreeView# 실시간 Start 보내기");
        try {
            MainProtocol mainProtocol = this.mprotocol;
            MainProtocol.m_out_stream.write(bArr, 0, 17);
            MainProtocol mainProtocol2 = this.mprotocol;
            MainProtocol.m_out_stream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void freeviewcon() {
        byte[] bArr = new byte[256];
        bArr[0] = 2;
        bArr[1] = 1;
        bArr[2] = -1;
        bArr[3] = 0;
        bArr[4] = 0;
        bArr[5] = 0;
        bArr[6] = 0;
        bArr[7] = 3;
        bArr[8] = bArr[0];
        for (int i = 1; i < 8; i++) {
            bArr[8] = (byte) (bArr[8] ^ bArr[i]);
        }
        try {
            MainProtocol mainProtocol = this.mprotocol;
            MainProtocol.m_out_stream.write(bArr, 0, 9);
            MainProtocol mainProtocol2 = this.mprotocol;
            MainProtocol.m_out_stream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        new Handler().postDelayed(new Runnable() { // from class: com.logon_e100.MainFreeView.7
            @Override // java.lang.Runnable
            public void run() {
                MainFreeView.this.screen_mode();
            }
        }, 1000L);
        if (this.mIsTimeSaved) {
            return;
        }
        new Handler().postDelayed(new Runnable() { // from class: com.logon_e100.MainFreeView.8
            @Override // java.lang.Runnable
            public void run() {
                MainFreeView.this.setTime();
                MainFreeView.this.mIsTimeSaved = true;
            }
        }, 2000L);
    }

    public void CamChange() {
        byte[] bArr = new byte[256];
        bArr[0] = 2;
        bArr[1] = -96;
        bArr[2] = 54;
        bArr[3] = (byte) 4;
        byte b = (byte) 0;
        bArr[4] = b;
        bArr[5] = b;
        bArr[6] = b;
        bArr[7] = 0;
        bArr[8] = 0;
        bArr[9] = 0;
        bArr[10] = 0;
        bArr[11] = 3;
        bArr[12] = bArr[0];
        for (int i = 1; i < 12; i++) {
            bArr[12] = (byte) (bArr[12] ^ bArr[i]);
        }
        Logger.d("MainFreeView# 전후방변경");
        try {
            MainProtocol mainProtocol = this.mprotocol;
            MainProtocol.m_out_stream.write(bArr, 0, 13);
            MainProtocol mainProtocol2 = this.mprotocol;
            MainProtocol.m_out_stream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setTime() {
        Calendar calendar = Calendar.getInstance();
        int i = calendar.get(1);
        int i2 = calendar.get(2) + 1;
        int i3 = calendar.get(5);
        int i4 = calendar.get(11);
        int i5 = calendar.get(12);
        int i6 = calendar.get(13);
        if (i > 2037 || i < 2014 || i2 > 12 || i2 < 1 || i3 > 31 || i3 < 1 || i4 > 23 || i4 < 0 || i5 > 59 || i5 < 0 || i6 > 59 || i6 < 0) {
            Logger.d("MainFreeView# get time failed");
            return;
        }
        MainProtocol.CNSTime cNSTime = new MainProtocol.CNSTime();
        cNSTime.setYear(i);
        cNSTime.setMonth(i2);
        cNSTime.setDay(i3);
        cNSTime.setHour(i4);
        cNSTime.setMinute(i5);
        cNSTime.setSecond(i6);
        Logger.d("MainFreeView# #### Set Time = " + i + "/" + i2 + "/" + i3 + " " + i4 + ":" + i5 + ":" + i6 + " ####");
        MainProtocol.send(MainProtocol.NAT_CMD_SET_TIME, cNSTime.getBytes(), 0);
    }

    protected void onFreeView() {
        new Thread() { // from class: com.logon_e100.MainFreeView.9
            @Override // java.lang.Thread, java.lang.Runnable
            public void run() {
                while (true) {
                    try {
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (MainFreeView.freeviewstop_flag == 0) {
                        if (MainFreeView.this.bOrientation.booleanValue()) {
                            if (MainFreeView.this.ORIENTATION_FLAG == 1) {
                                MainFreeView.this.backbit = Bitmap.createBitmap(720, 640, Bitmap.Config.ARGB_8888);
                            } else {
                                MainFreeView.this.backbit = Bitmap.createBitmap(1280, 720, Bitmap.Config.ARGB_8888);
                            }
                            MainFreeView.naSetup(MainFreeView.this.width, MainFreeView.this.height);
                            MainFreeView.this.bOrientation = false;
                        }
                        if (MainFreeView.renderFrame(MainFreeView.this.backbit) >= 0) {
                            MainFreeView.this.handler.sendEmptyMessage(0);
                        } else {
                            Logger.d("MainFreeView# read frame timeout.. stop rendering!!");
                            if (MainFreeView.freeviewstop_flag == 0) {
                                MainFreeView.this.readFrameTimeoutProcess();
                            }
                        }
                    } else {
                        Logger.d("MainFreeView# 실시간 재생 renderFrame 스레드 종료");
                        MainFreeView.this.backbit = Bitmap.createBitmap(MainFreeView.this.width, MainFreeView.this.height, Bitmap.Config.ARGB_8888);
                        MainFreeView.this.handler.sendEmptyMessage(0);
                        MainFreeView.naStop();
                        if (MainFreeView.this.backbit == null || MainFreeView.this.backbit.isRecycled()) {
                            return;
                        }
                        MainFreeView.this.backbit = null;
                        return;
                    }
                }
            }
        }.start();
    }

    public void readFrameTimeoutProcess() {
        freeviewstop_flag = 1;
        this.backbit = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888);
        this.handler.sendEmptyMessage(0);
        displayRenderTimeoutAlertHandler.sendEmptyMessage(0);
    }

    public void ViewDialog() {
        runOnUiThread(new Runnable() { // from class: com.logon_e100.MainFreeView.12
            @Override // java.lang.Runnable
            public void run() {
                String string = MainFreeView.this.getResources().getString(R.string.preview_video_preparing);
                MainFreeView.this.main_ProgressDialog44 = ProgressDialog.show(MainFreeView.mContext, "", string, true);
            }
        });
    }

    public void FileDownDialog() {
        runOnUiThread(new Runnable() { // from class: com.logon_e100.MainFreeView.14
            @Override // java.lang.Runnable
            public void run() {
                String string = MainFreeView.mContext.getResources().getString(R.string.preview_video_preparing);
                MainFreeView.this.main_ProgressDialog44 = ProgressDialog.show(MainFreeView.mContext, "", string, true);
            }
        });
    }

    public void SsidInfo() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService("connectivity");
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService("wifi");
        if (connectivityManager.getActiveNetworkInfo() != null) {
            new String();
            String ssid = wifiManager.getConnectionInfo().getSSID();
            if (ssid != null) {
                this.tvSsid.setText(ssid);
            } else {
                this.tvSsid.setText(getResources().getString(R.string.wifi_connection_required));
            }
            MainProtocol.WIFI_SSID = ssid;
        }
    }

    public void PreViewOff() {
        byte[] bArr = new byte[256];
        bArr[0] = 2;
        MainProtocol mainProtocol = this.mprotocol;
        bArr[1] = 32;
        bArr[2] = 2;
        bArr[3] = (byte) 4;
        byte b = (byte) 0;
        bArr[4] = b;
        bArr[5] = b;
        bArr[6] = b;
        bArr[7] = 3;
        bArr[8] = bArr[0];
        for (int i = 1; i < 8; i++) {
            bArr[8] = (byte) (bArr[8] ^ bArr[i]);
        }
        try {
            MainProtocol mainProtocol2 = this.mprotocol;
            MainProtocol.m_out_stream.write(bArr, 0, 9);
            MainProtocol mainProtocol3 = this.mprotocol;
            MainProtocol.m_out_stream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Logger.d("MainFreeView# 핸드폰 상시 프리뷰 정지");
    }

    public void RecordOnOff() {
        byte[] bArr = new byte[256];
        bArr[0] = 2;
        MainProtocol mainProtocol = this.mprotocol;
        bArr[1] = -96;
        bArr[2] = 58;
        bArr[3] = (byte) 4;
        byte b = (byte) 0;
        bArr[4] = b;
        bArr[5] = b;
        bArr[6] = b;
        bArr[7] = 0;
        bArr[8] = 0;
        bArr[9] = 0;
        bArr[10] = 0;
        bArr[11] = 3;
        bArr[12] = bArr[0];
        for (int i = 1; i < 12; i++) {
            bArr[12] = (byte) (bArr[12] ^ bArr[i]);
        }
        try {
            MainProtocol mainProtocol2 = this.mprotocol;
            MainProtocol.m_out_stream.write(bArr, 0, 13);
            MainProtocol mainProtocol3 = this.mprotocol;
            MainProtocol.m_out_stream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Logger.d("MainFreeView# NAT_CMD_RECORD_ON_OFF 핸드폰 상시 영상 온오프보내기");
    }

    public static void displayRenderTimeoutAlert() {
        String string = mContext.getResources().getString(R.string.action_confirm);
        String string2 = mContext.getResources().getString(R.string.preview_error_wifi_level_failed);
        String string3 = mContext.getResources().getString(R.string.preview_error_title);
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(string2).setCancelable(false).setPositiveButton(string, new DialogInterface.OnClickListener() { // from class: com.logon_e100.MainFreeView.22
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                MainProtocol.CNSSimpleIndex cNSSimpleIndex = new MainProtocol.CNSSimpleIndex();
                cNSSimpleIndex.setIndex(0);
                if (MainProtocol.send(MainProtocol.NAT_CMD_NET_CONNECT_STATE3, cNSSimpleIndex.getBytes(), -1) < 0) {
                    Logger.d("MainFreeView# WIFI 연결이 필요 합니다. 종료 실패");
                } else {
                    Logger.d("MainFreeView# WIFI 연결이 필요 합니다. 종료 성공");
                }
                if (MainProtocol.mSock != null) {
                    try {
                        MainProtocol.mSock.close();
                        MainProtocol.m_out_stream.close();
                        MainProtocol.m_in_stream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (MainFreeView.wifiManager2 != null && MainFreeView.wifiManager2.isWifiEnabled()) {
                    MainFreeView.wifiManager2.setWifiEnabled(false);
                }
                Process.killProcess(Process.myPid());
            }
        });
        AlertDialog create = builder.create();
        create.setTitle(string3);
        create.setIcon(R.drawable.ic_launcher);
        create.show();
    }

    public static void displayInfoAlert(String str, String str2) {
        String string = mContext.getResources().getString(R.string.action_confirm);
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(str2).setCancelable(false).setPositiveButton(string, new DialogInterface.OnClickListener() { // from class: com.logon_e100.MainFreeView.23
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                MainFreeView.mButtonVoiceRecord.setEnabled(true);
            }
        });
        AlertDialog create = builder.create();
        create.setTitle(str);
        create.setIcon(R.drawable.ic_launcher);
        create.show();
    }

    public static void displayErrorAlert(String str, String str2) {
        String string = mContext.getResources().getString(R.string.action_confirm);
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(str2).setCancelable(false).setPositiveButton(string, new DialogInterface.OnClickListener() { // from class: com.logon_e100.MainFreeView.24
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                MainProtocol.CNSSimpleIndex cNSSimpleIndex = new MainProtocol.CNSSimpleIndex();
                cNSSimpleIndex.setIndex(0);
                MainProtocol.send(MainProtocol.NAT_CMD_NET_CONNECT_STATE3, cNSSimpleIndex.getBytes(), -1);
                if (MainProtocol.mSock != null) {
                    try {
                        MainProtocol.mSock.close();
                        MainProtocol.m_out_stream.close();
                        MainProtocol.m_in_stream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (MainFreeView.wifiManager2 != null && MainFreeView.wifiManager2.isWifiEnabled()) {
                    MainFreeView.wifiManager2.setWifiEnabled(false);
                }
                Process.killProcess(Process.myPid());
            }
        });
        AlertDialog create = builder.create();
        create.setTitle(str);
        create.setIcon(R.drawable.ic_launcher);
        create.show();
    }

    public static void displayErrorSocketAlert(String str, String str2) {
        String string = mContext.getResources().getString(R.string.action_confirm);
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(str2).setCancelable(false).setPositiveButton(string, new DialogInterface.OnClickListener() { // from class: com.logon_e100.MainFreeView.25
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                MainProtocol.CNSSimpleIndex cNSSimpleIndex = new MainProtocol.CNSSimpleIndex();
                cNSSimpleIndex.setIndex(0);
                MainProtocol.send(MainProtocol.NAT_CMD_NET_CONNECT_STATE3, cNSSimpleIndex.getBytes(), -1);
                if (MainProtocol.mSock != null) {
                    try {
                        MainProtocol.mSock.close();
                        MainProtocol.m_out_stream.close();
                        MainProtocol.m_in_stream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (MainFreeView.wifiManager2 != null && MainFreeView.wifiManager2.isWifiEnabled()) {
                    MainFreeView.wifiManager2.setWifiEnabled(false);
                }
                Process.killProcess(Process.myPid());
            }
        });
        AlertDialog create = builder.create();
        create.setTitle(str);
        create.setIcon(R.drawable.ic_launcher);
        create.show();
    }

    public void onReceive(int i, byte[] bArr) {
        switch (i) {
            case MainProtocol.NAT_CMD_SET_PHOTO_RESP /* 40905 */:
                if (new MainProtocol.CNSResult(bArr).getResult() >= 0) {
                    this.m_photo = 1;
                    break;
                }
                break;
            case MainProtocol.NAT_CMD_SET_MUTE_RESP /* 40974 */:
                if (new MainProtocol.CNSResult(bArr).getResult() < 0) {
                    String[] strArr = {mContext.getResources().getString(R.string.preview_voice_record_title), mContext.getResources().getString(R.string.preview_voice_record_error)};
                    Message obtainMessage = displayAlertHandler.obtainMessage();
                    obtainMessage.obj = strArr;
                    displayAlertHandler.sendMessage(obtainMessage);
                    break;
                } else {
                    if (this.mVoiceRecordState) {
                        this.mVoiceRecordState = false;
                    } else {
                        this.mVoiceRecordState = true;
                    }
                    Message obtainMessage2 = buttonUpdateHandler.obtainMessage();
                    obtainMessage2.obj = Boolean.valueOf(this.mVoiceRecordState);
                    buttonUpdateHandler.sendMessage(obtainMessage2);
                    break;
                }
            case 40998:
                String str = new String(bArr);
                this.file_stae = str.substring(24, 25);
                byte[] bArr2 = new byte[4];
                byte[] bArr3 = new byte[4];
                System.arraycopy(bArr, 0, bArr2, 0, 4);
                System.arraycopy(bArr, 4, bArr3, 0, 4);
                int byteArrayToInt_Little = Utils.byteArrayToInt_Little(bArr2);
                int byteArrayToInt_Little2 = Utils.byteArrayToInt_Little(bArr3);
                for (int i2 = 8; i2 < bArr.length; i2 += 32) {
                    if (i2 < bArr.length) {
                        String substring = str.substring(i2, i2 + 21);
                        byte b = bArr[i2 + 31];
                        if (substring.substring(0, 1).equals("2")) {
                            Message obtainMessage3 = this.handler2.obtainMessage();
                            obtainMessage3.obj = substring;
                            if (b == 0) {
                                obtainMessage3.arg1 = 1;
                            } else {
                                obtainMessage3.arg1 = 2;
                            }
                            this.handler2.sendMessage(obtainMessage3);
                        }
                    }
                }
                if (byteArrayToInt_Little2 + 1 >= byteArrayToInt_Little) {
                    Logger.d("MainFreeView# current = " + byteArrayToInt_Little2 + " total = " + byteArrayToInt_Little);
                    if (MainProtocol.targetActivity == 6) {
                        MainFileListView_blackbox2.mProgressDialog2.dismiss();
                        break;
                    } else if (MainProtocol.targetActivity == 7) {
                        MainFileListView_blackbox3.mProgressDialog2.dismiss();
                        break;
                    } else if (MainProtocol.targetActivity == 8) {
                        MainFileListView_blackbox4.mProgressDialog2.dismiss();
                        break;
                    } else if (MainProtocol.targetActivity == 9) {
                        MainFileListView_blackbox5.mProgressDialog2.dismiss();
                        break;
                    }
                }
                break;
            case MainProtocol.NAT_CMD_GET_CONFIG_RESP /* 41013 */:
                CNSConfig cNSConfig = new CNSConfig();
                cNSConfig.setBytes(bArr);
                this.mVoiceRecordState = cNSConfig.getMute();
                Message obtainMessage4 = buttonUpdateHandler.obtainMessage();
                obtainMessage4.obj = Boolean.valueOf(this.mVoiceRecordState);
                buttonUpdateHandler.sendMessage(obtainMessage4);
                IpCamStart();
                break;
            case 41015:
                if (new MainProtocol.CNSResult(bArr).getResult() < 0) {
                    String[] strArr2 = {mContext.getResources().getString(R.string.cam_change_title), mContext.getResources().getString(R.string.cam_change_error)};
                    Message obtainMessage5 = displayAlertHandler.obtainMessage();
                    obtainMessage5.obj = strArr2;
                    displayAlertHandler.sendMessage(obtainMessage5);
                    Message obtainMessage6 = textViewUpdateHandler.obtainMessage();
                    obtainMessage6.obj = mContext.getResources().getString(R.string.blackbox_camera_front);
                    textViewUpdateHandler.sendMessage(obtainMessage6);
                    break;
                } else if (flag_CH == 1) {
                    flag_CH = 2;
                    Message obtainMessage7 = textViewUpdateHandler.obtainMessage();
                    obtainMessage7.obj = mContext.getResources().getString(R.string.blackbox_camera_rear);
                    textViewUpdateHandler.sendMessage(obtainMessage7);
                    if (this.camreversed_flag == 2) {
                        Message obtainMessage8 = buttonReversedUpdateHandler.obtainMessage();
                        obtainMessage8.obj = false;
                        buttonReversedUpdateHandler.sendMessage(obtainMessage8);
                        break;
                    } else {
                        Message obtainMessage9 = buttonReversedUpdateHandler.obtainMessage();
                        obtainMessage9.obj = true;
                        buttonReversedUpdateHandler.sendMessage(obtainMessage9);
                        break;
                    }
                } else {
                    flag_CH = 1;
                    Message obtainMessage10 = textViewUpdateHandler.obtainMessage();
                    obtainMessage10.obj = mContext.getResources().getString(R.string.blackbox_camera_front);
                    textViewUpdateHandler.sendMessage(obtainMessage10);
                    Message obtainMessage11 = buttonReversedUpdateHandler.obtainMessage();
                    obtainMessage11.obj = true;
                    buttonReversedUpdateHandler.sendMessage(obtainMessage11);
                    break;
                }
            case MainProtocol.NAT_CMD_NET_CONNECT_STATE_RESP /* 41023 */:
                if (MainProtocol.targetActivity >= 0) {
                    MainProtocol.CNSResult cNSResult = new MainProtocol.CNSResult(bArr);
                    if (cNSResult.getResult() < 0) {
                        Logger.d("MainFreeView# NAT_CMD_NET_CONNECT_STATE3 실패");
                        ProgressDialog progressDialog = this.main_ProgressDialog44;
                        if (progressDialog != null) {
                            progressDialog.dismiss();
                        }
                        String[] strArr3 = {mContext.getResources().getString(R.string.preview_error_title), mContext.getResources().getString(R.string.preview_error_noentry_already_connected)};
                        this.mAnotherPhone = 1;
                        Message obtainMessage12 = displayErrorAlertHandler.obtainMessage();
                        obtainMessage12.obj = strArr3;
                        displayErrorAlertHandler.sendMessage(obtainMessage12);
                        break;
                    } else if (cNSResult.getResult() == 0) {
                        Logger.d("MainFreeView# NAT_CMD_NET_CONNECT_STATE3 성공");
                        MainProtocol.send(MainProtocol.NAT_CMD_GET_CONFIG, null, 0);
                        break;
                    } else if (cNSResult.getResult() == 2) {
                        ProgressDialog progressDialog2 = this.main_ProgressDialog44;
                        if (progressDialog2 != null) {
                            progressDialog2.dismiss();
                        }
                        Logger.d("MainFreeView# NAT_CMD_NET_CONNECT_STATE3 재생중");
                        String[] strArr4 = {mContext.getResources().getString(R.string.preview_error_title), mContext.getResources().getString(R.string.preview_error_noentry_playback)};
                        Message obtainMessage13 = displayErrorAlertHandler.obtainMessage();
                        obtainMessage13.obj = strArr4;
                        displayErrorAlertHandler.sendMessage(obtainMessage13);
                        break;
                    } else if (cNSResult.getResult() == 3) {
                        ProgressDialog progressDialog3 = this.main_ProgressDialog44;
                        if (progressDialog3 != null) {
                            progressDialog3.dismiss();
                        }
                        String[] strArr5 = {mContext.getResources().getString(R.string.preview_error_title), mContext.getResources().getString(R.string.preview_error_noentry_view_photo)};
                        Message obtainMessage14 = displayErrorAlertHandler.obtainMessage();
                        obtainMessage14.obj = strArr5;
                        displayErrorAlertHandler.sendMessage(obtainMessage14);
                        break;
                    }
                }
                break;
            default:
                Logger.d("MainFreeView# what????????");
                break;
        }
    }

    @Override // android.app.Activity
    protected void onStart() {
        Logger.d("#### " + getClass().getName() + "::onStart ####");
        super.onStart();
    }

    @Override // android.app.Activity
    protected void onRestart() {
        Logger.d("#### " + getClass().getName() + "::onRestart ####");
        super.onRestart();
    }

    @Override // android.app.Activity
    protected void onResume() {
        Logger.d("#### " + getClass().getName() + "::onResume ####");
        super.onResume();
        MainProtocol.OnStopFlag = true;
        this.wifi_level_flag = false;
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService("wifi");
        int i = wifiManager.getDhcpInfo().gateway;
        this.ipAddress = String.format("%d.%d.%d.%d", Integer.valueOf(i & 255), Integer.valueOf((i >> 8) & 255), Integer.valueOf((i >> 16) & 255), Integer.valueOf((i >> 24) & 255));
        Logger.d("#### " + getClass().getName() + " ipAddress = " + this.ipAddress);
        SsidInfo();
        ((ConnectivityManager) getSystemService("connectivity")).getActiveNetworkInfo();
        new String();
        wifiManager.getConnectionInfo().getSSID();
        if (this.ipAddress.equals(MainProtocol.SERVERIP)) {
            FileDownDialog();
            this.WiFiLevelUpdateHandler.postDelayed(this.mWiFiLeVelUpdate, 20000L);
            new Handler().postDelayed(new Runnable() { // from class: com.logon_e100.MainFreeView.26
                @Override // java.lang.Runnable
                public void run() {
                    MainFreeView.this.SocKetConnect();
                    if (MainFreeView.this.wifi_connecterror_flag) {
                        new Handler().postDelayed(new Runnable() { // from class: com.logon_e100.MainFreeView.26.1
                            @Override // java.lang.Runnable
                            public void run() {
                                MainFreeView.this.WiFiConnect();
                            }
                        }, 1000L);
                        MainFreeView mainFreeView = MainFreeView.this;
                        mainFreeView.registerReceiver(mainFreeView.rssiReceiver, new IntentFilter("android.net.wifi.RSSI_CHANGED"));
                        new Handler().postDelayed(new Runnable() { // from class: com.logon_e100.MainFreeView.26.2
                            @Override // java.lang.Runnable
                            public void run() {
                                MainProtocol.CNSSimpleIndex cNSSimpleIndex = new MainProtocol.CNSSimpleIndex();
                                cNSSimpleIndex.index = 1;
                                if (MainProtocol.send(41018, cNSSimpleIndex.getBytes(), 0) < 0) {
                                    return;
                                }
                                Logger.d("#### " + getClass().getName() + " 레코드온 성공");
                            }
                        }, 1500L);
                    }
                }
            }, 500L);
            return;
        }
        String string = getResources().getString(R.string.wifi_connection_required);
        this.tvSsid.setText(string);
        ProgressDialog progressDialog = this.main_ProgressDialog44;
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        Toast.makeText(this, string, 0).show();
        WiFi_Error_box();
    }

    @Override // android.app.Activity
    protected void onPause() {
        Logger.d("#### " + getClass().getName() + "onPause");
        super.onPause();
        freeviewstop_flag = 1;
        Handler handler = this.WiFiLevelUpdateHandler;
        if (handler != null) {
            handler.removeCallbacks(this.mWiFiLeVelUpdate);
        }
        int i = ((WifiManager) getApplicationContext().getSystemService("wifi")).getDhcpInfo().gateway;
        if (String.format("%d.%d.%d.%d", Integer.valueOf(i & 255), Integer.valueOf((i >> 8) & 255), Integer.valueOf((i >> 16) & 255), Integer.valueOf((i >> 24) & 255)).equals(MainProtocol.SERVERIP)) {
            new Handler().postDelayed(new Runnable() { // from class: com.logon_e100.MainFreeView.27
                @Override // java.lang.Runnable
                public void run() {
                    MainFreeView.this.PreViewOff();
                }
            }, 500L);
            MainProtocol.CNSSimpleIndex cNSSimpleIndex = new MainProtocol.CNSSimpleIndex();
            cNSSimpleIndex.index = 0;
            if (MainProtocol.send(41018, cNSSimpleIndex.getBytes(), 0) < 0) {
                return;
            }
            Logger.d("#### " + getClass().getName() + " 레코드 OFF 성공");
        }
    }

    @Override // android.app.Activity
    protected void onStop() {
        Logger.d("#### " + getClass().getName() + "::onStop ####");
        super.onStop();
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        Logger.d("#### " + getClass().getName() + "::onDestroy ####");
        super.onDestroy();
    }

    @Override // android.app.Activity, android.view.KeyEvent.Callback
    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        switch (i) {
            case 3:
                break;
            case 4:
                String string = getResources().getString(R.string.app_exit);
                String string2 = getResources().getString(R.string.app_exit_quest);
                String string3 = getResources().getString(R.string.action_yes);
                new AlertDialog.Builder(MainActivity.context).setTitle(string).setMessage(string2).setPositiveButton(string3, new DialogInterface.OnClickListener() { // from class: com.logon_e100.MainFreeView.29
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i2) {
                        WifiManager wifiManager = (WifiManager) MainFreeView.this.getApplicationContext().getSystemService("wifi");
                        MainProtocol.CNSSimpleIndex cNSSimpleIndex = new MainProtocol.CNSSimpleIndex();
                        cNSSimpleIndex.setIndex(0);
                        MainProtocol.send(MainProtocol.NAT_CMD_NET_CONNECT_STATE3, cNSSimpleIndex.getBytes(), -1);
                        try {
                            if (MainProtocol.mSock != null) {
                                MainProtocol.mSock.close();
                                MainProtocol.m_out_stream.close();
                                MainProtocol.m_in_stream.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (wifiManager != null && wifiManager.isWifiEnabled()) {
                            wifiManager.setWifiEnabled(false);
                        }
                        Process.killProcess(Process.myPid());
                    }
                }).setNegativeButton(getResources().getString(R.string.action_no), (DialogInterface.OnClickListener) null).show();
                break;
            default:
                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService("wifi");
                MainProtocol.CNSSimpleIndex cNSSimpleIndex = new MainProtocol.CNSSimpleIndex();
                cNSSimpleIndex.setIndex(0);
                MainProtocol.send(MainProtocol.NAT_CMD_NET_CONNECT_STATE3, cNSSimpleIndex.getBytes(), -1);
                try {
                    if (MainProtocol.mSock != null) {
                        MainProtocol.mSock.close();
                        MainProtocol.m_out_stream.close();
                        MainProtocol.m_in_stream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (wifiManager.isWifiEnabled()) {
                    wifiManager.setWifiEnabled(false);
                }
                Process.killProcess(Process.myPid());
                break;
        }
        return super.onKeyDown(i, keyEvent);
    }

    public void displayPlayFailedAlert(String str, String str2) {
        String string = getResources().getString(R.string.action_confirm);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(str2).setCancelable(false).setPositiveButton(string, new DialogInterface.OnClickListener() { // from class: com.logon_e100.MainFreeView.30
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                MainProtocol.CNSSimpleIndex cNSSimpleIndex = new MainProtocol.CNSSimpleIndex();
                cNSSimpleIndex.setIndex(0);
                MainProtocol.send(MainProtocol.NAT_CMD_NET_CONNECT_STATE3, cNSSimpleIndex.getBytes(), -1);
                if (MainProtocol.mSock != null) {
                    try {
                        MainProtocol.mSock.close();
                        MainProtocol.m_out_stream.close();
                        MainProtocol.m_in_stream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (MainFreeView.wifiManager2 != null && MainFreeView.wifiManager2.isWifiEnabled()) {
                    MainFreeView.wifiManager2.setWifiEnabled(false);
                }
                Process.killProcess(Process.myPid());
            }
        });
        AlertDialog create = builder.create();
        create.setTitle(str);
        create.setIcon(R.drawable.ic_launcher);
        create.show();
    }

    private class TCPclient implements Runnable {
        private TCPclient() {
        }

        @Override // java.lang.Runnable
        public void run() {
            MainFreeView mainFreeView;
            try {
                try {
                    try {
                        MainFreeView.this.SocKetConnect();
                        if (MainFreeView.this.wifi_connecterror_flag) {
                            new Handler().postDelayed(new Runnable() { // from class: com.logon_e100.MainFreeView.TCPclient.1
                                @Override // java.lang.Runnable
                                public void run() {
                                    MainFreeView.this.WiFiConnect();
                                }
                            }, 500L);
                            MainProtocol.CNSSimpleIndex cNSSimpleIndex = new MainProtocol.CNSSimpleIndex();
                            cNSSimpleIndex.index = 1;
                            if (MainProtocol.send(41018, cNSSimpleIndex.getBytes(), 0) >= 0) {
                                Logger.d("#### " + getClass().getName() + "레코드온 성공");
                            }
                        }
                        mainFreeView = MainFreeView.this;
                    } catch (Throwable th) {
                        MainFreeView.this.t.interrupt();
                        throw th;
                    }
                } catch (Exception unused) {
                    mainFreeView = MainFreeView.this;
                }
                mainFreeView.t.interrupt();
            } catch (Exception unused2) {
            }
        }
    }

    public void WiFi_Error_box() {
        String string = getResources().getString(R.string.wifi_connect_error1);
        Locale locale = getResources().getConfiguration().locale;
        locale.getDisplayCountry();
        String country = locale.getCountry();
        Logger.d("MainFreeView# country = " + country);
        String string2 = getResources().getString(R.string.wifi_connect_error4);
        String string3 = getResources().getString(R.string.wifi_connect_error3);
        this.alert = new AlertDialog.Builder(mContext);
        View inflate = LayoutInflater.from(mContext).inflate(R.layout.dialog_main, (ViewGroup) null);
        ImageView imageView = (ImageView) inflate.findViewById(R.id.imageViewDialog);
        if (!country.equals("KR")) {
            imageView.setImageResource(R.drawable.wifi_box_eng);
        } else {
            imageView.setImageResource(R.drawable.wifi_box);
        }
        if (country.equals("TW") || country.equals("HK") || country.equals("CN")) {
            imageView.setImageResource(R.drawable.wifi_box_chn);
        }
        this.alert.setTitle(string);
        this.alert.setView(inflate);
        this.alert.setPositiveButton(string2, new DialogInterface.OnClickListener() { // from class: com.logon_e100.MainFreeView.31
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                WifiManager wifiManager = (WifiManager) MainFreeView.this.getApplicationContext().getSystemService("wifi");
                if (wifiManager != null && !wifiManager.isWifiEnabled()) {
                    wifiManager.setWifiEnabled(true);
                }
                MainProtocol.OnStopFlag = false;
                Intent intent = new Intent("android.settings.WIFI_SETTINGS");
                intent.setFlags(268435456);
                MainFreeView.this.startActivity(intent);
            }
        }).setNegativeButton(string3, (DialogInterface.OnClickListener) null);
        this.alert.show();
    }

    public void WiFiLevelCheck() {
        if (!this.wifi_level_flag) {
            Logger.d("#### " + getClass().getName() + " WiFiLevelCheck");
            ProgressDialog progressDialog = this.main_ProgressDialog44;
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
            if (this.mAnotherPhone != 1) {
                displayPlayFailedAlert(mContext.getResources().getString(R.string.preview_error_title), mContext.getResources().getString(R.string.preview_error_wifi_level_failed));
                return;
            }
            Logger.e("mAnotherPhone" + this.mAnotherPhone);
            return;
        }
        Logger.d("#### " + getClass().getName() + " WiFiLevelCheck 성공");
    }

    public void viewzoom(int i) {
        if (i == 0) {
            this.width = 1280;
            this.height = 720;
            new Handler().postDelayed(new Runnable() { // from class: com.logon_e100.MainFreeView.34
                @Override // java.lang.Runnable
                public void run() {
                    MainFreeView mainFreeView = MainFreeView.this;
                    mainFreeView.mAttacher = new PhotoViewAttacher(mainFreeView.tmpIv);
                    MainFreeView.this.mAttacher.setScaleType(ImageView.ScaleType.FIT_XY);
                }
            }, 250L);
        } else {
            this.width = 720;
            this.height = 640;
            new Handler().postDelayed(new Runnable() { // from class: com.logon_e100.MainFreeView.35
                @Override // java.lang.Runnable
                public void run() {
                    MainFreeView mainFreeView = MainFreeView.this;
                    mainFreeView.mAttacher = new PhotoViewAttacher(mainFreeView.tmpIv);
                    MainFreeView.this.mAttacher.setScaleType(ImageView.ScaleType.FIT_XY);
                    MainFreeView.this.mTempRelativeLayout.setVisibility(0);
                    MainFreeView.this.mTopRelativeLayout.setVisibility(0);
                    MainFreeView.this.mBottomLinearLayout.setVisibility(0);
                    MainActivity.tabHost.getTabWidget().setVisibility(0);
                    MainFreeView.this.tmpIv.setLayoutParams(new RelativeLayout.LayoutParams(-1, -2));
                    MainFreeView.this.mAttacher.cleanup();
                }
            }, 250L);
        }
    }
}
