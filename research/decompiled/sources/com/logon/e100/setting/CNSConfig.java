package com.logon.e100.setting;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import com.logon.e100.utils.Utils;
import com.logon_e100.MainProtocol;

/* loaded from: classes.dex */
public class CNSConfig implements Parcelable {
    public static final Parcelable.Creator<CNSConfig> CREATOR = new Parcelable.Creator<CNSConfig>() { // from class: com.logon.e100.setting.CNSConfig.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public CNSConfig createFromParcel(Parcel parcel) {
            return new CNSConfig(parcel);
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public CNSConfig[] newArray(int i) {
            return new CNSConfig[i];
        }
    };
    byte[] bytBuffer;
    byte[] bytProductName;
    byte[] bytProductSerial;
    byte[] bytWiFiPassword;
    byte[] byteProductFWVersion;
    int nCRC;
    int nContrast;
    int nCrashEventAlarm;
    int nExposure;
    int nLCDBrightness;
    int nMute;
    int nNormalImpactSensitivity;
    int nParkImpactSensitivity;
    int nParkingLowPower;
    int nParkingOPTime;
    int nPartition;
    int nRearMirror;
    int nRecordQuality;
    int nSwitchScreen;
    int nTimeZone;
    int nVelocity;
    int nVoiceGuide;
    int nVoiceVolume;
    int nWhiteBalance;
    int uAdas;
    int uBitrate;
    int uLanguage;
    int uLed;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public CNSConfig() {
        this.nRecordQuality = 0;
        this.nRearMirror = 1;
        this.nNormalImpactSensitivity = 5;
        this.nVoiceVolume = 2;
        this.nMute = 1;
        this.nLCDBrightness = 5;
        this.nWhiteBalance = 0;
        this.nContrast = 0;
        this.nExposure = 3;
        this.nSwitchScreen = 2;
        this.nTimeZone = 2;
        this.nCrashEventAlarm = 0;
        this.nParkingLowPower = 1;
        this.nParkImpactSensitivity = 5;
        this.nParkingOPTime = 0;
        this.nPartition = 2;
        this.nVoiceGuide = 0;
        this.bytProductName = new byte[16];
        this.bytProductSerial = new byte[16];
        this.byteProductFWVersion = new byte[20];
        this.nVelocity = 0;
        this.bytWiFiPassword = new byte[16];
        this.bytBuffer = new byte[112];
        this.nCRC = 0;
        this.uLanguage = 0;
        this.uBitrate = 0;
        this.uLed = 0;
        this.uAdas = 0;
    }

    public boolean getMute() {
        return this.nMute != 0;
    }

    public void setBytes(byte[] bArr) {
        Log.d("mhhan", "start parsing... length = " + bArr.length);
        this.nRecordQuality = Utils.byteArrayToInt_Little(bArr, 0);
        this.nRearMirror = Utils.byteArrayToInt_Little(bArr, 4);
        this.nNormalImpactSensitivity = Utils.byteArrayToInt_Little(bArr, 8);
        this.nVoiceVolume = Utils.byteArrayToInt_Little(bArr, 12);
        this.nMute = Utils.byteArrayToInt_Little(bArr, 16);
        this.nLCDBrightness = Utils.byteArrayToInt_Little(bArr, 20);
        this.nWhiteBalance = Utils.byteArrayToInt_Little(bArr, 24);
        this.nContrast = Utils.byteArrayToInt_Little(bArr, 28);
        this.nExposure = Utils.byteArrayToInt_Little(bArr, 32);
        this.nSwitchScreen = Utils.byteArrayToInt_Little(bArr, 36);
        this.nTimeZone = Utils.byteArrayToInt_Little(bArr, 40);
        this.nCrashEventAlarm = Utils.byteArrayToInt_Little(bArr, 44);
        this.nParkingLowPower = Utils.byteArrayToInt_Little(bArr, 48);
        this.nParkImpactSensitivity = Utils.byteArrayToInt_Little(bArr, 52);
        this.nParkingOPTime = Utils.byteArrayToInt_Little(bArr, 56);
        this.nPartition = Utils.byteArrayToInt_Little(bArr, 60);
        this.nVoiceGuide = Utils.byteArrayToInt_Little(bArr, 64);
        System.arraycopy(bArr, 68, this.bytProductName, 0, 16);
        System.arraycopy(bArr, 84, this.bytProductSerial, 0, 16);
        System.arraycopy(bArr, 100, this.byteProductFWVersion, 0, 20);
        this.nVelocity = Utils.byteArrayToInt_Little(bArr, 120);
        System.arraycopy(bArr, 124, this.bytWiFiPassword, 0, 16);
        this.uLanguage = Utils.byteArrayToInt_Little(bArr, 140);
        this.uBitrate = Utils.byteArrayToInt_Little(bArr, 144);
        this.uLed = Utils.byteArrayToInt_Little(bArr, 148);
        this.uAdas = Utils.byteArrayToInt_Little(bArr, 152);
        this.nCRC = Utils.byteArrayToInt_Little(bArr, 252);
        Log.d("mhhan", "end parsing... index = 256");
        displayConfigs();
    }

    public CNSConfig(Parcel parcel) {
        this.nRecordQuality = 0;
        this.nRearMirror = 1;
        this.nNormalImpactSensitivity = 5;
        this.nVoiceVolume = 2;
        this.nMute = 1;
        this.nLCDBrightness = 5;
        this.nWhiteBalance = 0;
        this.nContrast = 0;
        this.nExposure = 3;
        this.nSwitchScreen = 2;
        this.nTimeZone = 2;
        this.nCrashEventAlarm = 0;
        this.nParkingLowPower = 1;
        this.nParkImpactSensitivity = 5;
        this.nParkingOPTime = 0;
        this.nPartition = 2;
        this.nVoiceGuide = 0;
        this.bytProductName = new byte[16];
        this.bytProductSerial = new byte[16];
        this.byteProductFWVersion = new byte[20];
        this.nVelocity = 0;
        this.bytWiFiPassword = new byte[16];
        this.bytBuffer = new byte[112];
        this.nCRC = 0;
        this.uLanguage = 0;
        this.uBitrate = 0;
        this.uLed = 0;
        this.uAdas = 0;
        this.nRecordQuality = parcel.readInt();
        this.nRearMirror = parcel.readInt();
        this.nNormalImpactSensitivity = parcel.readInt();
        this.nVoiceVolume = parcel.readInt();
        this.nMute = parcel.readInt();
        this.nLCDBrightness = parcel.readInt();
        this.nWhiteBalance = parcel.readInt();
        this.nContrast = parcel.readInt();
        this.nExposure = parcel.readInt();
        this.nSwitchScreen = parcel.readInt();
        this.nTimeZone = parcel.readInt();
        this.nCrashEventAlarm = parcel.readInt();
        this.nParkingLowPower = parcel.readInt();
        this.nParkImpactSensitivity = parcel.readInt();
        this.nParkingOPTime = parcel.readInt();
        this.nPartition = parcel.readInt();
        this.nVoiceGuide = parcel.readInt();
        parcel.readByteArray(this.bytProductName);
        parcel.readByteArray(this.bytProductSerial);
        parcel.readByteArray(this.byteProductFWVersion);
        this.nVelocity = parcel.readInt();
        parcel.readByteArray(this.bytWiFiPassword);
        this.uLanguage = parcel.readInt();
        this.uBitrate = parcel.readInt();
        this.uLed = parcel.readInt();
        this.uAdas = parcel.readInt();
        parcel.readByteArray(this.bytBuffer);
        this.nCRC = parcel.readInt();
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.nRecordQuality);
        parcel.writeInt(this.nRearMirror);
        parcel.writeInt(this.nNormalImpactSensitivity);
        parcel.writeInt(this.nVoiceVolume);
        parcel.writeInt(this.nMute);
        parcel.writeInt(this.nLCDBrightness);
        parcel.writeInt(this.nWhiteBalance);
        parcel.writeInt(this.nContrast);
        parcel.writeInt(this.nExposure);
        parcel.writeInt(this.nSwitchScreen);
        parcel.writeInt(this.nTimeZone);
        parcel.writeInt(this.nCrashEventAlarm);
        parcel.writeInt(this.nParkingLowPower);
        parcel.writeInt(this.nParkImpactSensitivity);
        parcel.writeInt(this.nParkingOPTime);
        parcel.writeInt(this.nPartition);
        parcel.writeInt(this.nVoiceGuide);
        parcel.writeByteArray(this.bytProductName);
        parcel.writeByteArray(this.bytProductSerial);
        parcel.writeByteArray(this.byteProductFWVersion);
        parcel.writeInt(this.nVelocity);
        parcel.writeByteArray(this.bytWiFiPassword);
        parcel.writeInt(this.uLanguage);
        parcel.writeInt(this.uBitrate);
        parcel.writeInt(this.uLed);
        parcel.writeInt(this.uAdas);
        parcel.writeByteArray(this.bytBuffer);
        parcel.writeInt(this.nCRC);
    }

    public void update(CNSConfig cNSConfig) {
        this.nRecordQuality = cNSConfig.nRecordQuality;
        this.nRearMirror = cNSConfig.nRearMirror;
        this.nNormalImpactSensitivity = cNSConfig.nNormalImpactSensitivity;
        this.nVoiceVolume = cNSConfig.nVoiceVolume;
        this.nMute = cNSConfig.nMute;
        this.nLCDBrightness = cNSConfig.nLCDBrightness;
        this.nWhiteBalance = cNSConfig.nWhiteBalance;
        this.nContrast = cNSConfig.nContrast;
        this.nExposure = cNSConfig.nExposure;
        this.nSwitchScreen = cNSConfig.nSwitchScreen;
        this.nTimeZone = cNSConfig.nTimeZone;
        this.nCrashEventAlarm = cNSConfig.nCrashEventAlarm;
        this.nParkingLowPower = cNSConfig.nParkingLowPower;
        this.nParkImpactSensitivity = cNSConfig.nParkImpactSensitivity;
        this.nParkingOPTime = cNSConfig.nParkingOPTime;
        this.nPartition = cNSConfig.nPartition;
        this.nVoiceGuide = cNSConfig.nVoiceGuide;
        this.nVelocity = cNSConfig.nVelocity;
        this.uLanguage = cNSConfig.uLanguage;
        this.uBitrate = cNSConfig.uBitrate;
        this.uLed = cNSConfig.uLed;
        this.uAdas = cNSConfig.uAdas;
    }

    public void displayConfigs() {
        Log.i("mhhan", "******************** BlackBox Config ********************");
        Log.i("mhhan", "Record Quality = " + this.nRecordQuality);
        Log.i("mhhan", "Rear Mirror = " + this.nRearMirror);
        Log.i("mhhan", "Normal Impact Sensitivity = " + this.nNormalImpactSensitivity);
        Log.i("mhhan", "Voice Volume = " + this.nVoiceVolume);
        Log.i("mhhan", "Mute = " + this.nMute);
        Log.i("mhhan", "LCD Brightness = " + this.nLCDBrightness);
        Log.i("mhhan", "White Balance = " + this.nWhiteBalance);
        Log.i("mhhan", "Contrast = " + this.nContrast);
        Log.i("mhhan", "Exposure = " + this.nExposure);
        Log.i("mhhan", "Switch Screen(Screen Off Time) = " + this.nSwitchScreen);
        Log.i("mhhan", "Time Zone = " + this.nTimeZone);
        Log.i("mhhan", "Crash Event Alarm = " + this.nCrashEventAlarm);
        Log.i("mhhan", "Parking Low Power = " + this.nParkingLowPower);
        Log.i("mhhan", "Park Impact Sensitivity = " + this.nParkImpactSensitivity);
        Log.i("mhhan", "Parking OPTime = " + this.nParkingOPTime);
        Log.i("mhhan", "Partition = " + this.nPartition);
        Log.i("mhhan", "Voice Guide = " + this.nVoiceGuide);
        StringBuilder sb = new StringBuilder();
        sb.append("Product Name = ");
        byte[] bArr = this.bytProductName;
        sb.append(new String(bArr, 0, bArr.length));
        Log.i("mhhan", sb.toString());
        StringBuilder sb2 = new StringBuilder();
        sb2.append("Serial Numbar = ");
        byte[] bArr2 = this.bytProductSerial;
        sb2.append(new String(bArr2, 0, bArr2.length));
        Log.i("mhhan", sb2.toString());
        StringBuilder sb3 = new StringBuilder();
        sb3.append("Firmware Version = ");
        byte[] bArr3 = this.byteProductFWVersion;
        sb3.append(new String(bArr3, 0, bArr3.length));
        Log.i("mhhan", sb3.toString());
        Log.i("mhhan", "Velocity = " + this.nVelocity);
        Log.i("mhhan", "Language = " + this.uLanguage);
        Log.i("mhhan", "Bitrate = " + this.uBitrate);
        Log.i("mhhan", "Led = " + this.uLed);
        Log.i("mhhan", "Adas = " + this.uAdas);
        Log.i("mhhan", "*********************************************************");
        byte[] bArr4 = this.bytProductName;
        String str = new String(bArr4, 0, bArr4.length);
        byte[] bArr5 = this.byteProductFWVersion;
        String str2 = new String(bArr5, 0, bArr5.length);
        Log.i("mhhan", "ProductName = " + str);
        Log.i("mhhan", "txt1 = " + str2);
        if (str.contains("t8")) {
            Log.i("mhhan", "T8입니다.");
            MainProtocol.VER_CHECK = 1;
        } else {
            Log.i("mhhan", "T5아니요.");
            MainProtocol.VER_CHECK = 2;
        }
        if (str2.contains("FH")) {
            MainProtocol.FHD_CHECK = 1;
            Log.i("mhhan", "FHD");
        } else if (str2.contains("FF")) {
            MainProtocol.FHD_CHECK = 2;
            Log.i("mhhan", "버전 22222222");
        } else if (str2.contains("HH")) {
            MainProtocol.FHD_CHECK = 3;
            Log.i("mhhan", "버전 22222222");
        }
    }
}
