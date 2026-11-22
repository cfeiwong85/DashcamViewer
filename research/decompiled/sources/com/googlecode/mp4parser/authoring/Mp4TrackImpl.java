package com.googlecode.mp4parser.authoring;

import android.support.v4.internal.view.SupportMenu;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.CompositionTimeToSample;
import com.coremedia.iso.boxes.Container;
import com.coremedia.iso.boxes.EditListBox;
import com.coremedia.iso.boxes.MediaHeaderBox;
import com.coremedia.iso.boxes.MovieHeaderBox;
import com.coremedia.iso.boxes.SampleDependencyTypeBox;
import com.coremedia.iso.boxes.SampleDescriptionBox;
import com.coremedia.iso.boxes.SampleTableBox;
import com.coremedia.iso.boxes.SubSampleInformationBox;
import com.coremedia.iso.boxes.TimeToSampleBox;
import com.coremedia.iso.boxes.TrackBox;
import com.coremedia.iso.boxes.TrackHeaderBox;
import com.coremedia.iso.boxes.fragment.MovieExtendsBox;
import com.coremedia.iso.boxes.fragment.MovieFragmentBox;
import com.coremedia.iso.boxes.fragment.SampleFlags;
import com.coremedia.iso.boxes.fragment.TrackExtendsBox;
import com.coremedia.iso.boxes.fragment.TrackFragmentBox;
import com.coremedia.iso.boxes.fragment.TrackFragmentHeaderBox;
import com.coremedia.iso.boxes.fragment.TrackRunBox;
import com.coremedia.iso.boxes.mdat.SampleList;
import com.googlecode.mp4parser.AbstractContainerBox;
import com.googlecode.mp4parser.BasicContainer;
import com.googlecode.mp4parser.boxes.mp4.samplegrouping.GroupEntry;
import com.googlecode.mp4parser.boxes.mp4.samplegrouping.SampleGroupDescriptionBox;
import com.googlecode.mp4parser.boxes.mp4.samplegrouping.SampleToGroupBox;
import com.googlecode.mp4parser.util.CastUtils;
import com.googlecode.mp4parser.util.Mp4Arrays;
import com.googlecode.mp4parser.util.Path;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/* loaded from: classes.dex */
public class Mp4TrackImpl extends AbstractTrack {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private List<CompositionTimeToSample.Entry> compositionTimeEntries;
    private long[] decodingTimes;
    IsoFile[] fragments;
    private String handler;
    private List<SampleDependencyTypeBox.Entry> sampleDependencies;
    private SampleDescriptionBox sampleDescriptionBox;
    private List<Sample> samples;
    private SubSampleInformationBox subSampleInformationBox;
    private long[] syncSamples;
    TrackBox trackBox;
    private TrackMetaData trackMetaData;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public Mp4TrackImpl(String str, TrackBox trackBox, IsoFile... isoFileArr) {
        super(str);
        Iterator it;
        SampleFlags defaultSampleFlags;
        int i;
        Mp4TrackImpl mp4TrackImpl = this;
        mp4TrackImpl.syncSamples = null;
        mp4TrackImpl.trackMetaData = new TrackMetaData();
        mp4TrackImpl.subSampleInformationBox = null;
        mp4TrackImpl.trackBox = trackBox;
        long trackId = trackBox.getTrackHeaderBox().getTrackId();
        mp4TrackImpl.samples = new SampleList(trackBox, isoFileArr);
        SampleTableBox sampleTableBox = trackBox.getMediaBox().getMediaInformationBox().getSampleTableBox();
        mp4TrackImpl.handler = trackBox.getMediaBox().getHandlerBox().getHandlerType();
        ArrayList arrayList = new ArrayList();
        mp4TrackImpl.compositionTimeEntries = new ArrayList();
        mp4TrackImpl.sampleDependencies = new ArrayList();
        arrayList.addAll(sampleTableBox.getTimeToSampleBox().getEntries());
        if (sampleTableBox.getCompositionTimeToSample() != null) {
            mp4TrackImpl.compositionTimeEntries.addAll(sampleTableBox.getCompositionTimeToSample().getEntries());
        }
        if (sampleTableBox.getSampleDependencyTypeBox() != null) {
            mp4TrackImpl.sampleDependencies.addAll(sampleTableBox.getSampleDependencyTypeBox().getEntries());
        }
        if (sampleTableBox.getSyncSampleBox() != null) {
            mp4TrackImpl.syncSamples = sampleTableBox.getSyncSampleBox().getSampleNumber();
        }
        mp4TrackImpl.subSampleInformationBox = (SubSampleInformationBox) Path.getPath((AbstractContainerBox) sampleTableBox, SubSampleInformationBox.TYPE);
        ArrayList arrayList2 = new ArrayList();
        arrayList2.addAll(((Box) trackBox.getParent()).getParent().getBoxes(MovieFragmentBox.class));
        int length = isoFileArr.length;
        int i2 = 0;
        while (i2 < length) {
            arrayList2.addAll(isoFileArr[i2].getBoxes(MovieFragmentBox.class));
            i2++;
            trackId = trackId;
            mp4TrackImpl = this;
        }
        mp4TrackImpl.sampleDescriptionBox = sampleTableBox.getSampleDescriptionBox();
        List boxes = trackBox.getParent().getBoxes(MovieExtendsBox.class);
        if (boxes.size() > 0) {
            Iterator it2 = boxes.iterator();
            while (it2.hasNext()) {
                for (TrackExtendsBox trackExtendsBox : ((MovieExtendsBox) it2.next()).getBoxes(TrackExtendsBox.class)) {
                    if (trackExtendsBox.getTrackId() == trackId) {
                        if (Path.getPaths(((Box) trackBox.getParent()).getParent(), "/moof/traf/subs").size() > 0) {
                            mp4TrackImpl.subSampleInformationBox = new SubSampleInformationBox();
                        }
                        Iterator it3 = arrayList2.iterator();
                        long j = 1;
                        long j2 = 1;
                        while (it3.hasNext()) {
                            long j3 = j2;
                            for (TrackFragmentBox trackFragmentBox : ((MovieFragmentBox) it3.next()).getBoxes(TrackFragmentBox.class)) {
                                if (trackFragmentBox.getTrackFragmentHeaderBox().getTrackId() == trackId) {
                                    long j4 = trackId;
                                    long j5 = j;
                                    mp4TrackImpl.sampleGroups = getSampleGroups(sampleTableBox.getBoxes(SampleGroupDescriptionBox.class), Path.getPaths((Container) trackFragmentBox, SampleGroupDescriptionBox.TYPE), Path.getPaths((Container) trackFragmentBox, SampleToGroupBox.TYPE), mp4TrackImpl.sampleGroups, j3 - j);
                                    SubSampleInformationBox subSampleInformationBox = (SubSampleInformationBox) Path.getPath((AbstractContainerBox) trackFragmentBox, SubSampleInformationBox.TYPE);
                                    if (subSampleInformationBox != null) {
                                        long j6 = (j3 - 0) - j5;
                                        for (SubSampleInformationBox.SubSampleEntry subSampleEntry : subSampleInformationBox.getEntries()) {
                                            SubSampleInformationBox.SubSampleEntry subSampleEntry2 = new SubSampleInformationBox.SubSampleEntry();
                                            subSampleEntry2.getSubsampleEntries().addAll(subSampleEntry.getSubsampleEntries());
                                            if (j6 != 0) {
                                                subSampleEntry2.setSampleDelta(j6 + subSampleEntry.getSampleDelta());
                                                j6 = 0;
                                            } else {
                                                subSampleEntry2.setSampleDelta(subSampleEntry.getSampleDelta());
                                            }
                                            mp4TrackImpl.subSampleInformationBox.getEntries().add(subSampleEntry2);
                                        }
                                    }
                                    for (TrackRunBox trackRunBox : trackFragmentBox.getBoxes(TrackRunBox.class)) {
                                        TrackFragmentHeaderBox trackFragmentHeaderBox = ((TrackFragmentBox) trackRunBox.getParent()).getTrackFragmentHeaderBox();
                                        int i3 = 1;
                                        boolean z = true;
                                        for (TrackRunBox.Entry entry : trackRunBox.getEntries()) {
                                            if (trackRunBox.isSampleDurationPresent()) {
                                                if (arrayList.size() == 0) {
                                                    it = it2;
                                                } else if (((TimeToSampleBox.Entry) arrayList.get(arrayList.size() - i3)).getDelta() != entry.getSampleDuration()) {
                                                    it = it2;
                                                } else {
                                                    TimeToSampleBox.Entry entry2 = (TimeToSampleBox.Entry) arrayList.get(arrayList.size() - i3);
                                                    it = it2;
                                                    entry2.setCount(entry2.getCount() + j5);
                                                }
                                                arrayList.add(new TimeToSampleBox.Entry(j5, entry.getSampleDuration()));
                                            } else {
                                                it = it2;
                                                if (trackFragmentHeaderBox.hasDefaultSampleDuration()) {
                                                    arrayList.add(new TimeToSampleBox.Entry(j5, trackFragmentHeaderBox.getDefaultSampleDuration()));
                                                } else {
                                                    arrayList.add(new TimeToSampleBox.Entry(j5, trackExtendsBox.getDefaultSampleDuration()));
                                                }
                                            }
                                            if (trackRunBox.isSampleCompositionTimeOffsetPresent()) {
                                                if (mp4TrackImpl.compositionTimeEntries.size() != 0) {
                                                    List<CompositionTimeToSample.Entry> list = mp4TrackImpl.compositionTimeEntries;
                                                    i = 1;
                                                    if (list.get(list.size() - 1).getOffset() == entry.getSampleCompositionTimeOffset()) {
                                                        List<CompositionTimeToSample.Entry> list2 = mp4TrackImpl.compositionTimeEntries;
                                                        CompositionTimeToSample.Entry entry3 = list2.get(list2.size() - 1);
                                                        entry3.setCount(entry3.getCount() + 1);
                                                    }
                                                } else {
                                                    i = 1;
                                                }
                                                mp4TrackImpl.compositionTimeEntries.add(new CompositionTimeToSample.Entry(i, CastUtils.l2i(entry.getSampleCompositionTimeOffset())));
                                            }
                                            if (trackRunBox.isSampleFlagsPresent()) {
                                                defaultSampleFlags = entry.getSampleFlags();
                                            } else if (z && trackRunBox.isFirstSampleFlagsPresent()) {
                                                defaultSampleFlags = trackRunBox.getFirstSampleFlags();
                                            } else if (trackFragmentHeaderBox.hasDefaultSampleFlags()) {
                                                defaultSampleFlags = trackFragmentHeaderBox.getDefaultSampleFlags();
                                            } else {
                                                defaultSampleFlags = trackExtendsBox.getDefaultSampleFlags();
                                            }
                                            if (defaultSampleFlags != null && !defaultSampleFlags.isSampleIsDifferenceSample()) {
                                                mp4TrackImpl.syncSamples = Mp4Arrays.copyOfAndAppend(mp4TrackImpl.syncSamples, j3);
                                            }
                                            j3++;
                                            j5 = 1;
                                            z = false;
                                            i3 = 1;
                                            it2 = it;
                                        }
                                    }
                                    j = j5;
                                    trackId = j4;
                                }
                            }
                            j2 = j3;
                        }
                    }
                }
            }
        } else {
            mp4TrackImpl.sampleGroups = getSampleGroups(sampleTableBox.getBoxes(SampleGroupDescriptionBox.class), null, sampleTableBox.getBoxes(SampleToGroupBox.class), mp4TrackImpl.sampleGroups, 0L);
        }
        mp4TrackImpl.decodingTimes = TimeToSampleBox.blowupTimeToSamples(arrayList);
        MediaHeaderBox mediaHeaderBox = trackBox.getMediaBox().getMediaHeaderBox();
        TrackHeaderBox trackHeaderBox = trackBox.getTrackHeaderBox();
        mp4TrackImpl.trackMetaData.setTrackId(trackHeaderBox.getTrackId());
        mp4TrackImpl.trackMetaData.setCreationTime(mediaHeaderBox.getCreationTime());
        mp4TrackImpl.trackMetaData.setLanguage(mediaHeaderBox.getLanguage());
        mp4TrackImpl.trackMetaData.setModificationTime(mediaHeaderBox.getModificationTime());
        mp4TrackImpl.trackMetaData.setTimescale(mediaHeaderBox.getTimescale());
        mp4TrackImpl.trackMetaData.setHeight(trackHeaderBox.getHeight());
        mp4TrackImpl.trackMetaData.setWidth(trackHeaderBox.getWidth());
        mp4TrackImpl.trackMetaData.setLayer(trackHeaderBox.getLayer());
        mp4TrackImpl.trackMetaData.setMatrix(trackHeaderBox.getMatrix());
        mp4TrackImpl.trackMetaData.setVolume(trackHeaderBox.getVolume());
        EditListBox editListBox = (EditListBox) Path.getPath((AbstractContainerBox) trackBox, "edts/elst");
        MovieHeaderBox movieHeaderBox = (MovieHeaderBox) Path.getPath((AbstractContainerBox) trackBox, "../mvhd");
        if (editListBox != null) {
            Iterator<EditListBox.Entry> it4 = editListBox.getEntries().iterator();
            while (it4.hasNext()) {
                EditListBox.Entry next = it4.next();
                List<Edit> list3 = mp4TrackImpl.edits;
                long mediaTime = next.getMediaTime();
                long timescale = mediaHeaderBox.getTimescale();
                double mediaRate = next.getMediaRate();
                MediaHeaderBox mediaHeaderBox2 = mediaHeaderBox;
                double segmentDuration = next.getSegmentDuration();
                double timescale2 = movieHeaderBox.getTimescale();
                Double.isNaN(segmentDuration);
                Double.isNaN(timescale2);
                list3.add(new Edit(mediaTime, timescale, mediaRate, segmentDuration / timescale2));
                mediaHeaderBox = mediaHeaderBox2;
                it4 = it4;
                mp4TrackImpl = this;
            }
        }
    }

    private Map<GroupEntry, long[]> getSampleGroups(List<SampleGroupDescriptionBox> list, List<SampleGroupDescriptionBox> list2, List<SampleToGroupBox> list3, Map<GroupEntry, long[]> map, long j) {
        GroupEntry groupEntry;
        for (SampleToGroupBox sampleToGroupBox : list3) {
            int i = 0;
            for (SampleToGroupBox.Entry entry : sampleToGroupBox.getEntries()) {
                if (entry.getGroupDescriptionIndex() > 0) {
                    GroupEntry groupEntry2 = null;
                    if (entry.getGroupDescriptionIndex() > 65535) {
                        for (SampleGroupDescriptionBox sampleGroupDescriptionBox : list2) {
                            if (sampleGroupDescriptionBox.getGroupingType().equals(sampleToGroupBox.getGroupingType())) {
                                groupEntry2 = sampleGroupDescriptionBox.getGroupEntries().get((entry.getGroupDescriptionIndex() - 1) & SupportMenu.USER_MASK);
                            }
                        }
                        groupEntry = groupEntry2;
                    } else {
                        for (SampleGroupDescriptionBox sampleGroupDescriptionBox2 : list) {
                            if (sampleGroupDescriptionBox2.getGroupingType().equals(sampleToGroupBox.getGroupingType())) {
                                groupEntry2 = sampleGroupDescriptionBox2.getGroupEntries().get(entry.getGroupDescriptionIndex() - 1);
                            }
                        }
                        groupEntry = groupEntry2;
                    }
                    long[] jArr = map.get(groupEntry);
                    long[] jArr2 = jArr == null ? new long[0] : jArr;
                    long[] jArr3 = new long[CastUtils.l2i(entry.getSampleCount()) + jArr2.length];
                    System.arraycopy(jArr2, 0, jArr3, 0, jArr2.length);
                    int i2 = 0;
                    while (true) {
                        long j2 = i2;
                        if (j2 >= entry.getSampleCount()) {
                            break;
                        }
                        jArr3[jArr2.length + i2] = j + i + j2;
                        i2++;
                    }
                    map.put(groupEntry, jArr3);
                }
                i = (int) (i + entry.getSampleCount());
            }
        }
        return map;
    }

    @Override // java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        Container parent = this.trackBox.getParent();
        if (parent instanceof BasicContainer) {
            ((BasicContainer) parent).close();
        }
        IsoFile[] isoFileArr = this.fragments;
        if (isoFileArr != null) {
            for (IsoFile isoFile : isoFileArr) {
                isoFile.close();
            }
        }
    }

    @Override // com.googlecode.mp4parser.authoring.Track
    public List<Sample> getSamples() {
        return this.samples;
    }

    @Override // com.googlecode.mp4parser.authoring.Track
    public synchronized long[] getSampleDurations() {
        return this.decodingTimes;
    }

    @Override // com.googlecode.mp4parser.authoring.Track
    public SampleDescriptionBox getSampleDescriptionBox() {
        return this.sampleDescriptionBox;
    }

    @Override // com.googlecode.mp4parser.authoring.AbstractTrack, com.googlecode.mp4parser.authoring.Track
    public List<CompositionTimeToSample.Entry> getCompositionTimeEntries() {
        return this.compositionTimeEntries;
    }

    @Override // com.googlecode.mp4parser.authoring.AbstractTrack, com.googlecode.mp4parser.authoring.Track
    public long[] getSyncSamples() {
        long[] jArr = this.syncSamples;
        if (jArr == null || jArr.length == this.samples.size()) {
            return null;
        }
        return this.syncSamples;
    }

    @Override // com.googlecode.mp4parser.authoring.AbstractTrack, com.googlecode.mp4parser.authoring.Track
    public List<SampleDependencyTypeBox.Entry> getSampleDependencies() {
        return this.sampleDependencies;
    }

    @Override // com.googlecode.mp4parser.authoring.Track
    public TrackMetaData getTrackMetaData() {
        return this.trackMetaData;
    }

    @Override // com.googlecode.mp4parser.authoring.Track
    public String getHandler() {
        return this.handler;
    }

    @Override // com.googlecode.mp4parser.authoring.AbstractTrack, com.googlecode.mp4parser.authoring.Track
    public SubSampleInformationBox getSubsampleInformationBox() {
        return this.subSampleInformationBox;
    }
}
