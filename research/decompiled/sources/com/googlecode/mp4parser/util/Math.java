package com.googlecode.mp4parser.util;

/* loaded from: classes.dex */
public class Math {
    public static long gcd(long j, long j2) {
        while (true) {
            long j3 = j;
            j = j2;
            if (j <= 0) {
                return j3;
            }
            j2 = j3 % j;
        }
    }

    public static int gcd(int i, int i2) {
        int i3 = i;
        int i4 = i2;
        while (i4 > 0) {
            int i5 = i3 % i4;
            i3 = i4;
            i4 = i5;
        }
        return i3;
    }

    public static long lcm(long j, long j2) {
        return j * (j2 / gcd(j, j2));
    }

    public static long lcm(long[] jArr) {
        long j = jArr[0];
        for (int i = 1; i < jArr.length; i++) {
            j = lcm(j, jArr[i]);
        }
        return j;
    }

    public static int lcm(int i, int i2) {
        return i * (i2 / gcd(i, i2));
    }
}
