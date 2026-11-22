package com.logon.e100.utils;

/* loaded from: classes.dex */
public class Utils {
    private static long LAST_CLICK_TIME;

    public static final int byteArrayToInt_Big(byte[] bArr) {
        if (bArr.length == 1) {
            return bArr[0] & 255;
        }
        while (bArr.length != 2) {
            if (bArr.length == 4) {
                return (bArr[3] & 255) | ((bArr[0] & 255) << 24) | ((bArr[1] & 255) << 16) | ((bArr[2] & 255) << 8);
            }
        }
        return (bArr[1] & 255) | ((bArr[0] & 255) << 8);
    }

    public static final int byteArrayToInt_Little(byte[] bArr) {
        if (bArr.length == 1) {
            return bArr[0] & 255;
        }
        while (bArr.length != 2) {
            if (bArr.length == 4) {
                return ((bArr[3] & 255) << 24) | (bArr[0] & 255) | ((bArr[1] & 255) << 8) | ((bArr[2] & 255) << 16);
            }
        }
        return ((bArr[1] & 255) << 8) | (bArr[0] & 255);
    }

    public static final int byteArrayToInt_Little(byte[] bArr, int i) {
        return ((bArr[i + 3] & 255) << 24) | (bArr[i] & 255) | ((bArr[i + 1] & 255) << 8) | ((bArr[i + 2] & 255) << 16);
    }

    public static final long byteArrayToLong_Little(byte[] bArr, int i) {
        return ((bArr[i + 7] & 255) << 56) | (bArr[i] & 255) | ((bArr[i + 1] & 255) << 8) | ((bArr[i + 2] & 255) << 16) | ((bArr[i + 3] & 255) << 24) | ((bArr[i + 4] & 255) << 32) | ((bArr[i + 5] & 255) << 40) | ((bArr[i + 6] & 255) << 48);
    }

    public static final short byteArrayToShort_Little(byte[] bArr, int i) {
        return (short) (((bArr[i + 1] & 255) << 8) | (bArr[i] & 255));
    }

    public static final byte[] intToByteArray_Big(int i) {
        return new byte[]{(byte) (i >>> 24), (byte) (i >>> 16), (byte) (i >>> 8), (byte) i};
    }

    public static final byte[] intToByteArray_Little(int i) {
        return new byte[]{(byte) i, (byte) (i >>> 8), (byte) (i >>> 16), (byte) (i >>> 24)};
    }

    public static final byte[] longToByteArray_Little(long j) {
        return new byte[]{(byte) j, (byte) (j >>> 8), (byte) (j >>> 16), (byte) (j >>> 24), (byte) (j >>> 32), (byte) (j >>> 40), (byte) (j >>> 48), (byte) (j >>> 56)};
    }

    public static final byte[] shortToByteArray_Big(short s) {
        return new byte[]{(byte) (s >>> 8), (byte) s};
    }

    public static final byte[] shortToByteArray_Little(short s) {
        return new byte[]{(byte) s, (byte) (s >>> 8)};
    }

    public static final byte makeCRC(byte[] bArr, int i) {
        byte b = 0;
        for (byte b2 : bArr) {
            b = (byte) (b ^ b2);
        }
        return b;
    }

    public static String timeToText(int i) {
        return String.format("%02d:%02d", Integer.valueOf(i / 60), Integer.valueOf(i % 60));
    }

    public static boolean duplicateClickCheck() {
        long currentTimeMillis = System.currentTimeMillis();
        boolean z = currentTimeMillis - LAST_CLICK_TIME <= 1000;
        LAST_CLICK_TIME = currentTimeMillis;
        return z;
    }
}
