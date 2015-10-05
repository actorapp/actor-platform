/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.android.files;

import java.io.IOException;
import java.io.RandomAccessFile;

import im.actor.runtime.files.OutputFile;

public class AndroidOutputFile implements OutputFile {

    private RandomAccessFile randomAccessFile;

    public AndroidOutputFile(String fileName, int size) throws IOException {
        randomAccessFile = new RandomAccessFile(fileName, "rws");
        randomAccessFile.setLength(size);
    }

    @Override
    public synchronized boolean write(int fileOffset, byte[] data, int dataOffset, int dataLen) {
        try {
            randomAccessFile.seek(fileOffset);
            randomAccessFile.write(data, dataOffset, dataLen);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public synchronized boolean close() {
        try {
            randomAccessFile.getFD().sync();
            randomAccessFile.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
