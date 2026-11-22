package org.mp4parser.aspectj.lang.reflect;

/* loaded from: classes.dex */
public interface DeclarePrecedence {
    AjType getDeclaringType();

    TypePattern[] getPrecedenceOrder();
}
