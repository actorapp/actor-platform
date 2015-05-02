/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.android;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import im.actor.model.files.InputFile;

public class AndroidInputFile implements InputFile {

    private RandomAccessFile randomAccessFile;

    public AndroidInputFile(String fileName) throws FileNotFoundException {
        randomAccessFile = new RandomAccessFile(fileName, "r");
    }

    @Override
    public synchronized boolean read(int fileOffset, byte[] data, int offset, int len) {
        try {
            randomAccessFile.seek(fileOffset);
            // TODO: Better reading
            randomAccessFile.read(data, offset, len);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public synchronized boolean close() {
        return false;
    }
}
