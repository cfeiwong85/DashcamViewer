package com.logon.e100.setting;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;
import com.logon_e100.MainProtocol;
import com.logon_e100.R;

/* loaded from: classes.dex */
public class ActivityDateAndTime extends Activity {
    static final Handler displayAlertHandler = new Handler() { // from class: com.logon.e100.setting.ActivityDateAndTime.3
        @Override // android.os.Handler
        public void handleMessage(Message message) {
            String[] strArr = (String[]) message.obj;
            ActivityDateAndTime.displayInfoAlert(strArr[0], strArr[1]);
        }
    };
    private static Button mButtonConfirm;
    private static Context mContext;
    private Button mButtonCancel;
    View.OnClickListener mClickListener = new View.OnClickListener() { // from class: com.logon.e100.setting.ActivityDateAndTime.1
        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.button_date_and_time_cancel /* 2131230758 */:
                    Log.d("mhhan", "#### " + getClass().getName() + "::onClick    cancel clicked!! ####");
                    ActivityDateAndTime.this.finish();
                    break;
                case R.id.button_date_and_time_confirm /* 2131230759 */:
                    Log.d("mhhan", "#### " + getClass().getName() + "::onClick    confirm clicked!! ####");
                    ActivityDateAndTime.mButtonConfirm.setEnabled(false);
                    int year = ActivityDateAndTime.this.mDatePickerDate.getYear();
                    int month = ActivityDateAndTime.this.mDatePickerDate.getMonth() + 1;
                    int dayOfMonth = ActivityDateAndTime.this.mDatePickerDate.getDayOfMonth();
                    int intValue = ActivityDateAndTime.this.mTimePickerTime.getCurrentHour().intValue();
                    int intValue2 = ActivityDateAndTime.this.mTimePickerTime.getCurrentMinute().intValue();
                    if (year > 2037 || year < 2014 || month > 12 || month < 1 || dayOfMonth > 31 || dayOfMonth < 1 || intValue > 23 || intValue < 0 || intValue2 > 59 || intValue2 < 0) {
                        Log.d("mhhan", "get time failed");
                        ActivityDateAndTime.displayInfoAlert(ActivityDateAndTime.this.getResources().getString(R.string.setting_wifi_password_title), ActivityDateAndTime.this.getResources().getString(R.string.setting_wifi_password_error));
                        break;
                    } else {
                        MainProtocol.CNSTime cNSTime = new MainProtocol.CNSTime();
                        cNSTime.setYear(year);
                        cNSTime.setMonth(month);
                        cNSTime.setDay(dayOfMonth);
                        cNSTime.setHour(intValue);
                        cNSTime.setMinute(intValue2);
                        Log.d("mhhan", "#### " + ActivityDateAndTime.this.mDatePickerDate.getYear() + "/" + (ActivityDateAndTime.this.mDatePickerDate.getMonth() + 1) + "/" + ActivityDateAndTime.this.mDatePickerDate.getDayOfMonth() + " " + ActivityDateAndTime.this.mTimePickerTime.getCurrentHour() + ":" + ActivityDateAndTime.this.mTimePickerTime.getCurrentMinute() + " ####");
                        if (MainProtocol.send(MainProtocol.NAT_CMD_SET_TIME, cNSTime.getBytes(), 5) < 0) {
                            ActivityDateAndTime.displayInfoAlert(ActivityDateAndTime.this.getResources().getString(R.string.setting_wifi_password_title), ActivityDateAndTime.this.getResources().getString(R.string.setting_wifi_password_error));
                            break;
                        }
                    }
                    break;
                default:
                    Log.d("mhhan", "what????????");
                    break;
            }
        }
    };
    private DatePicker mDatePickerDate;
    private TimePicker mTimePickerTime;

    public void dateSet() {
    }

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setTheme(android.R.style.Theme.Holo.Light.NoActionBar.TranslucentDecor);
        setContentView(R.layout.activity_date_and_time);
        mContext = this;
        this.mDatePickerDate = (DatePicker) findViewById(R.id.datepicker_date);
        this.mTimePickerTime = (TimePicker) findViewById(R.id.timepicker_time);
        this.mButtonCancel = (Button) findViewById(R.id.button_date_and_time_cancel);
        mButtonConfirm = (Button) findViewById(R.id.button_date_and_time_confirm);
        dateSet();
        this.mButtonCancel.setOnClickListener(this.mClickListener);
        mButtonConfirm.setOnClickListener(this.mClickListener);
    }

    @Override // android.app.Activity
    protected void onStart() {
        super.onStart();
    }

    @Override // android.app.Activity
    protected void onRestart() {
        super.onRestart();
    }

    @Override // android.app.Activity
    protected void onResume() {
        super.onResume();
    }

    @Override // android.app.Activity
    protected void onPause() {
        super.onPause();
    }

    @Override // android.app.Activity
    protected void onStop() {
        super.onStop();
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        super.onDestroy();
    }

    public static void displayInfoAlert(String str, String str2) {
        String string = mContext.getResources().getString(R.string.action_confirm);
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(str2).setCancelable(false).setPositiveButton(string, new DialogInterface.OnClickListener() { // from class: com.logon.e100.setting.ActivityDateAndTime.2
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                ActivityDateAndTime.mButtonConfirm.setEnabled(true);
            }
        });
        AlertDialog create = builder.create();
        create.setTitle(str);
        create.setIcon(R.drawable.ic_launcher);
        create.show();
    }

    public static void onReceive(int i, byte[] bArr) {
        Log.d("mhhan", "#### onReceive cmd = " + i + " ####");
        if (i != 40978) {
            return;
        }
        if (new MainProtocol.CNSResult(bArr).getResult() < 0) {
            String[] strArr = {mContext.getResources().getString(R.string.setting_date_and_time_title), mContext.getResources().getString(R.string.setting_date_and_time_error)};
            Message obtainMessage = displayAlertHandler.obtainMessage();
            obtainMessage.obj = strArr;
            displayAlertHandler.sendMessage(obtainMessage);
            return;
        }
        String[] strArr2 = {mContext.getResources().getString(R.string.setting_date_and_time_title), mContext.getResources().getString(R.string.setting_date_and_time_conplete)};
        Message obtainMessage2 = displayAlertHandler.obtainMessage();
        obtainMessage2.obj = strArr2;
        displayAlertHandler.sendMessage(obtainMessage2);
    }
}
