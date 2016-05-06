/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.js.fs;

import im.actor.runtime.files.FileSystemReference;
import im.actor.runtime.files.InputFile;
import im.actor.runtime.files.OutputFile;
import im.actor.runtime.promise.Promise;

public class JsFileSystemReference implements FileSystemReference {

    private String key;
    private JsBlob file;

    public JsFileSystemReference(JsBlob file, String key) {
        this.key = key;
        this.file = file;
    }

    @Override
    public String getDescriptor() {
        return key;
    }

    @Override
    public boolean isExist() {
        return true;
    }

    @Override
    public boolean isInAppMemory() {
        return false;
    }

    @Override
    public boolean isInTempDirectory() {
        return false;
    }

    @Override
    public int getSize() {
        return file.getSize();
    }

    @Override
    public Promise<OutputFile> openWrite(int size) {
        return Promise.failure(new RuntimeException("Unsupported exception"));
    }

    @Override
    public Promise<InputFile> openRead() {
        return Promise.success(new JsFileInput(file));
    }
}
