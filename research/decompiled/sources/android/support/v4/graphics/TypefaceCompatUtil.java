package android.support.v4.graphics;

import android.content.Context;
import android.content.res.Resources;
import android.os.Process;
import android.support.annotation.RequiresApi;
import android.support.annotation.RestrictTo;
import android.util.Log;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

@RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
/* loaded from: classes.dex */
public class TypefaceCompatUtil {
    private static final String CACHE_FILE_PREFIX = ".font";
    private static final String TAG = "TypefaceCompatUtil";

    private TypefaceCompatUtil() {
    }

    public static File getTempFile(Context context) {
        String str = CACHE_FILE_PREFIX + Process.myPid() + "-" + Process.myTid() + "-";
        for (int i = 0; i < 100; i++) {
            File file = new File(context.getCacheDir(), str + i);
            if (file.createNewFile()) {
                return file;
            }
        }
        return null;
    }

    /* JADX WARN: Removed duplicated region for block: B:17:0x002e A[Catch: IOException -> 0x0032, TryCatch #1 {IOException -> 0x0032, blocks: (B:3:0x0001, B:6:0x0016, B:20:0x0025, B:18:0x0031, B:17:0x002e, B:24:0x002a), top: B:2:0x0001, inners: #3 }] */
    /* JADX WARN: Removed duplicated region for block: B:19:0x0025 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    @android.support.annotation.RequiresApi(19)
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    private static java.nio.ByteBuffer mmap(java.io.File r9) {
        /*
            r0 = 0
            java.io.FileInputStream r1 = new java.io.FileInputStream     // Catch: java.io.IOException -> L32
            r1.<init>(r9)     // Catch: java.io.IOException -> L32
            java.nio.channels.FileChannel r2 = r1.getChannel()     // Catch: java.lang.Throwable -> L1a java.lang.Throwable -> L1d
            long r6 = r2.size()     // Catch: java.lang.Throwable -> L1a java.lang.Throwable -> L1d
            java.nio.channels.FileChannel$MapMode r3 = java.nio.channels.FileChannel.MapMode.READ_ONLY     // Catch: java.lang.Throwable -> L1a java.lang.Throwable -> L1d
            r4 = 0
            java.nio.MappedByteBuffer r9 = r2.map(r3, r4, r6)     // Catch: java.lang.Throwable -> L1a java.lang.Throwable -> L1d
            r1.close()     // Catch: java.io.IOException -> L32
            return r9
        L1a:
            r9 = move-exception
            r2 = r0
            goto L23
        L1d:
            r9 = move-exception
            throw r9     // Catch: java.lang.Throwable -> L1f
        L1f:
            r2 = move-exception
            r8 = r2
            r2 = r9
            r9 = r8
        L23:
            if (r2 == 0) goto L2e
            r1.close()     // Catch: java.lang.Throwable -> L29 java.io.IOException -> L32
            goto L31
        L29:
            r1 = move-exception
            r2.addSuppressed(r1)     // Catch: java.io.IOException -> L32
            goto L31
        L2e:
            r1.close()     // Catch: java.io.IOException -> L32
        L31:
            throw r9     // Catch: java.io.IOException -> L32
        L32:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.v4.graphics.TypefaceCompatUtil.mmap(java.io.File):java.nio.ByteBuffer");
    }

    /* JADX WARN: Removed duplicated region for block: B:21:0x0041 A[Catch: all -> 0x0045, Throwable -> 0x0048, TryCatch #2 {all -> 0x0045, blocks: (B:5:0x000b, B:8:0x0024, B:24:0x0038, B:22:0x0044, B:21:0x0041, B:28:0x003d), top: B:4:0x000b }] */
    /* JADX WARN: Removed duplicated region for block: B:23:0x0038 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:35:0x0050  */
    /* JADX WARN: Removed duplicated region for block: B:45:? A[Catch: IOException -> 0x005f, SYNTHETIC, TRY_LEAVE, TryCatch #5 {IOException -> 0x005f, blocks: (B:3:0x0005, B:10:0x0029, B:39:0x0052, B:36:0x005b, B:43:0x0057, B:37:0x005e), top: B:2:0x0005, inners: #6 }] */
    @android.support.annotation.RequiresApi(19)
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public static java.nio.ByteBuffer mmap(android.content.Context r8, android.os.CancellationSignal r9, android.net.Uri r10) {
        /*
            android.content.ContentResolver r8 = r8.getContentResolver()
            r0 = 0
            java.lang.String r1 = "r"
            android.os.ParcelFileDescriptor r8 = r8.openFileDescriptor(r10, r1, r9)     // Catch: java.io.IOException -> L5f
            java.io.FileInputStream r9 = new java.io.FileInputStream     // Catch: java.lang.Throwable -> L45 java.lang.Throwable -> L48
            java.io.FileDescriptor r10 = r8.getFileDescriptor()     // Catch: java.lang.Throwable -> L45 java.lang.Throwable -> L48
            r9.<init>(r10)     // Catch: java.lang.Throwable -> L45 java.lang.Throwable -> L48
            java.nio.channels.FileChannel r1 = r9.getChannel()     // Catch: java.lang.Throwable -> L2d java.lang.Throwable -> L30
            long r5 = r1.size()     // Catch: java.lang.Throwable -> L2d java.lang.Throwable -> L30
            java.nio.channels.FileChannel$MapMode r2 = java.nio.channels.FileChannel.MapMode.READ_ONLY     // Catch: java.lang.Throwable -> L2d java.lang.Throwable -> L30
            r3 = 0
            java.nio.MappedByteBuffer r10 = r1.map(r2, r3, r5)     // Catch: java.lang.Throwable -> L2d java.lang.Throwable -> L30
            r9.close()     // Catch: java.lang.Throwable -> L45 java.lang.Throwable -> L48
            if (r8 == 0) goto L2c
            r8.close()     // Catch: java.io.IOException -> L5f
        L2c:
            return r10
        L2d:
            r10 = move-exception
            r1 = r0
            goto L36
        L30:
            r10 = move-exception
            throw r10     // Catch: java.lang.Throwable -> L32
        L32:
            r1 = move-exception
            r7 = r1
            r1 = r10
            r10 = r7
        L36:
            if (r1 == 0) goto L41
            r9.close()     // Catch: java.lang.Throwable -> L3c java.lang.Throwable -> L45
            goto L44
        L3c:
            r9 = move-exception
            r1.addSuppressed(r9)     // Catch: java.lang.Throwable -> L45 java.lang.Throwable -> L48
            goto L44
        L41:
            r9.close()     // Catch: java.lang.Throwable -> L45 java.lang.Throwable -> L48
        L44:
            throw r10     // Catch: java.lang.Throwable -> L45 java.lang.Throwable -> L48
        L45:
            r9 = move-exception
            r10 = r0
            goto L4e
        L48:
            r9 = move-exception
            throw r9     // Catch: java.lang.Throwable -> L4a
        L4a:
            r10 = move-exception
            r7 = r10
            r10 = r9
            r9 = r7
        L4e:
            if (r8 == 0) goto L5e
            if (r10 == 0) goto L5b
            r8.close()     // Catch: java.lang.Throwable -> L56 java.io.IOException -> L5f
            goto L5e
        L56:
            r8 = move-exception
            r10.addSuppressed(r8)     // Catch: java.io.IOException -> L5f
            goto L5e
        L5b:
            r8.close()     // Catch: java.io.IOException -> L5f
        L5e:
            throw r9     // Catch: java.io.IOException -> L5f
        L5f:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.v4.graphics.TypefaceCompatUtil.mmap(android.content.Context, android.os.CancellationSignal, android.net.Uri):java.nio.ByteBuffer");
    }

    @RequiresApi(19)
    public static ByteBuffer copyToDirectBuffer(Context context, Resources resources, int i) {
        File tempFile = getTempFile(context);
        if (tempFile == null) {
            return null;
        }
        try {
            if (copyToFile(tempFile, resources, i)) {
                return mmap(tempFile);
            }
            return null;
        } finally {
            tempFile.delete();
        }
    }

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    public static boolean copyToFile(File file, InputStream inputStream) {
        FileOutputStream fileOutputStream;
        FileOutputStream fileOutputStream2 = null;
        try {
            try {
                fileOutputStream = new FileOutputStream(file, false);
            } catch (Throwable th) {
                th = th;
            }
        } catch (IOException e) {
            e = e;
        }
        try {
            byte[] bArr = new byte[1024];
            while (true) {
                int read = inputStream.read(bArr);
                if (read != -1) {
                    fileOutputStream.write(bArr, 0, read);
                } else {
                    closeQuietly(fileOutputStream);
                    return true;
                }
            }
        } catch (IOException e2) {
            e = e2;
            fileOutputStream2 = fileOutputStream;
            Log.e(TAG, "Error copying resource contents to temp file: " + e.getMessage());
            closeQuietly(fileOutputStream2);
            return false;
        } catch (Throwable th2) {
            th = th2;
            fileOutputStream2 = fileOutputStream;
            closeQuietly(fileOutputStream2);
            throw th;
        }
    }

    public static boolean copyToFile(File file, Resources resources, int i) {
        InputStream inputStream;
        try {
            inputStream = resources.openRawResource(i);
            try {
                boolean copyToFile = copyToFile(file, inputStream);
                closeQuietly(inputStream);
                return copyToFile;
            } catch (Throwable th) {
                th = th;
                closeQuietly(inputStream);
                throw th;
            }
        } catch (Throwable th2) {
            th = th2;
            inputStream = null;
        }
    }

    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException unused) {
            }
        }
    }
}
