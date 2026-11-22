package com.logon_e100;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

/* loaded from: classes.dex */
public class MainPhotoView extends Activity {
    @Override // android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getWindow().addFlags(128);
        setContentView(R.layout.activity_mainphotoview);
        ImageView imageView = (ImageView) findViewById(R.id.imageview1);
        Intent intent = getIntent();
        String stringExtra = intent.getStringExtra("filename");
        MainProtocol.OnStopFlag = false;
        if (intent != null) {
            imageView.setImageBitmap(BitmapFactory.decodeFile(stringExtra, new BitmapFactory.Options()));
        }
    }
}
