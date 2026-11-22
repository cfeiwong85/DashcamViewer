package com.mp4parser.streaming.extensions;

import com.mp4parser.streaming.TrackExtension;

/* loaded from: classes.dex */
public class TrackIdTrackExtension implements TrackExtension {
    private long trackId;

    public TrackIdTrackExtension(long j) {
        this.trackId = 1L;
        this.trackId = j;
    }

    public long getTrackId() {
        return this.trackId;
    }
}
