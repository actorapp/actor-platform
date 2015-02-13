package im.actor.messenger.util.io;

import android.content.Context;
import im.actor.messenger.util.Logger;

import java.io.*;
import java.util.Random;
import java.util.zip.CRC32;

import static im.actor.messenger.util.io.StreamingUtils.*;

public class SafeFileWriter {
    private final String TAG;
    private Random random = new Random();
    private Context context;
    private String fileName;

    public SafeFileWriter(Context context, String fileName) {
        this.context = context;
        this.fileName = fileName;
        TAG = "SafeFileWriter#" + hashCode();
    }

    private File getFile() {
        File res = new File(context.getFilesDir().getPath() + "/" + fileName);
        return res;
    }

    private File getTempFile() {
        return new File(context.getFilesDir().getPath() + "/random_" + random.nextLong() + ".tmp");
    }

    public synchronized void saveData(byte[] data) {
        File file = getTempFile();
        if (file.exists()) {
            if (!file.delete()) {
                file.delete();
            }
        }

        FileOutputStream os = null;
        try {
            os = new FileOutputStream(file);
            writeProtoBytes(data, os);
            CRC32 crc32 = new CRC32();
            crc32.update(data);
            writeLong(crc32.getValue(), os);
            os.flush();
            os.getFD().sync();
            os.close();
            os = null;
            file.renameTo(getFile());
        } catch (FileNotFoundException e) {
            Logger.d(TAG, e);
        } catch (IOException e) {
            Logger.d(TAG, e);
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    Logger.d(TAG, e);
                }
            }
        }
    }

    public synchronized byte[] loadData() {
        File file = getFile();
        if (!file.exists())
            return null;

        FileInputStream is = null;
        try {
            is = new FileInputStream(file);
            byte[] res = readProtoBytes(is);
            CRC32 crc32 = new CRC32();
            crc32.update(res);
            long crc = readLong(is);
            if (crc32.getValue() != crc) {
                return null;
            }
            return res;
        } catch (FileNotFoundException e) {
            Logger.d(TAG, e);
        } catch (IOException e) {
            Logger.d(TAG, e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Logger.d(TAG, e);
                }
            }
        }
        return null;
    }

    public synchronized void remove() {
        File file = getFile();
        if (file.exists()) {
            if (!file.delete()) {
                file.delete();
            }
        }
    }
}
