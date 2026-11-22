package com.googlecode.mp4parser.authoring.builder;

import com.coremedia.iso.boxes.OriginalFormatBox;
import com.coremedia.iso.boxes.SampleDescriptionBox;
import com.coremedia.iso.boxes.sampleentry.AudioSampleEntry;
import com.googlecode.mp4parser.AbstractContainerBox;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.util.Math;
import com.googlecode.mp4parser.util.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

/* loaded from: classes.dex */
public class SyncSampleIntersectFinderImpl implements Fragmenter {
    private static Logger LOG = Logger.getLogger(SyncSampleIntersectFinderImpl.class.getName());
    private final int minFragmentDurationSeconds;
    private Movie movie;
    private Track referenceTrack;

    public SyncSampleIntersectFinderImpl(Movie movie, Track track, int i) {
        this.movie = movie;
        this.referenceTrack = track;
        this.minFragmentDurationSeconds = i;
    }

    static String getFormat(Track track) {
        SampleDescriptionBox sampleDescriptionBox = track.getSampleDescriptionBox();
        OriginalFormatBox originalFormatBox = (OriginalFormatBox) Path.getPath((AbstractContainerBox) sampleDescriptionBox, "enc./sinf/frma");
        if (originalFormatBox != null) {
            return originalFormatBox.getDataFormat();
        }
        return sampleDescriptionBox.getSampleEntry().getType();
    }

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    @Override // com.googlecode.mp4parser.authoring.builder.Fragmenter
    public long[] sampleNumbers(Track track) {
        if ("vide".equals(track.getHandler())) {
            if (track.getSyncSamples() != null && track.getSyncSamples().length > 0) {
                List<long[]> syncSamplesTimestamps = getSyncSamplesTimestamps(this.movie, track);
                return getCommonIndices(track.getSyncSamples(), getTimes(track, this.movie), track.getTrackMetaData().getTimescale(), (long[][]) syncSamplesTimestamps.toArray(new long[syncSamplesTimestamps.size()][]));
            }
            throw new RuntimeException("Video Tracks need sync samples. Only tracks other than video may have no sync samples.");
        }
        long j = 1;
        int i = 0;
        if ("soun".equals(track.getHandler())) {
            if (this.referenceTrack == null) {
                for (Track track2 : this.movie.getTracks()) {
                    if (track2.getSyncSamples() != null && "vide".equals(track2.getHandler()) && track2.getSyncSamples().length > 0) {
                        this.referenceTrack = track2;
                    }
                }
            }
            Track track3 = this.referenceTrack;
            if (track3 != null) {
                long[] sampleNumbers = sampleNumbers(track3);
                int size = this.referenceTrack.getSamples().size();
                long[] jArr = new long[sampleNumbers.length];
                long j2 = 192000;
                Iterator<Track> it = this.movie.getTracks().iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    Track next = it.next();
                    if (getFormat(track).equals(getFormat(next))) {
                        AudioSampleEntry audioSampleEntry = (AudioSampleEntry) next.getSampleDescriptionBox().getSampleEntry();
                        if (audioSampleEntry.getSampleRate() < 192000) {
                            long sampleRate = audioSampleEntry.getSampleRate();
                            double d = size;
                            Double.isNaN(r8);
                            Double.isNaN(d);
                            double d2 = r8 / d;
                            long j3 = next.getSampleDurations()[0];
                            int i2 = 0;
                            while (i2 < jArr.length) {
                                double d3 = sampleNumbers[i2] - j;
                                Double.isNaN(d3);
                                double d4 = j3;
                                Double.isNaN(d4);
                                jArr[i2] = (long) Math.ceil(d3 * d2 * d4);
                                i2++;
                                j = 1;
                            }
                            j2 = sampleRate;
                        }
                    }
                }
                AudioSampleEntry audioSampleEntry2 = (AudioSampleEntry) track.getSampleDescriptionBox().getSampleEntry();
                long j4 = track.getSampleDurations()[0];
                double sampleRate2 = audioSampleEntry2.getSampleRate();
                double d5 = j2;
                Double.isNaN(sampleRate2);
                Double.isNaN(d5);
                double d6 = sampleRate2 / d5;
                if (d6 != Math.rint(d6)) {
                    throw new RuntimeException("Sample rates must be a multiple of the lowest sample rate to create a correct file!");
                }
                while (i < jArr.length) {
                    double d7 = jArr[i];
                    Double.isNaN(d7);
                    double d8 = j4;
                    Double.isNaN(d8);
                    jArr[i] = (long) (((d7 * d6) / d8) + 1.0d);
                    i++;
                }
                return jArr;
            }
            throw new RuntimeException("There was absolutely no Track with sync samples. I can't work with that!");
        }
        for (Track track4 : this.movie.getTracks()) {
            if (track4.getSyncSamples() != null && track4.getSyncSamples().length > 0) {
                long[] sampleNumbers2 = sampleNumbers(track4);
                int size2 = track4.getSamples().size();
                long[] jArr2 = new long[sampleNumbers2.length];
                double d9 = size2;
                Double.isNaN(r7);
                Double.isNaN(d9);
                double d10 = r7 / d9;
                while (i < jArr2.length) {
                    double d11 = sampleNumbers2[i] - 1;
                    Double.isNaN(d11);
                    jArr2[i] = ((long) Math.ceil(d11 * d10)) + 1;
                    i++;
                }
                return jArr2;
            }
        }
        throw new RuntimeException("There was absolutely no Track with sync samples. I can't work with that!");
    }

    public static List<long[]> getSyncSamplesTimestamps(Movie movie, Track track) {
        long[] syncSamples;
        LinkedList linkedList = new LinkedList();
        for (Track track2 : movie.getTracks()) {
            if (track2.getHandler().equals(track.getHandler()) && (syncSamples = track2.getSyncSamples()) != null && syncSamples.length > 0) {
                linkedList.add(getTimes(track2, movie));
            }
        }
        return linkedList;
    }

    public long[] getCommonIndices(long[] jArr, long[] jArr2, long j, long[]... jArr3) {
        LinkedList linkedList;
        LinkedList linkedList2 = new LinkedList();
        LinkedList linkedList3 = new LinkedList();
        for (int i = 0; i < jArr2.length; i++) {
            boolean z = true;
            for (long[] jArr4 : jArr3) {
                z &= Arrays.binarySearch(jArr4, jArr2[i]) >= 0;
            }
            if (z) {
                linkedList2.add(Long.valueOf(jArr[i]));
                linkedList3.add(Long.valueOf(jArr2[i]));
            }
        }
        double size = linkedList2.size();
        double length = jArr.length;
        Double.isNaN(length);
        if (size < length * 0.25d) {
            String str = String.valueOf("") + String.format("%5d - Common:  [", Integer.valueOf(linkedList2.size()));
            Iterator it = linkedList2.iterator();
            while (it.hasNext()) {
                str = String.valueOf(str) + String.format("%10d,", Long.valueOf(((Long) it.next()).longValue()));
            }
            LOG.warning(String.valueOf(str) + "]");
            String str2 = String.valueOf("") + String.format("%5d - In    :  [", Integer.valueOf(jArr.length));
            for (long j2 : jArr) {
                str2 = String.valueOf(str2) + String.format("%10d,", Long.valueOf(j2));
            }
            LOG.warning(String.valueOf(str2) + "]");
            LOG.warning("There are less than 25% of common sync samples in the given track.");
            throw new RuntimeException("There are less than 25% of common sync samples in the given track.");
        }
        double size2 = linkedList2.size();
        double length2 = jArr.length;
        Double.isNaN(length2);
        if (size2 < length2 * 0.5d) {
            LOG.fine("There are less than 50% of common sync samples in the given track. This is implausible but I'm ok to continue.");
        } else if (linkedList2.size() < jArr.length) {
            LOG.finest("Common SyncSample positions vs. this tracks SyncSample positions: " + linkedList2.size() + " vs. " + jArr.length);
        }
        LinkedList linkedList4 = new LinkedList();
        if (this.minFragmentDurationSeconds > 0) {
            Iterator it2 = linkedList2.iterator();
            Iterator it3 = linkedList3.iterator();
            long j3 = -1;
            long j4 = -1;
            while (it2.hasNext() && it3.hasNext()) {
                long longValue = ((Long) it2.next()).longValue();
                long longValue2 = ((Long) it3.next()).longValue();
                if (j4 == j3 || (longValue2 - j4) / j >= this.minFragmentDurationSeconds) {
                    linkedList4.add(Long.valueOf(longValue));
                    j4 = longValue2;
                    j3 = -1;
                } else {
                    j3 = -1;
                }
            }
            linkedList = linkedList4;
        } else {
            linkedList = linkedList2;
        }
        long[] jArr5 = new long[linkedList.size()];
        for (int i2 = 0; i2 < jArr5.length; i2++) {
            jArr5[i2] = ((Long) linkedList.get(i2)).longValue();
        }
        return jArr5;
    }

    private static long[] getTimes(Track track, Movie movie) {
        long[] syncSamples = track.getSyncSamples();
        long[] jArr = new long[syncSamples.length];
        long calculateTracktimesScalingFactor = calculateTracktimesScalingFactor(movie, track);
        int i = 0;
        long j = 0;
        int i2 = 1;
        while (true) {
            long j2 = i2;
            if (j2 > syncSamples[syncSamples.length - 1]) {
                return jArr;
            }
            if (j2 == syncSamples[i]) {
                jArr[i] = j * calculateTracktimesScalingFactor;
                i++;
            }
            j += track.getSampleDurations()[i2 - 1];
            i2++;
        }
    }

    private static long calculateTracktimesScalingFactor(Movie movie, Track track) {
        long j = 1;
        for (Track track2 : movie.getTracks()) {
            if (track2.getHandler().equals(track.getHandler()) && track2.getTrackMetaData().getTimescale() != track.getTrackMetaData().getTimescale()) {
                j = Math.lcm(j, track2.getTrackMetaData().getTimescale());
            }
        }
        return j;
    }
}
