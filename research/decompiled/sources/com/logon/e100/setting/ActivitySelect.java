package com.logon.e100.setting;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.logon_e100.MainActivity;
import com.logon_e100.MainProtocol;
import com.logon_e100.R;
import java.io.IOException;
import java.util.ArrayList;

/* loaded from: classes.dex */
public class ActivitySelect extends Activity {
    private static CNSConfig mConfig;
    private static Context mContext;
    private static ListView mListView;
    private static ProgressDialog mProgressDialog;
    private static int mSelectedOpt;
    private static SettingSelectAdapter mSettingSelectAdapter;
    private ArrayList<SettingSelectItem> mAlSettingSelectList;
    private Handler mHandler;
    private int mMunuPostion;
    static final Handler listUpdateHandler = new Handler() { // from class: com.logon.e100.setting.ActivitySelect.7
        @Override // android.os.Handler
        public void handleMessage(Message message) {
            ActivitySelect.mSettingSelectAdapter.notifyDataSetChanged();
        }
    };
    static final Handler listEnableHandler = new Handler() { // from class: com.logon.e100.setting.ActivitySelect.8
        @Override // android.os.Handler
        public void handleMessage(Message message) {
            ActivitySelect.mListView.setEnabled(true);
        }
    };
    static final Handler displayAlertHandler = new Handler() { // from class: com.logon.e100.setting.ActivitySelect.9
        @Override // android.os.Handler
        public void handleMessage(Message message) {
            String[] strArr = (String[]) message.obj;
            ActivitySelect.displayInfoAlert(strArr[0], strArr[1]);
        }
    };
    static final Handler handler = new Handler() { // from class: com.logon.e100.setting.ActivitySelect.10
        @Override // android.os.Handler
        public void handleMessage(Message message) {
            ProgressDialog unused = ActivitySelect.mProgressDialog = ProgressDialog.show(ActivitySelect.mContext, "", ActivitySelect.mContext.getResources().getString(R.string.blackbox_reboot_info), true);
            new Handler().postDelayed(new Runnable() { // from class: com.logon.e100.setting.ActivitySelect.10.1
                @Override // java.lang.Runnable
                public void run() {
                    if (ActivitySelect.mProgressDialog != null) {
                        ActivitySelect.mProgressDialog.dismiss();
                    }
                    MainProtocol.CNSSimpleIndex cNSSimpleIndex = new MainProtocol.CNSSimpleIndex();
                    cNSSimpleIndex.setIndex(0);
                    ((ActivitySetting) ActivitySetting.mContext).finish();
                    MainProtocol.send(MainProtocol.NAT_CMD_NET_CONNECT_STATE3, cNSSimpleIndex.getBytes(), -1);
                    ActivitySelect.mContext.startActivity(new Intent(ActivitySelect.mContext, (Class<?>) MainActivity.class));
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
            }, 45000L);
        }
    };
    private Toast mToast = null;
    AdapterView.OnItemClickListener ibSettingSelectClickListener = new AdapterView.OnItemClickListener() { // from class: com.logon.e100.setting.ActivitySelect.6
        @Override // android.widget.AdapterView.OnItemClickListener
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long j) {
            Log.d("mhhan", "#### menu idx = " + ActivitySelect.this.mMunuPostion + " sel = " + i + " ####");
            ActivitySelect.mListView.setEnabled(false);
            if (!ActivitySelect.mSettingSelectAdapter.getEnable()) {
                ActivitySelect.this.displayInfoToast(ActivitySelect.this.getResources().getString(R.string.setting_error));
            }
            MainProtocol.CNSSimpleIndex cNSSimpleIndex = new MainProtocol.CNSSimpleIndex();
            int unused = ActivitySelect.mSelectedOpt = i;
            switch (ActivitySelect.this.mMunuPostion) {
                case 3:
                    if (ActivitySelect.mConfig.nRecordQuality == i) {
                        ActivitySelect.mListView.setEnabled(true);
                        break;
                    } else {
                        ActivitySelect.this.displayRecorderQualityAlert(i);
                        break;
                    }
                case 4:
                    if (ActivitySelect.mConfig.nNormalImpactSensitivity == i) {
                        ActivitySelect.mListView.setEnabled(true);
                        break;
                    } else {
                        cNSSimpleIndex.setIndex(i);
                        if (MainProtocol.send(MainProtocol.NAT_CMD_SET_NORMAL_IMPACT_SENSITIVITY, cNSSimpleIndex.getBytes(), 3) < 0) {
                            ActivitySelect.displayInfoAlert(ActivitySelect.this.getResources().getString(R.string.setting_title), ActivitySelect.this.getResources().getString(R.string.setting_save_error));
                            ActivitySelect.mSettingSelectAdapter.setEnable(false);
                            break;
                        }
                    }
                    break;
                case 5:
                    if (ActivitySelect.mConfig.nVoiceVolume == i) {
                        ActivitySelect.mListView.setEnabled(true);
                        break;
                    } else {
                        cNSSimpleIndex.setIndex(i);
                        if (MainProtocol.send(MainProtocol.NAT_CMD_SET_VOICE_VOLUME, cNSSimpleIndex.getBytes(), 3) < 0) {
                            ActivitySelect.displayInfoAlert(ActivitySelect.this.getResources().getString(R.string.setting_title), ActivitySelect.this.getResources().getString(R.string.setting_save_error));
                            ActivitySelect.mSettingSelectAdapter.setEnable(false);
                            break;
                        }
                    }
                    break;
                case 6:
                    if (ActivitySelect.mConfig.nMute == i) {
                        ActivitySelect.mListView.setEnabled(true);
                        break;
                    } else {
                        cNSSimpleIndex.setIndex(i);
                        if (MainProtocol.send(MainProtocol.NAT_CMD_SET_MUTE, cNSSimpleIndex.getBytes(), 3) < 0) {
                            ActivitySelect.displayInfoAlert(ActivitySelect.this.getResources().getString(R.string.setting_title), ActivitySelect.this.getResources().getString(R.string.setting_save_error));
                            ActivitySelect.mSettingSelectAdapter.setEnable(false);
                            break;
                        }
                    }
                    break;
                case 7:
                    if (ActivitySelect.mConfig.nLCDBrightness == i) {
                        ActivitySelect.mListView.setEnabled(true);
                        break;
                    } else {
                        cNSSimpleIndex.setIndex(i);
                        if (MainProtocol.send(MainProtocol.NAT_CMD_SET_LCD_BRIGHTNESS, cNSSimpleIndex.getBytes(), 3) < 0) {
                            ActivitySelect.displayInfoAlert(ActivitySelect.this.getResources().getString(R.string.setting_title), ActivitySelect.this.getResources().getString(R.string.setting_save_error));
                            ActivitySelect.mSettingSelectAdapter.setEnable(false);
                            break;
                        }
                    }
                    break;
                case 8:
                    if (ActivitySelect.mConfig.nWhiteBalance == i) {
                        ActivitySelect.mListView.setEnabled(true);
                        break;
                    } else {
                        cNSSimpleIndex.setIndex(i);
                        if (MainProtocol.send(MainProtocol.NAT_CMD_SET_WHITE_BALANCE, cNSSimpleIndex.getBytes(), 3) < 0) {
                            ActivitySelect.displayInfoAlert(ActivitySelect.this.getResources().getString(R.string.setting_title), ActivitySelect.this.getResources().getString(R.string.setting_save_error));
                            ActivitySelect.mSettingSelectAdapter.setEnable(false);
                            break;
                        }
                    }
                    break;
                case 9:
                    if (ActivitySelect.mConfig.nContrast == i) {
                        ActivitySelect.mListView.setEnabled(true);
                        break;
                    } else {
                        cNSSimpleIndex.setIndex(i);
                        if (MainProtocol.send(MainProtocol.NAT_CMD_SET_CONTRAST, cNSSimpleIndex.getBytes(), 3) < 0) {
                            ActivitySelect.displayInfoAlert(ActivitySelect.this.getResources().getString(R.string.setting_title), ActivitySelect.this.getResources().getString(R.string.setting_save_error));
                            ActivitySelect.mSettingSelectAdapter.setEnable(false);
                            break;
                        }
                    }
                    break;
                case 10:
                    if (ActivitySelect.mConfig.nExposure == i) {
                        ActivitySelect.mListView.setEnabled(true);
                        break;
                    } else {
                        cNSSimpleIndex.setIndex(i);
                        if (MainProtocol.send(MainProtocol.NAT_CMD_SET_EXPOSURE, cNSSimpleIndex.getBytes(), 3) < 0) {
                            ActivitySelect.displayInfoAlert(ActivitySelect.this.getResources().getString(R.string.setting_title), ActivitySelect.this.getResources().getString(R.string.setting_save_error));
                            ActivitySelect.mSettingSelectAdapter.setEnable(false);
                            break;
                        }
                    }
                    break;
                case 11:
                    if (ActivitySelect.mConfig.nSwitchScreen == i) {
                        ActivitySelect.mListView.setEnabled(true);
                        break;
                    } else {
                        cNSSimpleIndex.setIndex(i);
                        if (MainProtocol.send(MainProtocol.NAT_CMD_SET_SWITCH_SCREEN, cNSSimpleIndex.getBytes(), 3) < 0) {
                            ActivitySelect.displayInfoAlert(ActivitySelect.this.getResources().getString(R.string.setting_title), ActivitySelect.this.getResources().getString(R.string.setting_save_error));
                            ActivitySelect.mSettingSelectAdapter.setEnable(false);
                            break;
                        }
                    }
                    break;
                case 12:
                case 20:
                default:
                    ActivitySelect.mListView.setEnabled(true);
                    Log.d("mhhan", "what????????");
                    break;
                case 13:
                    if (ActivitySelect.mConfig.nCrashEventAlarm == i) {
                        ActivitySelect.mListView.setEnabled(true);
                        break;
                    } else {
                        cNSSimpleIndex.setIndex(i);
                        if (MainProtocol.send(MainProtocol.NAT_CMD_SET_CRASH_EVENT_ALARM, cNSSimpleIndex.getBytes(), 3) < 0) {
                            ActivitySelect.displayInfoAlert(ActivitySelect.this.getResources().getString(R.string.setting_title), ActivitySelect.this.getResources().getString(R.string.setting_save_error));
                            ActivitySelect.mSettingSelectAdapter.setEnable(false);
                            break;
                        }
                    }
                    break;
                case 14:
                    if (ActivitySelect.mConfig.nParkingLowPower == i) {
                        ActivitySelect.mListView.setEnabled(true);
                        break;
                    } else {
                        cNSSimpleIndex.setIndex(i);
                        if (MainProtocol.send(MainProtocol.NAT_CMD_SET_PARKING_LOW_POWER, cNSSimpleIndex.getBytes(), 3) < 0) {
                            ActivitySelect.displayInfoAlert(ActivitySelect.this.getResources().getString(R.string.setting_title), ActivitySelect.this.getResources().getString(R.string.setting_save_error));
                            ActivitySelect.mSettingSelectAdapter.setEnable(false);
                            break;
                        }
                    }
                    break;
                case 15:
                    if (ActivitySelect.mConfig.nParkImpactSensitivity == i) {
                        ActivitySelect.mListView.setEnabled(true);
                        break;
                    } else {
                        cNSSimpleIndex.setIndex(i);
                        if (MainProtocol.send(MainProtocol.NAT_CMD_SET_PARK_IMPACT_SENSITIVITY, cNSSimpleIndex.getBytes(), 3) < 0) {
                            ActivitySelect.displayInfoAlert(ActivitySelect.this.getResources().getString(R.string.setting_title), ActivitySelect.this.getResources().getString(R.string.setting_save_error));
                            ActivitySelect.mSettingSelectAdapter.setEnable(false);
                            break;
                        }
                    }
                    break;
                case 16:
                    if (ActivitySelect.mConfig.nParkingOPTime == i) {
                        ActivitySelect.mListView.setEnabled(true);
                        break;
                    } else {
                        cNSSimpleIndex.setIndex(i);
                        if (MainProtocol.send(MainProtocol.NAT_CMD_SET_PARKING_OPTIME, cNSSimpleIndex.getBytes(), 3) < 0) {
                            ActivitySelect.displayInfoAlert(ActivitySelect.this.getResources().getString(R.string.setting_title), ActivitySelect.this.getResources().getString(R.string.setting_save_error));
                            ActivitySelect.mSettingSelectAdapter.setEnable(false);
                            break;
                        }
                    }
                    break;
                case 17:
                    if (ActivitySelect.mConfig.nTimeZone == i) {
                        ActivitySelect.mListView.setEnabled(true);
                        break;
                    } else {
                        cNSSimpleIndex.setIndex(i);
                        if (MainProtocol.send(MainProtocol.NAT_CMD_SET_TIMEZONE, cNSSimpleIndex.getBytes(), 3) < 0) {
                            ActivitySelect.displayInfoAlert(ActivitySelect.this.getResources().getString(R.string.setting_title), ActivitySelect.this.getResources().getString(R.string.setting_save_error));
                            ActivitySelect.mSettingSelectAdapter.setEnable(false);
                            break;
                        }
                    }
                    break;
                case 18:
                    if (ActivitySelect.mConfig.nVoiceGuide == i) {
                        ActivitySelect.mListView.setEnabled(true);
                        break;
                    } else {
                        cNSSimpleIndex.setIndex(i);
                        if (MainProtocol.send(MainProtocol.NAT_CMD_SET_VOICE_GUIDE, cNSSimpleIndex.getBytes(), 3) < 0) {
                            ActivitySelect.displayInfoAlert(ActivitySelect.this.getResources().getString(R.string.setting_title), ActivitySelect.this.getResources().getString(R.string.setting_save_error));
                            ActivitySelect.mSettingSelectAdapter.setEnable(false);
                            break;
                        }
                    }
                    break;
                case 19:
                    if (ActivitySelect.mConfig.uLed == i) {
                        ActivitySelect.mListView.setEnabled(true);
                        break;
                    } else {
                        cNSSimpleIndex.setIndex(i);
                        if (MainProtocol.send(MainProtocol.NAT_CMD_SET_USE_LED, cNSSimpleIndex.getBytes(), 3) < 0) {
                            ActivitySelect.displayInfoAlert(ActivitySelect.this.getResources().getString(R.string.setting_title), ActivitySelect.this.getResources().getString(R.string.setting_save_error));
                            ActivitySelect.mSettingSelectAdapter.setEnable(false);
                            break;
                        }
                    }
                    break;
                case 21:
                    if (ActivitySelect.mConfig.nPartition == i) {
                        ActivitySelect.mListView.setEnabled(true);
                        break;
                    } else {
                        ActivitySelect.this.displayPartitionAlert(i);
                        break;
                    }
            }
        }
    };

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setTheme(android.R.style.Theme.Holo.Light.NoActionBar.TranslucentDecor);
        setContentView(R.layout.activity_select);
        mContext = this;
        Intent intent = getIntent();
        this.mMunuPostion = intent.getIntExtra("logon.e100.menu.position", 0);
        mConfig = (CNSConfig) intent.getParcelableExtra("logon.e100.config");
        mListView = (ListView) findViewById(R.id.setting_select_listview);
        this.mAlSettingSelectList = getSettingListItems(this.mMunuPostion);
        mSettingSelectAdapter = new SettingSelectAdapter(this, this.mAlSettingSelectList, mConfig, this.mMunuPostion, true);
        mListView.setAdapter((ListAdapter) mSettingSelectAdapter);
        mListView.setOnItemClickListener(this.ibSettingSelectClickListener);
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

    @Override // android.app.Activity, android.view.KeyEvent.Callback
    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        if (i == 4) {
            Intent intent = new Intent();
            intent.putExtra("logon.e100.config", mConfig);
            intent.putExtra("logon.e100.enable", mSettingSelectAdapter.getEnable());
            setResult(-1, intent);
        }
        return super.onKeyDown(i, keyEvent);
    }

    public void displayInfoToast(String str) {
        Toast toast = this.mToast;
        if (toast == null) {
            this.mToast = Toast.makeText(mContext, str, 1);
        } else {
            toast.setText(str);
        }
        this.mToast.show();
    }

    public static void displayInfoAlert(String str, String str2) {
        mListView.setEnabled(true);
        String string = mContext.getResources().getString(R.string.action_confirm);
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(str2).setCancelable(false).setPositiveButton(string, new DialogInterface.OnClickListener() { // from class: com.logon.e100.setting.ActivitySelect.1
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        AlertDialog create = builder.create();
        create.setTitle(str);
        create.setIcon(R.drawable.ic_launcher);
        create.show();
    }

    public void displayRecorderQualityAlert(final int i) {
        final String string = getResources().getString(R.string.setting_recorder_quality_title);
        String string2 = getResources().getString(R.string.setting_recorder_quality_info);
        String string3 = getResources().getString(R.string.action_confirm);
        String string4 = getResources().getString(R.string.action_cancel);
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(string2).setCancelable(false).setPositiveButton(string3, new DialogInterface.OnClickListener() { // from class: com.logon.e100.setting.ActivitySelect.3
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i2) {
                MainProtocol.CNSSimpleIndex cNSSimpleIndex = new MainProtocol.CNSSimpleIndex();
                cNSSimpleIndex.setIndex(i);
                if (MainProtocol.send(MainProtocol.NAT_CMD_SET_RECORDER_QUALITY, cNSSimpleIndex.getBytes(), 3) < 0) {
                    ActivitySelect.displayInfoAlert(string, ActivitySelect.this.getResources().getString(R.string.setting_recorder_quality_error));
                }
            }
        }).setNegativeButton(string4, new DialogInterface.OnClickListener() { // from class: com.logon.e100.setting.ActivitySelect.2
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i2) {
                ActivitySelect.mListView.setEnabled(true);
                dialogInterface.cancel();
            }
        });
        AlertDialog create = builder.create();
        create.setTitle(string);
        create.setIcon(R.drawable.ic_launcher);
        create.show();
    }

    public void displayPartitionAlert(final int i) {
        final String string = getResources().getString(R.string.setting_partition_title);
        String string2 = getResources().getString(R.string.setting_partition_info);
        String string3 = getResources().getString(R.string.action_confirm);
        String string4 = getResources().getString(R.string.action_cancel);
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(string2).setCancelable(false).setPositiveButton(string3, new DialogInterface.OnClickListener() { // from class: com.logon.e100.setting.ActivitySelect.5
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i2) {
                MainProtocol.CNSSimpleIndex cNSSimpleIndex = new MainProtocol.CNSSimpleIndex();
                cNSSimpleIndex.setIndex(i);
                if (MainProtocol.send(MainProtocol.NAT_CMD_SET_PARTITION, cNSSimpleIndex.getBytes(), 3) < 0) {
                    ActivitySelect.displayInfoAlert(string, ActivitySelect.this.getResources().getString(R.string.setting_partition_error));
                }
            }
        }).setNegativeButton(string4, new DialogInterface.OnClickListener() { // from class: com.logon.e100.setting.ActivitySelect.4
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i2) {
                ActivitySelect.mListView.setEnabled(true);
                dialogInterface.cancel();
            }
        });
        AlertDialog create = builder.create();
        create.setTitle(string);
        create.setIcon(R.drawable.ic_launcher);
        create.show();
    }

    public ArrayList<SettingSelectItem> getSettingListItems(int i) {
        ArrayList<SettingSelectItem> arrayList = new ArrayList<>();
        for (int i2 = 0; i2 < ActivitySetting.settingDefs.SETTING_ROW[i].length; i2++) {
            SettingSelectItem settingSelectItem = new SettingSelectItem();
            settingSelectItem.setTitle(ActivitySetting.settingDefs.SETTING_ROW[i][i2]);
            arrayList.add(settingSelectItem);
        }
        return arrayList;
    }

    public static void onReceive(int i, byte[] bArr) {
        MainProtocol.CNSResult cNSResult = new MainProtocol.CNSResult(bArr);
        Log.d("mhhan", "#### onReceive cmd = " + i + " result = " + cNSResult.getResult() + " ####");
        listEnableHandler.sendMessage(listEnableHandler.obtainMessage());
        switch (i) {
            case MainProtocol.NAT_CMD_SET_NORMAL_IMPACT_SENSITIVITY_RESP /* 40865 */:
                if (cNSResult.getResult() < 0) {
                    String[] strArr = {mContext.getResources().getString(R.string.setting_title), mContext.getResources().getString(R.string.setting_save_error)};
                    Message obtainMessage = displayAlertHandler.obtainMessage();
                    obtainMessage.obj = strArr;
                    displayAlertHandler.sendMessage(obtainMessage);
                    break;
                } else {
                    mConfig.nNormalImpactSensitivity = mSelectedOpt;
                    break;
                }
            case MainProtocol.NAT_CMD_SET_VOICE_VOLUME_RESP /* 40867 */:
                if (cNSResult.getResult() < 0) {
                    String[] strArr2 = {mContext.getResources().getString(R.string.setting_title), mContext.getResources().getString(R.string.setting_save_error)};
                    Message obtainMessage2 = displayAlertHandler.obtainMessage();
                    obtainMessage2.obj = strArr2;
                    displayAlertHandler.sendMessage(obtainMessage2);
                    break;
                } else {
                    mConfig.nVoiceVolume = mSelectedOpt;
                    break;
                }
            case MainProtocol.NAT_CMD_SET_LCD_BRIGHTNESS_RESP /* 40869 */:
                if (cNSResult.getResult() < 0) {
                    String[] strArr3 = {mContext.getResources().getString(R.string.setting_title), mContext.getResources().getString(R.string.setting_save_error)};
                    Message obtainMessage3 = displayAlertHandler.obtainMessage();
                    obtainMessage3.obj = strArr3;
                    displayAlertHandler.sendMessage(obtainMessage3);
                    break;
                } else {
                    mConfig.nLCDBrightness = mSelectedOpt;
                    break;
                }
            case MainProtocol.NAT_CMD_SET_CONTRAST_RESP /* 40871 */:
                if (cNSResult.getResult() < 0) {
                    String[] strArr4 = {mContext.getResources().getString(R.string.setting_title), mContext.getResources().getString(R.string.setting_save_error)};
                    Message obtainMessage4 = displayAlertHandler.obtainMessage();
                    obtainMessage4.obj = strArr4;
                    displayAlertHandler.sendMessage(obtainMessage4);
                    break;
                } else {
                    mConfig.nContrast = mSelectedOpt;
                    break;
                }
            case MainProtocol.NAT_CMD_SET_SWITCH_SCREEN_RESP /* 40873 */:
                if (cNSResult.getResult() < 0) {
                    String[] strArr5 = {mContext.getResources().getString(R.string.setting_title), mContext.getResources().getString(R.string.setting_save_error)};
                    Message obtainMessage5 = displayAlertHandler.obtainMessage();
                    obtainMessage5.obj = strArr5;
                    displayAlertHandler.sendMessage(obtainMessage5);
                    break;
                } else {
                    mConfig.nSwitchScreen = mSelectedOpt;
                    break;
                }
            case MainProtocol.NAT_CMD_SET_PARKING_LOW_POWER_RESP /* 40875 */:
                if (cNSResult.getResult() < 0) {
                    String[] strArr6 = {mContext.getResources().getString(R.string.setting_title), mContext.getResources().getString(R.string.setting_save_error)};
                    Message obtainMessage6 = displayAlertHandler.obtainMessage();
                    obtainMessage6.obj = strArr6;
                    displayAlertHandler.sendMessage(obtainMessage6);
                    break;
                } else {
                    mConfig.nParkingLowPower = mSelectedOpt;
                    break;
                }
            case MainProtocol.NAT_CMD_SET_PARKING_OPTIME_RESP /* 40877 */:
                if (cNSResult.getResult() < 0) {
                    String[] strArr7 = {mContext.getResources().getString(R.string.setting_title), mContext.getResources().getString(R.string.setting_save_error)};
                    Message obtainMessage7 = displayAlertHandler.obtainMessage();
                    obtainMessage7.obj = strArr7;
                    displayAlertHandler.sendMessage(obtainMessage7);
                    break;
                } else {
                    mConfig.nParkingOPTime = mSelectedOpt;
                    break;
                }
            case MainProtocol.NAT_CMD_SET_TIMEZONE_RESP /* 40879 */:
                if (cNSResult.getResult() < 0) {
                    String[] strArr8 = {mContext.getResources().getString(R.string.setting_title), mContext.getResources().getString(R.string.setting_save_error)};
                    Message obtainMessage8 = displayAlertHandler.obtainMessage();
                    obtainMessage8.obj = strArr8;
                    displayAlertHandler.sendMessage(obtainMessage8);
                    break;
                } else {
                    mConfig.nTimeZone = mSelectedOpt;
                    break;
                }
            case MainProtocol.NAT_CMD_SET_VOICE_GUIDE_RESP /* 40881 */:
                if (cNSResult.getResult() < 0) {
                    String[] strArr9 = {mContext.getResources().getString(R.string.setting_title), mContext.getResources().getString(R.string.setting_save_error)};
                    Message obtainMessage9 = displayAlertHandler.obtainMessage();
                    obtainMessage9.obj = strArr9;
                    displayAlertHandler.sendMessage(obtainMessage9);
                    break;
                } else {
                    mConfig.nVoiceGuide = mSelectedOpt;
                    break;
                }
            case MainProtocol.NAT_CMD_SET_PARTITION_RESP /* 40883 */:
                if (cNSResult.getResult() < 0) {
                    String[] strArr10 = {mContext.getResources().getString(R.string.setting_partition_title), mContext.getResources().getString(R.string.setting_partition_error)};
                    Message obtainMessage10 = displayAlertHandler.obtainMessage();
                    obtainMessage10.obj = strArr10;
                    displayAlertHandler.sendMessage(obtainMessage10);
                    break;
                } else {
                    mConfig.nPartition = mSelectedOpt;
                    handler.sendMessage(handler.obtainMessage());
                    break;
                }
            case MainProtocol.NAT_CMD_SET_CRASH_EVENT_ALARM_RESP /* 40885 */:
                if (cNSResult.getResult() < 0) {
                    String[] strArr11 = {mContext.getResources().getString(R.string.setting_title), mContext.getResources().getString(R.string.setting_save_error)};
                    Message obtainMessage11 = displayAlertHandler.obtainMessage();
                    obtainMessage11.obj = strArr11;
                    displayAlertHandler.sendMessage(obtainMessage11);
                    break;
                } else {
                    mConfig.nCrashEventAlarm = mSelectedOpt;
                    break;
                }
            case MainProtocol.NAT_CMD_SET_USE_LED_RESP /* 40893 */:
                if (cNSResult.getResult() < 0) {
                    String[] strArr12 = {mContext.getResources().getString(R.string.setting_title), mContext.getResources().getString(R.string.setting_save_error)};
                    Message obtainMessage12 = displayAlertHandler.obtainMessage();
                    obtainMessage12.obj = strArr12;
                    displayAlertHandler.sendMessage(obtainMessage12);
                    break;
                } else {
                    mConfig.uLed = mSelectedOpt;
                    break;
                }
            case MainProtocol.NAT_CMD_SET_RECORDER_QUALITY_RESP /* 40962 */:
                if (cNSResult.getResult() < 0) {
                    String[] strArr13 = {mContext.getResources().getString(R.string.setting_recorder_quality_title), mContext.getResources().getString(R.string.setting_recorder_quality_error)};
                    Message obtainMessage13 = displayAlertHandler.obtainMessage();
                    obtainMessage13.obj = strArr13;
                    displayAlertHandler.sendMessage(obtainMessage13);
                    break;
                } else {
                    mConfig.nRecordQuality = mSelectedOpt;
                    handler.sendMessage(handler.obtainMessage());
                    break;
                }
            case MainProtocol.NAT_CMD_SET_WHITE_BALANCE_RESP /* 40968 */:
                if (cNSResult.getResult() < 0) {
                    String[] strArr14 = {mContext.getResources().getString(R.string.setting_title), mContext.getResources().getString(R.string.setting_save_error)};
                    Message obtainMessage14 = displayAlertHandler.obtainMessage();
                    obtainMessage14.obj = strArr14;
                    displayAlertHandler.sendMessage(obtainMessage14);
                    break;
                } else {
                    mConfig.nWhiteBalance = mSelectedOpt;
                    break;
                }
            case MainProtocol.NAT_CMD_SET_EXPOSURE_RESP /* 40970 */:
                if (cNSResult.getResult() < 0) {
                    String[] strArr15 = {mContext.getResources().getString(R.string.setting_title), mContext.getResources().getString(R.string.setting_save_error)};
                    Message obtainMessage15 = displayAlertHandler.obtainMessage();
                    obtainMessage15.obj = strArr15;
                    displayAlertHandler.sendMessage(obtainMessage15);
                    break;
                } else {
                    mConfig.nExposure = mSelectedOpt;
                    break;
                }
            case MainProtocol.NAT_CMD_SET_MUTE_RESP /* 40974 */:
                if (cNSResult.getResult() < 0) {
                    String[] strArr16 = {mContext.getResources().getString(R.string.setting_title), mContext.getResources().getString(R.string.setting_save_error)};
                    Message obtainMessage16 = displayAlertHandler.obtainMessage();
                    obtainMessage16.obj = strArr16;
                    displayAlertHandler.sendMessage(obtainMessage16);
                    break;
                } else {
                    mConfig.nMute = mSelectedOpt;
                    break;
                }
            case MainProtocol.NAT_CMD_SET_PARK_IMPACT_SENSITIVITY_RESP /* 40986 */:
                if (cNSResult.getResult() < 0) {
                    String[] strArr17 = {mContext.getResources().getString(R.string.setting_title), mContext.getResources().getString(R.string.setting_save_error)};
                    Message obtainMessage17 = displayAlertHandler.obtainMessage();
                    obtainMessage17.obj = strArr17;
                    displayAlertHandler.sendMessage(obtainMessage17);
                    break;
                } else {
                    mConfig.nParkImpactSensitivity = mSelectedOpt;
                    break;
                }
            default:
                Log.d("mhhan", "what????????");
                break;
        }
        if (cNSResult.getResult() >= 0) {
            listUpdateHandler.sendMessage(listUpdateHandler.obtainMessage());
        }
    }

    public class SettingSelectAdapter extends BaseAdapter {
        private Context context;
        private ArrayList<SettingSelectItem> item;
        private CNSConfig mConfig;
        private boolean mEnable;
        private int menuPos;

        @Override // android.widget.Adapter
        public Object getItem(int i) {
            return null;
        }

        @Override // android.widget.Adapter
        public long getItemId(int i) {
            return 0L;
        }

        public SettingSelectAdapter(Context context, ArrayList<SettingSelectItem> arrayList, CNSConfig cNSConfig, int i, boolean z) {
            this.context = context;
            this.item = arrayList;
            this.menuPos = i;
            this.mConfig = cNSConfig;
            this.mEnable = z;
        }

        @Override // android.widget.Adapter
        public int getCount() {
            return this.item.size();
        }

        @Override // android.widget.Adapter
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder = new ViewHolder();
            if (view == null) {
                view = LayoutInflater.from(this.context).inflate(R.layout.setting_select_item, viewGroup, false);
            }
            viewHolder.tvTitle = (TextView) view.findViewById(R.id.textViewTitle);
            viewHolder.ivCheck = (ImageView) view.findViewById(R.id.imageViewNavigationAccept);
            viewHolder.tvTitle.setText(this.item.get(i).getTitle());
            if (isChecked(i)) {
                viewHolder.ivCheck.setVisibility(0);
            } else {
                viewHolder.ivCheck.setVisibility(8);
            }
            return view;
        }

        public void setEnable(boolean z) {
            this.mEnable = z;
        }

        public boolean getEnable() {
            return this.mEnable;
        }

        public void invalidateAdapterItems(ArrayList<SettingSelectItem> arrayList) {
            this.item = arrayList;
        }

        public boolean isChecked(int i) {
            switch (this.menuPos) {
                case 3:
                    return i == this.mConfig.nRecordQuality;
                case 4:
                    return i == this.mConfig.nNormalImpactSensitivity;
                case 5:
                    return i == this.mConfig.nVoiceVolume;
                case 6:
                    return i == this.mConfig.nMute;
                case 7:
                    return i == this.mConfig.nLCDBrightness;
                case 8:
                    return i == this.mConfig.nWhiteBalance;
                case 9:
                    return i == this.mConfig.nContrast;
                case 10:
                    return i == this.mConfig.nExposure;
                case 11:
                    return i == this.mConfig.nSwitchScreen;
                case 12:
                case 20:
                default:
                    Log.d("mhhan", "what????????");
                    return false;
                case 13:
                    return i == this.mConfig.nCrashEventAlarm;
                case 14:
                    return i == this.mConfig.nParkingLowPower;
                case 15:
                    return i == this.mConfig.nParkImpactSensitivity;
                case 16:
                    return i == this.mConfig.nParkingOPTime;
                case 17:
                    return i == this.mConfig.nTimeZone;
                case 18:
                    return i == this.mConfig.nVoiceGuide;
                case 19:
                    return i == this.mConfig.uLed;
                case 21:
                    return i == this.mConfig.nPartition;
            }
        }

        public class ViewHolder {
            ImageView ivCheck;
            TextView tvTitle;

            public ViewHolder() {
            }
        }
    }

    public class SettingSelectItem {
        private int nIndex;
        private String strTitle;

        public SettingSelectItem() {
        }

        public void setIndex(int i) {
            this.nIndex = i;
        }

        public int getIndex() {
            return this.nIndex;
        }

        public void setTitle(String str) {
            this.strTitle = str;
        }

        public String getTitle() {
            return this.strTitle;
        }
    }
}
