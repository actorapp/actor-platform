package im.actor.core.utils;

import android.content.Context;
import android.net.Uri;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class IOUtils {
    private static ThreadLocal<byte[]> buffers = new ThreadLocal<byte[]>() {
        @Override
        protected byte[] initialValue() {
            return new byte[4 * 1024];
        }
    };

    public static void delete(File src) {
        if (src.exists()) {
            if (!src.delete()) {
                src.deleteOnExit();
            }
        }
    }

    public static void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = buffers.get();
        int len;
        while ((len = in.read(buf)) > 0) {
            Thread.yield();
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    public static void copy(Uri uri, File dst, Context context) throws IOException {
        InputStream stream = context.getContentResolver().openInputStream(uri);
        try {
            copy(stream, dst);
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }

    public static void copy(InputStream in, File dst) throws IOException {
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = buffers.get();
        int len;
        while ((len = in.read(buf)) > 0) {
            Thread.yield();
            out.write(buf, 0, len);
        }
        out.close();
    }

    public static void writeAll(String fileName, byte[] data) throws IOException {
        OutputStream stream = new FileOutputStream(fileName);
        stream.write(data);
        stream.close();
    }

    public static byte[] readAll(String fileName) throws IOException {
        byte[] res;
        InputStream in = new FileInputStream(fileName);
        res = readAll(in);
        in.close();
        return res;
    }

    public static byte[] readAll(InputStream in) throws IOException {
        return readAll(in, null);
    }

    public static byte[] readAll(InputStream in, ProgressListener listener) throws IOException {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(in);
        ByteArrayOutputStream os = new ByteArrayOutputStream(4096);
        byte[] buffer = buffers.get();
        int len;
        int readed = 0;
        try {
            while ((len = bufferedInputStream.read(buffer)) >= 0) {
                Thread.yield();
                os.write(buffer, 0, len);
                readed += len;
                if (listener != null) {
                    listener.onProgress(readed);
                }
            }
        } catch (IOException e) {
        }
        return os.toByteArray();
    }

    public static String toString(FileInputStream fileInputStream) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(fileInputStream));
        StringBuilder total = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) {
            total.append(line);
        }
        return total.toString();
    }


    public static interface ProgressListener {
        public void onProgress(int bytes);
    }

    public static String getFileExtension(String name) {
        int lastIndexOf = name.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return ""; // empty extension
        }
        return name.substring(lastIndexOf + 1);
    }

}