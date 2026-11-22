package com.logon_e100;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Process;
import android.os.StrictMode;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.logon_e100.MainProtocol;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

@SuppressLint({"DefaultLocale"})
@TargetApi(9)
/* loaded from: classes.dex */
public class MainFileListView_blackbox5 extends Activity {
    public static Context context = null;
    static int file_flag = 0;
    static int flag_count = 1;
    public static MyItem items;
    public static ListView mListView;
    private static boolean mLockListView;
    public static MyListAdapter mMyAdapte;
    public static ProgressDialog mProgressDialog2;
    public static ArrayList<MyItem> marItem;
    private static final Comparator<MyItem> myComparator2 = new Comparator<MyItem>() { // from class: com.logon_e100.MainFileListView_blackbox5.5
        private final Collator collator = Collator.getInstance();

        @Override // java.util.Comparator
        public int compare(MyItem myItem, MyItem myItem2) {
            if (myItem.order < myItem2.order) {
                return -1;
            }
            return myItem.order > myItem2.order ? 1 : 0;
        }
    };
    public static TextView tvCustId2;
    private String country;
    String file_vo;
    private InputStream in;
    private Handler mHandler;
    private LayoutInflater mInflater;
    private ProgressDialog mProgressDialog;
    MainProtocol mprotocol;
    private FileOutputStream out;
    int total_filecount;
    byte[] readdata = new byte[8199];
    byte[] readsize = new byte[8199];
    int up_down_flag = 0;
    Socket socket = null;
    int time_sec = 0;
    int down_pos = 0;
    private Thread mCheckRecv = new Thread() { // from class: com.logon_e100.MainFileListView_blackbox5.10
        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            try {
                Log.e("HAN2", "파일 리드 시작");
                byte[] bArr = new byte[8192];
                while (true) {
                    int read = MainFileListView_blackbox5.this.in.read(bArr);
                    if (read <= 0) {
                        break;
                    } else {
                        MainFileListView_blackbox5.this.out.write(bArr, 0, read);
                    }
                }
                if (MainFileListView_blackbox5.this.mProgressDialog != null) {
                    MainFileListView_blackbox5.this.mProgressDialog.dismiss();
                }
            } catch (Exception e) {
                Log.d("tag", "File Receive error =" + e);
                if (MainFileListView_blackbox5.this.mProgressDialog != null) {
                    MainFileListView_blackbox5.this.mProgressDialog.dismiss();
                }
            }
        }
    };

    private static native int naFileAvi();

    private static native int[] naGetVideoRes();

    private static native int naInit(String str, String str2, String str3, int i);

    private static native int naIsFirstFrameReceived();

    private static native int naPlay();

    private static native int naPrepare(String str, String str2, int i);

    private static native int naSetSurface(Surface surface);

    private static native int naSetup(int i, int i2);

    private static native void naStop();

    private static native int renderFrame(Bitmap bitmap);

    @Override // android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setTheme(android.R.style.Theme.Holo.Light.NoActionBar.TranslucentDecor);
        setContentView(R.layout.activity_filelistviewblackbox5);
        Log.e("HAN2", "MainFileListView_blackbox4 시작 블랙박스 사진");
        if (Build.VERSION.SDK_INT > 9) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
        }
        File externalStorageDirectory = Environment.getExternalStorageDirectory();
        if (Environment.getExternalStorageState().equals("mounted")) {
            StringBuilder sb = new StringBuilder();
            sb.append(externalStorageDirectory);
            MainProtocol mainProtocol = this.mprotocol;
            sb.append(MainProtocol.FILE_PATH_PHOTO);
            String sb2 = sb.toString();
            StringBuilder sb3 = new StringBuilder();
            sb3.append(externalStorageDirectory);
            MainProtocol mainProtocol2 = this.mprotocol;
            sb3.append(MainProtocol.FILE_PATH_PHOTO);
            sb3.toString();
            Log.e("HAN", "dirPath  = " + sb2);
            File file = new File(sb2);
            if (!file.exists()) {
                file.mkdirs();
            }
        }
        this.country = getResources().getConfiguration().locale.getCountry();
        SharedPreferences sharedPreferences = getSharedPreferences("save_edit", 0);
        if (sharedPreferences != null) {
            this.up_down_flag = sharedPreferences.getInt("save_edit", 0);
            Log.e("HAN7", "up_down_flag5 =" + this.up_down_flag);
        }
        if (this.up_down_flag == 0) {
            if (this.country.equals("KR")) {
                MainFileListView.btn_descend.setBackgroundResource(R.drawable.updown_up);
            } else {
                MainFileListView.btn_descend.setBackgroundResource(R.drawable.updown_up_eng);
            }
            if (this.country.equals("TW") || this.country.equals("HK") || this.country.equals("CN")) {
                MainFileListView.btn_descend.setBackgroundResource(R.drawable.updown_up_chn);
            }
        } else {
            if (this.country.equals("KR")) {
                MainFileListView.btn_descend.setBackgroundResource(R.drawable.updown_down);
            } else {
                MainFileListView.btn_descend.setBackgroundResource(R.drawable.updown_down_eng);
            }
            if (this.country.equals("TW") || this.country.equals("HK") || this.country.equals("CN")) {
                MainFileListView.btn_descend.setBackgroundResource(R.drawable.updown_down_chn);
            }
        }
        flag_count = 1;
        MainFreeView.freeviewstop_flag = 1;
        tvCustId2 = (TextView) findViewById(R.id.tvCoustId2);
        int i = ((WifiManager) getApplicationContext().getSystemService("wifi")).getDhcpInfo().gateway;
        String format = String.format("%d.%d.%d.%d", Integer.valueOf(i & 255), Integer.valueOf((i >> 8) & 255), Integer.valueOf((i >> 16) & 255), Integer.valueOf((i >> 24) & 255));
        Log.e("HAN2", "ipAddress =" + format);
        if (format.equals("192.168.100.1")) {
            FilListDialog();
            new Handler().postDelayed(new Runnable() { // from class: com.logon_e100.MainFileListView_blackbox5.1
                @Override // java.lang.Runnable
                public void run() {
                    MainFileListView_blackbox5.this.RecordOnOff();
                }
            }, 300L);
            new Handler().postDelayed(new Runnable() { // from class: com.logon_e100.MainFileListView_blackbox5.2
                @Override // java.lang.Runnable
                public void run() {
                    MainFileListView_blackbox5 mainFileListView_blackbox5 = MainFileListView_blackbox5.this;
                    mainFileListView_blackbox5.FileList(mainFileListView_blackbox5.up_down_flag);
                }
            }, 350L);
        } else {
            Toast.makeText(this, getResources().getString(R.string.wifi_connection_required), 1).show();
        }
        mListView = (ListView) findViewById(R.id.moreList);
        marItem = new ArrayList<>();
        mLockListView = true;
        this.mInflater = (LayoutInflater) getSystemService("layout_inflater");
        mMyAdapte = new MyListAdapter(this, R.layout.activity_fileviewblackboc_item, marItem);
        mListView.setAdapter((ListAdapter) mMyAdapte);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() { // from class: com.logon_e100.MainFileListView_blackbox5.3
            /* JADX WARN: Type inference failed for: r3v1, types: [android.widget.Adapter] */
            @Override // android.widget.AdapterView.OnItemClickListener
            public void onItemClick(AdapterView<?> adapterView, View view, int i2, long j) {
                int i3;
                Log.e("HAN2", "파일리스트 클릭");
                if (MainFileListView_blackbox5.file_flag == 1) {
                    i3 = (MainFileListView_blackbox5.this.total_filecount - 1) - i2;
                    Log.e("HAN2", "position =  " + i3);
                } else {
                    i3 = i2;
                }
                Object item = adapterView.getAdapter().getItem(i2);
                MainFileListView_blackbox5 mainFileListView_blackbox5 = MainFileListView_blackbox5.this;
                mainFileListView_blackbox5.file_vo = (String) item;
                String substring = mainFileListView_blackbox5.file_vo.substring(16, 17);
                if (MainFileListView_blackbox5.this.file_vo.substring(17, 18).equals("B")) {
                    Log.e("HAN2", "2채널 재생 file_vo =" + MainFileListView_blackbox5.this.file_vo);
                    Intent intent = new Intent(MainFileListView_blackbox5.this, (Class<?>) MainFileFreeView.class);
                    intent.putExtra("file_index", i3);
                    intent.putExtra("file_stae", substring);
                    intent.putExtra("file_ch", 1);
                    MainFileListView_blackbox5.this.startActivity(intent);
                    return;
                }
                Log.e("HAN2", "1채널 재생 file_vo =" + MainFileListView_blackbox5.this.file_vo);
                Intent intent2 = new Intent(MainFileListView_blackbox5.this, (Class<?>) MainFileFreeView.class);
                intent2.putExtra("file_index", i3);
                intent2.putExtra("file_stae", substring);
                intent2.putExtra("file_ch", 0);
                MainFileListView_blackbox5.this.startActivity(intent2);
            }
        });
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() { // from class: com.logon_e100.MainFileListView_blackbox5.4
            @Override // android.widget.AbsListView.OnScrollListener
            public void onScroll(AbsListView absListView, int i2, int i3, int i4) {
            }

            @Override // android.widget.AbsListView.OnScrollListener
            public void onScrollStateChanged(AbsListView absListView, int i2) {
                Log.d("namh", "scrollStateChanged = " + i2);
                MainFileListView_blackbox5.mListView.getFirstVisiblePosition();
                MainFileListView_blackbox5.mListView.getChildCount();
                switch (i2) {
                    case 0:
                        Log.d("namh", "스크롤 정지");
                        MainFileListView.btn_descend.setEnabled(true);
                        break;
                    case 1:
                        Log.d("namh", "SCROLL_STATE_TOUCH_SCROLL = ");
                        Log.d("namh", "스크롤 이동중");
                        MainFileListView.btn_descend.setEnabled(false);
                        break;
                    case 2:
                        Log.d("namh", "SCROLL_STATE_FLING = ");
                        MainFileListView.btn_descend.setEnabled(false);
                        break;
                }
            }
        });
    }

    public static void liststart(String str, int i, int i2) {
        addItems(str, i, i2);
    }

    public static class MyItem {
        int order;
        String sCustId;
        String sCustId2;

        MyItem(String str, String str2, int i) {
            this.order = i;
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
        public View getView(final int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = this.lInflater.inflate(this.layout, viewGroup, false);
            }
            String str = this.alSrc.get(i).sCustId;
            ((TextView) view.findViewById(R.id.tvCoustId)).setText(this.alSrc.get(i).sCustId);
            String str2 = this.alSrc.get(i).sCustId2;
            ((TextView) view.findViewById(R.id.tvCoustId2)).setText(this.alSrc.get(i).sCustId2);
            view.findViewById(R.id.tvCoustId2).setVisibility(8);
            MainFileListView_blackbox5.this.total_filecount = this.alSrc.size();
            ((Button) view.findViewById(R.id.sendBtn)).setOnClickListener(new View.OnClickListener() { // from class: com.logon_e100.MainFileListView_blackbox5.MyListAdapter.1
                @Override // android.view.View.OnClickListener
                public void onClick(View view2) {
                    MainFileListView_blackbox5.this.down_pos = i;
                    Log.d("HAN3", "파일 다운로드  클릭");
                    if (MainFileListView_blackbox5.file_flag == 1) {
                        MainFileListView_blackbox5.this.down_pos = (MainFileListView_blackbox5.this.total_filecount - 1) - MainFileListView_blackbox5.this.down_pos;
                        Log.e("HAN2", "11111position =  " + MainFileListView_blackbox5.this.down_pos);
                    }
                    MainFileListView_blackbox5.this.FileDownDialog();
                    final String str3 = MyListAdapter.this.alSrc.get(i).sCustId;
                    String str4 = MyListAdapter.this.alSrc.get(i).sCustId2;
                    Log.d("HAN3", "다운로드  filename =" + str3);
                    Log.d("HAN3", "filename =" + str3);
                    Log.d("HAN3", "file_ch=" + str4);
                    new Handler().postDelayed(new Runnable() { // from class: com.logon_e100.MainFileListView_blackbox5.MyListAdapter.1.1
                        @Override // java.lang.Runnable
                        public void run() {
                            MainFileListView_blackbox5.this.Filedown(MainFileListView_blackbox5.this.down_pos, 0);
                        }
                    }, 1000L);
                    new Handler().postDelayed(new Runnable() { // from class: com.logon_e100.MainFileListView_blackbox5.MyListAdapter.1.2
                        @Override // java.lang.Runnable
                        public void run() {
                            new Thread(MainFileListView_blackbox5.this.new TCPclient(str3)).start();
                        }
                    }, 2000L);
                }
            });
            return view;
        }
    }

    static {
        System.loadLibrary("avutil-54");
        System.loadLibrary("avcodec-56");
        System.loadLibrary("avformat-56");
        System.loadLibrary("swresample-1");
        System.loadLibrary("swscale-3");
        System.loadLibrary("tutorial02");
    }

    public static void addItems(final String str, final int i, final int i2) {
        mLockListView = true;
        new Handler().postDelayed(new Runnable() { // from class: com.logon_e100.MainFileListView_blackbox5.6
            @Override // java.lang.Runnable
            public void run() {
                MainFileListView_blackbox5.items = new MyItem(str + "g", String.valueOf(i), i2);
                MainFileListView_blackbox5.marItem.add(MainFileListView_blackbox5.items);
                if (MainFileListView_blackbox5.file_flag != 0) {
                    Collections.sort(MainFileListView_blackbox5.marItem, MainFileListView_blackbox5.myComparator2);
                    Collections.reverse(MainFileListView_blackbox5.marItem);
                }
                MainFileListView_blackbox5.flag_count++;
                MainFileListView_blackbox5.mMyAdapte.notifyDataSetChanged();
                boolean unused = MainFileListView_blackbox5.mLockListView = false;
            }
        }, 100L);
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
        Log.e("HAN2", "wifi접속 데이터 보내기");
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
        Log.e("HAN2", "블랙박스 상시 영상 온오프보내기");
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
        Log.e("HAN2", "프리뷰 정지");
    }

    public void SdCardCheck() {
        byte[] bArr = new byte[256];
        bArr[0] = 2;
        MainProtocol mainProtocol = this.mprotocol;
        bArr[1] = 42;
        bArr[2] = (byte) 4;
        byte b = (byte) 0;
        bArr[3] = b;
        bArr[4] = b;
        bArr[5] = b;
        bArr[6] = 0;
        bArr[7] = 0;
        bArr[8] = 0;
        bArr[9] = 0;
        bArr[10] = 3;
        bArr[11] = bArr[0];
        for (int i = 1; i < 11; i++) {
            bArr[11] = (byte) (bArr[11] ^ bArr[i]);
        }
        try {
            MainProtocol mainProtocol2 = this.mprotocol;
            MainProtocol.m_out_stream.write(bArr, 0, 12);
            MainProtocol mainProtocol3 = this.mprotocol;
            MainProtocol.m_out_stream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.e("HAN2", "sd체크 보내기");
    }

    public void FileList(int i) {
        MainProtocol.CNSSimpleIndex cNSSimpleIndex = new MainProtocol.CNSSimpleIndex();
        cNSSimpleIndex.setIndex(3);
        file_flag = i;
        marItem.clear();
        MainProtocol.send(40997, cNSSimpleIndex.getBytes(), 9);
        Log.e("HAN2", "파일 리스트");
    }

    public void FileListTrans() {
        byte[] bArr = new byte[256];
        bArr[0] = 2;
        MainProtocol mainProtocol = this.mprotocol;
        bArr[1] = 32;
        bArr[2] = (byte) 4;
        byte b = (byte) 0;
        bArr[3] = b;
        bArr[4] = b;
        bArr[5] = b;
        bArr[6] = 1;
        bArr[7] = 0;
        bArr[8] = 0;
        bArr[9] = 0;
        bArr[10] = 3;
        bArr[11] = bArr[0];
        for (int i = 1; i < 11; i++) {
            bArr[11] = (byte) (bArr[11] ^ bArr[i]);
        }
        try {
            MainProtocol mainProtocol2 = this.mprotocol;
            MainProtocol.m_out_stream.write(bArr, 0, 12);
            MainProtocol mainProtocol3 = this.mprotocol;
            MainProtocol.m_out_stream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.e("HAN2", "파일리스트 트렌");
    }

    public void SetupList(final String str, int i, final int i2) {
        String string = getResources().getString(R.string.file_playback_title);
        new AlertDialog.Builder(MainActivity.context).setTitle(string).setIcon(R.drawable.ic_launcher).setItems(new CharSequence[]{getResources().getString(R.string.blackbox_camera_front), getResources().getString(R.string.blackbox_camera_rear)}, new DialogInterface.OnClickListener() { // from class: com.logon_e100.MainFileListView_blackbox5.7
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i3) {
                switch (i3) {
                    case 0:
                        Log.e("HAN2", "1채널 재생");
                        Intent intent = new Intent(MainFileListView_blackbox5.this, (Class<?>) MainFileFreeView.class);
                        intent.putExtra("file_index", i2);
                        intent.putExtra("file_stae", str);
                        intent.putExtra("file_ch", 0);
                        MainFileListView_blackbox5.this.startActivity(intent);
                        break;
                    case 1:
                        Log.e("HAN2", "2채널 재생");
                        Intent intent2 = new Intent(MainFileListView_blackbox5.this, (Class<?>) MainFileFreeView.class);
                        intent2.putExtra("file_index", i2);
                        intent2.putExtra("file_stae", str);
                        intent2.putExtra("file_ch", 1);
                        MainFileListView_blackbox5.this.startActivity(intent2);
                        break;
                }
            }
        }).show();
    }

    public void SetupDownList(final String str, int i, final int i2) {
        String string = getResources().getString(R.string.file_download_title);
        Log.e("HAN2", "fileroot3 =" + str);
        Log.e("HAN2", "fileroot3 = " + str);
        new AlertDialog.Builder(MainActivity.context).setTitle(string).setIcon(R.drawable.ic_launcher).setItems(new CharSequence[]{"전방", "후방"}, new DialogInterface.OnClickListener() { // from class: com.logon_e100.MainFileListView_blackbox5.8
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i3) {
                switch (i3) {
                    case 0:
                        Log.e("HAN2", "1채널 다운로드");
                        MainFileListView_blackbox5.this.FileDownDialog();
                        new Handler().postDelayed(new Runnable() { // from class: com.logon_e100.MainFileListView_blackbox5.8.1
                            @Override // java.lang.Runnable
                            public void run() {
                                MainFileListView_blackbox5.this.Filedown(i2, 0);
                            }
                        }, 1000L);
                        new Handler().postDelayed(new Runnable() { // from class: com.logon_e100.MainFileListView_blackbox5.8.2
                            @Override // java.lang.Runnable
                            public void run() {
                                MainFileListView_blackbox5.this.new TCPclient(str).run();
                            }
                        }, 4000L);
                        break;
                    case 1:
                        Log.e("HAN2", "2채널 다운로드");
                        MainFileListView_blackbox5.this.FileDownDialog();
                        new Handler().postDelayed(new Runnable() { // from class: com.logon_e100.MainFileListView_blackbox5.8.3
                            @Override // java.lang.Runnable
                            public void run() {
                                MainFileListView_blackbox5.this.Filedown(i2, 1);
                            }
                        }, 1000L);
                        new Handler().postDelayed(new Runnable() { // from class: com.logon_e100.MainFileListView_blackbox5.8.4
                            @Override // java.lang.Runnable
                            public void run() {
                                MainFileListView_blackbox5.this.new TCPclient(str).run();
                            }
                        }, 4000L);
                        break;
                }
            }
        }).show();
    }

    public void SetupDownList2(final String str, int i, final int i2) {
        Log.e("HAN2", "fileroot3 =" + str);
        Log.e("HAN2", "fileroot3 = " + str);
        new AlertDialog.Builder(MainActivity.context).setTitle("사진 다운로드").setIcon(R.drawable.ic_launcher).setItems(new CharSequence[]{str}, new DialogInterface.OnClickListener() { // from class: com.logon_e100.MainFileListView_blackbox5.9
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i3) {
                if (i3 != 0) {
                    return;
                }
                Log.e("HAN2", "1채널 다운로드");
                MainFileListView_blackbox5.this.FileDownDialog();
                new Handler().postDelayed(new Runnable() { // from class: com.logon_e100.MainFileListView_blackbox5.9.1
                    @Override // java.lang.Runnable
                    public void run() {
                        MainFileListView_blackbox5.this.Filedown(i2, 0);
                        Log.e("HAN2", "file_pos3 " + i2);
                    }
                }, 1000L);
                new Handler().postDelayed(new Runnable() { // from class: com.logon_e100.MainFileListView_blackbox5.9.2
                    @Override // java.lang.Runnable
                    public void run() {
                        Log.e("HAN2", "사진 다운 11111111111111111111");
                        TCPclient tCPclient = MainFileListView_blackbox5.this.new TCPclient(str);
                        Log.e("HAN2", "사진 다운 22222222222222222");
                        tCPclient.run();
                        Log.e("HAN2", "사진 다운 33333333333333");
                    }
                }, 2000L);
            }
        }).show();
    }

    public void Filedown(int i, int i2) {
        Log.e("HAN2", "상시 file_index =" + i);
        Log.e("HAN2", "상시 file_ch = " + i2);
        byte[] bArr = new byte[256];
        bArr[0] = 2;
        bArr[1] = 32;
        bArr[2] = 8;
        bArr[3] = (byte) 12;
        byte b = (byte) 0;
        bArr[4] = b;
        bArr[5] = b;
        bArr[6] = b;
        bArr[7] = 3;
        bArr[8] = 0;
        bArr[9] = 0;
        bArr[10] = 0;
        bArr[11] = (byte) (i & 255);
        bArr[12] = (byte) ((i >> 8) & 255);
        bArr[13] = (byte) ((i >> 16) & 255);
        bArr[14] = (byte) ((i >> 24) & 255);
        bArr[15] = (byte) i2;
        bArr[16] = 0;
        bArr[17] = 0;
        bArr[18] = 0;
        bArr[19] = 3;
        bArr[20] = bArr[0];
        for (int i3 = 1; i3 < 20; i3++) {
            bArr[20] = (byte) (bArr[20] ^ bArr[i3]);
        }
        try {
            MainProtocol mainProtocol = this.mprotocol;
            MainProtocol.m_out_stream.write(bArr, 0, 21);
            MainProtocol mainProtocol2 = this.mprotocol;
            MainProtocol.m_out_stream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.e("HAN2", "파일다운로드");
    }

    public void onBtnConnect(String str) {
        try {
            File externalStorageDirectory = Environment.getExternalStorageDirectory();
            if (this.socket == null) {
                this.socket = new Socket(MainProtocol.SERVERIP, MainProtocol.FILEDOWNPORT);
            }
            this.in = this.socket.getInputStream();
            StringBuilder sb = new StringBuilder();
            sb.append(externalStorageDirectory);
            MainProtocol mainProtocol = this.mprotocol;
            sb.append(MainProtocol.FILE_PATH_PHOTO);
            sb.append(str);
            this.out = new FileOutputStream(sb.toString());
            this.mCheckRecv.start();
            Log.e("HAN2", "파일 서버 접속");
        } catch (Exception e) {
            Log.d("tag", "Socket connect error =." + e);
            this.mProgressDialog.dismiss();
        }
    }

    private class TCPclient implements Runnable {
        private String filename;

        public TCPclient(String str) {
            this.filename = str;
        }

        @Override // java.lang.Runnable
        public void run() {
            FileOutputStream fileOutputStream;
            try {
                File externalStorageDirectory = Environment.getExternalStorageDirectory();
                Socket socket = new Socket(MainProtocol.SERVERIP, MainProtocol.FILEDOWNPORT);
                try {
                    try {
                        MainFileListView_blackbox5.this.in = socket.getInputStream();
                        StringBuilder sb = new StringBuilder();
                        sb.append("222 =");
                        sb.append(externalStorageDirectory);
                        MainProtocol mainProtocol = MainFileListView_blackbox5.this.mprotocol;
                        sb.append(MainProtocol.FILE_PATH_PHOTO);
                        sb.append(this.filename);
                        Log.d("HAN3", sb.toString());
                        MainFileListView_blackbox5 mainFileListView_blackbox5 = MainFileListView_blackbox5.this;
                        StringBuilder sb2 = new StringBuilder();
                        sb2.append(externalStorageDirectory);
                        MainProtocol mainProtocol2 = MainFileListView_blackbox5.this.mprotocol;
                        sb2.append(MainProtocol.FILE_PATH_PHOTO);
                        sb2.append(this.filename);
                        mainFileListView_blackbox5.out = new FileOutputStream(sb2.toString());
                        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(MainFileListView_blackbox5.this.out);
                        Log.d("HAN3", "filename =" + this.filename);
                        byte[] bArr = new byte[2048];
                        while (true) {
                            int read = MainFileListView_blackbox5.this.in.read(bArr, 0, bArr.length);
                            if (read == -1) {
                                break;
                            }
                            bufferedOutputStream.write(bArr, 0, read);
                            bufferedOutputStream.flush();
                        }
                        Log.d("HAN3", "HAN5 = bytesRead == -1");
                        bufferedOutputStream.close();
                        MainFileListView_blackbox5.this.out.close();
                        if (MainFileListView_blackbox5.this.mProgressDialog != null) {
                            MainFileListView_blackbox5.this.mProgressDialog.dismiss();
                        }
                        MainFileListView_blackbox5.this.out.flush();
                        fileOutputStream = MainFileListView_blackbox5.this.out;
                    } catch (Exception e) {
                        Log.e("TCP", "C: Error1", e);
                        MainFileListView_blackbox5.this.out.flush();
                        fileOutputStream = MainFileListView_blackbox5.this.out;
                    }
                    fileOutputStream.close();
                    socket.close();
                } catch (Throwable th) {
                    MainFileListView_blackbox5.this.out.flush();
                    MainFileListView_blackbox5.this.out.close();
                    socket.close();
                    throw th;
                }
            } catch (Exception unused) {
            }
        }
    }

    public void FileDownDialog() {
        runOnUiThread(new Runnable() { // from class: com.logon_e100.MainFileListView_blackbox5.11
            @Override // java.lang.Runnable
            public void run() {
                String string = MainFileListView_blackbox5.this.getResources().getString(R.string.file_downloading);
                MainFileListView_blackbox5.this.mProgressDialog = ProgressDialog.show(MainActivity.context, "", string, true);
            }
        });
    }

    public void FilListDialog() {
        runOnUiThread(new Runnable() { // from class: com.logon_e100.MainFileListView_blackbox5.12
            @Override // java.lang.Runnable
            public void run() {
                MainFileListView_blackbox5.mProgressDialog2 = ProgressDialog.show(MainActivity.context, "", MainFileListView_blackbox5.this.getResources().getString(R.string.file_list_loading), true);
            }
        });
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
                new AlertDialog.Builder(MainActivity.context).setTitle(string).setMessage(string2).setPositiveButton(string3, new DialogInterface.OnClickListener() { // from class: com.logon_e100.MainFileListView_blackbox5.13
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i2) {
                        WifiManager wifiManager = (WifiManager) MainFileListView_blackbox5.this.getApplicationContext().getSystemService("wifi");
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
                        SystemClock.sleep(100L);
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
}
