package com.logon.e100.setting;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.logon_e100.MainActivity;
import com.logon_e100.MainFreeView;
import com.logon_e100.MainProtocol;
import com.logon_e100.R;
import java.io.IOException;
import java.util.ArrayList;

/* loaded from: classes.dex */
public class ActivitySetting extends Activity {
    private static CNSConfig mConfig;
    public static Context mContext;
    private static ProgressDialog mProgressDialog;
    private static SettingListviewAdapter mSettingAdapter;
    public static SettingDefs settingDefs;
    private ArrayList<SettingItem> mAlSettingList;
    private boolean mGoingForwards;
    private Handler mHandler;
    private ListView mListView;
    static final Handler listUpdateHandler = new Handler() { // from class: com.logon.e100.setting.ActivitySetting.8
        @Override // android.os.Handler
        public void handleMessage(Message message) {
            ActivitySetting.mSettingAdapter.notifyDataSetChanged();
        }
    };
    static final Handler displayAlertHandler = new Handler() { // from class: com.logon.e100.setting.ActivitySetting.9
        @Override // android.os.Handler
        public void handleMessage(Message message) {
            String[] strArr = (String[]) message.obj;
            ActivitySetting.displayInfoAlert(strArr[0], strArr[1]);
        }
    };
    static final Handler handler = new Handler() { // from class: com.logon.e100.setting.ActivitySetting.10
        @Override // android.os.Handler
        public void handleMessage(Message message) {
            ProgressDialog unused = ActivitySetting.mProgressDialog = ProgressDialog.show(ActivitySetting.mContext, "", ActivitySetting.mContext.getResources().getString(R.string.blackbox_reboot_info), true);
            new Handler().postDelayed(new Runnable() { // from class: com.logon.e100.setting.ActivitySetting.10.1
                @Override // java.lang.Runnable
                public void run() {
                    if (ActivitySetting.mProgressDialog != null) {
                        ActivitySetting.mProgressDialog.dismiss();
                    }
                    MainProtocol.CNSSimpleIndex cNSSimpleIndex = new MainProtocol.CNSSimpleIndex();
                    cNSSimpleIndex.setIndex(0);
                    MainProtocol.send(MainProtocol.NAT_CMD_NET_CONNECT_STATE3, cNSSimpleIndex.getBytes(), -1);
                    ActivitySetting.mContext.startActivity(new Intent(ActivitySetting.mContext, (Class<?>) MainActivity.class));
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
            }, 50000L);
        }
    };
    private Toast mToast = null;
    AdapterView.OnItemClickListener ibSettingListviewClickListener = new AdapterView.OnItemClickListener() { // from class: com.logon.e100.setting.ActivitySetting.7
        @Override // android.widget.AdapterView.OnItemClickListener
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long j) {
            Log.e("mhhan", "setting pos = " + i);
            int cellType = ((SettingItem) ActivitySetting.mSettingAdapter.getItem(i)).getCellType();
            if (cellType == ActivitySetting.settingDefs.CELL_TITLE) {
                if (!ActivitySetting.mSettingAdapter.getEnable() && i != 25) {
                    ActivitySetting.this.displayInfoToast(ActivitySetting.this.getResources().getString(R.string.setting_error));
                    return;
                }
                if (i == 1) {
                    Intent intent = new Intent(ActivitySetting.this, (Class<?>) ActivityWifiPassword.class);
                    ActivitySetting.this.mGoingForwards = true;
                    MainProtocol.OnStopFlag = false;
                    ActivitySetting.this.startActivity(intent);
                    return;
                }
                if (i == 12) {
                    Intent intent2 = new Intent(ActivitySetting.this, (Class<?>) ActivityDateAndTime.class);
                    ActivitySetting.this.mGoingForwards = true;
                    MainProtocol.OnStopFlag = false;
                    ActivitySetting.this.startActivity(intent2);
                    return;
                }
                if (i == 20) {
                    ActivitySetting.this.displaySDCardFormatAlert();
                    return;
                }
                if (i == 22) {
                    ActivitySetting.this.displayFactoryResetAlert();
                    return;
                }
                switch (i) {
                    case 24:
                        String str = new String(ActivitySetting.mConfig.byteProductFWVersion, 0, ActivitySetting.mConfig.byteProductFWVersion.length);
                        Intent intent3 = new Intent(ActivitySetting.this, (Class<?>) ActivityFirmware.class);
                        intent3.putExtra("logon.e100.firmware.version", str);
                        ActivitySetting.this.mGoingForwards = true;
                        MainProtocol.OnStopFlag = false;
                        ActivitySetting.this.startActivity(intent3);
                        break;
                    case 25:
                        Intent intent4 = new Intent(ActivitySetting.this, (Class<?>) ActivityAppInfo.class);
                        ActivitySetting.this.mGoingForwards = true;
                        MainProtocol.OnStopFlag = false;
                        ActivitySetting.this.startActivity(intent4);
                        break;
                }
                return;
            }
            if (cellType == ActivitySetting.settingDefs.CELL_SUB) {
                if (!ActivitySetting.mSettingAdapter.getEnable()) {
                    ActivitySetting.this.displayInfoToast(ActivitySetting.this.getResources().getString(R.string.setting_error));
                    return;
                }
                if (MainProtocol.VER_CHECK == 1 && (i == 7 || i == 11)) {
                    ActivitySetting.this.displayInfoToast(ActivitySetting.this.getResources().getString(R.string.setting_error_ver));
                    return;
                }
                if (i == 21) {
                    Intent intent5 = new Intent(ActivitySetting.this, (Class<?>) ActivityPartitionSelect.class);
                    intent5.putExtra("logon.e100.menu.position", i);
                    intent5.putExtra("logon.e100.config", ActivitySetting.mConfig);
                    ActivitySetting.this.mGoingForwards = true;
                    MainProtocol.OnStopFlag = false;
                    ActivitySetting.this.startActivityForResult(intent5, 0);
                    return;
                }
                Intent intent6 = new Intent(ActivitySetting.this, (Class<?>) ActivitySelect.class);
                intent6.putExtra("logon.e100.menu.position", i);
                intent6.putExtra("logon.e100.config", ActivitySetting.mConfig);
                ActivitySetting.this.mGoingForwards = true;
                MainProtocol.OnStopFlag = false;
                ActivitySetting.this.startActivityForResult(intent6, 0);
            }
        }
    };

    @Override // android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_setting);
        MainFreeView.freeviewstop_flag = 1;
        mContext = this;
        settingDefs = new SettingDefs(this);
        mConfig = new CNSConfig();
        this.mListView = (ListView) findViewById(R.id.setting_listview);
        this.mAlSettingList = getSettingListItems();
        mSettingAdapter = new SettingListviewAdapter(this, this.mAlSettingList, mConfig, false);
        this.mListView.setAdapter((ListAdapter) mSettingAdapter);
        this.mListView.setOnItemClickListener(this.ibSettingListviewClickListener);
        this.mGoingForwards = false;
        int i = ((WifiManager) getApplicationContext().getSystemService("wifi")).getDhcpInfo().gateway;
        String format = String.format("%d.%d.%d.%d", Integer.valueOf(i & 255), Integer.valueOf((i >> 8) & 255), Integer.valueOf((i >> 16) & 255), Integer.valueOf((i >> 24) & 255));
        Log.e("HAN2", "mainFREEviw ipAddress =" + format);
        if (format.equals(MainProtocol.SERVERIP)) {
            displayProgress();
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
        if (this.mGoingForwards) {
            this.mGoingForwards = false;
        } else if (MainProtocol.send(MainProtocol.NAT_CMD_GET_CONFIG, null, 2) < 0) {
            mSettingAdapter.setEnable(false);
            mSettingAdapter.notifyDataSetChanged();
        }
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

    public ArrayList<SettingItem> getSettingListItems() {
        ArrayList<SettingItem> arrayList = new ArrayList<>();
        for (int i = 0; i < settingDefs.SETTING_LIST.length; i++) {
            SettingItem settingItem = new SettingItem();
            settingItem.setCellType(settingDefs.CELL_TYPE[i]);
            settingItem.setTitle(settingDefs.SETTING_LIST[i]);
            settingItem.setSubTitle("");
            arrayList.add(settingItem);
        }
        return arrayList;
    }

    public void displayProgress() {
        this.mHandler = new Handler();
        runOnUiThread(new Runnable() { // from class: com.logon.e100.setting.ActivitySetting.1
            @Override // java.lang.Runnable
            public void run() {
                ProgressDialog unused = ActivitySetting.mProgressDialog = ProgressDialog.show(MainActivity.context, "", ActivitySetting.mContext.getResources().getString(R.string.file_download_wait), true);
                ActivitySetting.this.mHandler.postDelayed(new Runnable() { // from class: com.logon.e100.setting.ActivitySetting.1.1
                    @Override // java.lang.Runnable
                    public void run() {
                        try {
                            if (ActivitySetting.mProgressDialog == null || !ActivitySetting.mProgressDialog.isShowing()) {
                                return;
                            }
                            ActivitySetting.mProgressDialog.dismiss();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, 1500L);
            }
        });
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
        String string = mContext.getResources().getString(R.string.action_confirm);
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(str2).setCancelable(false).setPositiveButton(string, new DialogInterface.OnClickListener() { // from class: com.logon.e100.setting.ActivitySetting.2
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        AlertDialog create = builder.create();
        create.setTitle(str);
        create.setIcon(R.drawable.ic_launcher);
        create.show();
    }

    public void displaySDCardFormatAlert() {
        final String string = getResources().getString(R.string.setting_sdcard_format_title);
        String string2 = getResources().getString(R.string.setting_sdcard_format_info);
        String string3 = getResources().getString(R.string.action_confirm);
        String string4 = getResources().getString(R.string.action_cancel);
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(string2).setCancelable(false).setPositiveButton(string3, new DialogInterface.OnClickListener() { // from class: com.logon.e100.setting.ActivitySetting.4
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                if (MainProtocol.send(MainProtocol.NAT_CMD_FORMAT_TF_CARD, new MainProtocol.CNSSimpleIndex().getBytes(), 2) < 0) {
                    String string5 = ActivitySetting.this.getResources().getString(R.string.setting_sdcard_format_error);
                    ActivitySetting.mSettingAdapter.setEnable(false);
                    ActivitySetting.mSettingAdapter.notifyDataSetChanged();
                    ActivitySetting.displayInfoAlert(string, string5);
                }
            }
        }).setNegativeButton(string4, new DialogInterface.OnClickListener() { // from class: com.logon.e100.setting.ActivitySetting.3
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        AlertDialog create = builder.create();
        create.setTitle(string);
        create.setIcon(R.drawable.ic_launcher);
        create.show();
    }

    public void displayFactoryResetAlert() {
        final String string = getResources().getString(R.string.setting_factory_format_title);
        String string2 = getResources().getString(R.string.setting_factory_format_info);
        String string3 = getResources().getString(R.string.action_confirm);
        String string4 = getResources().getString(R.string.action_cancel);
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(string2).setCancelable(false).setPositiveButton(string3, new DialogInterface.OnClickListener() { // from class: com.logon.e100.setting.ActivitySetting.6
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                if (MainProtocol.send(MainProtocol.NAT_CMD_FACTORY_RESET, new MainProtocol.CNSSimpleIndex().getBytes(), 2) < 0) {
                    String string5 = ActivitySetting.this.getResources().getString(R.string.setting_factory_format_error);
                    ActivitySetting.mSettingAdapter.setEnable(false);
                    ActivitySetting.mSettingAdapter.notifyDataSetChanged();
                    ActivitySetting.displayInfoAlert(string, string5);
                }
            }
        }).setNegativeButton(string4, new DialogInterface.OnClickListener() { // from class: com.logon.e100.setting.ActivitySetting.5
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        AlertDialog create = builder.create();
        create.setTitle(string);
        create.setIcon(R.drawable.ic_launcher);
        create.show();
    }

    @Override // android.app.Activity
    protected void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        Log.d("mhhan", "#### " + getClass().getName() + "::onActivityResult ####");
        if (i2 == -1) {
            Log.d("mhhan", "requestCode = " + i + ", resultCode = " + i2);
            CNSConfig cNSConfig = (CNSConfig) intent.getParcelableExtra("logon.e100.config");
            boolean booleanExtra = intent.getBooleanExtra("logon.e100.enable", true);
            mConfig.update(cNSConfig);
            mSettingAdapter.setEnable(booleanExtra);
            mSettingAdapter.notifyDataSetChanged();
        }
    }

    public static void onReceive(int i, byte[] bArr) {
        Log.d("mhhan", "#### onReceive cmd = " + i + " ####");
        if (i != 40992) {
            if (i == 41013) {
                mConfig.setBytes(bArr);
                mSettingAdapter.setEnable(true);
            } else if (i == 41045) {
                MainProtocol.CNSResult cNSResult = new MainProtocol.CNSResult(bArr);
                Log.d("mhhan", "#### factory reset resp = " + cNSResult.getResult());
                if (cNSResult.getResult() < 0) {
                    String[] strArr = {mContext.getResources().getString(R.string.setting_factory_format_title), mContext.getResources().getString(R.string.setting_factory_format_error)};
                    Message obtainMessage = displayAlertHandler.obtainMessage();
                    obtainMessage.obj = strArr;
                    displayAlertHandler.sendMessage(obtainMessage);
                } else {
                    mSettingAdapter.setEnable(false);
                    handler.sendMessage(handler.obtainMessage());
                }
            } else {
                Log.d("mhhan", "what????????");
            }
        } else if (new MainProtocol.CNSResult(bArr).getResult() < 0) {
            String[] strArr2 = {mContext.getResources().getString(R.string.setting_sdcard_format_title), mContext.getResources().getString(R.string.setting_sdcard_format_error)};
            Message obtainMessage2 = displayAlertHandler.obtainMessage();
            obtainMessage2.obj = strArr2;
            displayAlertHandler.sendMessage(obtainMessage2);
        } else {
            mSettingAdapter.setEnable(false);
            handler.sendMessage(handler.obtainMessage());
        }
        listUpdateHandler.sendMessage(listUpdateHandler.obtainMessage());
    }

    public class SettingListviewAdapter extends BaseAdapter {
        private Context context;
        private ArrayList<SettingItem> item;
        private CNSConfig mConfig;
        private boolean mEnable;

        @Override // android.widget.Adapter
        public long getItemId(int i) {
            return 0L;
        }

        public SettingListviewAdapter(Context context, ArrayList<SettingItem> arrayList, CNSConfig cNSConfig, boolean z) {
            this.context = context;
            this.item = arrayList;
            this.mConfig = cNSConfig;
            this.mEnable = z;
        }

        @Override // android.widget.Adapter
        public int getCount() {
            return this.item.size();
        }

        @Override // android.widget.Adapter
        public Object getItem(int i) {
            return this.item.get(i);
        }

        @Override // android.widget.Adapter
        public View getView(int i, View view, ViewGroup viewGroup) {
            int cellType = this.item.get(i).getCellType();
            ViewHolder viewHolder = new ViewHolder();
            if (view == null) {
                LayoutInflater from = LayoutInflater.from(this.context);
                if (cellType == ActivitySetting.settingDefs.CELL_SECTION) {
                    view = from.inflate(R.layout.setting_listview_item_section, viewGroup, false);
                    view.setTag(R.layout.setting_listview_item_section, viewHolder);
                } else if (cellType == ActivitySetting.settingDefs.CELL_TITLE) {
                    view = from.inflate(R.layout.setting_listview_item_title, viewGroup, false);
                    view.setTag(R.layout.setting_listview_item_title, viewHolder);
                } else {
                    view = from.inflate(R.layout.setting_listview_item_sub, viewGroup, false);
                    view.setTag(R.layout.setting_listview_item_sub, viewHolder);
                }
            } else if (cellType == ActivitySetting.settingDefs.CELL_SECTION) {
                if (view.getTag(R.layout.setting_listview_item_section) == null) {
                    view = LayoutInflater.from(this.context).inflate(R.layout.setting_listview_item_section, viewGroup, false);
                    viewHolder = new ViewHolder();
                    viewHolder.tvTitle = (TextView) view.findViewById(R.id.textViewTitle);
                    view.setTag(R.layout.setting_listview_item_section, viewHolder);
                } else {
                    viewHolder = (ViewHolder) view.getTag(R.layout.setting_listview_item_section);
                }
            } else if (cellType == ActivitySetting.settingDefs.CELL_TITLE) {
                if (view.getTag(R.layout.setting_listview_item_title) == null) {
                    view = LayoutInflater.from(this.context).inflate(R.layout.setting_listview_item_title, viewGroup, false);
                    viewHolder = new ViewHolder();
                    viewHolder.tvTitle = (TextView) view.findViewById(R.id.textViewTitle);
                    view.setTag(R.layout.setting_listview_item_title, viewHolder);
                } else {
                    viewHolder = (ViewHolder) view.getTag(R.layout.setting_listview_item_title);
                }
            } else if (view.getTag(R.layout.setting_listview_item_sub) == null) {
                view = LayoutInflater.from(this.context).inflate(R.layout.setting_listview_item_sub, viewGroup, false);
                viewHolder = new ViewHolder();
                viewHolder.tvTitle = (TextView) view.findViewById(R.id.textViewTitle);
                viewHolder.tvSubTitle = (TextView) view.findViewById(R.id.textViewSubTitle);
                view.setTag(R.layout.setting_listview_item_sub, viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag(R.layout.setting_listview_item_sub);
            }
            if (cellType == ActivitySetting.settingDefs.CELL_SECTION) {
                viewHolder.tvTitle = (TextView) view.findViewById(R.id.textViewTitle);
                viewHolder.tvTitle.setText(this.item.get(i).getTitle());
            } else if (cellType == ActivitySetting.settingDefs.CELL_TITLE) {
                viewHolder.tvTitle = (TextView) view.findViewById(R.id.textViewTitle);
                viewHolder.tvTitle.setText(this.item.get(i).getTitle());
            } else {
                viewHolder.tvTitle = (TextView) view.findViewById(R.id.textViewTitle);
                viewHolder.tvSubTitle = (TextView) view.findViewById(R.id.textViewSubTitle);
                viewHolder.tvTitle.setText(this.item.get(i).getTitle());
                if (this.mEnable) {
                    viewHolder.tvSubTitle.setText(getSubTitle(i));
                    viewHolder.tvSubTitle.setVisibility(0);
                } else {
                    viewHolder.tvSubTitle.setVisibility(8);
                }
                if (MainProtocol.VER_CHECK == 1) {
                    if (i == 9) {
                        Log.i("mhhan", "23452345234523position = " + i);
                    }
                    if (i == 7 || i == 11) {
                        Log.i("mhhan", "position = " + i);
                        viewHolder.tvTitle.setTextColor(Color.parseColor("#888888"));
                        viewHolder.tvSubTitle.setVisibility(8);
                    } else {
                        viewHolder.tvTitle.setTextColor(Color.parseColor("#000000"));
                    }
                }
            }
            return view;
        }

        public void setEnable(boolean z) {
            this.mEnable = z;
        }

        public boolean getEnable() {
            return this.mEnable;
        }

        public void invalidateAdapterItems(ArrayList<SettingItem> arrayList) {
            this.item = arrayList;
        }

        public String getSubTitle(int i) {
            switch (i) {
                case 0:
                case 1:
                case 2:
                case 12:
                case 20:
                case 22:
                case 23:
                case 24:
                case 25:
                    return "";
                case 3:
                    return ActivitySetting.settingDefs.RECORD_QUALITY[this.mConfig.nRecordQuality];
                case 4:
                    return ActivitySetting.settingDefs.NORMAL_IMPACT_SENSITIVITY[this.mConfig.nNormalImpactSensitivity];
                case 5:
                    return ActivitySetting.settingDefs.VOICE_VOLUME[this.mConfig.nVoiceVolume];
                case 6:
                    return ActivitySetting.settingDefs.VOICE_RECORD[this.mConfig.nMute];
                case 7:
                    return ActivitySetting.settingDefs.LCD_BRIGHTNESS[this.mConfig.nLCDBrightness];
                case 8:
                    return ActivitySetting.settingDefs.WHITE_BALANCE[this.mConfig.nWhiteBalance];
                case 9:
                    return ActivitySetting.settingDefs.CONTRAST[this.mConfig.nContrast];
                case 10:
                    return ActivitySetting.settingDefs.EXPOSURE[this.mConfig.nExposure];
                case 11:
                    return ActivitySetting.settingDefs.SWITCH_SCREEN[this.mConfig.nSwitchScreen];
                case 13:
                    return ActivitySetting.settingDefs.CRASH_EVENT_ALARM[this.mConfig.nCrashEventAlarm];
                case 14:
                    return ActivitySetting.settingDefs.PARKING_LOW_POWER[this.mConfig.nParkingLowPower];
                case 15:
                    return ActivitySetting.settingDefs.PARK_IMPACT_SENSITIVITY[this.mConfig.nParkImpactSensitivity];
                case 16:
                    return ActivitySetting.settingDefs.PARKING_OPTIME[this.mConfig.nParkingOPTime];
                case 17:
                    return ActivitySetting.settingDefs.TIME_ZONE[this.mConfig.nTimeZone];
                case 18:
                    return ActivitySetting.settingDefs.VOICE_GUIDE[this.mConfig.nVoiceGuide];
                case 19:
                    return ActivitySetting.settingDefs.LED[this.mConfig.uLed];
                case 21:
                    return ActivitySetting.settingDefs.PARTITION[this.mConfig.nPartition];
                default:
                    Log.d("mhhan", "what????????");
                    return "";
            }
        }

        public class ViewHolder {
            TextView tvSubTitle;
            TextView tvTitle;

            public ViewHolder() {
            }
        }
    }

    public class SettingItem {
        private int nCellType;
        private int nIndex;
        private String strSubTitle;
        private String strTitle;

        public SettingItem() {
        }

        public void setIndex(int i) {
            this.nIndex = i;
        }

        public int getIndex() {
            return this.nIndex;
        }

        public void setCellType(int i) {
            this.nCellType = i;
        }

        public int getCellType() {
            return this.nCellType;
        }

        public void setTitle(String str) {
            this.strTitle = str;
        }

        public String getTitle() {
            return this.strTitle;
        }

        public void setSubTitle(String str) {
            this.strSubTitle = str;
        }

        public String getSubTitle() {
            return this.strSubTitle;
        }
    }

    @Override // android.app.Activity, android.view.KeyEvent.Callback
    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        switch (i) {
            case 3:
                Process.killProcess(Process.myPid());
                break;
            case 4:
                String string = getResources().getString(R.string.app_exit);
                String string2 = getResources().getString(R.string.app_exit_quest);
                String string3 = getResources().getString(R.string.action_yes);
                new AlertDialog.Builder(MainActivity.context).setTitle(string).setMessage(string2).setPositiveButton(string3, new DialogInterface.OnClickListener() { // from class: com.logon.e100.setting.ActivitySetting.11
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i2) {
                        WifiManager wifiManager = (WifiManager) ActivitySetting.this.getApplicationContext().getSystemService("wifi");
                        MainProtocol.CNSSimpleIndex cNSSimpleIndex = new MainProtocol.CNSSimpleIndex();
                        cNSSimpleIndex.setIndex(0);
                        if (MainProtocol.send(MainProtocol.NAT_CMD_NET_CONNECT_STATE3, cNSSimpleIndex.getBytes(), -1) < 0) {
                            Log.d("HAN5", "환경설정 종료 실패");
                        } else {
                            Log.d("HAN5", "환경설정  종료 성공");
                        }
                        try {
                            if (MainProtocol.mSock != null) {
                                MainProtocol.mSock.close();
                                MainProtocol.m_out_stream.close();
                                MainProtocol.m_in_stream.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        SystemClock.sleep(100L);
                        if (wifiManager.isWifiEnabled()) {
                            wifiManager.setWifiEnabled(false);
                        }
                        Process.killProcess(Process.myPid());
                    }
                }).setNegativeButton(getResources().getString(R.string.action_no), (DialogInterface.OnClickListener) null).show();
                break;
        }
        return super.onKeyDown(i, keyEvent);
    }
}
