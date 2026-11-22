package com.logon_e100;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.MediaController;
import android.widget.VideoView;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;

@SuppressLint({"WorldReadableFiles", "NewApi"})
@TargetApi(10)
/* loaded from: classes.dex */
public class MainFileDownView2 extends Activity {
    private static int TIMEOUT_US = -1;
    MediaCodec codec;
    ByteBuffer[] codecInputBuffers;
    ByteBuffer[] codecOutputBuffers;
    MediaFormat format;
    MediaCodec.BufferInfo info;
    AudioTrack mAudioTrack;
    private MediaCodec mDecoder;
    Boolean sawInputEOS = false;
    Boolean sawOutputEOS = false;

    @Override // android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        requestWindowFeature(1);
        getWindow().setFlags(1024, 1024);
        setRequestedOrientation(0);
        setContentView(R.layout.activity_filedownview);
        getWindow().addFlags(128);
        MainProtocol.OnStopFlag = false;
        String stringExtra = getIntent().getStringExtra("filename");
        try {
            VideoView videoView = (VideoView) findViewById(R.id.videoView1);
            videoView.requestFocus();
            videoView.setMediaController(new MediaController(this));
            Log.e("HAN2", "filename = " + stringExtra);
            videoView.setVideoPath(stringExtra);
            videoView.start();
        } catch (Exception e) {
            Log.e("", e.toString());
        }
    }

    private void raw2file(Context context, int i, String str) throws Exception {
        in2file(context, context.getResources().openRawResource(i), str);
    }

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    private void in2file(Context context, InputStream inputStream, String str) throws Exception {
        FileOutputStream fileOutputStream;
        byte[] bArr = new byte[1024];
        try {
            fileOutputStream = context.openFileOutput(str, 1);
            while (true) {
                try {
                    int read = inputStream.read(bArr);
                    if (read > 0) {
                        fileOutputStream.write(bArr, 0, read);
                    } else {
                        fileOutputStream.close();
                        inputStream.close();
                        return;
                    }
                } catch (Exception e) {
                    e = e;
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (Exception unused) {
                            throw e;
                        }
                    }
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                    throw e;
                }
            }
        } catch (Exception e2) {
            e = e2;
            fileOutputStream = null;
        }
    }

    @Override // android.app.Activity, android.view.KeyEvent.Callback
    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        if (i == 4) {
            finish();
        }
        return super.onKeyDown(i, keyEvent);
    }

    private String getPlayTime(String str) {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(str);
        String extractMetadata = mediaMetadataRetriever.extractMetadata(9);
        String extractMetadata2 = mediaMetadataRetriever.extractMetadata(0);
        String extractMetadata3 = mediaMetadataRetriever.extractMetadata(10);
        Log.e("HAN2", "time2 = " + extractMetadata2);
        Log.e("HAN2", "time3 = " + extractMetadata3);
        long parseLong = Long.parseLong(extractMetadata) / 1000;
        long j = parseLong / 3600;
        long j2 = 3600 * j;
        long j3 = (parseLong - j2) / 60;
        return j + "시 " + j3 + "분 " + (parseLong - (j2 + (60 * j3))) + "초 ";
    }
}
