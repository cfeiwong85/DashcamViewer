package com.logon.e100.setting;

import android.content.Context;
import com.logon_e100.MainProtocol;
import com.logon_e100.R;

/* loaded from: classes.dex */
public class SettingDefs {
    public int[] CELL_TYPE;
    public String[] CONTRAST;
    public String[] CRASH_EVENT_ALARM;
    public String[] EMPTY;
    public String[] EXPOSURE;
    public String[] LCD_BRIGHTNESS;
    public String[] LED;
    public String[] NORMAL_IMPACT_SENSITIVITY;
    public String[] PARKING_LOW_POWER;
    public String[] PARKING_OPTIME;
    public String[] PARK_IMPACT_SENSITIVITY;
    public String[] PARTITION;
    public String[] REAR_MIRROR;
    public String[] RECORD_QUALITY;
    public String[] SETTING_LIST;
    public String[][] SETTING_ROW;
    public String[] SWITCH_SCREEN;
    public String[] TIME_ZONE;
    public String[] VOICE_GUIDE;
    public String[] VOICE_RECORD;
    public String[] VOICE_VOLUME;
    public String[] WHITE_BALANCE;
    public int CELL_SECTION = 0;
    public int CELL_TITLE = 1;
    public int CELL_SUB = 2;

    public SettingDefs(Context context) {
        this.SETTING_LIST = context.getResources().getStringArray(R.array.setting_list);
        int i = this.CELL_SECTION;
        int i2 = this.CELL_TITLE;
        int i3 = this.CELL_SUB;
        this.CELL_TYPE = new int[]{i, i2, i, i3, i3, i3, i3, i3, i3, i3, i3, i3, i2, i3, i3, i3, i3, i3, i3, i3, i2, i3, i2, i, i2, i2};
        if (MainProtocol.FHD_CHECK == 1) {
            this.RECORD_QUALITY = context.getResources().getStringArray(R.array.record_quality);
        } else if (MainProtocol.FHD_CHECK == 2) {
            this.RECORD_QUALITY = context.getResources().getStringArray(R.array.record_quality_ff);
        } else if (MainProtocol.FHD_CHECK == 3) {
            this.RECORD_QUALITY = context.getResources().getStringArray(R.array.record_quality_hh);
        }
        this.REAR_MIRROR = context.getResources().getStringArray(R.array.rear_mirror);
        this.NORMAL_IMPACT_SENSITIVITY = context.getResources().getStringArray(R.array.normal_impact_sensitivity);
        this.VOICE_VOLUME = context.getResources().getStringArray(R.array.voice_volume);
        this.VOICE_RECORD = context.getResources().getStringArray(R.array.voice_record);
        this.LCD_BRIGHTNESS = context.getResources().getStringArray(R.array.lcd_brightness);
        this.WHITE_BALANCE = context.getResources().getStringArray(R.array.white_balance);
        this.CONTRAST = context.getResources().getStringArray(R.array.contrast);
        this.EXPOSURE = context.getResources().getStringArray(R.array.exposure);
        this.SWITCH_SCREEN = context.getResources().getStringArray(R.array.switch_screen);
        this.CRASH_EVENT_ALARM = context.getResources().getStringArray(R.array.crash_event_alarm);
        this.PARKING_LOW_POWER = context.getResources().getStringArray(R.array.parking_low_power);
        this.PARK_IMPACT_SENSITIVITY = context.getResources().getStringArray(R.array.park_impact_sensitivity);
        if (MainProtocol.VER_CHECK == 1) {
            this.PARKING_OPTIME = context.getResources().getStringArray(R.array.parking_optime_v2);
        } else {
            this.PARKING_OPTIME = context.getResources().getStringArray(R.array.parking_optime);
        }
        this.TIME_ZONE = context.getResources().getStringArray(R.array.time_zone);
        this.VOICE_GUIDE = context.getResources().getStringArray(R.array.voice_guide);
        this.PARTITION = context.getResources().getStringArray(R.array.partition);
        this.LED = context.getResources().getStringArray(R.array.led);
        this.EMPTY = new String[]{"", ""};
        String[] strArr = this.EMPTY;
        this.SETTING_ROW = new String[][]{strArr, strArr, strArr, this.RECORD_QUALITY, this.NORMAL_IMPACT_SENSITIVITY, this.VOICE_VOLUME, this.VOICE_RECORD, this.LCD_BRIGHTNESS, this.WHITE_BALANCE, this.CONTRAST, this.EXPOSURE, this.SWITCH_SCREEN, strArr, this.CRASH_EVENT_ALARM, this.PARKING_LOW_POWER, this.PARK_IMPACT_SENSITIVITY, this.PARKING_OPTIME, this.TIME_ZONE, this.VOICE_GUIDE, this.LED, strArr, this.PARTITION, strArr, strArr, strArr, strArr};
    }
}
