package com.logon.e100.utils;

import android.content.Context;
import android.util.Log;
import java.io.File;

/* loaded from: classes.dex */
public class Logger {
    private static Context mContext = null;
    private static File mDataLoggerDirectory = null;
    private static File mDataLoggerFile = null;
    private static File mDataLoggerOldFile = null;
    private static String mLogTag = "HAN";
    private static boolean mLogflag = false;

    public static void d(String str) {
        show(3, mLogTag, str);
    }

    public static void d(String str, String str2) {
        show(3, str, str2);
    }

    public static void w(String str) {
        show(5, mLogTag, str);
    }

    public static void i(String str) {
        show(4, mLogTag, str);
    }

    public static void e(String str) {
        show(6, mLogTag, str);
    }

    public static void v(String str) {
        show(6, mLogTag, str);
    }

    private static void show(int i, String str, String str2) {
        if (str2.length() > 4000) {
            Log.i("Length ", str2.length() + "");
            while (str2.length() > 4000) {
                show(i, str, str2.substring(0, 4000));
                str2 = str2.substring(4000, str2.length());
            }
        }
        if (mLogflag) {
            switch (i) {
                case 2:
                    Log.v(str, str2);
                    break;
                case 3:
                    Log.d(str, str2);
                    break;
                case 4:
                    Log.i(str, str2);
                    break;
                case 5:
                    Log.w(str, str2);
                    break;
                case 6:
                    Log.e(str, str2);
                    break;
                case 7:
                    Log.wtf(str, str2);
                    break;
            }
        }
    }

    private static void show(Exception exc) {
        try {
            if (mLogflag) {
                exc.printStackTrace();
            }
        } catch (NullPointerException e) {
            show(e);
        }
    }

    public static boolean enableLog() {
        mLogflag = true;
        return mLogflag;
    }

    public static boolean disableLog() {
        mLogflag = false;
        return mLogflag;
    }
}
