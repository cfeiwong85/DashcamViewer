package com.logon_e100;

/* loaded from: classes.dex */
public class Debug_Print {
    private static boolean debug = true;

    public static boolean getDebug() {
        return debug;
    }

    public static void LogString(String str, String str2) {
        if (debug) {
            System.out.println(str + " : " + str2);
        }
    }

    public static void LogCharArray(String str) {
        if (debug) {
            char[] cArr = new char[str.length()];
            char[] charArray = str.toCharArray();
            System.out.println("LogPrint(String) " + str.getClass().getName() + ", size : " + str.length());
            if (str.length() >= 16) {
                int i = 0;
                while (true) {
                    if (i >= (str.length() % 16 == 0 ? str.length() / 16 : (str.length() / 16) + 1)) {
                        break;
                    }
                    int i2 = i + 1;
                    int i3 = i * 16;
                    System.out.printf("Line %03d [%06d]: ", Integer.valueOf(i2), Integer.valueOf(i3));
                    if (i >= str.length() / 16) {
                        for (int i4 = 0; i4 < str.length() % 16; i4++) {
                            logPrintchar(charArray[i3 + i4]);
                        }
                        for (int i5 = 0; i5 < 16 - (str.length() % 16); i5++) {
                            System.out.print("[_____]");
                        }
                        System.out.print("\t");
                        for (int i6 = 0; i6 < str.length() % 16; i6++) {
                            System.out.print(charArray[i3 + i6]);
                        }
                    } else {
                        for (int i7 = 0; i7 < 16; i7++) {
                            logPrintchar(charArray[i3 + i7]);
                        }
                        System.out.print("\t");
                        for (int i8 = 0; i8 < 16; i8++) {
                            System.out.print(charArray[i3 + i8]);
                        }
                    }
                    System.out.println();
                    i = i2;
                }
            } else {
                System.out.printf("Line %03d [%06d]: ", 1, 0);
                for (int i9 = 0; i9 < str.length(); i9++) {
                    logPrintchar(charArray[i9]);
                }
                for (int i10 = 0; i10 < 16 - str.length(); i10++) {
                    System.out.print("[_____]");
                }
                System.out.print("\t");
                for (int i11 = 0; i11 < str.length(); i11++) {
                    System.out.printf("%c", Character.valueOf(charArray[i11]));
                }
            }
            System.out.println();
        }
    }

    public static void LogByteArray(byte[] bArr) {
        if (debug) {
            System.out.println("LogPrint(String) " + bArr.getClass().getName() + ", size : " + bArr.length);
            if (bArr.length >= 16) {
                int i = 0;
                while (true) {
                    if (i >= (bArr.length % 16 == 0 ? bArr.length / 16 : (bArr.length / 16) + 1)) {
                        break;
                    }
                    int i2 = i + 1;
                    int i3 = i * 16;
                    System.out.printf("Line %03d [%06d]: ", Integer.valueOf(i2), Integer.valueOf(i3));
                    if (i >= bArr.length / 16) {
                        for (int i4 = 0; i4 < bArr.length % 16; i4++) {
                            logPrintbyte(bArr[i3 + i4]);
                        }
                        for (int i5 = 0; i5 < 16 - (bArr.length % 16); i5++) {
                            System.out.print("[___]");
                        }
                        System.out.print("\t");
                        for (int i6 = 0; i6 < bArr.length % 16; i6++) {
                        }
                    } else {
                        for (int i7 = 0; i7 < 16; i7++) {
                            logPrintbyte(bArr[i3 + i7]);
                        }
                        System.out.print("\t");
                        for (int i8 = 0; i8 < 16; i8++) {
                        }
                    }
                    System.out.println();
                    i = i2;
                }
            } else {
                System.out.printf("Line %03d [%06d]: ", 1, 0);
                for (byte b : bArr) {
                    logPrintbyte(b);
                }
                for (int i9 = 0; i9 < 16 - bArr.length; i9++) {
                    System.out.print("\t");
                }
                System.out.print("\t");
                for (byte b2 : bArr) {
                    System.out.printf("%c", Byte.valueOf(b2));
                }
            }
            System.out.println();
        }
    }

    public static void LogCharArray(char[] cArr) {
        if (debug) {
            System.out.println("LogPrint(String) " + cArr.getClass().getName() + ", size : " + cArr.length);
            if (cArr.length >= 16) {
                int i = 0;
                while (true) {
                    if (i >= (cArr.length % 16 == 0 ? cArr.length / 16 : (cArr.length / 16) + 1)) {
                        break;
                    }
                    int i2 = i + 1;
                    int i3 = i * 16;
                    System.out.printf("Line %03d [%06d]: ", Integer.valueOf(i2), Integer.valueOf(i3));
                    if (i >= cArr.length / 16) {
                        for (int i4 = 0; i4 < cArr.length % 16; i4++) {
                            logPrintchar(cArr[i3 + i4]);
                        }
                        for (int i5 = 0; i5 < 16 - (cArr.length % 16); i5++) {
                            System.out.print("[_____]");
                        }
                        System.out.print("\t");
                        for (int i6 = 0; i6 < cArr.length % 16; i6++) {
                            System.out.printf("%c", Character.valueOf(cArr[i3 + i6]));
                        }
                    } else {
                        for (int i7 = 0; i7 < 16; i7++) {
                            logPrintchar(cArr[i3 + i7]);
                        }
                        System.out.print("\t");
                        for (int i8 = 0; i8 < 16; i8++) {
                            System.out.printf("%c", Character.valueOf(cArr[i3 + i8]));
                        }
                    }
                    System.out.println();
                    i = i2;
                }
            } else {
                System.out.printf("Line %03d [%06d]: ", 1, 0);
                for (char c : cArr) {
                    logPrintchar(c);
                }
                for (int i9 = 0; i9 < 16 - cArr.length; i9++) {
                    System.out.print("[_____]");
                }
                System.out.print("\t");
                for (char c2 : cArr) {
                    System.out.printf("%c", Character.valueOf(c2));
                }
            }
            System.out.println();
        }
    }

    public static void LogIntArray(int[] iArr) {
        if (debug) {
            System.out.println("LogPrint(String) " + iArr.getClass().getName() + ", size : " + iArr.length);
            if (iArr.length >= 16) {
                int i = 0;
                while (true) {
                    if (i >= (iArr.length % 16 == 0 ? iArr.length / 16 : (iArr.length / 16) + 1)) {
                        break;
                    }
                    int i2 = i + 1;
                    int i3 = i * 16;
                    System.out.printf("Line %03d [%06d]: ", Integer.valueOf(i2), Integer.valueOf(i3));
                    if (i >= iArr.length / 16) {
                        for (int i4 = 0; i4 < iArr.length % 16; i4++) {
                            logPrintbyte(iArr[i3 + i4]);
                        }
                        for (int i5 = 0; i5 < 16 - (iArr.length % 16); i5++) {
                            System.out.print("[___]");
                        }
                        System.out.print("\t");
                        for (int i6 = 0; i6 < iArr.length % 16; i6++) {
                            System.out.printf("%c", Integer.valueOf(iArr[i3 + i6]));
                        }
                    } else {
                        for (int i7 = 0; i7 < 16; i7++) {
                            logPrintbyte(iArr[i3 + i7]);
                        }
                        System.out.print("\t");
                        for (int i8 = 0; i8 < 16; i8++) {
                            System.out.printf("%c", Integer.valueOf(iArr[i3 + i8]));
                        }
                    }
                    System.out.println();
                    i = i2;
                }
            } else {
                System.out.printf("Line %03d [%06d]: ", 1, 0);
                for (int i9 : iArr) {
                    logPrintbyte(i9);
                }
                for (int i10 = 0; i10 < 16 - iArr.length; i10++) {
                    System.out.print("\t");
                }
                System.out.print("\t");
                for (int i11 : iArr) {
                    System.out.printf("%c", Integer.valueOf(i11));
                }
            }
            System.out.println();
        }
    }

    public static void LogByteArray(String str) {
        if (debug) {
            byte[] bytes = str.getBytes();
            System.out.println("LogPrint(String) " + str.getClass().getName() + ", size : " + str.length());
            if (str.length() >= 16) {
                int i = 0;
                while (true) {
                    if (i >= (str.length() % 16 == 0 ? str.length() / 16 : (str.length() / 16) + 1)) {
                        break;
                    }
                    int i2 = i + 1;
                    int i3 = i * 16;
                    System.out.printf("Line %03d [%06d]: ", Integer.valueOf(i2), Integer.valueOf(i3));
                    if (i >= str.length() / 16) {
                        for (int i4 = 0; i4 < str.length() % 16; i4++) {
                            logPrintbyte(bytes[i3 + i4]);
                        }
                        for (int i5 = 0; i5 < 16 - (str.length() % 16); i5++) {
                            System.out.print("[___]");
                        }
                        System.out.print("\t");
                        for (int i6 = 0; i6 < str.length() % 16; i6++) {
                            System.out.printf("%c", Byte.valueOf(bytes[i3 + i6]));
                        }
                    } else {
                        for (int i7 = 0; i7 < 16; i7++) {
                            logPrintbyte(bytes[i3 + i7]);
                        }
                        System.out.print("\t");
                        for (int i8 = 0; i8 < 16; i8++) {
                            System.out.printf("%c", Byte.valueOf(bytes[i3 + i8]));
                        }
                    }
                    System.out.println();
                    i = i2;
                }
            } else {
                System.out.printf("Line %03d [%06d]: ", 1, 0);
                for (int i9 = 0; i9 < str.length(); i9++) {
                    logPrintbyte(bytes[i9]);
                }
                for (int i10 = 0; i10 < 16 - str.length(); i10++) {
                    System.out.print("\t");
                }
                System.out.print("\t");
                for (int i11 = 0; i11 < str.length(); i11++) {
                    System.out.printf("%c", Byte.valueOf(bytes[i11]));
                }
            }
            System.out.println();
        }
    }

    public static void LogCharArrayNHEX(char[] cArr) {
        if (debug) {
            System.out.println("LogPrint(String) " + cArr.getClass().getName() + ", size : " + cArr.length);
            if (cArr.length >= 16) {
                int i = 0;
                while (true) {
                    if (i >= (cArr.length % 16 == 0 ? cArr.length / 16 : (cArr.length / 16) + 1)) {
                        break;
                    }
                    int i2 = i + 1;
                    int i3 = i * 16;
                    System.out.printf("Line %03d [%06d]: ", Integer.valueOf(i2), Integer.valueOf(i3));
                    if (i >= cArr.length / 16) {
                        for (int i4 = 0; i4 < cArr.length % 16; i4++) {
                            System.out.printf("[%c]", Character.valueOf(cArr[i3 + i4]));
                        }
                    } else {
                        for (int i5 = 0; i5 < 16; i5++) {
                            System.out.printf("[%c]", Character.valueOf(cArr[i3 + i5]));
                        }
                    }
                    System.out.println();
                    i = i2;
                }
            } else {
                System.out.printf("Line %03d [%06d]: ", 1, 0);
                for (char c : cArr) {
                    System.out.printf("[%c]", Character.valueOf(c));
                }
            }
            System.out.println();
        }
    }

    public static void LogByteArrayNHEX(byte[] bArr) {
        if (debug) {
            System.out.println("LogPrint(String) " + bArr.getClass().getName() + ", size : " + bArr.length);
            if (bArr.length >= 16) {
                int i = 0;
                while (true) {
                    if (i >= (bArr.length % 16 == 0 ? bArr.length / 16 : (bArr.length / 16) + 1)) {
                        break;
                    }
                    int i2 = i + 1;
                    int i3 = i * 16;
                    System.out.printf("Line %03d [%06d]: ", Integer.valueOf(i2), Integer.valueOf(i3));
                    if (i >= bArr.length / 16) {
                        for (int i4 = 0; i4 < bArr.length % 16; i4++) {
                            System.out.printf("[%c]", Byte.valueOf(bArr[i3 + i4]));
                        }
                    } else {
                        for (int i5 = 0; i5 < 16; i5++) {
                            System.out.printf("[%c]", Byte.valueOf(bArr[i3 + i5]));
                        }
                    }
                    System.out.println();
                    i = i2;
                }
            } else {
                System.out.printf("Line %03d [%06d]: ", 1, 0);
                for (byte b : bArr) {
                    System.out.printf("[%c]", Byte.valueOf(b));
                }
            }
            System.out.println();
        }
    }

    public static void LogIntArrayNHEX(int[] iArr) {
        if (debug) {
            System.out.println("LogPrint(String) " + iArr.getClass().getName() + ", size : " + iArr.length);
            if (iArr.length >= 16) {
                int i = 0;
                while (true) {
                    if (i >= (iArr.length % 16 == 0 ? iArr.length / 16 : (iArr.length / 16) + 1)) {
                        break;
                    }
                    int i2 = i + 1;
                    int i3 = i * 16;
                    System.out.printf("Line %03d [%06d]: ", Integer.valueOf(i2), Integer.valueOf(i3));
                    if (i >= iArr.length / 16) {
                        for (int i4 = 0; i4 < iArr.length % 16; i4++) {
                            System.out.printf("[%c]", Character.valueOf((char) iArr[i3 + i4]));
                        }
                    } else {
                        for (int i5 = 0; i5 < 16; i5++) {
                            System.out.printf("[%c]", Character.valueOf((char) iArr[i3 + i5]));
                        }
                    }
                    System.out.println();
                    i = i2;
                }
            } else {
                System.out.printf("Line %03d [%06d]: ", 1, 0);
                for (int i6 : iArr) {
                    System.out.printf("[%c]", Character.valueOf((char) i6));
                }
            }
            System.out.println();
        }
    }

    public static void logPrintchar(char c) {
        if (c == 0) {
            System.out.printf("[_NUL_]", new Object[0]);
            return;
        }
        if (c == 1) {
            System.out.printf("[_SOH_]", new Object[0]);
            return;
        }
        if (c == 2) {
            System.out.printf("[_STX_]", new Object[0]);
            return;
        }
        if (c == 3) {
            System.out.printf("[_ETX_]", new Object[0]);
            return;
        }
        if (c == 4) {
            System.out.printf("[_EOT_]", new Object[0]);
            return;
        }
        if (c == 5) {
            System.out.printf("[_ENQ_]", new Object[0]);
            return;
        }
        if (c == 6) {
            System.out.printf("[_ACK_]", new Object[0]);
            return;
        }
        if (c == 7) {
            System.out.printf("[_BEL_]", new Object[0]);
            return;
        }
        if (c == '\b') {
            System.out.printf("[__BS_]", new Object[0]);
            return;
        }
        if (c == '\t') {
            System.out.printf("[__HT_]", new Object[0]);
            return;
        }
        if (c == '\n') {
            System.out.printf("[__LF_]", new Object[0]);
            return;
        }
        if (c == 11) {
            System.out.printf("[__VT_]", new Object[0]);
            return;
        }
        if (c == '\f') {
            System.out.printf("[__FF_]", new Object[0]);
            return;
        }
        if (c == '\r') {
            System.out.printf("[__CR_]", new Object[0]);
            return;
        }
        if (c == 14) {
            System.out.printf("[__SO_]", new Object[0]);
            return;
        }
        if (c == 15) {
            System.out.printf("[__SI_]", new Object[0]);
            return;
        }
        if (c == 16) {
            System.out.printf("[_DLE_]", new Object[0]);
            return;
        }
        if (c == 17) {
            System.out.printf("[_DC1_]", new Object[0]);
            return;
        }
        if (c == 18) {
            System.out.printf("[_DC2_]", new Object[0]);
            return;
        }
        if (c == 19) {
            System.out.printf("[_DC3_]", new Object[0]);
            return;
        }
        if (c == 20) {
            System.out.printf("[_DC4_]", new Object[0]);
            return;
        }
        if (c == 21) {
            System.out.printf("[_NAK_]", new Object[0]);
            return;
        }
        if (c == 22) {
            System.out.printf("[_SYN_]", new Object[0]);
            return;
        }
        if (c == 23) {
            System.out.printf("[_ETB_]", new Object[0]);
            return;
        }
        if (c == 24) {
            System.out.printf("[_CAN_]", new Object[0]);
            return;
        }
        if (c == 25) {
            System.out.printf("[__EM_]", new Object[0]);
            return;
        }
        if (c == 26) {
            System.out.printf("[_SUB_]", new Object[0]);
            return;
        }
        if (c == 27) {
            System.out.printf("[_ESC_]", new Object[0]);
            return;
        }
        if (c == 28) {
            System.out.printf("[__FS_]", new Object[0]);
            return;
        }
        if (c == 29) {
            System.out.printf("[__GS_]", new Object[0]);
            return;
        }
        if (c == 30) {
            System.out.printf("[__RS_]", new Object[0]);
            return;
        }
        if (c == 31) {
            System.out.printf("[__US_]", new Object[0]);
        } else if (c == ' ') {
            System.out.printf("[_SPB_]", new Object[0]);
        } else {
            System.out.printf("[x%04X]", Integer.valueOf(c));
        }
    }

    public static void logPrintbyte(byte b) {
        System.out.printf("[x%02x]", Byte.valueOf(b));
    }

    public static void logPrintbyte(int i) {
        if (i == 0) {
            System.out.printf("[NUL]", new Object[0]);
            return;
        }
        if (i == 1) {
            System.out.printf("[SOH]", new Object[0]);
            return;
        }
        if (i == 2) {
            System.out.printf("[STX]", new Object[0]);
            return;
        }
        if (i == 3) {
            System.out.printf("[ETX]", new Object[0]);
            return;
        }
        if (i == 4) {
            System.out.printf("[EOT]", new Object[0]);
            return;
        }
        if (i == 5) {
            System.out.printf("[ENQ]", new Object[0]);
            return;
        }
        if (i == 6) {
            System.out.printf("[ACK]", new Object[0]);
            return;
        }
        if (i == 7) {
            System.out.printf("[BEL]", new Object[0]);
            return;
        }
        if (i == 8) {
            System.out.printf("[ BS]", new Object[0]);
            return;
        }
        if (i == 9) {
            System.out.printf("[ HT]", new Object[0]);
            return;
        }
        if (i == 10) {
            System.out.printf("[ LF]", new Object[0]);
            return;
        }
        if (i == 11) {
            System.out.printf("[ VT]", new Object[0]);
            return;
        }
        if (i == 12) {
            System.out.printf("[ FF]", new Object[0]);
            return;
        }
        if (i == 13) {
            System.out.printf("[ CR]", new Object[0]);
            return;
        }
        if (i == 14) {
            System.out.printf("[ SO]", new Object[0]);
            return;
        }
        if (i == 15) {
            System.out.printf("[ SI]", new Object[0]);
            return;
        }
        if (i == 16) {
            System.out.printf("[DLE]", new Object[0]);
            return;
        }
        if (i == 17) {
            System.out.printf("[DC1]", new Object[0]);
            return;
        }
        if (i == 18) {
            System.out.printf("[DC2]", new Object[0]);
            return;
        }
        if (i == 19) {
            System.out.printf("[DC3]", new Object[0]);
            return;
        }
        if (i == 20) {
            System.out.printf("[DC4]", new Object[0]);
            return;
        }
        if (i == 21) {
            System.out.printf("[NAK]", new Object[0]);
            return;
        }
        if (i == 22) {
            System.out.printf("[SYN]", new Object[0]);
            return;
        }
        if (i == 23) {
            System.out.printf("[ETB]", new Object[0]);
            return;
        }
        if (i == 24) {
            System.out.printf("[CAN]", new Object[0]);
            return;
        }
        if (i == 25) {
            System.out.printf("[ EM]", new Object[0]);
            return;
        }
        if (i == 26) {
            System.out.printf("[SUB]", new Object[0]);
            return;
        }
        if (i == 27) {
            System.out.printf("[ESC]", new Object[0]);
            return;
        }
        if (i == 28) {
            System.out.printf("[ FS]", new Object[0]);
            return;
        }
        if (i == 29) {
            System.out.printf("[ GS]", new Object[0]);
            return;
        }
        if (i == 30) {
            System.out.printf("[ RS]", new Object[0]);
            return;
        }
        if (i == 31) {
            System.out.printf("[ US]", new Object[0]);
        } else if (i == 32) {
            System.out.printf("[SPB]", new Object[0]);
        } else {
            System.out.printf("[x%02X]", Integer.valueOf(i));
        }
    }
}
