package com.logon_e100;

import android.util.Log;
import com.logon.e100.utils.Utils;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;

/* loaded from: classes.dex */
public class MainProtocol {
    public static final String AAC_FILE = ".aac";
    public static final int ACTIVITY_ID_FILELIST = 1;
    public static final int ACTIVITY_ID_FILELIST_EVENT = 7;
    public static final int ACTIVITY_ID_FILELIST_PARK = 8;
    public static final int ACTIVITY_ID_FILELIST_PHOTO = 9;
    public static final int ACTIVITY_ID_FILELIST_REGULAR = 6;
    public static final int ACTIVITY_ID_FILE_PREVIEW = 10;
    public static final int ACTIVITY_ID_PREVIEW = 0;
    public static final int ACTIVITY_ID_SETTING = 2;
    public static final int ACTIVITY_ID_SETTING_DATE_AND_TIME = 5;
    public static final int ACTIVITY_ID_SETTING_SELECT = 3;
    public static final int ACTIVITY_ID_SETTING_WIFI_PASSWORD = 4;
    static final int AW_IOTYPE_USER_IPCAM_FILE_TRANS_START = 32;
    static final int AW_IOTYPE_USER_IPCAM_FILE_TRANS_START2 = 8;
    public static final int AW_IOTYPE_USER_IPCAM_FILE_TRANS_START_RESP = 8201;
    static final int AW_IOTYPE_USER_IPCAM_START = 1;
    static final int AW_IOTYPE_USER_IPCAM_START2 = 255;
    static final int AW_IOTYPE_USER_IPCAM_START_ENCODER = 32;
    static final int AW_IOTYPE_USER_IPCAM_START_ENCODER2 = 1;
    static final int AW_IOTYPE_USER_IPCAM_START_ENCODER3 = 8193;
    static final int AW_IOTYPE_USER_IPCAM_STOP = 767;
    static final int AW_IOTYPE_USER_IPCAM_STOP_ENCODER = 32;
    static final int AW_IOTYPE_USER_IPCAM_STOP_ENCODER2 = 2;
    static final byte EXT = 3;
    public static int FHD_CHECK = 0;
    public static final String FILE_PATH_EVENT = "/LOGON/EVENT/";
    public static final String FILE_PATH_NORMAL = "/LOGON/NORMAL/";
    public static final String FILE_PATH_PARK = "/LOGON/PARK/";
    public static final String FILE_PATH_PHOTO = "/LOGON/PHOTO/";
    static final byte HEAD_PHONE = 2;
    static final byte HEAD_W350 = 1;
    static final int NAT_CMD_CHANGE_CAM = 160;
    static final int NAT_CMD_CHANGE_CAM2 = 54;
    static final int NAT_CMD_CHANGE_CAM_RESP = 41015;
    static final int NAT_CMD_CHECK_TF_CARD = 41002;
    static final int NAT_CMD_CHECK_TF_CARD_RESP = 41003;
    public static final int NAT_CMD_FACTORY_RESET = 41044;
    public static final int NAT_CMD_FACTORY_RESET_RESP = 41045;
    public static final int NAT_CMD_FORMAT_TF_CARD = 40991;
    public static final int NAT_CMD_FORMAT_TF_CARD_RESP = 40992;
    public static final int NAT_CMD_GET_CONFIG = 41012;
    public static final int NAT_CMD_GET_CONFIG_RESP = 41013;
    static final int NAT_CMD_GET_FILE_LIST = 160;
    static final int NAT_CMD_GET_FILE_LIST2 = 37;
    static final int NAT_CMD_GET_FILE_LIST3 = 40997;
    static final int NAT_CMD_GET_FILE_LIST_RESP = 40998;
    public static final int NAT_CMD_NET_CONNECT_HOT = 40886;
    public static final int NAT_CMD_NET_CONNECT_HOT_RESP = 40887;
    static final int NAT_CMD_NET_CONNECT_STATE = 160;
    static final int NAT_CMD_NET_CONNECT_STATE2 = 62;
    public static final int NAT_CMD_NET_CONNECT_STATE3 = 41022;
    public static final int NAT_CMD_NET_CONNECT_STATE_RESP = 41023;
    public static final int NAT_CMD_NET_CONNECT_STOP = 40888;
    public static final int NAT_CMD_NET_CONNECT_STOP_RESP = 40889;
    static final int NAT_CMD_RECORD_ON_OFF = 160;
    static final int NAT_CMD_RECORD_ON_OFF2 = 58;
    static final int NAT_CMD_RECORD_ON_OFF3 = 41018;
    public static final int NAT_CMD_SET_CONTRAST = 40870;
    public static final int NAT_CMD_SET_CONTRAST_RESP = 40871;
    public static final int NAT_CMD_SET_CRASH_EVENT_ALARM = 40884;
    public static final int NAT_CMD_SET_CRASH_EVENT_ALARM_RESP = 40885;
    public static final int NAT_CMD_SET_EXPOSURE = 40969;
    public static final int NAT_CMD_SET_EXPOSURE_RESP = 40970;
    public static final int NAT_CMD_SET_LCD_BRIGHTNESS = 40868;
    public static final int NAT_CMD_SET_LCD_BRIGHTNESS_RESP = 40869;
    public static final int NAT_CMD_SET_MUTE = 40973;
    public static final int NAT_CMD_SET_MUTE_RESP = 40974;
    public static final int NAT_CMD_SET_NORMAL_IMPACT_SENSITIVITY = 40864;
    public static final int NAT_CMD_SET_NORMAL_IMPACT_SENSITIVITY_RESP = 40865;
    public static final int NAT_CMD_SET_PARKING_LOW_POWER = 40874;
    public static final int NAT_CMD_SET_PARKING_LOW_POWER_RESP = 40875;
    public static final int NAT_CMD_SET_PARKING_OPTIME = 40876;
    public static final int NAT_CMD_SET_PARKING_OPTIME_RESP = 40877;
    public static final int NAT_CMD_SET_PARK_IMPACT_SENSITIVITY = 40985;
    public static final int NAT_CMD_SET_PARK_IMPACT_SENSITIVITY_RESP = 40986;
    public static final int NAT_CMD_SET_PARTITION = 40882;
    public static final int NAT_CMD_SET_PARTITION_RESP = 40883;
    public static final int NAT_CMD_SET_PHOTO = 159;
    public static final int NAT_CMD_SET_PHOTO2 = 40904;
    public static final int NAT_CMD_SET_PHOTO_RESP = 40905;
    public static final int NAT_CMD_SET_RECORDER_QUALITY = 40961;
    public static final int NAT_CMD_SET_RECORDER_QUALITY_RESP = 40962;
    public static final int NAT_CMD_SET_SWITCH_SCREEN = 40872;
    public static final int NAT_CMD_SET_SWITCH_SCREEN_RESP = 40873;
    public static final int NAT_CMD_SET_TIME = 40977;
    public static final int NAT_CMD_SET_TIMEZONE = 40878;
    public static final int NAT_CMD_SET_TIMEZONE_RESP = 40879;
    public static final int NAT_CMD_SET_TIME_RESP = 40978;
    public static final int NAT_CMD_SET_USE_LED = 40892;
    public static final int NAT_CMD_SET_USE_LED_RESP = 40893;
    public static final int NAT_CMD_SET_VOICE_GUIDE = 40880;
    public static final int NAT_CMD_SET_VOICE_GUIDE_RESP = 40881;
    public static final int NAT_CMD_SET_VOICE_VOLUME = 40866;
    public static final int NAT_CMD_SET_VOICE_VOLUME_RESP = 40867;
    public static final int NAT_CMD_SET_WHITE_BALANCE = 40967;
    public static final int NAT_CMD_SET_WHITE_BALANCE_RESP = 40968;
    public static final int NAT_CMD_SET_WIFI_PWD = 41009;
    public static final String PCM_FILE = ".pcm";
    public static int VER_CHECK = 0;
    public static String WIFISSID = "WDVR";
    public static String WIFI_SSID = "";
    static InetAddress serverAddr;
    public static Boolean OnStopFlag = true;
    public static String SERVERIP = "192.168.100.1";
    public static int serverPort = 7777;
    public static int AUDIOPORT = 7779;
    public static int FILEDOWNPORT = 7778;
    public static int targetActivity = 2;
    public static Socket mSock = null;
    public static InputStream m_in_stream = null;
    public static BufferedOutputStream m_out_stream = null;
    static byte[] readdata = new byte[8192];
    static byte[] readsize = new byte[8192];
    static byte[] read_audio = new byte[2048];
    static InputStream file_m_in_stream = null;
    static BufferedOutputStream file_m_out_stream = null;
    static byte[] file_readdata = new byte[8192];
    static byte[] file_readsize = new byte[8192];

    public static int sendData(int i, byte[] bArr, byte[] bArr2, int i2) {
        if (i2 >= 0) {
            targetActivity = i2;
        }
        if (m_out_stream == null) {
            return -1;
        }
        byte[] bArr3 = new byte[1024];
        bArr3[0] = HEAD_PHONE;
        byte[] intToByteArray_Big = Utils.intToByteArray_Big(i);
        bArr3[1] = intToByteArray_Big[2];
        bArr3[2] = intToByteArray_Big[3];
        System.arraycopy(Utils.intToByteArray_Little(bArr != null ? bArr.length : 0), 0, bArr3, 3, 4);
        int i3 = 7;
        if (bArr != null) {
            System.arraycopy(bArr2, 0, bArr3, 7, bArr2.length);
            i3 = 7 + bArr.length;
        }
        bArr3[i3] = EXT;
        byte makeCRC = Utils.makeCRC(bArr3, i3);
        int i4 = i3 + 1;
        int i5 = i4 + 1;
        bArr3[i4] = makeCRC;
        String str = "";
        for (int i6 = 0; i6 < i5; i6++) {
            str = str + String.format("0x%02X ", Byte.valueOf(bArr3[i6]));
        }
        try {
            m_out_stream.write(bArr3, 0, i5);
            m_out_stream.flush();
            return 0;
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static int send(int i, byte[] bArr, int i2) {
        if (i2 >= 0) {
            targetActivity = i2;
        }
        if (m_out_stream == null) {
            Log.d("mhhan", "output stream is null!!!");
            return -1;
        }
        byte[] bArr2 = new byte[1024];
        bArr2[0] = HEAD_PHONE;
        byte[] intToByteArray_Big = Utils.intToByteArray_Big(i);
        bArr2[1] = intToByteArray_Big[2];
        bArr2[2] = intToByteArray_Big[3];
        System.arraycopy(Utils.intToByteArray_Little(bArr != null ? bArr.length : 0), 0, bArr2, 3, 4);
        int i3 = 7;
        if (bArr != null) {
            System.arraycopy(bArr, 0, bArr2, 7, bArr.length);
            i3 = 7 + bArr.length;
        }
        bArr2[i3] = EXT;
        byte makeCRC = Utils.makeCRC(bArr2, i3);
        int i4 = i3 + 1;
        int i5 = i4 + 1;
        bArr2[i4] = makeCRC;
        String str = "";
        for (int i6 = 0; i6 < i5; i6++) {
            str = str + String.format("0x%02X ", Byte.valueOf(bArr2[i6]));
        }
        Log.d("mhhan", "send packet...  " + str);
        try {
            m_out_stream.write(bArr2, 0, i5);
            m_out_stream.flush();
            return 0;
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static class CNSResult {
        int result;

        public int getResult() {
            return this.result;
        }

        public CNSResult() {
            this.result = -1;
        }

        public CNSResult(byte[] bArr) {
            this.result = -1;
            this.result = Utils.byteArrayToInt_Little(bArr);
        }
    }

    public static class CNSResultFileStart {
        int duration;
        int fileSize;

        public int getFileSize() {
            return this.fileSize;
        }

        public int getDuration() {
            return this.duration;
        }

        public CNSResultFileStart() {
            this.fileSize = -1;
            this.duration = -1;
        }

        public CNSResultFileStart(byte[] bArr) {
            this.fileSize = -1;
            this.duration = -1;
            if (bArr.length < 8) {
                return;
            }
            this.fileSize = Utils.byteArrayToInt_Little(bArr, 0);
            this.duration = Utils.byteArrayToInt_Little(bArr, 4);
        }
    }

    public static class CNSSimpleIndex {
        int index;

        public CNSSimpleIndex() {
            this.index = -1;
        }

        public CNSSimpleIndex(int i) {
            this.index = -1;
            this.index = i;
        }

        public void setIndex(int i) {
            this.index = i;
        }

        public byte[] getBytes() {
            return Utils.intToByteArray_Little(this.index);
        }
    }

    public static class CNSWiFiSetting {
        byte[] ssid = new byte[64];
        byte[] oldPwd = new byte[64];
        byte[] newPwd = new byte[64];

        public int setNewPwd(byte[] bArr) {
            Log.d("mhhan", "#### " + getClass().getName() + "::setNewPwd pw bytes length = " + bArr.length + " ####");
            byte[] bArr2 = this.newPwd;
            if (bArr2.length < bArr.length) {
                return -1;
            }
            System.arraycopy(bArr, 0, bArr2, 0, bArr.length);
            return 0;
        }

        public byte[] getBytes() {
            byte[] bArr = this.ssid;
            byte[] bArr2 = new byte[bArr.length + this.oldPwd.length + this.newPwd.length];
            System.arraycopy(bArr, 0, bArr2, 0, bArr.length);
            int length = this.ssid.length + 0;
            byte[] bArr3 = this.oldPwd;
            System.arraycopy(bArr3, 0, bArr2, length, bArr3.length);
            int length2 = length + this.oldPwd.length;
            byte[] bArr4 = this.newPwd;
            System.arraycopy(bArr4, 0, bArr2, length2, bArr4.length);
            int length3 = this.newPwd.length;
            return bArr2;
        }
    }

    public static class CNSTime {
        private int sec = 0;
        private int min = 0;
        private int hour = 0;
        private int mday = 0;
        private int mon = 0;
        private int year = 0;
        private int wday = 0;
        private int yday = 0;
        private int isdst = 0;

        public void setYear(int i) {
            this.year = i;
        }

        public void setMonth(int i) {
            this.mon = i;
        }

        public void setDay(int i) {
            this.mday = i;
        }

        public void setHour(int i) {
            this.hour = i;
        }

        public void setMinute(int i) {
            this.min = i;
        }

        public void setSecond(int i) {
            this.sec = i;
        }

        public byte[] getBytes() {
            byte[] bArr = new byte[36];
            byte[] intToByteArray_Little = Utils.intToByteArray_Little(this.sec);
            System.arraycopy(intToByteArray_Little, 0, bArr, 0, intToByteArray_Little.length);
            int length = intToByteArray_Little.length + 0;
            byte[] intToByteArray_Little2 = Utils.intToByteArray_Little(this.min);
            System.arraycopy(intToByteArray_Little2, 0, bArr, length, intToByteArray_Little2.length);
            int length2 = length + intToByteArray_Little2.length;
            byte[] intToByteArray_Little3 = Utils.intToByteArray_Little(this.hour);
            System.arraycopy(intToByteArray_Little3, 0, bArr, length2, intToByteArray_Little3.length);
            int length3 = length2 + intToByteArray_Little3.length;
            byte[] intToByteArray_Little4 = Utils.intToByteArray_Little(this.mday);
            System.arraycopy(intToByteArray_Little4, 0, bArr, length3, intToByteArray_Little4.length);
            int length4 = length3 + intToByteArray_Little4.length;
            byte[] intToByteArray_Little5 = Utils.intToByteArray_Little(this.mon);
            System.arraycopy(intToByteArray_Little5, 0, bArr, length4, intToByteArray_Little5.length);
            int length5 = length4 + intToByteArray_Little5.length;
            byte[] intToByteArray_Little6 = Utils.intToByteArray_Little(this.year);
            System.arraycopy(intToByteArray_Little6, 0, bArr, length5, intToByteArray_Little6.length);
            int length6 = length5 + intToByteArray_Little6.length;
            byte[] intToByteArray_Little7 = Utils.intToByteArray_Little(this.wday);
            System.arraycopy(intToByteArray_Little7, 0, bArr, length6, intToByteArray_Little7.length);
            int length7 = length6 + intToByteArray_Little7.length;
            byte[] intToByteArray_Little8 = Utils.intToByteArray_Little(this.yday);
            System.arraycopy(intToByteArray_Little8, 0, bArr, length7, intToByteArray_Little8.length);
            int length8 = length7 + intToByteArray_Little8.length;
            byte[] intToByteArray_Little9 = Utils.intToByteArray_Little(this.isdst);
            System.arraycopy(intToByteArray_Little9, 0, bArr, length8, intToByteArray_Little9.length);
            int length9 = intToByteArray_Little9.length;
            return bArr;
        }
    }
}
