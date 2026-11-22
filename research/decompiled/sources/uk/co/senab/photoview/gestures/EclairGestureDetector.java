package uk.co.senab.photoview.gestures;

import android.annotation.TargetApi;
import android.content.Context;
import android.view.MotionEvent;

@TargetApi(5)
/* loaded from: classes.dex */
public class EclairGestureDetector extends CupcakeGestureDetector {
    private static final int INVALID_POINTER_ID = -1;
    private int mActivePointerId;
    private int mActivePointerIndex;

    public EclairGestureDetector(Context context) {
        super(context);
        this.mActivePointerId = -1;
        this.mActivePointerIndex = 0;
    }

    @Override // uk.co.senab.photoview.gestures.CupcakeGestureDetector
    float getActiveX(MotionEvent motionEvent) {
        try {
            return motionEvent.getX(this.mActivePointerIndex);
        } catch (Exception unused) {
            return motionEvent.getX();
        }
    }

    @Override // uk.co.senab.photoview.gestures.CupcakeGestureDetector
    float getActiveY(MotionEvent motionEvent) {
        try {
            return motionEvent.getY(this.mActivePointerIndex);
        } catch (Exception unused) {
            return motionEvent.getY();
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:9:0x0049  */
    @Override // uk.co.senab.photoview.gestures.CupcakeGestureDetector, uk.co.senab.photoview.gestures.GestureDetector
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public boolean onTouchEvent(android.view.MotionEvent r7) {
        /*
            r6 = this;
            int r0 = r7.getAction()
            r0 = r0 & 255(0xff, float:3.57E-43)
            r1 = 3
            r2 = 1
            r3 = -1
            r4 = 0
            if (r0 == r1) goto L42
            r1 = 6
            if (r0 == r1) goto L1a
            switch(r0) {
                case 0: goto L13;
                case 1: goto L42;
                default: goto L12;
            }
        L12:
            goto L44
        L13:
            int r0 = r7.getPointerId(r4)
            r6.mActivePointerId = r0
            goto L44
        L1a:
            int r0 = r7.getAction()
            int r0 = uk.co.senab.photoview.Compat.getPointerIndex(r0)
            int r1 = r7.getPointerId(r0)
            int r5 = r6.mActivePointerId
            if (r1 != r5) goto L44
            if (r0 != 0) goto L2e
            r0 = 1
            goto L2f
        L2e:
            r0 = 0
        L2f:
            int r1 = r7.getPointerId(r0)
            r6.mActivePointerId = r1
            float r1 = r7.getX(r0)
            r6.mLastTouchX = r1
            float r0 = r7.getY(r0)
            r6.mLastTouchY = r0
            goto L44
        L42:
            r6.mActivePointerId = r3
        L44:
            int r0 = r6.mActivePointerId
            if (r0 == r3) goto L49
            goto L4a
        L49:
            r0 = 0
        L4a:
            int r0 = r7.findPointerIndex(r0)
            r6.mActivePointerIndex = r0
            boolean r7 = super.onTouchEvent(r7)     // Catch: java.lang.IllegalArgumentException -> L55
            return r7
        L55:
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: uk.co.senab.photoview.gestures.EclairGestureDetector.onTouchEvent(android.view.MotionEvent):boolean");
    }
}
