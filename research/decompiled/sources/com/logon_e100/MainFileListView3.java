package com.logon_e100;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Process;
import android.os.StrictMode;
import android.support.v4.content.FileProvider;
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
import com.logon_e100.MainProtocol;
import com.logon_e100.SwipeDismissListViewTouchListener;
import java.io.File;
import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

@SuppressLint({"DefaultLocale"})
@TargetApi(19)
/* loaded from: classes.dex */
public class MainFileListView3 extends Activity {
    public static MyItem items;
    private static Context mContext;
    public static ListView mListView;
    private static boolean mLockListView;
    public static MyListAdapter mMyAdapte;
    public static int mShareValue_E;
    public static ArrayList<MyItem> marItem;
    private static final Comparator myComparator = new Comparator() { // from class: com.logon_e100.MainFileListView3.7
        private final Collator collator = Collator.getInstance();

        @Override // java.util.Comparator
        public int compare(Object obj, Object obj2) {
            return 0;
        }
    };
    private String dirPath;
    private LayoutInflater mInflater;
    private String mRoot;
    private String mRoot2;
    MainProtocol mprotocol;
    public int COMPARETYPE_NAME = 0;
    public int COMPARETYPE_DATE = 1;

    @Override // android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setTheme(android.R.style.Theme.Holo.Light.NoActionBar.TranslucentDecor);
        setContentView(R.layout.activity_filelistview3);
        mContext = this;
        if (Build.VERSION.SDK_INT > 9) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
        }
        MainFreeView.freeviewstop_flag = 1;
        mListView = (ListView) findViewById(R.id.moreList);
        marItem = new ArrayList<>();
        mLockListView = true;
        this.mInflater = (LayoutInflater) getSystemService("layout_inflater");
        mMyAdapte = new MyListAdapter(this, R.layout.activity_fileview_item, marItem);
        mListView.setAdapter((ListAdapter) mMyAdapte);
        mMyAdapte.notifyDataSetInvalidated();
        Collections.sort(marItem, myComparator);
        SwipeDismissListViewTouchListener swipeDismissListViewTouchListener = new SwipeDismissListViewTouchListener(mListView, new SwipeDismissListViewTouchListener.DismissCallbacks() { // from class: com.logon_e100.MainFileListView3.1
            @Override // com.logon_e100.SwipeDismissListViewTouchListener.DismissCallbacks
            public boolean canDismiss(int i) {
                return true;
            }

            @Override // com.logon_e100.SwipeDismissListViewTouchListener.DismissCallbacks
            public void onDismiss(ListView listView, int[] iArr) {
                for (int i : iArr) {
                    String substring = ((String) listView.getAdapter().getItem(i)).substring(0, 22);
                    Log.e("HAN", "11filename_sub = " + substring);
                    File externalStorageDirectory = Environment.getExternalStorageDirectory();
                    Log.e("HAN", "str2 = " + externalStorageDirectory);
                    MainFileListView3.this.mRoot = externalStorageDirectory + MainProtocol.FILE_PATH_EVENT;
                    Log.e("HAN", "mRoot = " + MainFileListView3.this.mRoot);
                    Log.e("HAN", "전체경로 :  = " + MainFileListView3.this.mRoot + substring);
                    MainFileListView3.this.DialogSimple(MainFileListView3.this.mRoot + substring, 1);
                }
                MainFileListView3.mMyAdapte.notifyDataSetChanged();
            }
        });
        mListView.setOnTouchListener(swipeDismissListViewTouchListener);
        mListView.setOnScrollListener(swipeDismissListViewTouchListener.makeScrollListener());
        mMyAdapte.notifyDataSetChanged();
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() { // from class: com.logon_e100.MainFileListView3.2
            /* JADX WARN: Type inference failed for: r1v1, types: [android.widget.Adapter] */
            @Override // android.widget.AdapterView.OnItemClickListener
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long j) {
                Log.e("HAN2", "파일리스트 클릭");
                String substring = ((String) adapterView.getAdapter().getItem(i)).substring(0, 22);
                Log.d("HAN3", "filename_sub =" + substring);
                File externalStorageDirectory = Environment.getExternalStorageDirectory();
                Intent intent = new Intent(MainFileListView3.this, (Class<?>) MainFileDownView2.class);
                intent.putExtra("filename", externalStorageDirectory + MainProtocol.FILE_PATH_EVENT + substring);
                MainFileListView3.this.startActivity(intent);
            }
        });
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() { // from class: com.logon_e100.MainFileListView3.3
            /* JADX WARN: Type inference failed for: r2v1, types: [android.widget.Adapter] */
            @Override // android.widget.AdapterView.OnItemLongClickListener
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long j) {
                String substring = ((String) adapterView.getAdapter().getItem(i)).substring(0, 22);
                Log.e("HAN", "filename_sub = " + substring);
                Log.e("HAN", "arg3 = " + j);
                File externalStorageDirectory = Environment.getExternalStorageDirectory();
                Log.e("HAN", "str2 = " + externalStorageDirectory);
                MainFileListView3.this.mRoot = externalStorageDirectory + MainProtocol.FILE_PATH_EVENT;
                Log.e("HAN", "mRoot = " + MainFileListView3.this.mRoot);
                Log.e("HAN", "전체경로 :  = " + MainFileListView3.this.mRoot + substring);
                File file = new File(MainFileListView3.this.mRoot, substring);
                if (Build.VERSION.SDK_INT >= 24) {
                    Uri uriForFile = FileProvider.getUriForFile(MainFileListView3.mContext, "com.logon_e100.fileprovider", file);
                    MainFileListView3.mShareValue_E = 1;
                    Intent intent = new Intent("android.intent.action.SEND");
                    intent.addFlags(1);
                    intent.setType("video/*");
                    intent.putExtra("android.intent.extra.STREAM", uriForFile);
                    Log.i("Wanjae", "FileProvider fileUri: " + uriForFile);
                    MainFileListView3.this.startActivity(Intent.createChooser(intent, ""));
                } else {
                    Uri fromFile = Uri.fromFile(file);
                    Intent intent2 = new Intent("android.intent.action.SEND");
                    intent2.setType("video/*");
                    intent2.putExtra("android.intent.extra.STREAM", fromFile);
                    MainFileListView3.this.startActivity(Intent.createChooser(intent2, ""));
                }
                return true;
            }
        });
        File externalStorageDirectory = Environment.getExternalStorageDirectory();
        if (Environment.getExternalStorageState().equals("mounted")) {
            this.dirPath = externalStorageDirectory + MainProtocol.FILE_PATH_EVENT;
            this.mRoot = externalStorageDirectory + MainProtocol.FILE_PATH_EVENT;
            Log.e("HAN", "dirPath  = " + this.dirPath);
            File file = new File(this.dirPath);
            if (file.exists()) {
                return;
            }
            file.mkdirs();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void DialogSimple(final String str, int i) {
        String string = getResources().getString(R.string.action_cancel);
        String string2 = getResources().getString(R.string.action_remove);
        String string3 = getResources().getString(R.string.file_remove_quest);
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.context);
        builder.setMessage(string3).setCancelable(false).setPositiveButton(string2, new DialogInterface.OnClickListener() { // from class: com.logon_e100.MainFileListView3.5
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i2) {
                MainFileListView3.this.deleteDir2(str);
            }
        }).setNegativeButton(string, new DialogInterface.OnClickListener() { // from class: com.logon_e100.MainFileListView3.4
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i2) {
                dialogInterface.cancel();
            }
        });
        AlertDialog create = builder.create();
        create.setTitle(string2);
        create.setIcon(R.drawable.ic_launcher);
        create.show();
    }

    public int deleteDir2(String str) {
        new File(str).delete();
        this.mRoot = Environment.getExternalStorageDirectory() + MainProtocol.FILE_PATH_EVENT;
        Log.e("HAN", "a_path = " + str);
        Log.e("HAN", "mRoot = " + this.mRoot);
        insertQueryDataToListView(this.mRoot);
        return 1;
    }

    public void insertQueryDataToListView(String str) {
        ErrorFileDel();
        marItem = new ArrayList<>();
        for (File file : sortFileList(new File(str).listFiles(), this.COMPARETYPE_DATE)) {
            file.getName();
            addItems(file.getName(), 1);
        }
        mMyAdapte = new MyListAdapter(this, R.layout.activity_fileview_item, marItem);
        mListView.setAdapter((ListAdapter) mMyAdapte);
        mMyAdapte.notifyDataSetChanged();
    }

    public File[] sortFileList(File[] fileArr, final int i) {
        Arrays.sort(fileArr, new Comparator<Object>() { // from class: com.logon_e100.MainFileListView3.6
            @Override // java.util.Comparator
            public int compare(Object obj, Object obj2) {
                String str = "";
                String str2 = "";
                if (i == MainFileListView3.this.COMPARETYPE_NAME) {
                    str = ((File) obj).getName();
                    str2 = ((File) obj2).getName();
                } else if (i == MainFileListView3.this.COMPARETYPE_DATE) {
                    str = ((File) obj).lastModified() + "";
                    str2 = ((File) obj2).lastModified() + "";
                }
                return str.compareTo(str2);
            }
        });
        return fileArr;
    }

    private void getDir(String str) {
        for (File file : sortFileList(new File(str).listFiles(), this.COMPARETYPE_DATE)) {
            file.getName();
            addItems(file.getName(), 1);
        }
    }

    public static class MyItem {
        String sCustId;
        String sCustId2;

        MyItem(String str, String str2) {
            this.sCustId = str;
            this.sCustId2 = str2;
        }
    }

    class MyListAdapter extends BaseAdapter {
        ArrayList<MyItem> alSrc;
        Context cContext;
        LayoutInflater lInflater;
        int layout;

        @Override // android.widget.Adapter
        public long getItemId(int i) {
            return i;
        }

        public MyListAdapter(Context context, int i, ArrayList<MyItem> arrayList) {
            this.cContext = context;
            this.lInflater = (LayoutInflater) context.getSystemService("layout_inflater");
            this.alSrc = arrayList;
            this.layout = i;
        }

        @Override // android.widget.Adapter
        public int getCount() {
            return this.alSrc.size();
        }

        @Override // android.widget.Adapter
        public Object getItem(int i) {
            return this.alSrc.get(i).sCustId + this.alSrc.get(i).sCustId2;
        }

        @Override // android.widget.Adapter
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = this.lInflater.inflate(this.layout, viewGroup, false);
            }
            String str = this.alSrc.get(i).sCustId;
            ((TextView) view.findViewById(R.id.tvCoustId)).setText(this.alSrc.get(i).sCustId);
            String str2 = this.alSrc.get(i).sCustId2;
            ((TextView) view.findViewById(R.id.tvCoustId2)).setText(this.alSrc.get(i).sCustId2);
            view.findViewById(R.id.tvCoustId2).setVisibility(8);
            view.findViewById(R.id.sendBtn).setVisibility(4);
            return view;
        }
    }

    public static void addItems(final String str, final int i) {
        mLockListView = true;
        new Handler().postDelayed(new Runnable() { // from class: com.logon_e100.MainFileListView3.8
            @Override // java.lang.Runnable
            public void run() {
                MainFileListView3.items = new MyItem(str, String.valueOf(i));
                MainFileListView3.marItem.add(MainFileListView3.items);
                MainFileListView3.mMyAdapte.notifyDataSetChanged();
                boolean unused = MainFileListView3.mLockListView = false;
            }
        }, 100L);
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
        Log.e("HAN2", "핸드폰 상시 프리뷰 정지");
    }

    public void wificon() {
        byte[] bArr = new byte[256];
        bArr[0] = 2;
        MainProtocol mainProtocol = this.mprotocol;
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
        try {
            MainProtocol mainProtocol2 = this.mprotocol;
            MainProtocol.m_out_stream.write(bArr, 0, 13);
            MainProtocol mainProtocol3 = this.mprotocol;
            MainProtocol.m_out_stream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.e("HAN2", "핸드폰 상시 wifi접속 데이터 보내기");
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
        Log.e("HAN2", "핸드폰 충격 영상 온오프보내기");
    }

    @Override // android.app.Activity
    protected void onPause() {
        Log.e("HAN2", "onPause");
        super.onPause();
        int i = ((WifiManager) getApplicationContext().getSystemService("wifi")).getDhcpInfo().gateway;
        String format = String.format("%d.%d.%d.%d", Integer.valueOf(i & 255), Integer.valueOf((i >> 8) & 255), Integer.valueOf((i >> 16) & 255), Integer.valueOf((i >> 24) & 255));
        Log.e("HAN2", "ipAddress =" + format);
        format.equals(MainProtocol.SERVERIP);
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
                new AlertDialog.Builder(MainActivity.context).setTitle(string).setMessage(string2).setPositiveButton(string3, new DialogInterface.OnClickListener() { // from class: com.logon_e100.MainFileListView3.9
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i2) {
                        WifiManager wifiManager = (WifiManager) MainFileListView3.this.getApplicationContext().getSystemService("wifi");
                        MainProtocol.CNSSimpleIndex cNSSimpleIndex = new MainProtocol.CNSSimpleIndex();
                        cNSSimpleIndex.setIndex(0);
                        if (MainProtocol.send(MainProtocol.NAT_CMD_NET_CONNECT_STATE3, cNSSimpleIndex.getBytes(), -1) < 0) {
                            Log.d("HAN5", "종료 실패");
                        } else {
                            Log.d("HAN5", "종료 성공");
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
                        if (wifiManager != null && wifiManager.isWifiEnabled()) {
                            wifiManager.setWifiEnabled(false);
                        }
                        Process.killProcess(Process.myPid());
                    }
                }).setNegativeButton(getResources().getString(R.string.action_no), (DialogInterface.OnClickListener) null).show();
                break;
        }
        return super.onKeyDown(i, keyEvent);
    }

    @Override // android.app.Activity
    protected void onResume() {
        Log.d("mhhan", "#### " + getClass().getName() + "::onResume ####");
        this.dirPath = Environment.getExternalStorageDirectory() + MainProtocol.FILE_PATH_EVENT;
        insertQueryDataToListView(this.dirPath);
        super.onResume();
    }

    public void ErrorFileDel() {
        String str = Environment.getExternalStorageDirectory() + MainProtocol.FILE_PATH_EVENT;
        Log.e("HAN", "mRoot = " + str);
        String[] list = new File(str).list();
        if (list != null) {
            for (String str2 : list) {
                if (str2.length() < 22) {
                    File file = new File(str + str2);
                    if (file.exists()) {
                        file.delete();
                    }
                } else if (MainProtocol.PCM_FILE.equals(str2.substring(18, 22))) {
                    File file2 = new File(str + str2);
                    if (file2.exists()) {
                        file2.delete();
                    }
                }
            }
        }
    }
}
