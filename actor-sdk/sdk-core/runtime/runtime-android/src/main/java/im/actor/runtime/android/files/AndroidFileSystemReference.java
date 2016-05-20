/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.android.files;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import im.actor.runtime.files.FileSystemReference;
import im.actor.runtime.files.InputFile;
import im.actor.runtime.files.OutputFile;
import im.actor.runtime.promise.Promise;

public class AndroidFileSystemReference implements FileSystemReference {

    private String fileName;

    public AndroidFileSystemReference(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String getDescriptor() {
        return fileName;
    }

    @Override
    public boolean isExist() {
        return new File(fileName).exists();
    }

    @Override
    public boolean isInAppMemory() {
        // TODO: Implement
        return false;
    }

    @Override
    public boolean isInTempDirectory() {
        // TODO: Implement
        return false;
    }

    @Override
    public int getSize() {
        return (int) new File(fileName).length();
    }

    @Override
    public Promise<OutputFile> openWrite(int size) {
        try {
            return Promise.success(new AndroidOutputFile(fileName, size));
        } catch (IOException e) {
            return Promise.failure(e);
        }
    }

    @Override
    public Promise<InputFile> openRead() {
        try {
            return Promise.success(new AndroidInputFile(fileName));
        } catch (IOException e) {
            return Promise.failure(e);
        }
    }
}
