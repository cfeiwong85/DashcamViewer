package com.googlecode.mp4parser.authoring.tracks.h264;

import android.support.v7.widget.ActivityChooserView;
import com.coremedia.iso.boxes.CompositionTimeToSample;
import com.coremedia.iso.boxes.SampleDependencyTypeBox;
import com.coremedia.iso.boxes.SampleDescriptionBox;
import com.coremedia.iso.boxes.sampleentry.VisualSampleEntry;
import com.googlecode.mp4parser.DataSource;
import com.googlecode.mp4parser.authoring.Sample;
import com.googlecode.mp4parser.authoring.tracks.AbstractH26XTrack;
import com.googlecode.mp4parser.authoring.tracks.h264.SliceHeader;
import com.googlecode.mp4parser.h264.model.PictureParameterSet;
import com.googlecode.mp4parser.h264.model.SeqParameterSet;
import com.googlecode.mp4parser.h264.read.CAVLCReader;
import com.googlecode.mp4parser.util.Mp4Arrays;
import com.googlecode.mp4parser.util.RangeStartMap;
import com.mp4parser.iso14496.part15.AvcConfigurationBox;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/* loaded from: classes.dex */
public class H264TrackImpl extends AbstractH26XTrack {
    private static final Logger LOG = Logger.getLogger(H264TrackImpl.class.getName());
    PictureParameterSet currentPictureParameterSet;
    SeqParameterSet currentSeqParameterSet;
    private boolean determineFrameRate;
    PictureParameterSet firstPictureParameterSet;
    SeqParameterSet firstSeqParameterSet;
    int frameNrInGop;
    private int frametick;
    private int height;
    private String lang;
    int[] pictureOrderCounts;
    RangeStartMap<Integer, byte[]> pictureParameterRangeMap;
    Map<Integer, PictureParameterSet> ppsIdToPps;
    Map<Integer, byte[]> ppsIdToPpsBytes;
    int prevPicOrderCntLsb;
    int prevPicOrderCntMsb;
    SampleDescriptionBox sampleDescriptionBox;
    private List<Sample> samples;
    private SEIMessage seiMessage;
    RangeStartMap<Integer, byte[]> seqParameterRangeMap;
    Map<Integer, SeqParameterSet> spsIdToSps;
    Map<Integer, byte[]> spsIdToSpsBytes;
    private long timescale;
    private int width;

    @Override // com.googlecode.mp4parser.authoring.Track
    public String getHandler() {
        return "vide";
    }

    public H264TrackImpl(DataSource dataSource, String str, long j, int i) throws IOException {
        super(dataSource);
        this.spsIdToSpsBytes = new HashMap();
        this.spsIdToSps = new HashMap();
        this.ppsIdToPpsBytes = new HashMap();
        this.ppsIdToPps = new HashMap();
        this.firstSeqParameterSet = null;
        this.firstPictureParameterSet = null;
        this.currentSeqParameterSet = null;
        this.currentPictureParameterSet = null;
        this.seqParameterRangeMap = new RangeStartMap<>();
        this.pictureParameterRangeMap = new RangeStartMap<>();
        this.frameNrInGop = 0;
        this.pictureOrderCounts = new int[0];
        this.prevPicOrderCntLsb = 0;
        this.prevPicOrderCntMsb = 0;
        this.determineFrameRate = true;
        this.lang = "eng";
        this.lang = str;
        this.timescale = j;
        this.frametick = i;
        if (j > 0 && i > 0) {
            this.determineFrameRate = false;
        }
        parse(new AbstractH26XTrack.LookAhead(dataSource));
    }

    public H264TrackImpl(DataSource dataSource, String str) throws IOException {
        this(dataSource, str, -1L, -1);
    }

    public H264TrackImpl(DataSource dataSource) throws IOException {
        this(dataSource, "eng");
    }

    public static H264NalUnitHeader getNalUnitHeader(ByteBuffer byteBuffer) {
        H264NalUnitHeader h264NalUnitHeader = new H264NalUnitHeader();
        byte b = byteBuffer.get(0);
        h264NalUnitHeader.nal_ref_idc = (b >> 5) & 3;
        h264NalUnitHeader.nal_unit_type = b & 31;
        return h264NalUnitHeader;
    }

    private void parse(AbstractH26XTrack.LookAhead lookAhead) throws IOException {
        this.samples = new ArrayList();
        if (!readSamples(lookAhead)) {
            throw new IOException();
        }
        if (!readVariables()) {
            throw new IOException();
        }
        this.sampleDescriptionBox = new SampleDescriptionBox();
        VisualSampleEntry visualSampleEntry = new VisualSampleEntry(VisualSampleEntry.TYPE3);
        visualSampleEntry.setDataReferenceIndex(1);
        visualSampleEntry.setDepth(24);
        visualSampleEntry.setFrameCount(1);
        visualSampleEntry.setHorizresolution(72.0d);
        visualSampleEntry.setVertresolution(72.0d);
        visualSampleEntry.setWidth(this.width);
        visualSampleEntry.setHeight(this.height);
        visualSampleEntry.setCompressorname("AVC Coding");
        AvcConfigurationBox avcConfigurationBox = new AvcConfigurationBox();
        avcConfigurationBox.setSequenceParameterSets(new ArrayList(this.spsIdToSpsBytes.values()));
        avcConfigurationBox.setPictureParameterSets(new ArrayList(this.ppsIdToPpsBytes.values()));
        avcConfigurationBox.setAvcLevelIndication(this.firstSeqParameterSet.level_idc);
        avcConfigurationBox.setAvcProfileIndication(this.firstSeqParameterSet.profile_idc);
        avcConfigurationBox.setBitDepthLumaMinus8(this.firstSeqParameterSet.bit_depth_luma_minus8);
        avcConfigurationBox.setBitDepthChromaMinus8(this.firstSeqParameterSet.bit_depth_chroma_minus8);
        avcConfigurationBox.setChromaFormat(this.firstSeqParameterSet.chroma_format_idc.getId());
        avcConfigurationBox.setConfigurationVersion(1);
        avcConfigurationBox.setLengthSizeMinusOne(3);
        avcConfigurationBox.setProfileCompatibility((this.firstSeqParameterSet.constraint_set_0_flag ? 128 : 0) + (this.firstSeqParameterSet.constraint_set_1_flag ? 64 : 0) + (this.firstSeqParameterSet.constraint_set_2_flag ? 32 : 0) + (this.firstSeqParameterSet.constraint_set_3_flag ? 16 : 0) + (this.firstSeqParameterSet.constraint_set_4_flag ? 8 : 0) + ((int) (this.firstSeqParameterSet.reserved_zero_2bits & 3)));
        visualSampleEntry.addBox(avcConfigurationBox);
        this.sampleDescriptionBox.addBox(visualSampleEntry);
        this.trackMetaData.setCreationTime(new Date());
        this.trackMetaData.setModificationTime(new Date());
        this.trackMetaData.setLanguage(this.lang);
        this.trackMetaData.setTimescale(this.timescale);
        this.trackMetaData.setWidth(this.width);
        this.trackMetaData.setHeight(this.height);
    }

    @Override // com.googlecode.mp4parser.authoring.Track
    public SampleDescriptionBox getSampleDescriptionBox() {
        return this.sampleDescriptionBox;
    }

    @Override // com.googlecode.mp4parser.authoring.Track
    public List<Sample> getSamples() {
        return this.samples;
    }

    private boolean readVariables() {
        int i;
        this.width = (this.firstSeqParameterSet.pic_width_in_mbs_minus1 + 1) * 16;
        int i2 = this.firstSeqParameterSet.frame_mbs_only_flag ? 1 : 2;
        this.height = (this.firstSeqParameterSet.pic_height_in_map_units_minus1 + 1) * 16 * i2;
        if (this.firstSeqParameterSet.frame_cropping_flag) {
            if ((this.firstSeqParameterSet.residual_color_transform_flag ? 0 : this.firstSeqParameterSet.chroma_format_idc.getId()) != 0) {
                i = this.firstSeqParameterSet.chroma_format_idc.getSubWidth();
                i2 *= this.firstSeqParameterSet.chroma_format_idc.getSubHeight();
            } else {
                i = 1;
            }
            this.width -= i * (this.firstSeqParameterSet.frame_crop_left_offset + this.firstSeqParameterSet.frame_crop_right_offset);
            this.height -= i2 * (this.firstSeqParameterSet.frame_crop_top_offset + this.firstSeqParameterSet.frame_crop_bottom_offset);
        }
        return true;
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r5v2, types: [com.googlecode.mp4parser.authoring.tracks.h264.H264TrackImpl$1FirstVclNalDetector] */
    private boolean readSamples(AbstractH26XTrack.LookAhead lookAhead) throws IOException {
        ArrayList arrayList = new ArrayList();
        C1FirstVclNalDetector c1FirstVclNalDetector = 0;
        while (true) {
            ByteBuffer findNextNal = findNextNal(lookAhead);
            if (findNextNal != null) {
                H264NalUnitHeader nalUnitHeader = getNalUnitHeader(findNextNal);
                switch (nalUnitHeader.nal_unit_type) {
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                        ?? r5 = new Object(findNextNal, nalUnitHeader.nal_ref_idc, nalUnitHeader.nal_unit_type) { // from class: com.googlecode.mp4parser.authoring.tracks.h264.H264TrackImpl.1FirstVclNalDetector
                            boolean bottom_field_flag;
                            int delta_pic_order_cnt_0;
                            int delta_pic_order_cnt_1;
                            int delta_pic_order_cnt_bottom;
                            boolean field_pic_flag;
                            int frame_num;
                            boolean idrPicFlag;
                            int idr_pic_id;
                            int nal_ref_idc;
                            int pic_order_cnt_lsb;
                            int pic_order_cnt_type;
                            int pic_parameter_set_id;

                            {
                                SliceHeader sliceHeader = new SliceHeader(H264TrackImpl.cleanBuffer(H264TrackImpl.this.new ByteBufferBackedInputStream(findNextNal)), H264TrackImpl.this.spsIdToSps, H264TrackImpl.this.ppsIdToPps, r8 == 5);
                                this.frame_num = sliceHeader.frame_num;
                                this.pic_parameter_set_id = sliceHeader.pic_parameter_set_id;
                                this.field_pic_flag = sliceHeader.field_pic_flag;
                                this.bottom_field_flag = sliceHeader.bottom_field_flag;
                                this.nal_ref_idc = r7;
                                this.pic_order_cnt_type = H264TrackImpl.this.spsIdToSps.get(Integer.valueOf(H264TrackImpl.this.ppsIdToPps.get(Integer.valueOf(sliceHeader.pic_parameter_set_id)).seq_parameter_set_id)).pic_order_cnt_type;
                                this.delta_pic_order_cnt_bottom = sliceHeader.delta_pic_order_cnt_bottom;
                                this.pic_order_cnt_lsb = sliceHeader.pic_order_cnt_lsb;
                                this.delta_pic_order_cnt_0 = sliceHeader.delta_pic_order_cnt_0;
                                this.delta_pic_order_cnt_1 = sliceHeader.delta_pic_order_cnt_1;
                                this.idr_pic_id = sliceHeader.idr_pic_id;
                            }

                            boolean isFirstInNew(C1FirstVclNalDetector c1FirstVclNalDetector2) {
                                boolean z;
                                boolean z2;
                                boolean z3;
                                if (c1FirstVclNalDetector2.frame_num != this.frame_num || c1FirstVclNalDetector2.pic_parameter_set_id != this.pic_parameter_set_id || (z = c1FirstVclNalDetector2.field_pic_flag) != this.field_pic_flag) {
                                    return true;
                                }
                                if ((z && c1FirstVclNalDetector2.bottom_field_flag != this.bottom_field_flag) || c1FirstVclNalDetector2.nal_ref_idc != this.nal_ref_idc) {
                                    return true;
                                }
                                if (c1FirstVclNalDetector2.pic_order_cnt_type == 0 && this.pic_order_cnt_type == 0 && (c1FirstVclNalDetector2.pic_order_cnt_lsb != this.pic_order_cnt_lsb || c1FirstVclNalDetector2.delta_pic_order_cnt_bottom != this.delta_pic_order_cnt_bottom)) {
                                    return true;
                                }
                                if (!(c1FirstVclNalDetector2.pic_order_cnt_type == 1 && this.pic_order_cnt_type == 1 && (c1FirstVclNalDetector2.delta_pic_order_cnt_0 != this.delta_pic_order_cnt_0 || c1FirstVclNalDetector2.delta_pic_order_cnt_1 != this.delta_pic_order_cnt_1)) && (z2 = c1FirstVclNalDetector2.idrPicFlag) == (z3 = this.idrPicFlag)) {
                                    return z2 && z3 && c1FirstVclNalDetector2.idr_pic_id != this.idr_pic_id;
                                }
                                return true;
                            }
                        };
                        if (c1FirstVclNalDetector != 0 && c1FirstVclNalDetector.isFirstInNew(r5)) {
                            LOG.finer("Wrapping up cause of first vcl nal is found");
                            createSample(arrayList);
                        }
                        arrayList.add((ByteBuffer) findNextNal.rewind());
                        c1FirstVclNalDetector = r5;
                        continue;
                        break;
                    case 6:
                        if (c1FirstVclNalDetector != 0) {
                            LOG.finer("Wrapping up cause of SEI after vcl marks new sample");
                            createSample(arrayList);
                            c1FirstVclNalDetector = 0;
                        }
                        this.seiMessage = new SEIMessage(cleanBuffer(new ByteBufferBackedInputStream(findNextNal)), this.currentSeqParameterSet);
                        arrayList.add(findNextNal);
                        continue;
                    case 7:
                        if (c1FirstVclNalDetector != 0) {
                            LOG.finer("Wrapping up cause of SPS after vcl marks new sample");
                            createSample(arrayList);
                            c1FirstVclNalDetector = 0;
                        }
                        handleSPS((ByteBuffer) findNextNal.rewind());
                        continue;
                    case 8:
                        if (c1FirstVclNalDetector != 0) {
                            LOG.finer("Wrapping up cause of PPS after vcl marks new sample");
                            createSample(arrayList);
                            c1FirstVclNalDetector = 0;
                        }
                        handlePPS((ByteBuffer) findNextNal.rewind());
                        continue;
                    case 9:
                        if (c1FirstVclNalDetector != 0) {
                            LOG.finer("Wrapping up cause of AU after vcl marks new sample");
                            createSample(arrayList);
                            c1FirstVclNalDetector = 0;
                        }
                        arrayList.add(findNextNal);
                        continue;
                    case 10:
                    case 11:
                        break;
                    case 12:
                    default:
                        LOG.warning("Unknown NAL unit type: " + nalUnitHeader.nal_unit_type);
                        continue;
                    case 13:
                        throw new RuntimeException("Sequence parameter set extension is not yet handled. Needs TLC.");
                }
            }
        }
        if (arrayList.size() > 0) {
            createSample(arrayList);
        }
        calcCtts();
        this.decodingTimes = new long[this.samples.size()];
        Arrays.fill(this.decodingTimes, this.frametick);
        return true;
    }

    public void calcCtts() {
        int i = 0;
        int i2 = 0;
        int i3 = -1;
        while (i < this.pictureOrderCounts.length) {
            int i4 = 0;
            int i5 = ActivityChooserView.ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED;
            for (int max = Math.max(0, i - 128); max < Math.min(this.pictureOrderCounts.length, i + 128); max++) {
                int[] iArr = this.pictureOrderCounts;
                if (iArr[max] > i3 && iArr[max] < i5) {
                    i5 = iArr[max];
                    i4 = max;
                }
            }
            int[] iArr2 = this.pictureOrderCounts;
            int i6 = iArr2[i4];
            iArr2[i4] = i2;
            i++;
            i3 = i6;
            i2++;
        }
        for (int i7 = 0; i7 < this.pictureOrderCounts.length; i7++) {
            this.ctts.add(new CompositionTimeToSample.Entry(1, this.pictureOrderCounts[i7] - i7));
        }
        this.pictureOrderCounts = new int[0];
    }

    private void createSample(List<ByteBuffer> list) throws IOException {
        int i;
        SampleDependencyTypeBox.Entry entry = new SampleDependencyTypeBox.Entry(0);
        Iterator<ByteBuffer> it = list.iterator();
        H264NalUnitHeader h264NalUnitHeader = null;
        boolean z = false;
        while (it.hasNext()) {
            H264NalUnitHeader nalUnitHeader = getNalUnitHeader(it.next());
            switch (nalUnitHeader.nal_unit_type) {
                case 1:
                case 2:
                case 3:
                case 4:
                    h264NalUnitHeader = nalUnitHeader;
                    break;
                case 5:
                    z = true;
                    h264NalUnitHeader = nalUnitHeader;
                    break;
            }
        }
        if (h264NalUnitHeader == null) {
            LOG.warning("Sample without Slice");
            return;
        }
        if (z) {
            calcCtts();
        }
        SliceHeader sliceHeader = new SliceHeader(cleanBuffer(new ByteBufferBackedInputStream(list.get(list.size() - 1))), this.spsIdToSps, this.ppsIdToPps, z);
        if (h264NalUnitHeader.nal_ref_idc == 0) {
            entry.setSampleIsDependentOn(2);
        } else {
            entry.setSampleIsDependentOn(1);
        }
        if (sliceHeader.slice_type == SliceHeader.SliceType.I || sliceHeader.slice_type == SliceHeader.SliceType.SI) {
            entry.setSampleDependsOn(2);
        } else {
            entry.setSampleDependsOn(1);
        }
        Sample createSampleObject = createSampleObject(list);
        list.clear();
        SEIMessage sEIMessage = this.seiMessage;
        if (sEIMessage == null || sEIMessage.n_frames == 0) {
            this.frameNrInGop = 0;
        }
        if (sliceHeader.sps.pic_order_cnt_type == 0) {
            int i2 = 1 << (sliceHeader.sps.log2_max_pic_order_cnt_lsb_minus4 + 4);
            int i3 = sliceHeader.pic_order_cnt_lsb;
            int i4 = this.prevPicOrderCntLsb;
            if (i3 < i4 && i4 - i3 >= i2 / 2) {
                i = this.prevPicOrderCntMsb + i2;
            } else {
                int i5 = this.prevPicOrderCntLsb;
                if (i3 > i5 && i3 - i5 > i2 / 2) {
                    i = this.prevPicOrderCntMsb - i2;
                } else {
                    i = this.prevPicOrderCntMsb;
                }
            }
            this.pictureOrderCounts = Mp4Arrays.copyOfAndAppend(this.pictureOrderCounts, i + i3);
            this.prevPicOrderCntLsb = i3;
            this.prevPicOrderCntMsb = i;
        } else {
            if (sliceHeader.sps.pic_order_cnt_type == 1) {
                throw new RuntimeException("pic_order_cnt_type == 1 needs to be implemented");
            }
            if (sliceHeader.sps.pic_order_cnt_type == 2) {
                this.pictureOrderCounts = Mp4Arrays.copyOfAndAppend(this.pictureOrderCounts, this.samples.size());
            }
        }
        this.sdtp.add(entry);
        this.frameNrInGop++;
        this.samples.add(createSampleObject);
        if (z) {
            this.stss.add(Integer.valueOf(this.samples.size()));
        }
    }

    private int calcPoc(int i, H264NalUnitHeader h264NalUnitHeader, SliceHeader sliceHeader) {
        if (sliceHeader.sps.pic_order_cnt_type == 0) {
            return calcPOC0(h264NalUnitHeader, sliceHeader);
        }
        if (sliceHeader.sps.pic_order_cnt_type == 1) {
            return calcPOC1(i, h264NalUnitHeader, sliceHeader);
        }
        return calcPOC2(i, h264NalUnitHeader, sliceHeader);
    }

    private int calcPOC2(int i, H264NalUnitHeader h264NalUnitHeader, SliceHeader sliceHeader) {
        return h264NalUnitHeader.nal_ref_idc == 0 ? (i * 2) - 1 : i * 2;
    }

    private int calcPOC1(int i, H264NalUnitHeader h264NalUnitHeader, SliceHeader sliceHeader) {
        int i2;
        if (sliceHeader.sps.num_ref_frames_in_pic_order_cnt_cycle == 0) {
            i = 0;
        }
        if (h264NalUnitHeader.nal_ref_idc == 0 && i > 0) {
            i--;
        }
        int i3 = 0;
        for (int i4 = 0; i4 < sliceHeader.sps.num_ref_frames_in_pic_order_cnt_cycle; i4++) {
            i3 += sliceHeader.sps.offsetForRefFrame[i4];
        }
        if (i > 0) {
            int i5 = i - 1;
            int i6 = i5 / sliceHeader.sps.num_ref_frames_in_pic_order_cnt_cycle;
            int i7 = i5 % sliceHeader.sps.num_ref_frames_in_pic_order_cnt_cycle;
            i2 = i6 * i3;
            for (int i8 = 0; i8 <= i7; i8++) {
                i2 += sliceHeader.sps.offsetForRefFrame[i8];
            }
        } else {
            i2 = 0;
        }
        if (h264NalUnitHeader.nal_ref_idc == 0) {
            i2 += sliceHeader.sps.offset_for_non_ref_pic;
        }
        return i2 + sliceHeader.delta_pic_order_cnt_0;
    }

    private int calcPOC0(H264NalUnitHeader h264NalUnitHeader, SliceHeader sliceHeader) {
        int i;
        int i2 = sliceHeader.pic_order_cnt_lsb;
        int i3 = 1 << (sliceHeader.sps.log2_max_pic_order_cnt_lsb_minus4 + 4);
        int i4 = this.prevPicOrderCntLsb;
        if (i2 < i4 && i4 - i2 >= i3 / 2) {
            i = this.prevPicOrderCntMsb + i3;
        } else {
            int i5 = this.prevPicOrderCntLsb;
            if (i2 > i5 && i2 - i5 > i3 / 2) {
                i = this.prevPicOrderCntMsb - i3;
            } else {
                i = this.prevPicOrderCntMsb;
            }
        }
        if (h264NalUnitHeader.nal_ref_idc != 0) {
            this.prevPicOrderCntMsb = i;
            this.prevPicOrderCntLsb = i2;
        }
        return i + i2;
    }

    private void handlePPS(ByteBuffer byteBuffer) throws IOException {
        ByteBufferBackedInputStream byteBufferBackedInputStream = new ByteBufferBackedInputStream(byteBuffer);
        byteBufferBackedInputStream.read();
        PictureParameterSet read = PictureParameterSet.read(byteBufferBackedInputStream);
        if (this.firstPictureParameterSet == null) {
            this.firstPictureParameterSet = read;
        }
        this.currentPictureParameterSet = read;
        byte[] array = toArray((ByteBuffer) byteBuffer.rewind());
        byte[] bArr = this.ppsIdToPpsBytes.get(Integer.valueOf(read.pic_parameter_set_id));
        if (bArr != null && !Arrays.equals(bArr, array)) {
            throw new RuntimeException("OMG - I got two SPS with same ID but different settings! (AVC3 is the solution)");
        }
        if (bArr == null) {
            this.pictureParameterRangeMap.put((RangeStartMap<Integer, byte[]>) Integer.valueOf(this.samples.size()), (Integer) array);
        }
        this.ppsIdToPpsBytes.put(Integer.valueOf(read.pic_parameter_set_id), array);
        this.ppsIdToPps.put(Integer.valueOf(read.pic_parameter_set_id), read);
    }

    private void handleSPS(ByteBuffer byteBuffer) throws IOException {
        InputStream cleanBuffer = cleanBuffer(new ByteBufferBackedInputStream(byteBuffer));
        cleanBuffer.read();
        SeqParameterSet read = SeqParameterSet.read(cleanBuffer);
        if (this.firstSeqParameterSet == null) {
            this.firstSeqParameterSet = read;
            configureFramerate();
        }
        this.currentSeqParameterSet = read;
        byte[] array = toArray((ByteBuffer) byteBuffer.rewind());
        byte[] bArr = this.spsIdToSpsBytes.get(Integer.valueOf(read.seq_parameter_set_id));
        if (bArr != null && !Arrays.equals(bArr, array)) {
            throw new RuntimeException("OMG - I got two SPS with same ID but different settings!");
        }
        if (bArr != null) {
            this.seqParameterRangeMap.put((RangeStartMap<Integer, byte[]>) Integer.valueOf(this.samples.size()), (Integer) array);
        }
        this.spsIdToSpsBytes.put(Integer.valueOf(read.seq_parameter_set_id), array);
        this.spsIdToSps.put(Integer.valueOf(read.seq_parameter_set_id), read);
    }

    private void configureFramerate() {
        if (this.determineFrameRate) {
            if (this.firstSeqParameterSet.vuiParams != null) {
                this.timescale = this.firstSeqParameterSet.vuiParams.time_scale >> 1;
                this.frametick = this.firstSeqParameterSet.vuiParams.num_units_in_tick;
                if (this.timescale == 0 || this.frametick == 0) {
                    LOG.warning("vuiParams contain invalid values: time_scale: " + this.timescale + " and frame_tick: " + this.frametick + ". Setting frame rate to 25fps");
                    this.timescale = 90000L;
                    this.frametick = 3600;
                }
                if (this.timescale / this.frametick > 100) {
                    LOG.warning("Framerate is " + (this.timescale / this.frametick) + ". That is suspicious.");
                    return;
                }
                return;
            }
            LOG.warning("Can't determine frame rate. Guessing 25 fps");
            this.timescale = 90000L;
            this.frametick = 3600;
        }
    }

    public class ByteBufferBackedInputStream extends InputStream {
        private final ByteBuffer buf;

        public ByteBufferBackedInputStream(ByteBuffer byteBuffer) {
            this.buf = byteBuffer.duplicate();
        }

        @Override // java.io.InputStream
        public int read() throws IOException {
            if (this.buf.hasRemaining()) {
                return this.buf.get() & 255;
            }
            return -1;
        }

        @Override // java.io.InputStream
        public int read(byte[] bArr, int i, int i2) throws IOException {
            if (!this.buf.hasRemaining()) {
                return -1;
            }
            int min = Math.min(i2, this.buf.remaining());
            this.buf.get(bArr, i, min);
            return min;
        }
    }

    public class SEIMessage {
        boolean clock_timestamp_flag;
        int cnt_dropped_flag;
        int counting_type;
        int cpb_removal_delay;
        int ct_type;
        int discontinuity_flag;
        int dpb_removal_delay;
        int full_timestamp_flag;
        int hours_value;
        int minutes_value;
        int n_frames;
        int nuit_field_based_flag;
        int payloadSize;
        int payloadType;
        int pic_struct;
        boolean removal_delay_flag;
        int seconds_value;
        SeqParameterSet sps;
        int time_offset;
        int time_offset_length;

        public SEIMessage(InputStream inputStream, SeqParameterSet seqParameterSet) throws IOException {
            int i;
            this.payloadType = 0;
            this.payloadSize = 0;
            this.sps = seqParameterSet;
            inputStream.read();
            int available = inputStream.available();
            int i2 = 0;
            while (i2 < available) {
                this.payloadType = 0;
                this.payloadSize = 0;
                int read = inputStream.read();
                while (true) {
                    i2++;
                    if (read == 255) {
                        this.payloadType += read;
                        read = inputStream.read();
                    } else {
                        this.payloadType += read;
                        int read2 = inputStream.read();
                        int i3 = i2 + 1;
                        while (read2 == 255) {
                            this.payloadSize += read2;
                            read2 = inputStream.read();
                            i3++;
                        }
                        this.payloadSize += read2;
                        if (available - i3 < this.payloadSize) {
                            i2 = available;
                        } else if (this.payloadType == 1) {
                            if (seqParameterSet.vuiParams != null && (seqParameterSet.vuiParams.nalHRDParams != null || seqParameterSet.vuiParams.vclHRDParams != null || seqParameterSet.vuiParams.pic_struct_present_flag)) {
                                byte[] bArr = new byte[this.payloadSize];
                                inputStream.read(bArr);
                                i2 = i3 + this.payloadSize;
                                CAVLCReader cAVLCReader = new CAVLCReader(new ByteArrayInputStream(bArr));
                                if (seqParameterSet.vuiParams.nalHRDParams != null || seqParameterSet.vuiParams.vclHRDParams != null) {
                                    this.removal_delay_flag = true;
                                    this.cpb_removal_delay = cAVLCReader.readU(seqParameterSet.vuiParams.nalHRDParams.cpb_removal_delay_length_minus1 + 1, "SEI: cpb_removal_delay");
                                    this.dpb_removal_delay = cAVLCReader.readU(seqParameterSet.vuiParams.nalHRDParams.dpb_output_delay_length_minus1 + 1, "SEI: dpb_removal_delay");
                                } else {
                                    this.removal_delay_flag = false;
                                }
                                if (seqParameterSet.vuiParams.pic_struct_present_flag) {
                                    this.pic_struct = cAVLCReader.readU(4, "SEI: pic_struct");
                                    switch (this.pic_struct) {
                                        case 3:
                                        case 4:
                                        case 7:
                                            i = 2;
                                            break;
                                        case 5:
                                        case 6:
                                        case 8:
                                            i = 3;
                                            break;
                                        default:
                                            i = 1;
                                            break;
                                    }
                                    for (int i4 = 0; i4 < i; i4++) {
                                        this.clock_timestamp_flag = cAVLCReader.readBool("pic_timing SEI: clock_timestamp_flag[" + i4 + "]");
                                        if (this.clock_timestamp_flag) {
                                            this.ct_type = cAVLCReader.readU(2, "pic_timing SEI: ct_type");
                                            this.nuit_field_based_flag = cAVLCReader.readU(1, "pic_timing SEI: nuit_field_based_flag");
                                            this.counting_type = cAVLCReader.readU(5, "pic_timing SEI: counting_type");
                                            this.full_timestamp_flag = cAVLCReader.readU(1, "pic_timing SEI: full_timestamp_flag");
                                            this.discontinuity_flag = cAVLCReader.readU(1, "pic_timing SEI: discontinuity_flag");
                                            this.cnt_dropped_flag = cAVLCReader.readU(1, "pic_timing SEI: cnt_dropped_flag");
                                            this.n_frames = cAVLCReader.readU(8, "pic_timing SEI: n_frames");
                                            if (this.full_timestamp_flag == 1) {
                                                this.seconds_value = cAVLCReader.readU(6, "pic_timing SEI: seconds_value");
                                                this.minutes_value = cAVLCReader.readU(6, "pic_timing SEI: minutes_value");
                                                this.hours_value = cAVLCReader.readU(5, "pic_timing SEI: hours_value");
                                            } else if (cAVLCReader.readBool("pic_timing SEI: seconds_flag")) {
                                                this.seconds_value = cAVLCReader.readU(6, "pic_timing SEI: seconds_value");
                                                if (cAVLCReader.readBool("pic_timing SEI: minutes_flag")) {
                                                    this.minutes_value = cAVLCReader.readU(6, "pic_timing SEI: minutes_value");
                                                    if (cAVLCReader.readBool("pic_timing SEI: hours_flag")) {
                                                        this.hours_value = cAVLCReader.readU(5, "pic_timing SEI: hours_value");
                                                    }
                                                }
                                            }
                                            if (seqParameterSet.vuiParams.nalHRDParams != null) {
                                                this.time_offset_length = seqParameterSet.vuiParams.nalHRDParams.time_offset_length;
                                            } else if (seqParameterSet.vuiParams.vclHRDParams != null) {
                                                this.time_offset_length = seqParameterSet.vuiParams.vclHRDParams.time_offset_length;
                                            } else {
                                                this.time_offset_length = 24;
                                            }
                                            this.time_offset = cAVLCReader.readU(24, "pic_timing SEI: time_offset");
                                        }
                                    }
                                }
                            } else {
                                int i5 = i3;
                                for (int i6 = 0; i6 < this.payloadSize; i6++) {
                                    inputStream.read();
                                    i5++;
                                }
                                i2 = i5;
                            }
                        } else {
                            int i7 = i3;
                            for (int i8 = 0; i8 < this.payloadSize; i8++) {
                                inputStream.read();
                                i7++;
                            }
                            i2 = i7;
                        }
                        H264TrackImpl.LOG.fine(toString());
                    }
                }
            }
        }

        public String toString() {
            String str = "SEIMessage{payloadType=" + this.payloadType + ", payloadSize=" + this.payloadSize;
            if (this.payloadType == 1) {
                if (this.sps.vuiParams.nalHRDParams != null || this.sps.vuiParams.vclHRDParams != null) {
                    str = String.valueOf(str) + ", cpb_removal_delay=" + this.cpb_removal_delay + ", dpb_removal_delay=" + this.dpb_removal_delay;
                }
                if (this.sps.vuiParams.pic_struct_present_flag) {
                    str = String.valueOf(str) + ", pic_struct=" + this.pic_struct;
                    if (this.clock_timestamp_flag) {
                        str = String.valueOf(str) + ", ct_type=" + this.ct_type + ", nuit_field_based_flag=" + this.nuit_field_based_flag + ", counting_type=" + this.counting_type + ", full_timestamp_flag=" + this.full_timestamp_flag + ", discontinuity_flag=" + this.discontinuity_flag + ", cnt_dropped_flag=" + this.cnt_dropped_flag + ", n_frames=" + this.n_frames + ", seconds_value=" + this.seconds_value + ", minutes_value=" + this.minutes_value + ", hours_value=" + this.hours_value + ", time_offset_length=" + this.time_offset_length + ", time_offset=" + this.time_offset;
                    }
                }
            }
            return String.valueOf(str) + '}';
        }
    }
}
