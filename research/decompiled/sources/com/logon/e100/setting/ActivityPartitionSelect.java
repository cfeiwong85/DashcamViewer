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
public class ActivityPartitionSelect extends Activity {
    private static CNSConfig mConfig;
    private static Context mContext;
    private static ListView mListView;
    private static ProgressDialog mProgressDialog;
    private static int mSelectedOpt;
    private static SettingSelectAdapter mSettingSelectAdapter;
    private ArrayList<SettingSelectItem> mAlSettingSelectList;
    private Handler mHandler;
    private int mMunuPostion;
    private ImageView partition_image;
    static final Handler listUpdateHandler = new Handler() { // from class: com.logon.e100.setting.ActivityPartitionSelect.7
        @Override // android.os.Handler
        public void handleMessage(Message message) {
            ActivityPartitionSelect.mSettingSelectAdapter.notifyDataSetChanged();
        }
    };
    static final Handler listEnableHandler = new Handler() { // from class: com.logon.e100.setting.ActivityPartitionSelect.8
        @Override // android.os.Handler
        public void handleMessage(Message message) {
            ActivityPartitionSelect.mListView.setEnabled(true);
        }
    };
    static final Handler displayAlertHandler = new Handler() { // from class: com.logon.e100.setting.ActivityPartitionSelect.9
        @Override // android.os.Handler
        public void handleMessage(Message message) {
            String[] strArr = (String[]) message.obj;
            ActivityPartitionSelect.displayInfoAlert(strArr[0], strArr[1]);
        }
    };
    static final Handler handler = new Handler() { // from class: com.logon.e100.setting.ActivityPartitionSelect.10
        @Override // android.os.Handler
        public void handleMessage(Message message) {
            ProgressDialog unused = ActivityPartitionSelect.mProgressDialog = ProgressDialog.show(ActivityPartitionSelect.mContext, "", ActivityPartitionSelect.mContext.getResources().getString(R.string.blackbox_reboot_info), true);
            new Handler().postDelayed(new Runnable() { // from class: com.logon.e100.setting.ActivityPartitionSelect.10.1
                @Override // java.lang.Runnable
                public void run() {
                    if (ActivityPartitionSelect.mProgressDialog != null) {
                        ActivityPartitionSelect.mProgressDialog.dismiss();
                    }
                    MainProtocol.CNSSimpleIndex cNSSimpleIndex = new MainProtocol.CNSSimpleIndex();
                    cNSSimpleIndex.setIndex(0);
                    ((ActivitySetting) ActivitySetting.mContext).finish();
                    MainProtocol.send(MainProtocol.NAT_CMD_NET_CONNECT_STATE3, cNSSimpleIndex.getBytes(), -1);
                    ActivityPartitionSelect.mContext.startActivity(new Intent(ActivityPartitionSelect.mContext, (Class<?>) MainActivity.class));
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
            }, 30000L);
        }
    };
    private Toast mToast = null;
    AdapterView.OnItemClickListener ibSettingSelectClickListener = new AdapterView.OnItemClickListener() { // from class: com.logon.e100.setting.ActivityPartitionSelect.6
        @Override // android.widget.AdapterView.OnItemClickListener
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long j) {
            Log.d("mhhan", "#### menu idx = " + ActivityPartitionSelect.this.mMunuPostion + " sel = " + i + " ####");
            ActivityPartitionSelect.mListView.setEnabled(false);
            if (!ActivityPartitionSelect.mSettingSelectAdapter.getEnable()) {
                ActivityPartitionSelect.this.displayInfoToast(ActivityPartitionSelect.this.getResources().getString(R.string.setting_error));
                return;
            }
            new MainProtocol.CNSSimpleIndex();
            int unused = ActivityPartitionSelect.mSelectedOpt = i;
            if (ActivityPartitionSelect.this.mMunuPostion != 21) {
                ActivityPartitionSelect.mListView.setEnabled(true);
                Log.d("mhhan", "what????????");
            } else if (ActivityPartitionSelect.mConfig.nPartition == i) {
                ActivityPartitionSelect.mListView.setEnabled(true);
            } else {
                ActivityPartitionSelect.this.displayPartitionAlert(i);
            }
        }
    };

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setTheme(android.R.style.Theme.Holo.Light.NoActionBar.TranslucentDecor);
        setContentView(R.layout.activity_partition_select);
        mContext = this;
        Intent intent = getIntent();
        this.mMunuPostion = intent.getIntExtra("logon.e100.menu.position", 0);
        mConfig = (CNSConfig) intent.getParcelableExtra("logon.e100.config");
        Log.e("HAN2", "여기 타니???? mMunuPostion =" + this.mMunuPostion);
        mListView = (ListView) findViewById(R.id.setting_select_listview);
        this.mAlSettingSelectList = getSettingListItems(this.mMunuPostion);
        mSettingSelectAdapter = new SettingSelectAdapter(this, this.mAlSettingSelectList, mConfig, this.mMunuPostion, true);
        mListView.setAdapter((ListAdapter) mSettingSelectAdapter);
        mListView.setOnItemClickListener(this.ibSettingSelectClickListener);
        this.partition_image = (ImageView) findViewById(R.id.main_image);
        this.partition_image.setImageResource(R.drawable.partition);
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
        builder.setMessage(str2).setCancelable(false).setPositiveButton(string, new DialogInterface.OnClickListener() { // from class: com.logon.e100.setting.ActivityPartitionSelect.1
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
        builder.setMessage(string2).setCancelable(false).setPositiveButton(string3, new DialogInterface.OnClickListener() { // from class: com.logon.e100.setting.ActivityPartitionSelect.3
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i2) {
                MainProtocol.CNSSimpleIndex cNSSimpleIndex = new MainProtocol.CNSSimpleIndex();
                cNSSimpleIndex.setIndex(i);
                if (MainProtocol.send(MainProtocol.NAT_CMD_SET_RECORDER_QUALITY, cNSSimpleIndex.getBytes(), 3) < 0) {
                    ActivityPartitionSelect.displayInfoAlert(string, ActivityPartitionSelect.this.getResources().getString(R.string.setting_recorder_quality_error));
                }
            }
        }).setNegativeButton(string4, new DialogInterface.OnClickListener() { // from class: com.logon.e100.setting.ActivityPartitionSelect.2
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i2) {
                ActivityPartitionSelect.mListView.setEnabled(true);
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
        builder.setMessage(string2).setCancelable(false).setPositiveButton(string3, new DialogInterface.OnClickListener() { // from class: com.logon.e100.setting.ActivityPartitionSelect.5
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i2) {
                MainProtocol.CNSSimpleIndex cNSSimpleIndex = new MainProtocol.CNSSimpleIndex();
                cNSSimpleIndex.setIndex(i);
                if (MainProtocol.send(MainProtocol.NAT_CMD_SET_PARTITION, cNSSimpleIndex.getBytes(), 3) < 0) {
                    ActivityPartitionSelect.displayInfoAlert(string, ActivityPartitionSelect.this.getResources().getString(R.string.setting_partition_error));
                }
            }
        }).setNegativeButton(string4, new DialogInterface.OnClickListener() { // from class: com.logon.e100.setting.ActivityPartitionSelect.4
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i2) {
                ActivityPartitionSelect.mListView.setEnabled(true);
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
        if (i == 40883) {
            if (cNSResult.getResult() < 0) {
                String[] strArr = {mContext.getResources().getString(R.string.setting_partition_title), mContext.getResources().getString(R.string.setting_partition_error)};
                Message obtainMessage = displayAlertHandler.obtainMessage();
                obtainMessage.obj = strArr;
                displayAlertHandler.sendMessage(obtainMessage);
            } else {
                mConfig.nPartition = mSelectedOpt;
                handler.sendMessage(handler.obtainMessage());
            }
        } else {
            Log.d("mhhan", "what????????");
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
            if (this.menuPos == 21) {
                return i == this.mConfig.nPartition;
            }
            Log.d("mhhan", "what????????");
            return false;
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
