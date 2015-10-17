/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.android.files;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import im.actor.runtime.files.FileReadCallback;
import im.actor.runtime.files.InputFile;

public class AndroidInputFile implements InputFile {

    private static Executor executor = Executors.newSingleThreadExecutor();

    private RandomAccessFile randomAccessFile;

    public AndroidInputFile(String fileName) throws FileNotFoundException {
        randomAccessFile = new RandomAccessFile(fileName, "r");
    }

//    @Override
//    public synchronized boolean read(int fileOffset, byte[] data, int offset, int len) {
//        try {
//            randomAccessFile.seek(fileOffset);
//            // TODO: Better reading
//            randomAccessFile.read(data, offset, len);
//            return true;
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return false;
//    }

    @Override
    public void read(final int fileOffset, final byte[] data, final int offset, final int len, final FileReadCallback callback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    randomAccessFile.seek(fileOffset);
                    // TODO: Better reading
                    randomAccessFile.read(data, offset, len);

                    callback.onFileRead(fileOffset, data, offset, len);
                } catch (IOException e) {
                    e.printStackTrace();

                    callback.onFileReadError();
                }
            }
        });
    }

    @Override
    public synchronized boolean close() {
        try {
            randomAccessFile.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
