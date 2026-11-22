package com.mp4parser.streaming;

import java.nio.ByteBuffer;

/* loaded from: classes.dex */
public interface StreamingSample {
    ByteBuffer getContent();

    long getDuration();

    SampleExtension[] getExtensions();
}
