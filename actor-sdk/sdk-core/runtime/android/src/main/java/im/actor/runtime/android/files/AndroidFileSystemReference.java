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
    public int getSize() {
        return (int) new File(fileName).length();
    }

    @Override
    public OutputFile openWrite(int size) {
        try {
            return new AndroidOutputFile(fileName, size);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public InputFile openRead() {
        try {
            return new AndroidInputFile(fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
