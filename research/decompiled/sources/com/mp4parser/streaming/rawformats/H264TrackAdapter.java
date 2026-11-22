package com.mp4parser.streaming.rawformats;

import com.googlecode.mp4parser.FileDataSourceImpl;
import com.googlecode.mp4parser.authoring.Sample;
import com.googlecode.mp4parser.authoring.tracks.h264.H264TrackImpl;
import com.mp4parser.streaming.AbstractStreamingTrack;
import com.mp4parser.streaming.MultiTrackFragmentedMp4Writer;
import com.mp4parser.streaming.SampleExtension;
import com.mp4parser.streaming.StreamingSample;
import com.mp4parser.streaming.StreamingTrack;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

/* loaded from: classes.dex */
public class H264TrackAdapter extends AbstractStreamingTrack {
    H264TrackImpl h264Track;

    /* JADX WARN: Type inference failed for: r0v1, types: [com.mp4parser.streaming.rawformats.H264TrackAdapter$1] */
    public H264TrackAdapter(H264TrackImpl h264TrackImpl) throws InterruptedException {
        this.h264Track = h264TrackImpl;
        this.samples = new ArrayBlockingQueue(100, true);
        new Thread() { // from class: com.mp4parser.streaming.rawformats.H264TrackAdapter.1
            @Override // java.lang.Thread, java.lang.Runnable
            public void run() {
                try {
                    H264TrackAdapter.this.parse();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
        this.stsd = h264TrackImpl.getSampleDescriptionBox();
    }

    public void parse() throws InterruptedException {
        List<Sample> samples = this.h264Track.getSamples();
        for (int i = 0; i < samples.size(); i++) {
            System.err.println("Jo! " + i + " of " + samples.size());
            final long j = this.h264Track.getSampleDurations()[i];
            final Sample sample = samples.get(i);
            this.samples.put(new StreamingSample() { // from class: com.mp4parser.streaming.rawformats.H264TrackAdapter.2
                @Override // com.mp4parser.streaming.StreamingSample
                public ByteBuffer getContent() {
                    return sample.asByteBuffer().duplicate();
                }

                @Override // com.mp4parser.streaming.StreamingSample
                public long getDuration() {
                    return j;
                }

                @Override // com.mp4parser.streaming.StreamingSample
                public SampleExtension[] getExtensions() {
                    return new SampleExtension[0];
                }
            });
        }
        System.err.println("Jo!");
    }

    @Override // com.mp4parser.streaming.StreamingTrack
    public long getTimescale() {
        return this.h264Track.getTrackMetaData().getTimescale();
    }

    @Override // com.mp4parser.streaming.StreamingTrack
    public String getHandler() {
        return this.h264Track.getHandler();
    }

    @Override // com.mp4parser.streaming.StreamingTrack
    public String getLanguage() {
        return this.h264Track.getTrackMetaData().getLanguage();
    }

    public static void main(String[] strArr) throws IOException, InterruptedException {
        new MultiTrackFragmentedMp4Writer(new StreamingTrack[]{new H264TrackAdapter(new H264TrackImpl(new FileDataSourceImpl("c:\\content\\big_buck_bunny_1080p_h264-2min.h264")))}, new FileOutputStream("output.mp4")).write();
    }
}
