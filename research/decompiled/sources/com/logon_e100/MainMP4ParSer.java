package com.logon_e100;

import android.media.MediaCodec;
import android.media.MediaCrypto;
import android.media.MediaFormat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.Surface;
import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.FileDataSourceImpl;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.tracks.AACTrackImpl;
import com.googlecode.mp4parser.authoring.tracks.h264.H264TrackImpl;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/* loaded from: classes.dex */
public class MainMP4ParSer {
    static String audiopath;
    static String out_audioname;
    static String out_filename;
    static int video_ch;
    static String videopath;

    public static void MainMP4ParSer(String str, String str2, int i) {
        videopath = str;
        audiopath = str2;
        video_ch = i;
        out_audioname = String.format("%s.aac", audiopath);
        out_filename = String.format("%s.mp4", videopath);
        PcmToAAC();
    }

    public static void PcmToAAC() {
        long j;
        byte[] bArr;
        try {
            FileInputStream fileInputStream = new FileInputStream(new File(audiopath));
            FileOutputStream fileOutputStream = new FileOutputStream(out_audioname);
            MediaFormat mediaFormat = new MediaFormat();
            mediaFormat.setString("mime", "audio/mp4a-latm");
            mediaFormat.setInteger("aac-profile", 2);
            mediaFormat.setInteger("sample-rate", 8000);
            boolean z = true;
            mediaFormat.setInteger("channel-count", 1);
            mediaFormat.setInteger("bitrate", 131072);
            MediaCodec createEncoderByType = MediaCodec.createEncoderByType("audio/mp4a-latm");
            createEncoderByType.configure(mediaFormat, (Surface) null, (MediaCrypto) null, 1);
            createEncoderByType.start();
            ByteBuffer[] inputBuffers = createEncoderByType.getInputBuffers();
            ByteBuffer[] outputBuffers = createEncoderByType.getOutputBuffers();
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            byte[] bArr2 = new byte[8000];
            double d = 0.0d;
            int i = 0;
            int i2 = 0;
            while (true) {
                int i3 = i2;
                double d2 = d;
                int i4 = 0;
                while (i4 != -1 && z) {
                    i4 = createEncoderByType.dequeueInputBuffer(1000L);
                    if (i4 >= 0) {
                        ByteBuffer byteBuffer = inputBuffers[i4];
                        byteBuffer.clear();
                        int read = fileInputStream.read(bArr2, i, byteBuffer.limit());
                        if (read == -1) {
                            z = false;
                            createEncoderByType.queueInputBuffer(i4, 0, 0, (long) d2, 4);
                            bArr = bArr2;
                            d2 = d2;
                        } else {
                            i3 += read;
                            byteBuffer.put(bArr2, i, read);
                            bArr = bArr2;
                            createEncoderByType.queueInputBuffer(i4, 0, read, (long) d2, 0);
                            d2 = ((i3 / 2) * 1000000) / PlaybackStateCompat.ACTION_PREPARE_FROM_URI;
                        }
                        bArr2 = bArr;
                        i = 0;
                    }
                }
                byte[] bArr3 = bArr2;
                int dequeueOutputBuffer = createEncoderByType.dequeueOutputBuffer(bufferInfo, 1000L);
                while (dequeueOutputBuffer >= 0) {
                    ByteBuffer byteBuffer2 = outputBuffers[dequeueOutputBuffer];
                    int i5 = bufferInfo.size + 7;
                    byte[] bArr4 = new byte[i5];
                    addADTStoPacket(bArr4, i5);
                    byteBuffer2.get(bArr4, 7, bufferInfo.size);
                    byteBuffer2.position(bufferInfo.offset);
                    createEncoderByType.releaseOutputBuffer(dequeueOutputBuffer, false);
                    try {
                        fileOutputStream.write(bArr4);
                    } catch (Error e) {
                        e.printStackTrace();
                        j = 1000;
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                    j = 1000;
                    dequeueOutputBuffer = createEncoderByType.dequeueOutputBuffer(bufferInfo, j);
                }
                if (bufferInfo.flags == 4) {
                    break;
                }
                d = d2;
                i2 = i3;
                bArr2 = bArr3;
                i = 0;
            }
            fileInputStream.close();
            createEncoderByType.stop();
            createEncoderByType.release();
        } catch (FileNotFoundException e3) {
            e3.printStackTrace();
        } catch (IOException e4) {
            e4.printStackTrace();
        }
        makeMP4();
    }

    public static void addADTStoPacket(byte[] bArr, int i) {
        bArr[0] = -1;
        bArr[1] = -7;
        bArr[2] = (byte) 108;
        bArr[3] = (byte) (64 + (i >> 11));
        bArr[4] = (byte) ((i & 2047) >> 3);
        bArr[5] = (byte) (((i & 7) << 5) + 31);
        bArr[6] = -4;
    }

    public static void makeMP4() {
        H264TrackImpl h264TrackImpl;
        try {
            if (video_ch == 0) {
                h264TrackImpl = new H264TrackImpl(new FileDataSourceImpl(videopath), "eng", 30L, 1);
            } else {
                h264TrackImpl = new H264TrackImpl(new FileDataSourceImpl(videopath), "eng", 25L, 1);
            }
            AACTrackImpl aACTrackImpl = new AACTrackImpl(new FileDataSourceImpl(out_audioname));
            Movie movie = new Movie();
            movie.addTrack(h264TrackImpl);
            movie.addTrack(aACTrackImpl);
            Container build = new DefaultMp4Builder().build(movie);
            FileChannel channel = new FileOutputStream(new File(out_filename)).getChannel();
            build.writeContainer(channel);
            channel.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
    }
}
