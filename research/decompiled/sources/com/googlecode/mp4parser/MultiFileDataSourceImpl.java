package com.googlecode.mp4parser;

import com.googlecode.mp4parser.util.CastUtils;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;

/* loaded from: classes.dex */
public class MultiFileDataSourceImpl implements DataSource {
    FileChannel[] fcs;
    int index = 0;

    public MultiFileDataSourceImpl(File... fileArr) throws FileNotFoundException {
        this.fcs = new FileChannel[fileArr.length];
        for (int i = 0; i < fileArr.length; i++) {
            this.fcs[i] = new FileInputStream(fileArr[i]).getChannel();
        }
    }

    @Override // com.googlecode.mp4parser.DataSource
    public int read(ByteBuffer byteBuffer) throws IOException {
        int remaining = byteBuffer.remaining();
        int read = this.fcs[this.index].read(byteBuffer);
        if (read == remaining) {
            return read;
        }
        this.index++;
        return read + read(byteBuffer);
    }

    @Override // com.googlecode.mp4parser.DataSource
    public long size() throws IOException {
        long j = 0;
        for (FileChannel fileChannel : this.fcs) {
            j += fileChannel.size();
        }
        return j;
    }

    @Override // com.googlecode.mp4parser.DataSource
    public long position() throws IOException {
        long j = 0;
        int i = 0;
        while (true) {
            int i2 = this.index;
            if (i < i2) {
                j += this.fcs[i].size();
                i++;
            } else {
                return j + this.fcs[i2].position();
            }
        }
    }

    @Override // com.googlecode.mp4parser.DataSource
    public void position(long j) throws IOException {
        int i = 0;
        while (true) {
            FileChannel[] fileChannelArr = this.fcs;
            if (i >= fileChannelArr.length) {
                return;
            }
            if (j - fileChannelArr[i].size() < 0) {
                this.fcs[i].position(j);
                this.index = i;
                return;
            } else {
                j -= this.fcs[i].size();
                i++;
            }
        }
    }

    @Override // com.googlecode.mp4parser.DataSource
    public long transferTo(long j, long j2, WritableByteChannel writableByteChannel) throws IOException {
        if (j2 == 0) {
            return 0L;
        }
        long j3 = 0;
        for (FileChannel fileChannel : this.fcs) {
            long size = fileChannel.size();
            if (j >= j3 && j < j3 + size && j + j2 > j3) {
                long j4 = j - j3;
                long min = Math.min(j2, size - j4);
                fileChannel.transferTo(j4, min, writableByteChannel);
                return min + transferTo(j + min, j2 - min, writableByteChannel);
            }
            j3 += size;
        }
        return 0L;
    }

    @Override // com.googlecode.mp4parser.DataSource
    public ByteBuffer map(long j, long j2) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(CastUtils.l2i(j2));
        transferTo(j, j2, Channels.newChannel(byteArrayOutputStream));
        return ByteBuffer.wrap(byteArrayOutputStream.toByteArray());
    }

    @Override // com.googlecode.mp4parser.DataSource, java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        for (FileChannel fileChannel : this.fcs) {
            fileChannel.close();
        }
    }
}
