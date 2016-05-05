/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.android.files;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.files.FilePart;
import im.actor.runtime.files.InputFile;
import im.actor.runtime.promise.Promise;

public class AndroidInputFile implements InputFile {

    private static Executor executor = Executors.newSingleThreadExecutor();

    private RandomAccessFile randomAccessFile;

    public AndroidInputFile(String fileName) throws FileNotFoundException {
        randomAccessFile = new RandomAccessFile(fileName, "r");
    }

    @Override
    public Promise<FilePart> read(int fileOffset, int len) {
        return new Promise<>(resolver -> {
            executor.execute(() -> {
                try {
                    byte[] data = new byte[len];
                    randomAccessFile.seek(fileOffset);
                    // TODO: Better reading. For big len result can be truncated
                    randomAccessFile.read(data, 0, len);
                    resolver.result(new FilePart(fileOffset, len, data));
                } catch (Exception e) {
                    e.printStackTrace();
                    resolver.error(e);
                }
            });
        });
    }


    @Override
    public Promise<Void> close() {
        return new Promise<>(resolver -> {
            try {
                randomAccessFile.close();
                resolver.result(null);
            } catch (IOException e) {
                resolver.error(e);
            }
        });
    }
}
