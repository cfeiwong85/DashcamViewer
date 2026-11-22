package com.googlecode.mp4parser.authoring.tracks;

import android.support.v4.internal.view.SupportMenu;
import android.support.v4.media.session.PlaybackStateCompat;
import com.coremedia.iso.boxes.CompositionTimeToSample;
import com.coremedia.iso.boxes.SampleDependencyTypeBox;
import com.coremedia.iso.boxes.SampleDescriptionBox;
import com.coremedia.iso.boxes.sampleentry.AudioSampleEntry;
import com.googlecode.mp4parser.DataSource;
import com.googlecode.mp4parser.authoring.AbstractTrack;
import com.googlecode.mp4parser.authoring.Sample;
import com.googlecode.mp4parser.authoring.TrackMetaData;
import com.googlecode.mp4parser.boxes.DTSSpecificBox;
import java.io.EOFException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/* loaded from: classes.dex */
public class DTSTrackImpl extends AbstractTrack {
    private static final int BUFFER = 67108864;
    int bcCoreBitRate;
    int bcCoreChannelMask;
    int bcCoreMaxSampleRate;
    int bitrate;
    int channelCount;
    int channelMask;
    int codecDelayAtMaxFs;
    int coreBitRate;
    int coreChannelMask;
    int coreFramePayloadInBytes;
    int coreMaxSampleRate;
    boolean coreSubStreamPresent;
    private int dataOffset;
    private DataSource dataSource;
    DTSSpecificBox ddts;
    int extAvgBitrate;
    int extFramePayloadInBytes;
    int extPeakBitrate;
    int extSmoothBuffSize;
    boolean extensionSubStreamPresent;
    int frameSize;
    boolean isVBR;
    private String lang;
    int lbrCodingPresent;
    int lsbTrimPercent;
    int maxSampleRate;
    int numExtSubStreams;
    int numFramesTotal;
    int numSamplesOrigAudioAtMaxFs;
    SampleDescriptionBox sampleDescriptionBox;
    private long[] sampleDurations;
    int sampleSize;
    int samplerate;
    private List<Sample> samples;
    int samplesPerFrame;
    int samplesPerFrameAtMaxFs;
    TrackMetaData trackMetaData;
    String type;

    @Override // com.googlecode.mp4parser.authoring.AbstractTrack, com.googlecode.mp4parser.authoring.Track
    public List<CompositionTimeToSample.Entry> getCompositionTimeEntries() {
        return null;
    }

    @Override // com.googlecode.mp4parser.authoring.Track
    public String getHandler() {
        return "soun";
    }

    @Override // com.googlecode.mp4parser.authoring.AbstractTrack, com.googlecode.mp4parser.authoring.Track
    public List<SampleDependencyTypeBox.Entry> getSampleDependencies() {
        return null;
    }

    @Override // com.googlecode.mp4parser.authoring.AbstractTrack, com.googlecode.mp4parser.authoring.Track
    public long[] getSyncSamples() {
        return null;
    }

    public DTSTrackImpl(DataSource dataSource, String str) throws IOException {
        super(dataSource.toString());
        this.trackMetaData = new TrackMetaData();
        this.frameSize = 0;
        this.dataOffset = 0;
        this.ddts = new DTSSpecificBox();
        this.isVBR = false;
        this.coreSubStreamPresent = false;
        this.extensionSubStreamPresent = false;
        this.numExtSubStreams = 0;
        this.coreMaxSampleRate = 0;
        this.coreBitRate = 0;
        this.coreChannelMask = 0;
        this.coreFramePayloadInBytes = 0;
        this.extAvgBitrate = 0;
        this.extPeakBitrate = 0;
        this.extSmoothBuffSize = 0;
        this.extFramePayloadInBytes = 0;
        this.maxSampleRate = 0;
        this.lbrCodingPresent = 0;
        this.numFramesTotal = 0;
        this.samplesPerFrameAtMaxFs = 0;
        this.numSamplesOrigAudioAtMaxFs = 0;
        this.channelMask = 0;
        this.codecDelayAtMaxFs = 0;
        this.bcCoreMaxSampleRate = 0;
        this.bcCoreBitRate = 0;
        this.bcCoreChannelMask = 0;
        this.lsbTrimPercent = 0;
        this.type = "none";
        this.lang = "eng";
        this.lang = str;
        this.dataSource = dataSource;
        parse();
    }

    public DTSTrackImpl(DataSource dataSource) throws IOException {
        super(dataSource.toString());
        this.trackMetaData = new TrackMetaData();
        this.frameSize = 0;
        this.dataOffset = 0;
        this.ddts = new DTSSpecificBox();
        this.isVBR = false;
        this.coreSubStreamPresent = false;
        this.extensionSubStreamPresent = false;
        this.numExtSubStreams = 0;
        this.coreMaxSampleRate = 0;
        this.coreBitRate = 0;
        this.coreChannelMask = 0;
        this.coreFramePayloadInBytes = 0;
        this.extAvgBitrate = 0;
        this.extPeakBitrate = 0;
        this.extSmoothBuffSize = 0;
        this.extFramePayloadInBytes = 0;
        this.maxSampleRate = 0;
        this.lbrCodingPresent = 0;
        this.numFramesTotal = 0;
        this.samplesPerFrameAtMaxFs = 0;
        this.numSamplesOrigAudioAtMaxFs = 0;
        this.channelMask = 0;
        this.codecDelayAtMaxFs = 0;
        this.bcCoreMaxSampleRate = 0;
        this.bcCoreBitRate = 0;
        this.bcCoreChannelMask = 0;
        this.lsbTrimPercent = 0;
        this.type = "none";
        this.lang = "eng";
        this.dataSource = dataSource;
        parse();
    }

    @Override // java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        this.dataSource.close();
    }

    private void parse() throws IOException {
        if (!readVariables()) {
            throw new IOException();
        }
        this.sampleDescriptionBox = new SampleDescriptionBox();
        AudioSampleEntry audioSampleEntry = new AudioSampleEntry(this.type);
        audioSampleEntry.setChannelCount(this.channelCount);
        audioSampleEntry.setSampleRate(this.samplerate);
        audioSampleEntry.setDataReferenceIndex(1);
        audioSampleEntry.setSampleSize(16);
        audioSampleEntry.addBox(this.ddts);
        this.sampleDescriptionBox.addBox(audioSampleEntry);
        this.trackMetaData.setCreationTime(new Date());
        this.trackMetaData.setModificationTime(new Date());
        this.trackMetaData.setLanguage(this.lang);
        this.trackMetaData.setTimescale(this.samplerate);
    }

    @Override // com.googlecode.mp4parser.authoring.Track
    public List<Sample> getSamples() {
        return this.samples;
    }

    @Override // com.googlecode.mp4parser.authoring.Track
    public SampleDescriptionBox getSampleDescriptionBox() {
        return this.sampleDescriptionBox;
    }

    @Override // com.googlecode.mp4parser.authoring.Track
    public long[] getSampleDurations() {
        return this.sampleDurations;
    }

    @Override // com.googlecode.mp4parser.authoring.Track
    public TrackMetaData getTrackMetaData() {
        return this.trackMetaData;
    }

    private void parseDtshdhdr(int i, ByteBuffer byteBuffer) {
        byteBuffer.getInt();
        byteBuffer.get();
        byteBuffer.getInt();
        byteBuffer.get();
        short s = byteBuffer.getShort();
        byteBuffer.get();
        this.numExtSubStreams = byteBuffer.get();
        if ((s & 1) == 1) {
            this.isVBR = true;
        }
        if ((s & 8) == 8) {
            this.coreSubStreamPresent = true;
        }
        if ((s & 16) == 16) {
            this.extensionSubStreamPresent = true;
            this.numExtSubStreams++;
        } else {
            this.numExtSubStreams = 0;
        }
        for (int i2 = 14; i2 < i; i2++) {
            byteBuffer.get();
        }
    }

    private boolean parseCoressmd(int i, ByteBuffer byteBuffer) {
        this.coreMaxSampleRate = (byteBuffer.get() << 16) | (byteBuffer.getShort() & 65535);
        this.coreBitRate = byteBuffer.getShort();
        this.coreChannelMask = byteBuffer.getShort();
        this.coreFramePayloadInBytes = byteBuffer.getInt();
        for (int i2 = 11; i2 < i; i2++) {
            byteBuffer.get();
        }
        return true;
    }

    private boolean parseAuprhdr(int i, ByteBuffer byteBuffer) {
        int i2;
        byteBuffer.get();
        short s = byteBuffer.getShort();
        this.maxSampleRate = (byteBuffer.get() << 16) | (byteBuffer.getShort() & 65535);
        this.numFramesTotal = byteBuffer.getInt();
        this.samplesPerFrameAtMaxFs = byteBuffer.getShort();
        this.numSamplesOrigAudioAtMaxFs = (byteBuffer.get() << 32) | (byteBuffer.getInt() & SupportMenu.USER_MASK);
        this.channelMask = byteBuffer.getShort();
        this.codecDelayAtMaxFs = byteBuffer.getShort();
        if ((s & 3) == 3) {
            this.bcCoreMaxSampleRate = (byteBuffer.get() << 16) | (byteBuffer.getShort() & 65535);
            this.bcCoreBitRate = byteBuffer.getShort();
            this.bcCoreChannelMask = byteBuffer.getShort();
            i2 = 28;
        } else {
            i2 = 21;
        }
        if ((s & 4) > 0) {
            this.lsbTrimPercent = byteBuffer.get();
            i2++;
        }
        if ((s & 8) > 0) {
            this.lbrCodingPresent = 1;
        }
        while (i2 < i) {
            byteBuffer.get();
            i2++;
        }
        return true;
    }

    private boolean parseExtssmd(int i, ByteBuffer byteBuffer) {
        int i2;
        this.extAvgBitrate = (byteBuffer.get() << 16) | (byteBuffer.getShort() & 65535);
        if (this.isVBR) {
            this.extPeakBitrate = (byteBuffer.get() << 16) | (byteBuffer.getShort() & 65535);
            this.extSmoothBuffSize = byteBuffer.getShort();
            i2 = 8;
        } else {
            this.extFramePayloadInBytes = byteBuffer.getInt();
            i2 = 7;
        }
        while (i2 < i) {
            byteBuffer.get();
            i2++;
        }
        return true;
    }

    /* JADX WARN: Code restructure failed: missing block: B:101:0x0346, code lost:
    
        return false;
     */
    /* JADX WARN: Code restructure failed: missing block: B:103:?, code lost:
    
        return false;
     */
    /* JADX WARN: Code restructure failed: missing block: B:105:?, code lost:
    
        return false;
     */
    /* JADX WARN: Code restructure failed: missing block: B:107:0x03c3, code lost:
    
        return false;
     */
    /* JADX WARN: Code restructure failed: missing block: B:109:0x02e4, code lost:
    
        r17 = r16;
        r16 = r23;
        r18 = r24;
        r19 = r25;
        r1 = -1;
        r2 = true;
        r5 = 0;
     */
    /* JADX WARN: Code restructure failed: missing block: B:10:0x0031, code lost:
    
        r3 = r0.getLong();
        r26.dataOffset = r0.position();
        r1 = -1;
        r2 = false;
        r8 = -1;
        r9 = -1;
        r10 = 65535;
        r11 = false;
        r12 = false;
        r13 = 0;
        r14 = false;
        r15 = false;
        r16 = false;
        r17 = 0;
        r18 = false;
        r19 = false;
     */
    /* JADX WARN: Code restructure failed: missing block: B:112:0x0050, code lost:
    
        r0 = r26.samplesPerFrame;
     */
    /* JADX WARN: Code restructure failed: missing block: B:113:0x0054, code lost:
    
        if (r0 == 512) goto L27;
     */
    /* JADX WARN: Code restructure failed: missing block: B:115:0x0058, code lost:
    
        if (r0 == 1024) goto L26;
     */
    /* JADX WARN: Code restructure failed: missing block: B:117:0x005c, code lost:
    
        if (r0 == 2048) goto L25;
     */
    /* JADX WARN: Code restructure failed: missing block: B:119:0x0060, code lost:
    
        if (r0 == 4096) goto L24;
     */
    /* JADX WARN: Code restructure failed: missing block: B:120:0x0062, code lost:
    
        r0 = -1;
     */
    /* JADX WARN: Code restructure failed: missing block: B:121:0x006b, code lost:
    
        if (r0 != r1) goto L30;
     */
    /* JADX WARN: Code restructure failed: missing block: B:122:0x006d, code lost:
    
        return r5;
     */
    /* JADX WARN: Code restructure failed: missing block: B:123:0x006e, code lost:
    
        r1 = 31;
     */
    /* JADX WARN: Code restructure failed: missing block: B:124:0x0070, code lost:
    
        if (r9 == 0) goto L35;
     */
    /* JADX WARN: Code restructure failed: missing block: B:125:0x0072, code lost:
    
        if (r9 == 2) goto L35;
     */
    /* JADX WARN: Code restructure failed: missing block: B:126:0x0074, code lost:
    
        switch(r9) {
            case 4: goto L35;
            case 5: goto L35;
            case 6: goto L35;
            case 7: goto L35;
            case 8: goto L35;
            case 9: goto L35;
            default: goto L36;
        };
     */
    /* JADX WARN: Code restructure failed: missing block: B:127:0x0079, code lost:
    
        if (r8 != 0) goto L57;
     */
    /* JADX WARN: Code restructure failed: missing block: B:129:0x007c, code lost:
    
        if (r11 != true) goto L42;
     */
    /* JADX WARN: Code restructure failed: missing block: B:12:0x004e, code lost:
    
        if (r2 == false) goto L218;
     */
    /* JADX WARN: Code restructure failed: missing block: B:130:0x007e, code lost:
    
        if (r15 != false) goto L41;
     */
    /* JADX WARN: Code restructure failed: missing block: B:131:0x0080, code lost:
    
        r9 = 17;
        r26.type = com.coremedia.iso.boxes.sampleentry.AudioSampleEntry.TYPE11;
     */
    /* JADX WARN: Code restructure failed: missing block: B:132:0x00b7, code lost:
    
        r26.samplerate = r26.maxSampleRate;
        r26.sampleSize = 24;
        r2 = r9;
     */
    /* JADX WARN: Code restructure failed: missing block: B:133:0x01dc, code lost:
    
        r26.ddts.setDTSSamplingFrequency(r26.maxSampleRate);
     */
    /* JADX WARN: Code restructure failed: missing block: B:134:0x01e6, code lost:
    
        if (r26.isVBR == false) goto L185;
     */
    /* JADX WARN: Code restructure failed: missing block: B:135:0x01e8, code lost:
    
        r26.ddts.setMaxBitRate((r26.coreBitRate + r26.extPeakBitrate) * 1000);
     */
    /* JADX WARN: Code restructure failed: missing block: B:136:0x0203, code lost:
    
        r26.ddts.setAvgBitRate((r26.coreBitRate + r26.extAvgBitrate) * 1000);
        r26.ddts.setPcmSampleDepth(r26.sampleSize);
        r26.ddts.setFrameDuration(r0);
        r26.ddts.setStreamConstruction(r2);
        r0 = r26.coreChannelMask;
     */
    /* JADX WARN: Code restructure failed: missing block: B:137:0x0225, code lost:
    
        if ((r0 & 8) > 0) goto L192;
     */
    /* JADX WARN: Code restructure failed: missing block: B:139:0x0229, code lost:
    
        if ((r0 & 4096) <= 0) goto L191;
     */
    /* JADX WARN: Code restructure failed: missing block: B:13:0x02ca, code lost:
    
        r23 = r16;
        r16 = r17;
        r24 = r18;
        r25 = r19;
        r19 = r0.position();
        r7 = r0.getInt();
     */
    /* JADX WARN: Code restructure failed: missing block: B:140:0x022c, code lost:
    
        r26.ddts.setCoreLFEPresent(r5);
     */
    /* JADX WARN: Code restructure failed: missing block: B:141:0x0238, code lost:
    
        r26.ddts.setCoreLayout(r1);
        r26.ddts.setCoreSize(r26.coreFramePayloadInBytes);
        r26.ddts.setStereoDownmix(r5);
        r26.ddts.setRepresentationType(4);
        r26.ddts.setChannelLayout(r26.channelMask);
     */
    /* JADX WARN: Code restructure failed: missing block: B:142:0x0258, code lost:
    
        if (r26.coreMaxSampleRate <= 0) goto L198;
     */
    /* JADX WARN: Code restructure failed: missing block: B:144:0x025c, code lost:
    
        if (r26.extAvgBitrate <= 0) goto L198;
     */
    /* JADX WARN: Code restructure failed: missing block: B:145:0x025e, code lost:
    
        r26.ddts.setMultiAssetFlag(1);
     */
    /* JADX WARN: Code restructure failed: missing block: B:146:0x026a, code lost:
    
        r26.ddts.setLBRDurationMod(r26.lbrCodingPresent);
        r26.ddts.setReservedBoxPresent(r5);
        r26.channelCount = r5;
        r5 = r5;
     */
    /* JADX WARN: Code restructure failed: missing block: B:148:0x027a, code lost:
    
        if (r5 < 16) goto L204;
     */
    /* JADX WARN: Code restructure failed: missing block: B:14:0x02e0, code lost:
    
        if (r7 != 2147385345) goto L346;
     */
    /* JADX WARN: Code restructure failed: missing block: B:150:0x02a2, code lost:
    
        if (((r26.channelMask >> r5) & 1) != 1) goto L216;
     */
    /* JADX WARN: Code restructure failed: missing block: B:151:0x02a4, code lost:
    
        if (r5 == 0) goto L214;
     */
    /* JADX WARN: Code restructure failed: missing block: B:153:0x02a8, code lost:
    
        if (r5 == 12) goto L215;
     */
    /* JADX WARN: Code restructure failed: missing block: B:155:0x02ac, code lost:
    
        if (r5 == 14) goto L215;
     */
    /* JADX WARN: Code restructure failed: missing block: B:156:0x02ae, code lost:
    
        switch(r5) {
            case 3: goto L215;
            case 4: goto L215;
            default: goto L212;
        };
     */
    /* JADX WARN: Code restructure failed: missing block: B:157:0x02b1, code lost:
    
        switch(r5) {
            case 7: goto L215;
            case 8: goto L215;
            default: goto L213;
        };
     */
    /* JADX WARN: Code restructure failed: missing block: B:158:0x02b4, code lost:
    
        r26.channelCount += 2;
     */
    /* JADX WARN: Code restructure failed: missing block: B:160:0x02c7, code lost:
    
        r5 = r5 + 1;
     */
    /* JADX WARN: Code restructure failed: missing block: B:161:0x02bd, code lost:
    
        r26.channelCount++;
     */
    /* JADX WARN: Code restructure failed: missing block: B:167:0x027c, code lost:
    
        r26.samples = generateSamples(r26.dataSource, r26.dataOffset, r3, r8);
        r26.sampleDurations = new long[r26.samples.size()];
        java.util.Arrays.fill(r26.sampleDurations, r26.samplesPerFrame);
     */
    /* JADX WARN: Code restructure failed: missing block: B:168:0x029c, code lost:
    
        return true;
     */
    /* JADX WARN: Code restructure failed: missing block: B:169:0x0265, code lost:
    
        r26.ddts.setMultiAssetFlag(r5);
     */
    /* JADX WARN: Code restructure failed: missing block: B:16:0x03c5, code lost:
    
        r20 = r2;
        r2 = 20;
     */
    /* JADX WARN: Code restructure failed: missing block: B:170:0x0232, code lost:
    
        r26.ddts.setCoreLFEPresent(1);
     */
    /* JADX WARN: Code restructure failed: missing block: B:171:0x01f6, code lost:
    
        r26.ddts.setMaxBitRate((r26.coreBitRate + r26.extAvgBitrate) * 1000);
     */
    /* JADX WARN: Code restructure failed: missing block: B:172:0x0087, code lost:
    
        r9 = 21;
        r26.type = com.coremedia.iso.boxes.sampleentry.AudioSampleEntry.TYPE12;
     */
    /* JADX WARN: Code restructure failed: missing block: B:173:0x008e, code lost:
    
        if (r14 != true) goto L44;
     */
    /* JADX WARN: Code restructure failed: missing block: B:174:0x0090, code lost:
    
        r9 = 18;
        r26.type = com.coremedia.iso.boxes.sampleentry.AudioSampleEntry.TYPE13;
     */
    /* JADX WARN: Code restructure failed: missing block: B:175:0x0097, code lost:
    
        if (r15 != true) goto L55;
     */
    /* JADX WARN: Code restructure failed: missing block: B:176:0x0099, code lost:
    
        r26.type = com.coremedia.iso.boxes.sampleentry.AudioSampleEntry.TYPE12;
        r9 = r16;
     */
    /* JADX WARN: Code restructure failed: missing block: B:177:0x009f, code lost:
    
        if (r9 != false) goto L49;
     */
    /* JADX WARN: Code restructure failed: missing block: B:178:0x00a1, code lost:
    
        if (r11 != false) goto L49;
     */
    /* JADX WARN: Code restructure failed: missing block: B:179:0x00a3, code lost:
    
        r9 = 19;
     */
    /* JADX WARN: Code restructure failed: missing block: B:17:0x03ce, code lost:
    
        if (r7 != 1683496997) goto L355;
     */
    /* JADX WARN: Code restructure failed: missing block: B:180:0x00a6, code lost:
    
        if (r9 != true) goto L52;
     */
    /* JADX WARN: Code restructure failed: missing block: B:181:0x00a8, code lost:
    
        if (r11 != false) goto L52;
     */
    /* JADX WARN: Code restructure failed: missing block: B:182:0x00aa, code lost:
    
        r9 = 20;
     */
    /* JADX WARN: Code restructure failed: missing block: B:183:0x00ad, code lost:
    
        if (r9 != false) goto L55;
     */
    /* JADX WARN: Code restructure failed: missing block: B:184:0x00af, code lost:
    
        if (r11 != true) goto L55;
     */
    /* JADX WARN: Code restructure failed: missing block: B:185:0x00b1, code lost:
    
        r9 = 21;
     */
    /* JADX WARN: Code restructure failed: missing block: B:186:0x00b6, code lost:
    
        r9 = 0;
     */
    /* JADX WARN: Code restructure failed: missing block: B:187:0x00c2, code lost:
    
        r9 = r16;
     */
    /* JADX WARN: Code restructure failed: missing block: B:188:0x00c5, code lost:
    
        if (r10 >= 1) goto L70;
     */
    /* JADX WARN: Code restructure failed: missing block: B:189:0x00c7, code lost:
    
        if (r13 <= 0) goto L69;
     */
    /* JADX WARN: Code restructure failed: missing block: B:190:0x00c9, code lost:
    
        r2 = r17;
     */
    /* JADX WARN: Code restructure failed: missing block: B:191:0x00cb, code lost:
    
        if (r2 == 0) goto L68;
     */
    /* JADX WARN: Code restructure failed: missing block: B:192:0x00cd, code lost:
    
        if (r2 == 2) goto L67;
     */
    /* JADX WARN: Code restructure failed: missing block: B:194:0x00d0, code lost:
    
        if (r2 == 6) goto L66;
     */
    /* JADX WARN: Code restructure failed: missing block: B:195:0x00d2, code lost:
    
        r26.type = com.coremedia.iso.boxes.sampleentry.AudioSampleEntry.TYPE12;
        r2 = 0;
     */
    /* JADX WARN: Code restructure failed: missing block: B:196:0x00d9, code lost:
    
        r26.type = com.coremedia.iso.boxes.sampleentry.AudioSampleEntry.TYPE12;
        r2 = 3;
     */
    /* JADX WARN: Code restructure failed: missing block: B:197:0x00e0, code lost:
    
        r26.type = "dtsc";
        r2 = 4;
     */
    /* JADX WARN: Code restructure failed: missing block: B:198:0x00e7, code lost:
    
        r26.type = "dtsc";
        r2 = 2;
     */
    /* JADX WARN: Code restructure failed: missing block: B:199:0x00ee, code lost:
    
        r26.type = "dtsc";
        r2 = 1;
     */
    /* JADX WARN: Code restructure failed: missing block: B:19:0x03d1, code lost:
    
        if (r8 != (-1)) goto L255;
     */
    /* JADX WARN: Code restructure failed: missing block: B:200:0x00f5, code lost:
    
        r2 = r17;
        r26.type = com.coremedia.iso.boxes.sampleentry.AudioSampleEntry.TYPE12;
     */
    /* JADX WARN: Code restructure failed: missing block: B:201:0x00fb, code lost:
    
        if (r13 != 0) goto L124;
     */
    /* JADX WARN: Code restructure failed: missing block: B:202:0x00fd, code lost:
    
        if (r15 != false) goto L82;
     */
    /* JADX WARN: Code restructure failed: missing block: B:204:0x0100, code lost:
    
        if (r9 != true) goto L82;
     */
    /* JADX WARN: Code restructure failed: missing block: B:205:0x0102, code lost:
    
        r10 = r18;
     */
    /* JADX WARN: Code restructure failed: missing block: B:206:0x0104, code lost:
    
        if (r10 != false) goto L83;
     */
    /* JADX WARN: Code restructure failed: missing block: B:207:0x0106, code lost:
    
        r12 = r19;
     */
    /* JADX WARN: Code restructure failed: missing block: B:208:0x0108, code lost:
    
        if (r12 != false) goto L84;
     */
    /* JADX WARN: Code restructure failed: missing block: B:209:0x010a, code lost:
    
        if (r11 != false) goto L84;
     */
    /* JADX WARN: Code restructure failed: missing block: B:20:0x03d3, code lost:
    
        r26.samplesPerFrame = r26.samplesPerFrameAtMaxFs;
        r8 = 0;
     */
    /* JADX WARN: Code restructure failed: missing block: B:210:0x010c, code lost:
    
        if (r14 != false) goto L84;
     */
    /* JADX WARN: Code restructure failed: missing block: B:211:0x010e, code lost:
    
        r2 = 5;
     */
    /* JADX WARN: Code restructure failed: missing block: B:212:0x0115, code lost:
    
        if (r15 != false) goto L92;
     */
    /* JADX WARN: Code restructure failed: missing block: B:213:0x0117, code lost:
    
        if (r9 != false) goto L92;
     */
    /* JADX WARN: Code restructure failed: missing block: B:214:0x0119, code lost:
    
        if (r10 != false) goto L92;
     */
    /* JADX WARN: Code restructure failed: missing block: B:215:0x011b, code lost:
    
        r2 = true;
     */
    /* JADX WARN: Code restructure failed: missing block: B:216:0x011c, code lost:
    
        if (r12 != true) goto L93;
     */
    /* JADX WARN: Code restructure failed: missing block: B:217:0x011e, code lost:
    
        if (r11 != false) goto L93;
     */
    /* JADX WARN: Code restructure failed: missing block: B:218:0x0120, code lost:
    
        if (r14 != false) goto L93;
     */
    /* JADX WARN: Code restructure failed: missing block: B:219:0x0122, code lost:
    
        r2 = 6;
     */
    /* JADX WARN: Code restructure failed: missing block: B:21:0x03d8, code lost:
    
        r7 = new com.googlecode.mp4parser.boxes.mp4.objectdescriptors.BitReaderBuffer(r0);
        r10 = 8;
        r7.readBits(8);
        r7.readBits(2);
     */
    /* JADX WARN: Code restructure failed: missing block: B:220:0x0126, code lost:
    
        if (r15 != false) goto L100;
     */
    /* JADX WARN: Code restructure failed: missing block: B:221:0x0128, code lost:
    
        if (r9 != r2) goto L100;
     */
    /* JADX WARN: Code restructure failed: missing block: B:222:0x012a, code lost:
    
        if (r10 != false) goto L100;
     */
    /* JADX WARN: Code restructure failed: missing block: B:223:0x012c, code lost:
    
        if (r12 != r2) goto L100;
     */
    /* JADX WARN: Code restructure failed: missing block: B:224:0x012e, code lost:
    
        if (r11 != false) goto L100;
     */
    /* JADX WARN: Code restructure failed: missing block: B:225:0x0130, code lost:
    
        if (r14 != false) goto L100;
     */
    /* JADX WARN: Code restructure failed: missing block: B:226:0x0132, code lost:
    
        r2 = 9;
     */
    /* JADX WARN: Code restructure failed: missing block: B:227:0x0136, code lost:
    
        if (r15 != false) goto L108;
     */
    /* JADX WARN: Code restructure failed: missing block: B:228:0x0138, code lost:
    
        if (r9 != false) goto L108;
     */
    /* JADX WARN: Code restructure failed: missing block: B:22:0x03eb, code lost:
    
        if (r7.readBits(1) != 0) goto L258;
     */
    /* JADX WARN: Code restructure failed: missing block: B:230:0x013b, code lost:
    
        if (r10 != true) goto L108;
     */
    /* JADX WARN: Code restructure failed: missing block: B:231:0x013d, code lost:
    
        if (r12 != false) goto L108;
     */
    /* JADX WARN: Code restructure failed: missing block: B:232:0x013f, code lost:
    
        if (r11 != false) goto L108;
     */
    /* JADX WARN: Code restructure failed: missing block: B:233:0x0141, code lost:
    
        if (r14 != false) goto L108;
     */
    /* JADX WARN: Code restructure failed: missing block: B:234:0x0143, code lost:
    
        r2 = 10;
     */
    /* JADX WARN: Code restructure failed: missing block: B:235:0x0147, code lost:
    
        if (r15 != false) goto L116;
     */
    /* JADX WARN: Code restructure failed: missing block: B:237:0x014a, code lost:
    
        if (r9 != true) goto L116;
     */
    /* JADX WARN: Code restructure failed: missing block: B:238:0x014c, code lost:
    
        if (r10 != true) goto L116;
     */
    /* JADX WARN: Code restructure failed: missing block: B:239:0x014e, code lost:
    
        if (r12 != false) goto L116;
     */
    /* JADX WARN: Code restructure failed: missing block: B:23:0x03ed, code lost:
    
        r2 = 16;
     */
    /* JADX WARN: Code restructure failed: missing block: B:240:0x0150, code lost:
    
        if (r11 != false) goto L116;
     */
    /* JADX WARN: Code restructure failed: missing block: B:241:0x0152, code lost:
    
        if (r14 != false) goto L116;
     */
    /* JADX WARN: Code restructure failed: missing block: B:242:0x0154, code lost:
    
        r2 = 13;
     */
    /* JADX WARN: Code restructure failed: missing block: B:243:0x0158, code lost:
    
        if (r15 != false) goto L181;
     */
    /* JADX WARN: Code restructure failed: missing block: B:244:0x015a, code lost:
    
        if (r9 != false) goto L181;
     */
    /* JADX WARN: Code restructure failed: missing block: B:245:0x015c, code lost:
    
        if (r10 != false) goto L181;
     */
    /* JADX WARN: Code restructure failed: missing block: B:246:0x015e, code lost:
    
        if (r12 != false) goto L181;
     */
    /* JADX WARN: Code restructure failed: missing block: B:248:0x0161, code lost:
    
        if (r11 != true) goto L181;
     */
    /* JADX WARN: Code restructure failed: missing block: B:249:0x0163, code lost:
    
        if (r14 != false) goto L181;
     */
    /* JADX WARN: Code restructure failed: missing block: B:24:0x03f2, code lost:
    
        r10 = r7.readBits(r10) + 1;
        r2 = r7.readBits(r2) + 1;
        r0.position(r19 + r10);
        r7 = r0.getInt();
     */
    /* JADX WARN: Code restructure failed: missing block: B:250:0x0165, code lost:
    
        r2 = 14;
     */
    /* JADX WARN: Code restructure failed: missing block: B:251:0x01db, code lost:
    
        r2 = 0;
     */
    /* JADX WARN: Code restructure failed: missing block: B:252:0x0125, code lost:
    
        r2 = true;
     */
    /* JADX WARN: Code restructure failed: missing block: B:253:0x0113, code lost:
    
        r12 = r19;
     */
    /* JADX WARN: Code restructure failed: missing block: B:254:0x0111, code lost:
    
        r10 = r18;
     */
    /* JADX WARN: Code restructure failed: missing block: B:255:0x0169, code lost:
    
        r10 = r18;
        r12 = r19;
     */
    /* JADX WARN: Code restructure failed: missing block: B:256:0x016d, code lost:
    
        if (r2 != 0) goto L134;
     */
    /* JADX WARN: Code restructure failed: missing block: B:257:0x016f, code lost:
    
        if (r15 != false) goto L134;
     */
    /* JADX WARN: Code restructure failed: missing block: B:258:0x0171, code lost:
    
        if (r9 != false) goto L134;
     */
    /* JADX WARN: Code restructure failed: missing block: B:259:0x0173, code lost:
    
        if (r10 != false) goto L134;
     */
    /* JADX WARN: Code restructure failed: missing block: B:25:0x0408, code lost:
    
        if (r7 != 1515870810) goto L264;
     */
    /* JADX WARN: Code restructure failed: missing block: B:261:0x0176, code lost:
    
        if (r12 != true) goto L134;
     */
    /* JADX WARN: Code restructure failed: missing block: B:262:0x0178, code lost:
    
        if (r11 != false) goto L134;
     */
    /* JADX WARN: Code restructure failed: missing block: B:263:0x017a, code lost:
    
        if (r14 != false) goto L134;
     */
    /* JADX WARN: Code restructure failed: missing block: B:264:0x017c, code lost:
    
        r2 = 7;
     */
    /* JADX WARN: Code restructure failed: missing block: B:266:0x0180, code lost:
    
        if (r2 != 6) goto L144;
     */
    /* JADX WARN: Code restructure failed: missing block: B:267:0x0182, code lost:
    
        if (r15 != false) goto L144;
     */
    /* JADX WARN: Code restructure failed: missing block: B:268:0x0184, code lost:
    
        if (r9 != false) goto L144;
     */
    /* JADX WARN: Code restructure failed: missing block: B:269:0x0186, code lost:
    
        if (r10 != false) goto L144;
     */
    /* JADX WARN: Code restructure failed: missing block: B:26:0x040a, code lost:
    
        if (r12 != true) goto L263;
     */
    /* JADX WARN: Code restructure failed: missing block: B:271:0x0189, code lost:
    
        if (r12 != true) goto L144;
     */
    /* JADX WARN: Code restructure failed: missing block: B:272:0x018b, code lost:
    
        if (r11 != false) goto L144;
     */
    /* JADX WARN: Code restructure failed: missing block: B:273:0x018d, code lost:
    
        if (r14 != false) goto L144;
     */
    /* JADX WARN: Code restructure failed: missing block: B:274:0x018f, code lost:
    
        r2 = 8;
     */
    /* JADX WARN: Code restructure failed: missing block: B:275:0x0192, code lost:
    
        if (r2 != 0) goto L153;
     */
    /* JADX WARN: Code restructure failed: missing block: B:276:0x0194, code lost:
    
        if (r15 != false) goto L153;
     */
    /* JADX WARN: Code restructure failed: missing block: B:277:0x0196, code lost:
    
        if (r9 != false) goto L153;
     */
    /* JADX WARN: Code restructure failed: missing block: B:279:0x0199, code lost:
    
        if (r10 != true) goto L153;
     */
    /* JADX WARN: Code restructure failed: missing block: B:27:0x040c, code lost:
    
        r20 = true;
     */
    /* JADX WARN: Code restructure failed: missing block: B:280:0x019b, code lost:
    
        if (r12 != false) goto L153;
     */
    /* JADX WARN: Code restructure failed: missing block: B:281:0x019d, code lost:
    
        if (r11 != false) goto L153;
     */
    /* JADX WARN: Code restructure failed: missing block: B:282:0x019f, code lost:
    
        if (r14 != false) goto L153;
     */
    /* JADX WARN: Code restructure failed: missing block: B:283:0x01a1, code lost:
    
        r2 = 11;
     */
    /* JADX WARN: Code restructure failed: missing block: B:285:0x01a5, code lost:
    
        if (r2 != 6) goto L163;
     */
    /* JADX WARN: Code restructure failed: missing block: B:286:0x01a7, code lost:
    
        if (r15 != false) goto L163;
     */
    /* JADX WARN: Code restructure failed: missing block: B:287:0x01a9, code lost:
    
        if (r9 != false) goto L163;
     */
    /* JADX WARN: Code restructure failed: missing block: B:289:0x01ac, code lost:
    
        if (r10 != true) goto L163;
     */
    /* JADX WARN: Code restructure failed: missing block: B:28:0x040e, code lost:
    
        r21 = r3;
        r10 = r23;
        r18 = r24;
        r3 = r25;
        r12 = true;
     */
    /* JADX WARN: Code restructure failed: missing block: B:290:0x01ae, code lost:
    
        if (r12 != false) goto L163;
     */
    /* JADX WARN: Code restructure failed: missing block: B:291:0x01b0, code lost:
    
        if (r11 != false) goto L163;
     */
    /* JADX WARN: Code restructure failed: missing block: B:292:0x01b2, code lost:
    
        if (r14 != false) goto L163;
     */
    /* JADX WARN: Code restructure failed: missing block: B:293:0x01b4, code lost:
    
        r2 = 12;
     */
    /* JADX WARN: Code restructure failed: missing block: B:294:0x01b7, code lost:
    
        if (r2 != 0) goto L172;
     */
    /* JADX WARN: Code restructure failed: missing block: B:295:0x01b9, code lost:
    
        if (r15 != false) goto L172;
     */
    /* JADX WARN: Code restructure failed: missing block: B:296:0x01bb, code lost:
    
        if (r9 != false) goto L172;
     */
    /* JADX WARN: Code restructure failed: missing block: B:297:0x01bd, code lost:
    
        if (r10 != false) goto L172;
     */
    /* JADX WARN: Code restructure failed: missing block: B:298:0x01bf, code lost:
    
        if (r12 != false) goto L172;
     */
    /* JADX WARN: Code restructure failed: missing block: B:29:0x047e, code lost:
    
        if (r20 != false) goto L300;
     */
    /* JADX WARN: Code restructure failed: missing block: B:300:0x01c2, code lost:
    
        if (r11 != true) goto L172;
     */
    /* JADX WARN: Code restructure failed: missing block: B:301:0x01c4, code lost:
    
        if (r14 != false) goto L172;
     */
    /* JADX WARN: Code restructure failed: missing block: B:302:0x01c6, code lost:
    
        r2 = 15;
     */
    /* JADX WARN: Code restructure failed: missing block: B:303:0x01c9, code lost:
    
        if (r2 != 2) goto L181;
     */
    /* JADX WARN: Code restructure failed: missing block: B:304:0x01cb, code lost:
    
        if (r15 != false) goto L181;
     */
    /* JADX WARN: Code restructure failed: missing block: B:305:0x01cd, code lost:
    
        if (r9 != false) goto L181;
     */
    /* JADX WARN: Code restructure failed: missing block: B:306:0x01cf, code lost:
    
        if (r10 != false) goto L181;
     */
    /* JADX WARN: Code restructure failed: missing block: B:307:0x01d1, code lost:
    
        if (r12 != false) goto L181;
     */
    /* JADX WARN: Code restructure failed: missing block: B:309:0x01d4, code lost:
    
        if (r11 != true) goto L181;
     */
    /* JADX WARN: Code restructure failed: missing block: B:30:0x0480, code lost:
    
        r26.frameSize += r2;
     */
    /* JADX WARN: Code restructure failed: missing block: B:310:0x01d6, code lost:
    
        if (r14 != false) goto L181;
     */
    /* JADX WARN: Code restructure failed: missing block: B:311:0x01d8, code lost:
    
        r2 = 16;
     */
    /* JADX WARN: Code restructure failed: missing block: B:312:0x0078, code lost:
    
        r1 = r9;
     */
    /* JADX WARN: Code restructure failed: missing block: B:313:0x0064, code lost:
    
        r0 = 3;
     */
    /* JADX WARN: Code restructure failed: missing block: B:314:0x0066, code lost:
    
        r0 = 2;
     */
    /* JADX WARN: Code restructure failed: missing block: B:315:0x0068, code lost:
    
        r0 = 1;
     */
    /* JADX WARN: Code restructure failed: missing block: B:316:0x006a, code lost:
    
        r0 = 0;
     */
    /* JADX WARN: Code restructure failed: missing block: B:31:0x0485, code lost:
    
        r0.position(r19 + r2);
        r19 = r3;
        r17 = r16;
        r2 = r20;
        r3 = r21;
        r1 = -1;
        r5 = 0;
        r16 = r10;
        r10 = 1;
     */
    /* JADX WARN: Code restructure failed: missing block: B:35:0x041c, code lost:
    
        if (r7 != 1191201283) goto L270;
     */
    /* JADX WARN: Code restructure failed: missing block: B:37:0x0420, code lost:
    
        if (r23 != true) goto L269;
     */
    /* JADX WARN: Code restructure failed: missing block: B:38:0x0422, code lost:
    
        r20 = true;
     */
    /* JADX WARN: Code restructure failed: missing block: B:39:0x0424, code lost:
    
        r21 = r3;
        r18 = r24;
        r3 = r25;
        r10 = true;
     */
    /* JADX WARN: Code restructure failed: missing block: B:40:0x042c, code lost:
    
        r10 = r23;
     */
    /* JADX WARN: Code restructure failed: missing block: B:41:0x0431, code lost:
    
        if (r7 != 496366178) goto L276;
     */
    /* JADX WARN: Code restructure failed: missing block: B:43:0x0435, code lost:
    
        if (r24 != true) goto L275;
     */
    /* JADX WARN: Code restructure failed: missing block: B:44:0x0437, code lost:
    
        r20 = true;
     */
    /* JADX WARN: Code restructure failed: missing block: B:45:0x0439, code lost:
    
        r21 = r3;
        r3 = r25;
        r18 = true;
     */
    /* JADX WARN: Code restructure failed: missing block: B:46:0x0440, code lost:
    
        r21 = r3;
     */
    /* JADX WARN: Code restructure failed: missing block: B:47:0x0447, code lost:
    
        if (r7 != 1700671838) goto L282;
     */
    /* JADX WARN: Code restructure failed: missing block: B:49:0x044b, code lost:
    
        if (r25 != true) goto L281;
     */
    /* JADX WARN: Code restructure failed: missing block: B:50:0x044d, code lost:
    
        r20 = true;
     */
    /* JADX WARN: Code restructure failed: missing block: B:51:0x044f, code lost:
    
        r18 = r24;
        r3 = true;
     */
    /* JADX WARN: Code restructure failed: missing block: B:52:0x0453, code lost:
    
        r3 = r25;
     */
    /* JADX WARN: Code restructure failed: missing block: B:53:0x0458, code lost:
    
        if (r7 != 176167201) goto L287;
     */
    /* JADX WARN: Code restructure failed: missing block: B:54:0x045a, code lost:
    
        if (r14 != true) goto L286;
     */
    /* JADX WARN: Code restructure failed: missing block: B:55:0x045c, code lost:
    
        r20 = true;
     */
    /* JADX WARN: Code restructure failed: missing block: B:56:0x045e, code lost:
    
        r18 = r24;
        r14 = true;
     */
    /* JADX WARN: Code restructure failed: missing block: B:58:0x0465, code lost:
    
        if (r7 != 1101174087) goto L292;
     */
    /* JADX WARN: Code restructure failed: missing block: B:59:0x0467, code lost:
    
        if (r11 != true) goto L291;
     */
    /* JADX WARN: Code restructure failed: missing block: B:60:0x0469, code lost:
    
        r20 = true;
     */
    /* JADX WARN: Code restructure failed: missing block: B:61:0x046b, code lost:
    
        r18 = r24;
        r11 = true;
     */
    /* JADX WARN: Code restructure failed: missing block: B:63:0x0472, code lost:
    
        if (r7 != 45126241) goto L297;
     */
    /* JADX WARN: Code restructure failed: missing block: B:64:0x0474, code lost:
    
        if (r15 != true) goto L296;
     */
    /* JADX WARN: Code restructure failed: missing block: B:65:0x0476, code lost:
    
        r20 = true;
     */
    /* JADX WARN: Code restructure failed: missing block: B:66:0x0478, code lost:
    
        r18 = r24;
        r15 = true;
     */
    /* JADX WARN: Code restructure failed: missing block: B:67:0x047c, code lost:
    
        r18 = r24;
     */
    /* JADX WARN: Code restructure failed: missing block: B:68:0x03f0, code lost:
    
        r10 = 12;
     */
    /* JADX WARN: Code restructure failed: missing block: B:71:0x04b0, code lost:
    
        throw new java.io.IOException("No DTS_SYNCWORD_* found at " + r0.position());
     */
    /* JADX WARN: Code restructure failed: missing block: B:74:0x02e2, code lost:
    
        if (r8 != 1) goto L347;
     */
    /* JADX WARN: Code restructure failed: missing block: B:76:0x02f1, code lost:
    
        r5 = new com.googlecode.mp4parser.boxes.mp4.objectdescriptors.BitReaderBuffer(r0);
        r7 = r5.readBits(1);
        r9 = r5.readBits(5);
        r8 = r5.readBits(1);
     */
    /* JADX WARN: Code restructure failed: missing block: B:77:0x0303, code lost:
    
        if (r7 != 1) goto L350;
     */
    /* JADX WARN: Code restructure failed: missing block: B:79:0x0307, code lost:
    
        if (r9 != 31) goto L352;
     */
    /* JADX WARN: Code restructure failed: missing block: B:80:0x0309, code lost:
    
        if (r8 == 0) goto L228;
     */
    /* JADX WARN: Code restructure failed: missing block: B:81:0x030d, code lost:
    
        r26.samplesPerFrame = (r5.readBits(7) + 1) * 32;
        r1 = r5.readBits(14);
        r26.frameSize += r1 + 1;
        r9 = r5.readBits(6);
        r26.samplerate = getSampleRate(r5.readBits(4));
        r26.bitrate = getBitRate(r5.readBits(5));
     */
    /* JADX WARN: Code restructure failed: missing block: B:82:0x0344, code lost:
    
        if (r5.readBits(1) == 0) goto L232;
     */
    /* JADX WARN: Code restructure failed: missing block: B:83:0x0348, code lost:
    
        r5.readBits(1);
        r5.readBits(1);
        r5.readBits(1);
        r5.readBits(1);
        r16 = r5.readBits(3);
        r13 = r5.readBits(1);
        r5.readBits(1);
        r20 = r2;
        r5.readBits(2);
        r5.readBits(1);
     */
    /* JADX WARN: Code restructure failed: missing block: B:84:0x0369, code lost:
    
        if (r8 != 1) goto L235;
     */
    /* JADX WARN: Code restructure failed: missing block: B:85:0x036b, code lost:
    
        r5.readBits(16);
     */
    /* JADX WARN: Code restructure failed: missing block: B:86:0x0370, code lost:
    
        r5.readBits(1);
        r8 = r5.readBits(4);
        r5.readBits(2);
     */
    /* JADX WARN: Code restructure failed: missing block: B:87:0x0380, code lost:
    
        switch(r5.readBits(3)) {
            case 0: goto L241;
            case 1: goto L241;
            case 2: goto L240;
            case 3: goto L240;
            case 4: goto L349;
            case 5: goto L239;
            case 6: goto L239;
            default: goto L349;
        };
     */
    /* JADX WARN: Code restructure failed: missing block: B:88:0x0385, code lost:
    
        r26.sampleSize = 24;
        r2 = 1;
     */
    /* JADX WARN: Code restructure failed: missing block: B:89:0x0396, code lost:
    
        r5.readBits(r2);
        r5.readBits(r2);
     */
    /* JADX WARN: Code restructure failed: missing block: B:90:0x039c, code lost:
    
        switch(r8) {
            case 6: goto L246;
            case 7: goto L245;
            default: goto L244;
        };
     */
    /* JADX WARN: Code restructure failed: missing block: B:91:0x039f, code lost:
    
        r5.readBits(4);
     */
    /* JADX WARN: Code restructure failed: missing block: B:92:0x03ad, code lost:
    
        r0.position((r19 + r1) + 1);
        r17 = r16;
        r2 = r20;
        r16 = r23;
        r18 = r24;
        r19 = r25;
        r1 = -1;
        r5 = 0;
        r8 = 1;
     */
    /* JADX WARN: Code restructure failed: missing block: B:94:0x03a4, code lost:
    
        r5.readBits(4);
     */
    /* JADX WARN: Code restructure failed: missing block: B:95:0x03a9, code lost:
    
        r5.readBits(4);
     */
    /* JADX WARN: Code restructure failed: missing block: B:96:0x038b, code lost:
    
        r26.sampleSize = 20;
        r2 = 1;
     */
    /* JADX WARN: Code restructure failed: missing block: B:97:0x0391, code lost:
    
        r26.sampleSize = 16;
        r2 = 1;
     */
    /* JADX WARN: Code restructure failed: missing block: B:99:0x0383, code lost:
    
        return false;
     */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v11, types: [com.googlecode.mp4parser.boxes.DTSSpecificBox] */
    /* JADX WARN: Type inference failed for: r0v15, types: [com.googlecode.mp4parser.boxes.DTSSpecificBox] */
    /* JADX WARN: Type inference failed for: r0v17, types: [com.googlecode.mp4parser.boxes.DTSSpecificBox] */
    /* JADX WARN: Type inference failed for: r0v33, types: [com.googlecode.mp4parser.boxes.DTSSpecificBox] */
    /* JADX WARN: Type inference failed for: r5v0 */
    /* JADX WARN: Type inference failed for: r5v10 */
    /* JADX WARN: Type inference failed for: r5v16 */
    /* JADX WARN: Type inference failed for: r5v2, types: [boolean, int] */
    /* JADX WARN: Type inference failed for: r5v7 */
    /* JADX WARN: Type inference failed for: r5v9 */
    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    private boolean readVariables() throws java.io.IOException {
        /*
            Method dump skipped, instructions count: 1356
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.googlecode.mp4parser.authoring.tracks.DTSTrackImpl.readVariables():boolean");
    }

    private List<Sample> generateSamples(DataSource dataSource, int i, long j, int i2) throws IOException {
        LookAhead lookAhead = new LookAhead(dataSource, i, j, i2);
        ArrayList arrayList = new ArrayList();
        while (true) {
            final ByteBuffer findNextStart = lookAhead.findNextStart();
            if (findNextStart != null) {
                arrayList.add(new Sample() { // from class: com.googlecode.mp4parser.authoring.tracks.DTSTrackImpl.1
                    @Override // com.googlecode.mp4parser.authoring.Sample
                    public void writeTo(WritableByteChannel writableByteChannel) throws IOException {
                        writableByteChannel.write((ByteBuffer) findNextStart.rewind());
                    }

                    @Override // com.googlecode.mp4parser.authoring.Sample
                    public long getSize() {
                        return findNextStart.rewind().remaining();
                    }

                    @Override // com.googlecode.mp4parser.authoring.Sample
                    public ByteBuffer asByteBuffer() {
                        return findNextStart;
                    }
                });
            } else {
                System.err.println("all samples found");
                return arrayList;
            }
        }
    }

    private int getBitRate(int i) throws IOException {
        switch (i) {
            case 0:
                return 32;
            case 1:
                return 56;
            case 2:
                return 64;
            case 3:
                return 96;
            case 4:
                return 112;
            case 5:
                return 128;
            case 6:
                return 192;
            case 7:
                return 224;
            case 8:
                return 256;
            case 9:
                return 320;
            case 10:
                return 384;
            case 11:
                return 448;
            case 12:
                return 512;
            case 13:
                return 576;
            case 14:
                return 640;
            case 15:
                return 768;
            case 16:
                return 960;
            case 17:
                return 1024;
            case 18:
                return 1152;
            case 19:
                return 1280;
            case 20:
                return 1344;
            case 21:
                return 1408;
            case 22:
                return 1411;
            case 23:
                return 1472;
            case 24:
                return 1536;
            case 25:
                return -1;
            default:
                throw new IOException("Unknown bitrate value");
        }
    }

    private int getSampleRate(int i) throws IOException {
        switch (i) {
            case 1:
                return 8000;
            case 2:
                return 16000;
            case 3:
                return 32000;
            case 4:
            case 5:
            case 9:
            case 10:
            default:
                throw new IOException("Unknown Sample Rate");
            case 6:
                return 11025;
            case 7:
                return 22050;
            case 8:
                return 44100;
            case 11:
                return 12000;
            case 12:
                return 24000;
            case 13:
                return 48000;
        }
    }

    class LookAhead {
        ByteBuffer buffer;
        long bufferStartPos;
        private final int corePresent;
        long dataEnd;
        DataSource dataSource;
        int inBufferPos = 0;
        long start;

        LookAhead(DataSource dataSource, long j, long j2, int i) throws IOException {
            this.dataSource = dataSource;
            this.bufferStartPos = j;
            this.dataEnd = j2 + j;
            this.corePresent = i;
            fillBuffer();
        }

        public ByteBuffer findNextStart() throws IOException {
            while (true) {
                try {
                    if (this.corePresent == 1) {
                        if (nextFourEquals0x7FFE8001()) {
                            break;
                        }
                        discardByte();
                    } else {
                        if (nextFourEquals0x64582025()) {
                            break;
                        }
                        discardByte();
                    }
                } catch (EOFException unused) {
                    return null;
                }
                return null;
            }
            discardNext4AndMarkStart();
            while (true) {
                if (this.corePresent == 1) {
                    if (nextFourEquals0x7FFE8001orEof()) {
                        break;
                    }
                    discardQWord();
                } else {
                    if (nextFourEquals0x64582025orEof()) {
                        break;
                    }
                    discardQWord();
                }
                return null;
            }
            return getSample();
        }

        private void fillBuffer() throws IOException {
            System.err.println("Fill Buffer");
            DataSource dataSource = this.dataSource;
            long j = this.bufferStartPos;
            this.buffer = dataSource.map(j, Math.min(this.dataEnd - j, 67108864L));
        }

        private boolean nextFourEquals0x64582025() throws IOException {
            return nextFourEquals((byte) 100, (byte) 88, (byte) 32, (byte) 37);
        }

        private boolean nextFourEquals0x7FFE8001() throws IOException {
            return nextFourEquals(Byte.MAX_VALUE, (byte) -2, Byte.MIN_VALUE, (byte) 1);
        }

        private boolean nextFourEquals(byte b, byte b2, byte b3, byte b4) throws IOException {
            int limit = this.buffer.limit();
            int i = this.inBufferPos;
            if (limit - i >= 4) {
                return this.buffer.get(i) == b && this.buffer.get(this.inBufferPos + 1) == b2 && this.buffer.get(this.inBufferPos + 2) == b3 && this.buffer.get(this.inBufferPos + 3) == b4;
            }
            if (this.bufferStartPos + i + 4 < this.dataSource.size()) {
                return false;
            }
            throw new EOFException();
        }

        private boolean nextFourEquals0x64582025orEof() throws IOException {
            return nextFourEqualsOrEof((byte) 100, (byte) 88, (byte) 32, (byte) 37);
        }

        private boolean nextFourEquals0x7FFE8001orEof() throws IOException {
            return nextFourEqualsOrEof(Byte.MAX_VALUE, (byte) -2, Byte.MIN_VALUE, (byte) 1);
        }

        private boolean nextFourEqualsOrEof(byte b, byte b2, byte b3, byte b4) throws IOException {
            int limit = this.buffer.limit();
            int i = this.inBufferPos;
            if (limit - i >= 4) {
                if ((this.bufferStartPos + i) % PlaybackStateCompat.ACTION_SET_CAPTIONING_ENABLED == 0) {
                    PrintStream printStream = System.err;
                    StringBuilder sb = new StringBuilder();
                    sb.append(((this.bufferStartPos + this.inBufferPos) / PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID) / PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID);
                    printStream.println(sb.toString());
                }
                return this.buffer.get(this.inBufferPos) == b && this.buffer.get(this.inBufferPos + 1) == b2 && this.buffer.get(this.inBufferPos + 2) == b3 && this.buffer.get(this.inBufferPos + 3) == b4;
            }
            long j = this.bufferStartPos;
            long j2 = i + j + 4;
            long j3 = this.dataEnd;
            if (j2 > j3) {
                return j + ((long) i) == j3;
            }
            this.bufferStartPos = this.start;
            this.inBufferPos = 0;
            fillBuffer();
            return nextFourEquals0x7FFE8001();
        }

        private void discardByte() {
            this.inBufferPos++;
        }

        private void discardQWord() {
            this.inBufferPos += 4;
        }

        private void discardNext4AndMarkStart() {
            long j = this.bufferStartPos;
            int i = this.inBufferPos;
            this.start = j + i;
            this.inBufferPos = i + 4;
        }

        private ByteBuffer getSample() {
            long j = this.start;
            long j2 = this.bufferStartPos;
            if (j >= j2) {
                this.buffer.position((int) (j - j2));
                ByteBuffer slice = this.buffer.slice();
                slice.limit((int) (this.inBufferPos - (this.start - this.bufferStartPos)));
                return slice;
            }
            throw new RuntimeException("damn! NAL exceeds buffer");
        }
    }
}
