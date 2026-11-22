package org.mp4parser.aspectj.lang.reflect;

/* loaded from: classes.dex */
public interface SourceLocation {
    int getColumn();

    String getFileName();

    int getLine();

    Class getWithinType();
}
