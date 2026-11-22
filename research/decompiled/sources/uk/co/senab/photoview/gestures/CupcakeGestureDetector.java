package uk.co.senab.photoview.gestures;

import android.content.Context;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;

/* loaded from: classes.dex */
public class CupcakeGestureDetector implements GestureDetector {
    private static final String LOG_TAG = "CupcakeGestureDetector";
    private boolean mIsDragging;
    float mLastTouchX;
    float mLastTouchY;
    protected OnGestureListener mListener;
    final float mMinimumVelocity;
    final float mTouchSlop;
    private VelocityTracker mVelocityTracker;

    @Override // uk.co.senab.photoview.gestures.GestureDetector
    public boolean isScaling() {
        return false;
    }

    @Override // uk.co.senab.photoview.gestures.GestureDetector
    public void setOnGestureListener(OnGestureListener onGestureListener) {
        this.mListener = onGestureListener;
    }

    public CupcakeGestureDetector(Context context) {
        ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        this.mMinimumVelocity = viewConfiguration.getScaledMinimumFlingVelocity();
        this.mTouchSlop = viewConfiguration.getScaledTouchSlop();
    }

    float getActiveX(MotionEvent motionEvent) {
        return motionEvent.getX();
    }

    float getActiveY(MotionEvent motionEvent) {
        return motionEvent.getY();
    }

    @Override // uk.co.senab.photoview.gestures.GestureDetector
    public boolean isDragging() {
        return this.mIsDragging;
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARN: Code restructure failed: missing block: B:32:0x00cf, code lost:
    
        return true;
     */
    @Override // uk.co.senab.photoview.gestures.GestureDetector
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public boolean onTouchEvent(android.view.MotionEvent r12) {
        /*
            r11 = this;
            int r0 = r12.getAction()
            r1 = 0
            r2 = 0
            r3 = 1
            switch(r0) {
                case 0: goto La8;
                case 1: goto L55;
                case 2: goto L17;
                case 3: goto Lc;
                default: goto La;
            }
        La:
            goto Lcf
        Lc:
            android.view.VelocityTracker r12 = r11.mVelocityTracker
            if (r12 == 0) goto Lcf
            r12.recycle()
            r11.mVelocityTracker = r2
            goto Lcf
        L17:
            float r0 = r11.getActiveX(r12)
            float r2 = r11.getActiveY(r12)
            float r4 = r11.mLastTouchX
            float r4 = r0 - r4
            float r5 = r11.mLastTouchY
            float r5 = r2 - r5
            boolean r6 = r11.mIsDragging
            if (r6 != 0) goto L3f
            float r6 = r4 * r4
            float r7 = r5 * r5
            float r6 = r6 + r7
            double r6 = (double) r6
            double r6 = java.lang.Math.sqrt(r6)
            float r8 = r11.mTouchSlop
            double r8 = (double) r8
            int r10 = (r6 > r8 ? 1 : (r6 == r8 ? 0 : -1))
            if (r10 < 0) goto L3d
            r1 = 1
        L3d:
            r11.mIsDragging = r1
        L3f:
            boolean r1 = r11.mIsDragging
            if (r1 == 0) goto Lcf
            uk.co.senab.photoview.gestures.OnGestureListener r1 = r11.mListener
            r1.onDrag(r4, r5)
            r11.mLastTouchX = r0
            r11.mLastTouchY = r2
            android.view.VelocityTracker r0 = r11.mVelocityTracker
            if (r0 == 0) goto Lcf
            r0.addMovement(r12)
            goto Lcf
        L55:
            boolean r0 = r11.mIsDragging
            if (r0 == 0) goto L9e
            android.view.VelocityTracker r0 = r11.mVelocityTracker
            if (r0 == 0) goto L9e
            float r0 = r11.getActiveX(r12)
            r11.mLastTouchX = r0
            float r0 = r11.getActiveY(r12)
            r11.mLastTouchY = r0
            android.view.VelocityTracker r0 = r11.mVelocityTracker
            r0.addMovement(r12)
            android.view.VelocityTracker r12 = r11.mVelocityTracker
            r0 = 1000(0x3e8, float:1.401E-42)
            r12.computeCurrentVelocity(r0)
            android.view.VelocityTracker r12 = r11.mVelocityTracker
            float r12 = r12.getXVelocity()
            android.view.VelocityTracker r0 = r11.mVelocityTracker
            float r0 = r0.getYVelocity()
            float r1 = java.lang.Math.abs(r12)
            float r4 = java.lang.Math.abs(r0)
            float r1 = java.lang.Math.max(r1, r4)
            float r4 = r11.mMinimumVelocity
            int r1 = (r1 > r4 ? 1 : (r1 == r4 ? 0 : -1))
            if (r1 < 0) goto L9e
            uk.co.senab.photoview.gestures.OnGestureListener r1 = r11.mListener
            float r4 = r11.mLastTouchX
            float r5 = r11.mLastTouchY
            float r12 = -r12
            float r0 = -r0
            r1.onFling(r4, r5, r12, r0)
        L9e:
            android.view.VelocityTracker r12 = r11.mVelocityTracker
            if (r12 == 0) goto Lcf
            r12.recycle()
            r11.mVelocityTracker = r2
            goto Lcf
        La8:
            android.view.VelocityTracker r0 = android.view.VelocityTracker.obtain()
            r11.mVelocityTracker = r0
            android.view.VelocityTracker r0 = r11.mVelocityTracker
            if (r0 == 0) goto Lb6
            r0.addMovement(r12)
            goto Lc1
        Lb6:
            uk.co.senab.photoview.log.Logger r0 = uk.co.senab.photoview.log.LogManager.getLogger()
            java.lang.String r2 = "CupcakeGestureDetector"
            java.lang.String r4 = "Velocity tracker is null"
            r0.i(r2, r4)
        Lc1:
            float r0 = r11.getActiveX(r12)
            r11.mLastTouchX = r0
            float r12 = r11.getActiveY(r12)
            r11.mLastTouchY = r12
            r11.mIsDragging = r1
        Lcf:
            return r3
        */
        throw new UnsupportedOperationException("Method not decompiled: uk.co.senab.photoview.gestures.CupcakeGestureDetector.onTouchEvent(android.view.MotionEvent):boolean");
    }
}
