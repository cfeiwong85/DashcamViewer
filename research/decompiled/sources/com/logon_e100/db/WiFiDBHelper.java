package com.logon_e100.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/* loaded from: classes.dex */
public class WiFiDBHelper extends SQLiteOpenHelper {
    public WiFiDBHelper(Context context) {
        super(context, "WiFiDBHelper3.db", (SQLiteDatabase.CursorFactory) null, 1);
    }

    @Override // android.database.sqlite.SQLiteOpenHelper
    public void onCreate(SQLiteDatabase sQLiteDatabase) {
        sQLiteDatabase.execSQL("create table wifilist(_id INTEGER PRIMARY KEY AUTOINCREMENT, ssid TEXT,password TEXT);");
    }

    @Override // android.database.sqlite.SQLiteOpenHelper
    public void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        if (i < i2) {
            sQLiteDatabase.execSQL("DROP TABLE IF EXISTS wifilist");
            onCreate(sQLiteDatabase);
        }
    }
}
