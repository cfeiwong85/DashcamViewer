package org.mp4parser.aspectj.internal.lang.reflect;

import org.mp4parser.aspectj.lang.reflect.PerClauseKind;
import org.mp4parser.aspectj.lang.reflect.PointcutBasedPerClause;
import org.mp4parser.aspectj.lang.reflect.PointcutExpression;

/* loaded from: classes.dex */
public class PointcutBasedPerClauseImpl extends PerClauseImpl implements PointcutBasedPerClause {
    private final PointcutExpression pointcutExpression;

    public PointcutBasedPerClauseImpl(PerClauseKind perClauseKind, String str) {
        super(perClauseKind);
        this.pointcutExpression = new PointcutExpressionImpl(str);
    }

    @Override // org.mp4parser.aspectj.lang.reflect.PointcutBasedPerClause
    public PointcutExpression getPointcutExpression() {
        return this.pointcutExpression;
    }

    @Override // org.mp4parser.aspectj.internal.lang.reflect.PerClauseImpl
    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        switch (getKind()) {
            case PERCFLOW:
                stringBuffer.append("percflow(");
                break;
            case PERCFLOWBELOW:
                stringBuffer.append("percflowbelow(");
                break;
            case PERTARGET:
                stringBuffer.append("pertarget(");
                break;
            case PERTHIS:
                stringBuffer.append("perthis(");
                break;
        }
        stringBuffer.append(this.pointcutExpression.asString());
        stringBuffer.append(")");
        return stringBuffer.toString();
    }
}
