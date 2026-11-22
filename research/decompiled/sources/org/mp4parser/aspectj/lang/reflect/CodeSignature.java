package org.mp4parser.aspectj.lang.reflect;

/* loaded from: classes.dex */
public interface CodeSignature extends MemberSignature {
    Class[] getExceptionTypes();

    String[] getParameterNames();

    Class[] getParameterTypes();
}
