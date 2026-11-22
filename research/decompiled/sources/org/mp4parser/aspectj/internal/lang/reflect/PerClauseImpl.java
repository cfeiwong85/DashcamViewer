package org.mp4parser.aspectj.internal.lang.reflect;

import org.mp4parser.aspectj.lang.reflect.PerClause;
import org.mp4parser.aspectj.lang.reflect.PerClauseKind;

/* loaded from: classes.dex */
public class PerClauseImpl implements PerClause {
    private final PerClauseKind kind;

    public String toString() {
        return "issingleton()";
    }

    protected PerClauseImpl(PerClauseKind perClauseKind) {
        this.kind = perClauseKind;
    }

    @Override // org.mp4parser.aspectj.lang.reflect.PerClause
    public PerClauseKind getKind() {
        return this.kind;
    }
}
