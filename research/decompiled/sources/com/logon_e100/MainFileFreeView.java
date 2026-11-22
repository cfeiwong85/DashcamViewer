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
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.media.AudioTrack;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.os.StrictMode;
import android.os.SystemClock;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import com.logon.e100.utils.Utils;
import com.logon_e100.MainProtocol;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.Thread;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import uk.co.senab.photoview.PhotoViewAttacher;

@SuppressLint({"HandlerLeak", "ClickableViewAccessibility"})
@TargetApi(11)
/* loaded from: classes.dex */
public class MainFileFreeView extends Activity implements View.OnClickListener {
    private static int FRONT_REAR_DURATION = 30;
    private static int INTERVAL_FOR_25FPS = 40000;
    private static int INTERVAL_FOR_30FPS = 33300;
    public static String filechannele = null;
    public static int filefreeviewstop_flag2 = 0;
    public static String filename2 = null;
    private static Context mContext = null;
    private static SeekBar mSeekBar = null;
    private static TextView mTextViewDuration = null;
    private static int minBufferSize = 2048;
    private static AudioTrack myAudioTrack;
    static final Handler setDurationHandler;
    ProgressBar asyncProgressBar;
    Bitmap backbit;
    int file_ch;
    int file_index;
    String file_stae;
    int height;
    float heightScaledRatio;
    private InputStream in_audio;
    PhotoViewAttacher mAttacher;
    private LinearLayout mBottomControlBox;
    private int mCurrentFrame;
    private int mDuration;
    private Handler mHandler;
    private ProgressDialog mProgressDialog;
    BufferedReader mReader;
    private SurfaceView mSurfaceView;
    private TextView mTextViewCurrentTime;
    BufferedWriter mWriter;
    MainProtocol mprotocol;
    int old_height;
    int old_width;
    private BufferedOutputStream out_audio;
    int[] res;
    int[] screenRes;
    Matrix sideInversion;
    Bitmap sideInversionImg;
    Socket socket3;
    StringTokenizer st;
    Thread thread;
    ImageView tmpIv;
    int width;
    float widthScaledRatio;
    Socket mSock = null;
    String mRecvData = "";
    byte[] readsize = new byte[1024];
    byte[] readchannle = new byte[1024];
    int data_size = 0;
    int db_ch = 0;
    Socket socket_audio = null;
    boolean audio_flag = false;
    Handler seekbarUpdateHandler = null;
    private long TIME_BASE = 1000;
    private long mInterval = INTERVAL_FOR_25FPS / this.TIME_BASE;
    private long mOldTime = 0;
    private long mCurTime = 0;
    private List<byte[]> mAudioQueue2 = Collections.synchronizedList(new LinkedList());
    private boolean mAudioStarted = false;
    private int mFrameCnt = 4;
    Boolean bOrientation = false;
    int deviceHeight = 0;
    int time_sec = 0;
    int time_sec_check = 0;
    boolean videoStarted = true;
    int mRevers_flag = 1;
    int ORIENTATION_FLAG = 1;
    private Thread mCheckRecv = new Thread() { // from class: com.logon_e100.MainFileFreeView.2
        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            try {
                Log.e("HAN2", "리드 시작");
                int i = 0;
                while (true) {
                    int i2 = MainFileFreeView.minBufferSize;
                    byte[] bArr = new byte[MainFileFreeView.minBufferSize];
                    int i3 = i;
                    int i4 = 0;
                    while (true) {
                        if (i4 >= MainFileFreeView.minBufferSize) {
                            i = i3;
                            break;
                        }
                        i3 = MainFileFreeView.this.in_audio.read(bArr, i4, i2);
                        if (i3 <= 0) {
                            i = i3;
                            break;
                        } else {
                            i4 += i3;
                            i2 -= i3;
                        }
                    }
                    if (i <= 0) {
                        return;
                    } else {
                        MainFileFreeView.this.mAudioQueue2.add(bArr);
                    }
                }
            } catch (Exception unused) {
                Log.d("tag", "오디오 Receive error");
                MainFileFreeView.this.mCheckRecv.interrupt();
            }
        }
    };
    private Thread mAudioRender = new Thread() { // from class: com.logon_e100.MainFileFreeView.3
        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            byte[] bArr = new byte[MainFileFreeView.minBufferSize];
            while (!Thread.interrupted() && MainFileFreeView.filefreeviewstop_flag2 == 0 && MainFileFreeView.myAudioTrack != null) {
                if (!MainFileFreeView.this.mAudioStarted && MainFileFreeView.this.mCurrentFrame > 0) {
                    Log.d("mhhan", "audio start!!!");
                    MainFileFreeView.myAudioTrack.play();
                    MainFileFreeView.this.mAudioStarted = true;
                }
                if (MainFileFreeView.this.isAudioWrite()) {
                    byte[] bArr2 = (byte[]) MainFileFreeView.this.mAudioQueue2.get(0);
                    if (MainFileFreeView.myAudioTrack.write(bArr2, 0, bArr2.length) == bArr2.length) {
                        MainFileFreeView.this.mAudioQueue2.remove(0);
                    }
                    try {
                        Thread.sleep(20L);
                    } catch (InterruptedException e) {
                        Log.d("mhhan", e.toString());
                    }
                }
            }
            MainFileFreeView.this.closeAudioTrack();
        }
    };
    private Object syncObj = new Object();
    Handler displayAlertFileDownFailHandler = new Handler() { // from class: com.logon_e100.MainFileFreeView.6
        @Override // android.os.Handler
        public void handleMessage(Message message) {
            String[] strArr = (String[]) message.obj;
            MainFileFreeView.this.displayFileDownAlert(strArr[0], strArr[1]);
        }
    };
    private Handler handler = new Handler() { // from class: com.logon_e100.MainFileFreeView.9
        @Override // android.os.Handler
        public void handleMessage(Message message) {
            if (MainFileFreeView.this.backbit != null) {
                MainFileFreeView.this.tmpIv.setImageBitmap(MainFileFreeView.this.backbit);
                MainFileFreeView.this.tmpIv.invalidate();
                MainFileFreeView.this.mOldTime = System.currentTimeMillis();
                MainFileFreeView.access$608(MainFileFreeView.this);
            }
        }
    };
    View.OnTouchListener mTouchListener = new View.OnTouchListener() { // from class: com.logon_e100.MainFileFreeView.13
        @Override // android.view.View.OnTouchListener
        public boolean onTouch(View view, MotionEvent motionEvent) {
            return true;
        }
    };
    Runnable mRunSeekbarUpdate = new Runnable() { // from class: com.logon_e100.MainFileFreeView.15
        @Override // java.lang.Runnable
        public void run() {
            if (MainFileFreeView.mSeekBar.getMax() != 0) {
                MainFileFreeView.mSeekBar.setProgress(MainFileFreeView.this.mCurrentFrame);
                MainFileFreeView.this.mTextViewCurrentTime.setText(Utils.timeToText(MainFileFreeView.this.mCurrentFrame / MainFileFreeView.FRONT_REAR_DURATION));
            }
            if (MainFileFreeView.this.seekbarUpdateHandler != null) {
                MainFileFreeView.this.seekbarUpdateHandler.postDelayed(this, 500L);
            }
        }
    };
    private Thread checkUpdate2 = new Thread() { // from class: com.logon_e100.MainFileFreeView.18
        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            while (MainFileFreeView.this.time_sec_check <= 6) {
                try {
                    Thread.sleep(1000L);
                    Log.e("HAN3", "time_sec_check =" + MainFileFreeView.this.time_sec_check);
                    MainFileFreeView mainFileFreeView = MainFileFreeView.this;
                    mainFileFreeView.time_sec_check = mainFileFreeView.time_sec_check + 1;
                    if (MainFileFreeView.this.time_sec_check == 6) {
                        Log.e("HAN3", "여기니?");
                        Log.e("HAN3", "time_sec_check =" + MainFileFreeView.this.time_sec_check);
                        if (MainFileFreeView.this.mProgressDialog != null) {
                            MainFileFreeView.this.mProgressDialog.dismiss();
                        }
                    }
                } catch (Exception e) {
                    Log.e("HAN3", "time_sec111111111111111111111 ㄷ" + e);
                    return;
                }
            }
        }
    };
    final Handler handler16 = new Handler() { // from class: com.logon_e100.MainFileFreeView.19
        @Override // android.os.Handler
        public void handleMessage(Message message) {
            MainFileFreeView.this.displayPlayFailedAlert2(MainFileFreeView.this.getResources().getString(R.string.file_playback_title), MainFileFreeView.this.getResources().getString(R.string.file_playback_error));
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

    /* JADX INFO: Access modifiers changed from: private */
    public static native void naStop();

    /* JADX INFO: Access modifiers changed from: private */
    public static native int renderFrame(Bitmap bitmap);

    public Bitmap decodeSampledBitmapFromResource(Resources resources, int i, int i2, int i3) {
        return null;
    }

    static /* synthetic */ int access$608(MainFileFreeView mainFileFreeView) {
        int i = mainFileFreeView.mCurrentFrame;
        mainFileFreeView.mCurrentFrame = i + 1;
        return i;
    }

    @Override // android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getWindow().addFlags(128);
        setContentView(R.layout.activity_filefreeview);
        Log.e("HAN3", "MainFileFreeView onCreate");
        mContext = this;
        getWindow().setFlags(16777216, 16777216);
        if (Build.VERSION.SDK_INT > 9) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
        }
        this.sideInversion = new Matrix();
        this.sideInversion.setScale(-1.0f, 1.0f);
        this.mHandler = new Handler();
        filefreeviewstop_flag2 = 0;
        MainFreeView.freeviewstop_flag2 = 0;
        MainProtocol.OnStopFlag = false;
        FilListDialog();
        this.checkUpdate2.start();
        new Counter().start();
        Resources resources = getResources();
        int identifier = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (identifier > 0) {
            this.deviceHeight = resources.getDimensionPixelSize(identifier);
            Log.e("HAN2", "deviceHeight = " + this.deviceHeight);
        }
        Intent intent = new Intent(getIntent());
        this.file_index = intent.getIntExtra("file_index", 0);
        this.file_stae = intent.getExtras().getString("file_stae");
        this.file_ch = intent.getIntExtra("file_ch", 0);
        Log.e("HAN2", "file_index = " + this.file_index);
        Log.e("HAN2", "file_stae = " + this.file_stae);
        Log.e("HAN2", "file_ch = " + this.file_ch);
        SharedPreferences sharedPreferences = getSharedPreferences("seve_camreversed", 0);
        if (sharedPreferences != null) {
            this.mRevers_flag = sharedPreferences.getInt("seve_camreversed", 1);
            Log.e("HAN7", "mRevers_flag  =" + this.mRevers_flag);
        }
        if (this.file_ch == 0) {
            this.mInterval = INTERVAL_FOR_30FPS / this.TIME_BASE;
            FRONT_REAR_DURATION = 30;
            minBufferSize = 2144;
        } else {
            this.mInterval = INTERVAL_FOR_25FPS / this.TIME_BASE;
            FRONT_REAR_DURATION = 25;
            minBufferSize = 2560;
        }
        Log.d("mhhan", "play interval = " + this.mInterval);
        myAudioTrack = new AudioTrack(3, 8000, 4, 2, minBufferSize, 1);
        Log.d("mhhan", "audio minbuffer size = " + minBufferSize);
        initControls();
        new Handler().postDelayed(new Runnable() { // from class: com.logon_e100.MainFileFreeView.1
            @Override // java.lang.Runnable
            public void run() {
                MainFileFreeView mainFileFreeView = MainFileFreeView.this;
                mainFileFreeView.Fileview(mainFileFreeView.file_index, MainFileFreeView.this.file_stae, MainFileFreeView.this.file_ch);
            }
        }, 1000L);
    }

    public void onBtnConnect() {
        try {
            MainProtocol mainProtocol = this.mprotocol;
            String str = MainProtocol.SERVERIP;
            MainProtocol mainProtocol2 = this.mprotocol;
            this.socket_audio = new Socket(str, MainProtocol.AUDIOPORT);
            this.out_audio = new BufferedOutputStream(this.socket_audio.getOutputStream());
            this.in_audio = this.socket_audio.getInputStream();
            Log.e("HAN2", "#### thread state = " + this.mCheckRecv.getState());
            if (this.mCheckRecv.getState() == Thread.State.NEW) {
                this.mCheckRecv.start();
            }
            Log.e("HAN2", "서버 접속");
        } catch (Exception e) {
            Log.d("tag", "Socket connect error.");
            Log.d("HAN3", "error = " + e);
            this.mAudioRender.interrupt();
        }
    }

    public void currentFrameIns() {
        synchronized (this.syncObj) {
            this.mCurrentFrame++;
        }
    }

    public boolean isAudioWrite() {
        synchronized (this.syncObj) {
            if (!this.mAudioStarted || this.mAudioQueue2.isEmpty() || this.mCurrentFrame < this.mFrameCnt) {
                return false;
            }
            this.mFrameCnt += 4;
            return true;
        }
    }

    public void closeAudioTrack() {
        Log.d("mhhan", "audio track close");
        AudioTrack audioTrack = myAudioTrack;
        if (audioTrack != null) {
            audioTrack.flush();
            myAudioTrack.stop();
            myAudioTrack.release();
            myAudioTrack = null;
        }
        if (this.mAudioQueue2.isEmpty()) {
            return;
        }
        this.mAudioQueue2.clear();
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        Log.e("HAN2", "클릭");
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
        int naInit;
        Log.e("HAN2", "파일 ffmpeg 시작");
        if (this.file_ch == 1) {
            naInit = naInit("abc", "abc", "b", 1);
        } else {
            naInit = naInit("abc", "abc", "b", 1);
        }
        if (naInit < 0) {
            SystemClock.sleep(1000L);
            if (this.file_ch == 1) {
                naInit = naInit("abc", "abc", "b", 1);
            } else {
                naInit = naInit("abc", "abc", "b", 1);
            }
            Log.d("HAN2", "재시도 ");
        }
        Log.d("HAN2", "ret = " + naInit);
        if (naInit < 0) {
            Log.e("mhhan", "test nainit failed!!");
            ProgressDialog progressDialog = this.mProgressDialog;
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
            this.mAudioRender.interrupt();
            displayPlayFailedAlert(getResources().getString(R.string.file_playback_title), getResources().getString(R.string.file_playback_error));
            return;
        }
        this.res = naGetVideoRes();
        this.screenRes = getScreenRes();
        int[] iArr = this.screenRes;
        int[] iArr2 = this.res;
        this.widthScaledRatio = (iArr[0] * 1.0f) / iArr2[0];
        this.heightScaledRatio = (iArr[1] * 1.0f) / iArr2[1];
        Log.d("HAN2", "res  =" + this.res);
        Log.d("HAN2", "screenRes  =" + this.screenRes);
        int[] iArr3 = this.res;
        this.width = (int) (((float) iArr3[0]) * this.widthScaledRatio);
        this.height = (int) (((float) iArr3[1]) * this.heightScaledRatio);
        this.old_width = this.width;
        this.old_height = this.height;
        Log.d("HAN2", "res[0] = " + this.res[0] + "  res[1] = " + this.res[1]);
        Log.d("HAN2", "widthScaledRatio = " + this.widthScaledRatio + "  heightScaledRatio = " + this.heightScaledRatio);
        Log.d("HAN2", "width = " + this.width + "  height = " + this.height);
        this.width = 720;
        this.height = 640;
        naSetup(this.width, this.height);
        if (filefreeviewstop_flag2 == 0) {
            this.backbit = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888);
            this.tmpIv = (ImageView) findViewById(R.id.musicimage);
            setRequestedOrientation(4);
            this.tmpIv.setLayoutParams(new FrameLayout.LayoutParams(-1, -2));
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) this.tmpIv.getLayoutParams();
            ViewGroup.LayoutParams layoutParams2 = this.tmpIv.getLayoutParams();
            this.tmpIv.setBackgroundColor(ViewCompat.MEASURED_STATE_MASK);
            layoutParams.topMargin = this.old_width / 3;
            layoutParams2.height = (this.old_height + this.deviceHeight) / 2;
            this.tmpIv.setLayoutParams(layoutParams2);
            onFreeView();
            this.bOrientation = true;
        }
    }

    @Override // android.app.Activity, android.content.ComponentCallbacks
    public void onConfigurationChanged(Configuration configuration) {
        Log.d("mhhan", "###onConfigurationChanged###");
        if (configuration.orientation == 2) {
            Log.d("mhhan", "###  ORIENTATION_LANDSCAPE  ###");
            int[] iArr = this.screenRes;
            int[] iArr2 = this.res;
            this.widthScaledRatio = (iArr[0] * 1.0f) / iArr2[0];
            this.heightScaledRatio = (iArr[1] * 1.0f) / iArr2[1];
            this.height = (int) (iArr2[0] * this.widthScaledRatio);
            this.width = (int) (iArr2[1] * this.heightScaledRatio);
            Log.d("HAN2", "res[0] = " + this.res[0] + "  res[1] = " + this.res[1]);
            Log.d("HAN2", "widthScaledRatio = " + this.widthScaledRatio + "  heightScaledRatio = " + this.heightScaledRatio);
            Log.d("HAN2", "width = " + this.width + "  height = " + this.height);
            Log.d("mhhan", "screen width = " + this.screenRes[0] + " screen height = " + this.screenRes[1]);
            this.ORIENTATION_FLAG = 2;
            this.width = 1280;
            this.height = 720;
            this.bOrientation = true;
            viewzoom(0);
        } else if (configuration.orientation == 1) {
            Log.d("mhhan", "###  ORIENTATION_PORTRAIT  ###");
            int[] iArr3 = this.screenRes;
            int[] iArr4 = this.res;
            this.widthScaledRatio = (iArr3[0] * 1.0f) / iArr4[0];
            this.heightScaledRatio = (iArr3[1] * 1.0f) / iArr4[1];
            this.width = (int) (iArr4[0] * this.widthScaledRatio);
            this.height = (int) (iArr4[1] * this.heightScaledRatio);
            Log.d("HAN2", "res[0] = " + this.res[0] + "  res[1] = " + this.res[1]);
            Log.d("HAN2", "widthScaledRatio = " + this.widthScaledRatio + "  heightScaledRatio = " + this.heightScaledRatio);
            Log.d("HAN2", "width = " + this.width + "  height = " + this.height);
            Log.d("mhhan", "screen width = " + this.screenRes[0] + " screen height = " + this.screenRes[1]);
            this.width = 720;
            this.height = 640;
            this.ORIENTATION_FLAG = 1;
            this.bOrientation = true;
            viewzoom(1);
            Log.d("HAN7", "###11111111111111111111###");
        } else {
            Log.d("mhhan", "###  ORIENTATION + " + configuration.orientation + "  ###");
        }
        super.onConfigurationChanged(configuration);
    }

    static {
        System.loadLibrary("avutil-54");
        System.loadLibrary("avcodec-56");
        System.loadLibrary("avformat-56");
        System.loadLibrary("swresample-1");
        System.loadLibrary("swscale-3");
        System.loadLibrary("tutorial02");
        setDurationHandler = new Handler() { // from class: com.logon_e100.MainFileFreeView.14
            @Override // android.os.Handler
            public void handleMessage(Message message) {
                int i = message.arg1;
                if (i > 0) {
                    MainFileFreeView.mSeekBar.setMax(i);
                    MainFileFreeView.mTextViewDuration.setText(Utils.timeToText(i / MainFileFreeView.FRONT_REAR_DURATION));
                }
            }
        };
    }

    public void wificon() {
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
        try {
            MainProtocol mainProtocol = this.mprotocol;
            MainProtocol.m_out_stream.write(bArr, 0, 13);
            MainProtocol mainProtocol2 = this.mprotocol;
            MainProtocol.m_out_stream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.e("HAN2", "wifi접속 데이터 보내기");
    }

    public void freeviewstop() {
        byte[] bArr = new byte[256];
        bArr[0] = 2;
        bArr[1] = 32;
        bArr[2] = 2;
        bArr[3] = (byte) 4;
        byte b = (byte) 0;
        bArr[4] = b;
        bArr[5] = b;
        bArr[6] = b;
        bArr[7] = 0;
        bArr[8] = 3;
        bArr[9] = bArr[0];
        for (int i = 1; i < 9; i++) {
            bArr[9] = (byte) (bArr[9] ^ bArr[i]);
        }
        try {
            MainProtocol mainProtocol = this.mprotocol;
            MainProtocol.m_out_stream.write(bArr, 0, 10);
            MainProtocol mainProtocol2 = this.mprotocol;
            MainProtocol.m_out_stream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.e("HAN2", "프리뷰 정지");
    }

    public void Fileview(int i, String str, int i2) {
        Log.e("HAN2", "Fileview m_file_index = " + i);
        Log.e("HAN2", "Fileview file_stae = " + str);
        Log.e("HAN2", "Fileview m_file_ch = " + i2);
        byte[] bArr = new byte[256];
        bArr[0] = 2;
        bArr[1] = 32;
        int i3 = 8;
        bArr[2] = 8;
        bArr[3] = (byte) 12;
        byte b = (byte) 0;
        bArr[4] = b;
        bArr[5] = b;
        bArr[6] = b;
        if (str.equals("I")) {
            bArr[7] = 0;
        } else if (str.equals("E")) {
            bArr[7] = 1;
        } else if (str.equals("P")) {
            bArr[7] = 2;
        } else if (str.equals("J")) {
            bArr[7] = 3;
        } else {
            i3 = 7;
        }
        int i4 = i3 + 1;
        bArr[i3] = 0;
        int i5 = i4 + 1;
        bArr[i4] = 0;
        int i6 = i5 + 1;
        bArr[i5] = 0;
        int i7 = i6 + 1;
        bArr[i6] = (byte) (i & 255);
        int i8 = i7 + 1;
        bArr[i7] = (byte) ((i >> 8) & 255);
        int i9 = i8 + 1;
        bArr[i8] = (byte) ((i >> 16) & 255);
        int i10 = i9 + 1;
        bArr[i9] = (byte) ((i >> 24) & 255);
        int i11 = i10 + 1;
        bArr[i10] = (byte) i2;
        int i12 = i11 + 1;
        bArr[i11] = 0;
        int i13 = i12 + 1;
        bArr[i12] = 0;
        int i14 = i13 + 1;
        bArr[i13] = 0;
        int i15 = i14 + 1;
        bArr[i14] = 3;
        bArr[i15] = bArr[0];
        for (int i16 = 1; i16 < i15; i16++) {
            bArr[i15] = (byte) (bArr[i15] ^ bArr[i16]);
        }
        int i17 = i15 + 1;
        MainProtocol.targetActivity = 10;
        try {
            MainProtocol mainProtocol = this.mprotocol;
            MainProtocol.m_out_stream.write(bArr, 0, i17);
            MainProtocol mainProtocol2 = this.mprotocol;
            MainProtocol.m_out_stream.flush();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("HAN2", "IOException 오류  = " + e);
            String[] strArr = {mContext.getResources().getString(R.string.file_playback_title), mContext.getResources().getString(R.string.file_file_title2)};
            Message obtainMessage = this.displayAlertFileDownFailHandler.obtainMessage();
            obtainMessage.obj = strArr;
            this.displayAlertFileDownFailHandler.sendMessage(obtainMessage);
        }
        Log.e("HAN2", "파일프리뷰");
        if (!str.equals("J")) {
            this.mAudioRender.start();
        }
        new Handler().postDelayed(new Runnable() { // from class: com.logon_e100.MainFileFreeView.4
            @Override // java.lang.Runnable
            public void run() {
                MainFileFreeView.this.onBtnConnect();
            }
        }, 1500L);
        new Handler().postDelayed(new Runnable() { // from class: com.logon_e100.MainFileFreeView.5
            @Override // java.lang.Runnable
            public void run() {
                MainFileFreeView.this.screen_mode();
            }
        }, 2000L);
    }

    private static byte[] intToByteArray(int i) {
        ByteBuffer allocate = ByteBuffer.allocate(4);
        allocate.putInt(i);
        allocate.order(ByteOrder.LITTLE_ENDIAN);
        return allocate.array();
    }

    public void displayFileDownAlert(String str, String str2) {
        String string = MainActivity.context.getResources().getString(R.string.action_confirm);
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.context);
        builder.setMessage(str2).setCancelable(false).setPositiveButton(string, new DialogInterface.OnClickListener() { // from class: com.logon_e100.MainFileFreeView.7
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                MainFileFreeView.mContext.startActivity(new Intent(MainFileFreeView.mContext, (Class<?>) MainActivity.class));
                ((MainFileFreeView) MainFileFreeView.mContext).finish();
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
        });
        AlertDialog create = builder.create();
        create.setTitle(str);
        create.setIcon(R.drawable.ic_launcher);
        create.show();
    }

    protected void onFreeView() {
        new Thread() { // from class: com.logon_e100.MainFileFreeView.8
            @Override // java.lang.Thread, java.lang.Runnable
            public void run() {
                MainFileFreeView.this.checkUpdate2.interrupt();
                MainFileFreeView.this.videoStarted = false;
                while (true) {
                    try {
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (MainFileFreeView.filefreeviewstop_flag2 == 0) {
                        if (MainFileFreeView.this.backbit != null) {
                            if (MainFileFreeView.renderFrame(MainFileFreeView.this.backbit) < 0) {
                                Log.d("HAN2", "renderFrame return -1");
                                MainFileFreeView.filefreeviewstop_flag2 = 1;
                                if (MainFileFreeView.mSeekBar != null) {
                                    MainFileFreeView.this.mCurrentFrame = MainFileFreeView.mSeekBar.getMax();
                                }
                            }
                            MainFileFreeView.this.mCurTime = System.currentTimeMillis();
                            if (MainFileFreeView.this.mCurTime - MainFileFreeView.this.mOldTime < MainFileFreeView.this.mInterval) {
                                int i = (int) (MainFileFreeView.this.mInterval - (MainFileFreeView.this.mCurTime - MainFileFreeView.this.mOldTime));
                                if (i > 0) {
                                    Thread.sleep(i);
                                }
                                MainFileFreeView.this.mOldTime = MainFileFreeView.this.mCurTime + (MainFileFreeView.this.mInterval - (MainFileFreeView.this.mCurTime - MainFileFreeView.this.mOldTime));
                            } else {
                                MainFileFreeView.this.mOldTime = MainFileFreeView.this.mCurTime;
                            }
                            MainFileFreeView.this.mHandler.post(new Runnable() { // from class: com.logon_e100.MainFileFreeView.8.1
                                @Override // java.lang.Runnable
                                public void run() {
                                    if (MainFileFreeView.this.backbit != null) {
                                        if (MainFileFreeView.this.file_ch == 0) {
                                            MainFileFreeView.this.tmpIv.setImageBitmap(MainFileFreeView.this.backbit);
                                            MainFileFreeView.this.tmpIv.invalidate();
                                        } else if (MainFileFreeView.this.mRevers_flag == 1) {
                                            MainFileFreeView.this.tmpIv.setImageBitmap(MainFileFreeView.this.backbit);
                                            MainFileFreeView.this.tmpIv.invalidate();
                                        } else if (MainFileFreeView.this.ORIENTATION_FLAG == 1) {
                                            MainFileFreeView.this.sideInversionImg = Bitmap.createBitmap(MainFileFreeView.this.backbit, 0, 0, 720, 640, MainFileFreeView.this.sideInversion, false);
                                            MainFileFreeView.this.tmpIv.setImageBitmap(MainFileFreeView.this.sideInversionImg);
                                            MainFileFreeView.this.tmpIv.invalidate();
                                        } else {
                                            MainFileFreeView.this.sideInversionImg = Bitmap.createBitmap(MainFileFreeView.this.backbit, 0, 0, MainFileFreeView.this.backbit.getWidth(), MainFileFreeView.this.backbit.getHeight(), MainFileFreeView.this.sideInversion, false);
                                            MainFileFreeView.this.tmpIv.setImageBitmap(MainFileFreeView.this.sideInversionImg);
                                            MainFileFreeView.this.tmpIv.invalidate();
                                        }
                                        MainFileFreeView.this.currentFrameIns();
                                    }
                                }
                            });
                        }
                        if (MainFileFreeView.this.mProgressDialog != null) {
                            MainFileFreeView.this.mProgressDialog.dismiss();
                        }
                    } else {
                        Log.e("HAN2", " 파일 renderFrame 스레드 종료");
                        if (MainFileFreeView.this.socket_audio != null) {
                            MainFileFreeView.this.socket_audio.close();
                        }
                        MainFileFreeView.naStop();
                        if (MainFileFreeView.this.file_stae.equals("J")) {
                            return;
                        }
                        Thread.sleep(1500L);
                        if (MainFileFreeView.this.seekbarUpdateHandler != null) {
                            MainFileFreeView.this.seekbarUpdateHandler.removeCallbacks(MainFileFreeView.this.mRunSeekbarUpdate);
                        }
                        MainFileFreeView.this.finish();
                        return;
                    }
                }
            }
        }.start();
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        Handler handler = this.seekbarUpdateHandler;
        if (handler != null) {
            handler.removeCallbacks(this.mRunSeekbarUpdate);
        }
        Log.d("HAN2", "onDestroy");
        Log.e("HAN2", " 파일 renderFrame 스레드 종료1");
        if (this.backbit != null) {
            Log.e("HAN2", " 파일 renderFrame 스레드 종료2");
            this.backbit = null;
            Log.e("HAN2", " 파일 renderFrame 스레드 종료3");
        }
        Log.e("HAN2", " 파일 renderFrame 스레드 종료4");
        Log.e("HAN2", " 파일 renderFrame 스레드 종료5");
        super.onDestroy();
    }

    public void FileList() {
        byte[] bArr = new byte[256];
        bArr[0] = 2;
        MainProtocol mainProtocol = this.mprotocol;
        bArr[1] = -96;
        bArr[2] = 37;
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
        Log.e("HAN2", "파일 리스트");
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
        Log.e("HAN2", "영상 온오프보내기");
    }

    @Override // android.app.Activity, android.view.KeyEvent.Callback
    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        switch (i) {
            case 3:
                Process.killProcess(Process.myPid());
                break;
            case 4:
                filefreeviewstop_flag2 = 1;
                break;
        }
        return super.onKeyDown(i, keyEvent);
    }

    public void FilListDialog() {
        runOnUiThread(new Runnable() { // from class: com.logon_e100.MainFileFreeView.10
            @Override // java.lang.Runnable
            public void run() {
                String string = MainFileFreeView.this.getResources().getString(R.string.file_play_preparing);
                MainFileFreeView mainFileFreeView = MainFileFreeView.this;
                mainFileFreeView.mProgressDialog = ProgressDialog.show(mainFileFreeView, "", string, true);
            }
        });
    }

    public Bitmap resizeBitmapImage(Bitmap bitmap, int i) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if (width > height) {
            if (i < width) {
                int i2 = (int) (height * (i / width));
                width = i;
                i = i2;
            }
            i = height;
        } else {
            if (i < height) {
                width = (int) (width * (i / height));
            }
            i = height;
        }
        return Bitmap.createScaledBitmap(bitmap, width, i, true);
    }

    public void initControls() {
        this.mBottomControlBox = (LinearLayout) findViewById(R.id.file_preview_bottom_controlbox);
        mSeekBar = (SeekBar) findViewById(R.id.file_preview_seekbar);
        this.mTextViewCurrentTime = (TextView) findViewById(R.id.file_preview_current_time);
        mTextViewDuration = (TextView) findViewById(R.id.file_preview_duration);
        if (this.file_stae.equals("J")) {
            this.mBottomControlBox.setVisibility(8);
            return;
        }
        this.mCurrentFrame = 0;
        this.mDuration = 0;
        mSeekBar.setOnTouchListener(this.mTouchListener);
        mSeekBar.setMax(0);
        mSeekBar.setProgress(0);
        this.seekbarUpdateHandler = new Handler();
        this.seekbarUpdateHandler.post(this.mRunSeekbarUpdate);
        this.mBottomControlBox.setVisibility(0);
    }

    public void displayPlayFailedAlert(String str, String str2) {
        String string = getResources().getString(R.string.action_confirm);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(str2).setCancelable(false).setPositiveButton(string, new DialogInterface.OnClickListener() { // from class: com.logon_e100.MainFileFreeView.11
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                MainFileFreeView.this.finish();
            }
        });
        AlertDialog create = builder.create();
        create.setTitle(str);
        create.setIcon(R.drawable.ic_launcher);
        create.show();
    }

    public void displayPlayFailedAlert2(String str, String str2) {
        String string = getResources().getString(R.string.action_confirm);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(str2).setCancelable(false).setPositiveButton(string, new DialogInterface.OnClickListener() { // from class: com.logon_e100.MainFileFreeView.12
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                MainFileFreeView.mContext.startActivity(new Intent(MainFileFreeView.mContext, (Class<?>) MainActivity.class));
                Log.e("HAN3", "여기니7?");
                ((MainFileFreeView) MainFileFreeView.mContext).finish();
                ((MainFileListView) MainFileListView.mContext2).finish();
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
        });
        AlertDialog create = builder.create();
        create.setTitle(str);
        create.setIcon(R.drawable.ic_launcher);
        create.show();
    }

    public static void onReceive(int i, byte[] bArr) {
        Log.d("mhhan", "#### onReceive cmd = " + i + " size = " + bArr.length + " ####");
        if (i != 8201) {
            return;
        }
        MainProtocol.targetActivity = 2;
        MainProtocol.CNSResultFileStart cNSResultFileStart = new MainProtocol.CNSResultFileStart(bArr);
        Log.d("mhhan", "#### file size = " + cNSResultFileStart.getFileSize() + " duration = " + cNSResultFileStart.getDuration() + " ####");
        if (cNSResultFileStart.getDuration() < 0) {
            return;
        }
        Message obtainMessage = setDurationHandler.obtainMessage();
        obtainMessage.arg1 = cNSResultFileStart.getDuration();
        setDurationHandler.sendMessage(obtainMessage);
    }

    public void viewzoom(int i) {
        if (i == 0) {
            new Handler().postDelayed(new Runnable() { // from class: com.logon_e100.MainFileFreeView.16
                @Override // java.lang.Runnable
                public void run() {
                    MainFileFreeView mainFileFreeView = MainFileFreeView.this;
                    mainFileFreeView.mAttacher = new PhotoViewAttacher(mainFileFreeView.tmpIv);
                    MainFileFreeView.this.mAttacher.setScaleType(ImageView.ScaleType.FIT_XY);
                    FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) MainFileFreeView.this.tmpIv.getLayoutParams();
                    MainFileFreeView.this.tmpIv.setBackgroundColor(ViewCompat.MEASURED_STATE_MASK);
                    layoutParams.topMargin = 0;
                }
            }, 250L);
        } else {
            new Handler().postDelayed(new Runnable() { // from class: com.logon_e100.MainFileFreeView.17
                @Override // java.lang.Runnable
                public void run() {
                    MainFileFreeView mainFileFreeView = MainFileFreeView.this;
                    mainFileFreeView.mAttacher = new PhotoViewAttacher(mainFileFreeView.tmpIv);
                    MainFileFreeView.this.mAttacher.setScaleType(ImageView.ScaleType.FIT_XY);
                    MainFileFreeView.this.tmpIv.setLayoutParams(new FrameLayout.LayoutParams(-1, -2));
                    FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) MainFileFreeView.this.tmpIv.getLayoutParams();
                    ViewGroup.LayoutParams layoutParams2 = MainFileFreeView.this.tmpIv.getLayoutParams();
                    MainFileFreeView.this.widthScaledRatio = (r2.screenRes[0] * 1.0f) / MainFileFreeView.this.res[0];
                    MainFileFreeView.this.heightScaledRatio = (r2.screenRes[1] * 1.0f) / MainFileFreeView.this.res[1];
                    MainFileFreeView.this.width = (int) (r2.res[0] * MainFileFreeView.this.widthScaledRatio);
                    MainFileFreeView.this.height = (int) (r2.res[1] * MainFileFreeView.this.heightScaledRatio);
                    MainFileFreeView.this.tmpIv.setBackgroundColor(ViewCompat.MEASURED_STATE_MASK);
                    layoutParams.topMargin = MainFileFreeView.this.old_width / 3;
                    layoutParams2.height = MainFileFreeView.this.old_height / 2;
                    MainFileFreeView.this.tmpIv.setLayoutParams(layoutParams2);
                    MainFileFreeView.this.mAttacher.cleanup();
                }
            }, 250L);
        }
    }

    class Counter extends Thread {
        int count = 0;

        Counter() {
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            while (MainFileFreeView.this.videoStarted) {
                this.count++;
                Log.e("HAN3", "count = " + this.count);
                if (this.count == 20) {
                    if (MainFileFreeView.this.mProgressDialog != null) {
                        MainFileFreeView.this.mProgressDialog.dismiss();
                        Log.e("HAN3", "여기니2?");
                    }
                    MainFileFreeView.this.handler16.sendMessage(MainFileFreeView.this.handler16.obtainMessage());
                    return;
                }
                SystemClock.sleep(1000L);
            }
        }
    }
}
