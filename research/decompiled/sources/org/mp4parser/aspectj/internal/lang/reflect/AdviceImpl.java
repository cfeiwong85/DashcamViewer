package org.mp4parser.aspectj.internal.lang.reflect;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import org.mp4parser.aspectj.lang.annotation.AdviceName;
import org.mp4parser.aspectj.lang.reflect.Advice;
import org.mp4parser.aspectj.lang.reflect.AdviceKind;
import org.mp4parser.aspectj.lang.reflect.AjType;
import org.mp4parser.aspectj.lang.reflect.AjTypeSystem;
import org.mp4parser.aspectj.lang.reflect.PointcutExpression;

/* loaded from: classes.dex */
public class AdviceImpl implements Advice {
    private static final String AJC_INTERNAL = "org.mp4parser.aspectj.runtime.internal";
    private final Method adviceMethod;
    private AjType[] exceptionTypes;
    private Type[] genericParameterTypes;
    private boolean hasExtraParam;
    private final AdviceKind kind;
    private AjType[] parameterTypes;
    private PointcutExpression pointcutExpression;

    protected AdviceImpl(Method method, String str, AdviceKind adviceKind) {
        this.hasExtraParam = false;
        this.kind = adviceKind;
        this.adviceMethod = method;
        this.pointcutExpression = new PointcutExpressionImpl(str);
    }

    protected AdviceImpl(Method method, String str, AdviceKind adviceKind, String str2) {
        this(method, str, adviceKind);
        this.hasExtraParam = true;
    }

    @Override // org.mp4parser.aspectj.lang.reflect.Advice
    public AjType getDeclaringType() {
        return AjTypeSystem.getAjType(this.adviceMethod.getDeclaringClass());
    }

    @Override // org.mp4parser.aspectj.lang.reflect.Advice
    public Type[] getGenericParameterTypes() {
        if (this.genericParameterTypes == null) {
            Type[] genericParameterTypes = this.adviceMethod.getGenericParameterTypes();
            int i = 0;
            int i2 = 0;
            for (Type type : genericParameterTypes) {
                if ((type instanceof Class) && ((Class) type).getPackage().getName().equals(AJC_INTERNAL)) {
                    i2++;
                }
            }
            this.genericParameterTypes = new Type[genericParameterTypes.length - i2];
            while (true) {
                Type[] typeArr = this.genericParameterTypes;
                if (i >= typeArr.length) {
                    break;
                }
                if (genericParameterTypes[i] instanceof Class) {
                    typeArr[i] = AjTypeSystem.getAjType((Class) genericParameterTypes[i]);
                } else {
                    typeArr[i] = genericParameterTypes[i];
                }
                i++;
            }
        }
        return this.genericParameterTypes;
    }

    @Override // org.mp4parser.aspectj.lang.reflect.Advice
    public AjType<?>[] getParameterTypes() {
        if (this.parameterTypes == null) {
            Class<?>[] parameterTypes = this.adviceMethod.getParameterTypes();
            int i = 0;
            int i2 = 0;
            for (Class<?> cls : parameterTypes) {
                if (cls.getPackage().getName().equals(AJC_INTERNAL)) {
                    i2++;
                }
            }
            this.parameterTypes = new AjType[parameterTypes.length - i2];
            while (true) {
                AjType[] ajTypeArr = this.parameterTypes;
                if (i >= ajTypeArr.length) {
                    break;
                }
                ajTypeArr[i] = AjTypeSystem.getAjType(parameterTypes[i]);
                i++;
            }
        }
        return this.parameterTypes;
    }

    @Override // org.mp4parser.aspectj.lang.reflect.Advice
    public AjType<?>[] getExceptionTypes() {
        if (this.exceptionTypes == null) {
            Class<?>[] exceptionTypes = this.adviceMethod.getExceptionTypes();
            this.exceptionTypes = new AjType[exceptionTypes.length];
            for (int i = 0; i < exceptionTypes.length; i++) {
                this.exceptionTypes[i] = AjTypeSystem.getAjType(exceptionTypes[i]);
            }
        }
        return this.exceptionTypes;
    }

    @Override // org.mp4parser.aspectj.lang.reflect.Advice
    public AdviceKind getKind() {
        return this.kind;
    }

    @Override // org.mp4parser.aspectj.lang.reflect.Advice
    public String getName() {
        String name = this.adviceMethod.getName();
        if (!name.startsWith("ajc$")) {
            return name;
        }
        AdviceName adviceName = (AdviceName) this.adviceMethod.getAnnotation(AdviceName.class);
        return adviceName != null ? adviceName.value() : "";
    }

    @Override // org.mp4parser.aspectj.lang.reflect.Advice
    public PointcutExpression getPointcutExpression() {
        return this.pointcutExpression;
    }

    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        if (getName().length() > 0) {
            stringBuffer.append("@AdviceName(\"");
            stringBuffer.append(getName());
            stringBuffer.append("\") ");
        }
        if (getKind() == AdviceKind.AROUND) {
            stringBuffer.append(this.adviceMethod.getGenericReturnType().toString());
            stringBuffer.append(" ");
        }
        switch (getKind()) {
            case AFTER:
                stringBuffer.append("after(");
                break;
            case AFTER_RETURNING:
                stringBuffer.append("after(");
                break;
            case AFTER_THROWING:
                stringBuffer.append("after(");
                break;
            case AROUND:
                stringBuffer.append("around(");
                break;
            case BEFORE:
                stringBuffer.append("before(");
                break;
        }
        AjType<?>[] parameterTypes = getParameterTypes();
        int length = parameterTypes.length;
        if (this.hasExtraParam) {
            length--;
        }
        int i = 0;
        int i2 = 0;
        while (i2 < length) {
            stringBuffer.append(parameterTypes[i2].getName());
            i2++;
            if (i2 < length) {
                stringBuffer.append(",");
            }
        }
        stringBuffer.append(") ");
        switch (getKind()) {
            case AFTER_RETURNING:
                stringBuffer.append("returning");
                if (this.hasExtraParam) {
                    stringBuffer.append("(");
                    stringBuffer.append(parameterTypes[length - 1].getName());
                    stringBuffer.append(") ");
                }
            case AFTER_THROWING:
                stringBuffer.append("throwing");
                if (this.hasExtraParam) {
                    stringBuffer.append("(");
                    stringBuffer.append(parameterTypes[length - 1].getName());
                    stringBuffer.append(") ");
                    break;
                }
                break;
        }
        AjType<?>[] exceptionTypes = getExceptionTypes();
        if (exceptionTypes.length > 0) {
            stringBuffer.append("throws ");
            while (i < exceptionTypes.length) {
                stringBuffer.append(exceptionTypes[i].getName());
                i++;
                if (i < exceptionTypes.length) {
                    stringBuffer.append(",");
                }
            }
            stringBuffer.append(" ");
        }
        stringBuffer.append(": ");
        stringBuffer.append(getPointcutExpression().asString());
        return stringBuffer.toString();
    }
}
