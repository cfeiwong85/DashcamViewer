package com.googlecode.mp4parser.authoring.builder;

import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.util.Mp4Arrays;
import java.util.Arrays;

/* loaded from: classes.dex */
public class BetterFragmenter implements Fragmenter {
    private double targetDuration;

    public BetterFragmenter(double d) {
        this.targetDuration = d;
    }

    @Override // com.googlecode.mp4parser.authoring.builder.Fragmenter
    public long[] sampleNumbers(Track track) {
        long timescale = track.getTrackMetaData().getTimescale();
        double d = this.targetDuration;
        double d2 = timescale;
        Double.isNaN(d2);
        long j = (long) (d * d2);
        long[] jArr = new long[0];
        long[] syncSamples = track.getSyncSamples();
        long[] sampleDurations = track.getSampleDurations();
        long j2 = 2;
        if (syncSamples != null) {
            long[] jArr2 = new long[syncSamples.length];
            long duration = track.getDuration();
            long j3 = 0;
            long j4 = 0;
            int i = 0;
            while (i < sampleDurations.length) {
                int binarySearch = Arrays.binarySearch(syncSamples, i + 1);
                if (binarySearch >= 0) {
                    jArr2[binarySearch] = j4;
                }
                j4 += sampleDurations[i];
                i++;
                j2 = 2;
            }
            int i2 = 0;
            while (i2 < jArr2.length - 1) {
                long j5 = jArr2[i2];
                int i3 = i2 + 1;
                long j6 = jArr2[i3];
                if (j3 <= j6 && Math.abs(j5 - j3) < Math.abs(j6 - j3)) {
                    jArr = Mp4Arrays.copyOfAndAppend(jArr, syncSamples[i2]);
                    j3 = jArr2[i2] + j;
                }
                i2 = i3;
            }
            return duration - jArr2[jArr2.length - 1] > j / j2 ? Mp4Arrays.copyOfAndAppend(jArr, syncSamples[jArr2.length - 1]) : jArr;
        }
        long[] jArr3 = {1};
        double d3 = 0.0d;
        for (int i4 = 1; i4 < sampleDurations.length; i4++) {
            double d4 = sampleDurations[i4];
            Double.isNaN(d4);
            Double.isNaN(d2);
            d3 += d4 / d2;
            if (d3 >= this.targetDuration) {
                if (i4 > 0) {
                    jArr3 = Mp4Arrays.copyOfAndAppend(jArr3, i4 + 1);
                }
                d3 = 0.0d;
            }
        }
        if (d3 < this.targetDuration && jArr3.length > 1) {
            jArr3[jArr3.length - 1] = jArr3[jArr3.length - 2] + (((sampleDurations.length + 1) - jArr3[jArr3.length - 2]) / 2);
        }
        return jArr3;
    }
}
