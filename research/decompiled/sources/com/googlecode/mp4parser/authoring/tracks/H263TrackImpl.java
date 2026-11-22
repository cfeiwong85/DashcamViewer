package com.googlecode.mp4parser.authoring.tracks;

import com.coremedia.iso.Hex;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.boxes.SampleDescriptionBox;
import com.coremedia.iso.boxes.sampleentry.VisualSampleEntry;
import com.googlecode.mp4parser.DataSource;
import com.googlecode.mp4parser.FileDataSourceImpl;
import com.googlecode.mp4parser.MultiFileDataSourceImpl;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Sample;
import com.googlecode.mp4parser.authoring.SampleImpl;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.tracks.AbstractH26XTrack;
import com.googlecode.mp4parser.boxes.mp4.ESDescriptorBox;
import com.googlecode.mp4parser.boxes.mp4.objectdescriptors.BitReaderBuffer;
import com.googlecode.mp4parser.boxes.mp4.objectdescriptors.DecoderConfigDescriptor;
import com.googlecode.mp4parser.boxes.mp4.objectdescriptors.DecoderSpecificInfo;
import com.googlecode.mp4parser.boxes.mp4.objectdescriptors.ESDescriptor;
import com.googlecode.mp4parser.boxes.mp4.objectdescriptors.SLConfigDescriptor;
import com.googlecode.mp4parser.util.CastUtils;
import com.googlecode.mp4parser.util.Mp4Arrays;
import com.googlecode.mp4parser.util.Path;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/* loaded from: classes.dex */
public class H263TrackImpl extends AbstractH26XTrack {
    private static Logger LOG = Logger.getLogger(ESDescriptor.class.getName());
    int BINARY;
    int BINARY_ONLY;
    int GRAYSCALE;
    int RECTANGULAR;
    boolean esdsComplete;
    List<ByteBuffer> esdsStuff;
    int fixed_vop_time_increment;
    List<Sample> samples;
    SampleDescriptionBox stsd;
    int vop_time_increment_resolution;

    @Override // com.googlecode.mp4parser.authoring.Track
    public String getHandler() {
        return "vide";
    }

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public H263TrackImpl(DataSource dataSource) throws IOException {
        super(dataSource, false);
        int i;
        int i2;
        char c = 0;
        this.RECTANGULAR = 0;
        this.BINARY = 1;
        int i3 = 2;
        this.BINARY_ONLY = 2;
        this.GRAYSCALE = 3;
        this.samples = new ArrayList();
        this.esdsStuff = new ArrayList();
        this.esdsComplete = false;
        this.fixed_vop_time_increment = -1;
        this.vop_time_increment_resolution = 0;
        AbstractH26XTrack.LookAhead lookAhead = new AbstractH26XTrack.LookAhead(dataSource);
        List<? extends ByteBuffer> arrayList = new ArrayList<>();
        VisualSampleEntry visualSampleEntry = new VisualSampleEntry(VisualSampleEntry.TYPE1);
        this.stsd = new SampleDescriptionBox();
        this.stsd.addBox(visualSampleEntry);
        long j = 0;
        int i4 = 0;
        long j2 = -1;
        while (true) {
            ByteBuffer findNextNal = findNextNal(lookAhead);
            if (findNextNal != null) {
                ByteBuffer duplicate = findNextNal.duplicate();
                int readUInt8 = IsoTypeReader.readUInt8(findNextNal);
                if (readUInt8 == 176) {
                    i = i4;
                } else if (readUInt8 == 181) {
                    i = i4;
                } else if (readUInt8 == 0) {
                    i = i4;
                } else if (readUInt8 == 32) {
                    i = i4;
                } else if (readUInt8 == 178) {
                    i = i4;
                } else if (readUInt8 == 179) {
                    this.esdsComplete = true;
                    int readBits = new BitReaderBuffer(findNextNal).readBits(18);
                    j = (readBits & 63) + (((readBits >>> 7) & 63) * 60) + (((readBits >>> 13) & 31) * 60 * 60);
                    this.stss.add(Integer.valueOf(this.samples.size() + 1));
                    arrayList.add(duplicate);
                    c = 0;
                    i3 = 2;
                } else if (readUInt8 == 182) {
                    BitReaderBuffer bitReaderBuffer = new BitReaderBuffer(findNextNal);
                    bitReaderBuffer.readBits(2);
                    while (bitReaderBuffer.readBool()) {
                        j++;
                    }
                    bitReaderBuffer.readBool();
                    int i5 = 0;
                    while (this.vop_time_increment_resolution >= (1 << i5)) {
                        i5++;
                    }
                    int readBits2 = bitReaderBuffer.readBits(i5);
                    int i6 = i4;
                    long j3 = (readBits2 % r7) + (this.vop_time_increment_resolution * j);
                    if (j2 != -1) {
                        this.decodingTimes = Mp4Arrays.copyOfAndAppend(this.decodingTimes, j3 - j2);
                    }
                    System.err.println("Frame increment: " + (j3 - j2) + " vop time increment: " + readBits2 + " last_sync_point: " + j + " time_code: " + j3);
                    arrayList.add(duplicate);
                    this.samples.add(createSampleObject(arrayList));
                    arrayList.clear();
                    j2 = j3;
                    i4 = i6;
                    c = 0;
                    i3 = 2;
                } else {
                    throw new RuntimeException("Got start code I don't know. Ask Sebastian via mp4parser mailing list what to do");
                }
                if (this.esdsComplete) {
                    i2 = i;
                } else {
                    this.esdsStuff.add(duplicate);
                    if (readUInt8 == 32) {
                        i2 = i;
                        parse0x20Unit(findNextNal, i2, visualSampleEntry);
                    } else {
                        i2 = i;
                        if (readUInt8 == 181) {
                            i4 = parse0x05Unit(findNextNal);
                            c = 0;
                            i3 = 2;
                        }
                    }
                }
                i4 = i2;
                c = 0;
                i3 = 2;
            } else {
                long[] jArr = this.decodingTimes;
                long[] jArr2 = new long[1];
                jArr2[c] = this.decodingTimes[this.decodingTimes.length - 1];
                this.decodingTimes = Mp4Arrays.copyOfAndAppend(jArr, jArr2);
                ESDescriptor eSDescriptor = new ESDescriptor();
                eSDescriptor.setEsId(1);
                DecoderConfigDescriptor decoderConfigDescriptor = new DecoderConfigDescriptor();
                decoderConfigDescriptor.setObjectTypeIndication(32);
                decoderConfigDescriptor.setStreamType(4);
                DecoderSpecificInfo decoderSpecificInfo = new DecoderSpecificInfo();
                Sample createSampleObject = createSampleObject(this.esdsStuff);
                byte[] bArr = new byte[CastUtils.l2i(createSampleObject.getSize())];
                createSampleObject.asByteBuffer().get(bArr);
                decoderSpecificInfo.setData(bArr);
                decoderConfigDescriptor.setDecoderSpecificInfo(decoderSpecificInfo);
                eSDescriptor.setDecoderConfigDescriptor(decoderConfigDescriptor);
                SLConfigDescriptor sLConfigDescriptor = new SLConfigDescriptor();
                sLConfigDescriptor.setPredefined(i3);
                eSDescriptor.setSlConfigDescriptor(sLConfigDescriptor);
                ESDescriptorBox eSDescriptorBox = new ESDescriptorBox();
                eSDescriptorBox.setEsDescriptor(eSDescriptor);
                visualSampleEntry.addBox(eSDescriptorBox);
                this.trackMetaData.setTimescale(this.vop_time_increment_resolution);
                return;
            }
        }
    }

    private int parse0x05Unit(ByteBuffer byteBuffer) {
        BitReaderBuffer bitReaderBuffer = new BitReaderBuffer(byteBuffer);
        if (!bitReaderBuffer.readBool()) {
            return 0;
        }
        int readBits = bitReaderBuffer.readBits(4);
        bitReaderBuffer.readBits(3);
        return readBits;
    }

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    private void parse0x20Unit(ByteBuffer byteBuffer, int i, VisualSampleEntry visualSampleEntry) {
        BitReaderBuffer bitReaderBuffer = new BitReaderBuffer(byteBuffer);
        bitReaderBuffer.readBool();
        bitReaderBuffer.readBits(8);
        if (bitReaderBuffer.readBool()) {
            i = bitReaderBuffer.readBits(4);
            bitReaderBuffer.readBits(3);
        }
        if (bitReaderBuffer.readBits(4) == 15) {
            bitReaderBuffer.readBits(8);
            bitReaderBuffer.readBits(8);
        }
        if (bitReaderBuffer.readBool()) {
            bitReaderBuffer.readBits(2);
            bitReaderBuffer.readBool();
            if (bitReaderBuffer.readBool()) {
                throw new RuntimeException("Implemented when needed");
            }
        }
        int readBits = bitReaderBuffer.readBits(2);
        if (readBits == this.GRAYSCALE && i != 1) {
            bitReaderBuffer.readBits(4);
        }
        bitReaderBuffer.readBool();
        this.vop_time_increment_resolution = bitReaderBuffer.readBits(16);
        bitReaderBuffer.readBool();
        if (bitReaderBuffer.readBool()) {
            LOG.info("Fixed Frame Rate");
            int i2 = 0;
            while (this.vop_time_increment_resolution >= (1 << i2)) {
                i2++;
            }
            this.fixed_vop_time_increment = bitReaderBuffer.readBits(i2);
        }
        if (readBits != this.BINARY_ONLY) {
            if (readBits == this.RECTANGULAR) {
                bitReaderBuffer.readBool();
                visualSampleEntry.setWidth(bitReaderBuffer.readBits(13));
                bitReaderBuffer.readBool();
                visualSampleEntry.setHeight(bitReaderBuffer.readBits(13));
                bitReaderBuffer.readBool();
                return;
            }
            return;
        }
        throw new RuntimeException("Please implmenet me");
    }

    @Override // com.googlecode.mp4parser.authoring.tracks.AbstractH26XTrack
    protected Sample createSampleObject(List<? extends ByteBuffer> list) {
        ByteBuffer wrap = ByteBuffer.wrap(new byte[]{0, 0, 1});
        ByteBuffer[] byteBufferArr = new ByteBuffer[list.size() * 2];
        for (int i = 0; i < list.size(); i++) {
            int i2 = i * 2;
            byteBufferArr[i2] = wrap;
            byteBufferArr[i2 + 1] = list.get(i);
        }
        return new SampleImpl(byteBufferArr);
    }

    @Override // com.googlecode.mp4parser.authoring.Track
    public SampleDescriptionBox getSampleDescriptionBox() {
        return this.stsd;
    }

    @Override // com.googlecode.mp4parser.authoring.Track
    public List<Sample> getSamples() {
        return this.samples;
    }

    public static void main1(String[] strArr) throws IOException {
        File[] listFiles = new File("C:\\dev\\mp4parser\\frames").listFiles();
        Arrays.sort(listFiles);
        Movie movie = new Movie();
        movie.addTrack(new H263TrackImpl(new MultiFileDataSourceImpl(listFiles)));
        new DefaultMp4Builder().build(movie).writeContainer(Channels.newChannel(new FileOutputStream("output.mp4")));
    }

    public static void main(String[] strArr) throws IOException {
        FileDataSourceImpl fileDataSourceImpl = new FileDataSourceImpl("C:\\content\\bbb.h263");
        Movie movie = new Movie();
        movie.addTrack(new H263TrackImpl(fileDataSourceImpl));
        new DefaultMp4Builder().build(movie).writeContainer(Channels.newChannel(new FileOutputStream("output.mp4")));
    }

    public static void main2(String[] strArr) throws IOException {
        ESDescriptorBox eSDescriptorBox = (ESDescriptorBox) Path.getPath(new IsoFile("C:\\content\\bbb.mp4"), "/moov[0]/trak[0]/mdia[0]/minf[0]/stbl[0]/stsd[0]/mp4v[0]/esds[0]");
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        eSDescriptorBox.getBox(Channels.newChannel(byteArrayOutputStream));
        System.err.println(Hex.encodeHex(byteArrayOutputStream.toByteArray()));
        System.err.println(eSDescriptorBox.getEsDescriptor());
        ByteArrayOutputStream byteArrayOutputStream2 = new ByteArrayOutputStream();
        eSDescriptorBox.getBox(Channels.newChannel(byteArrayOutputStream2));
        System.err.println(Hex.encodeHex(byteArrayOutputStream2.toByteArray()));
    }
}
