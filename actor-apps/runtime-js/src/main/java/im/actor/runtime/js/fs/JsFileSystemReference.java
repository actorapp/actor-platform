/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.js.fs;

import im.actor.runtime.files.FileSystemReference;
import im.actor.runtime.files.InputFile;
import im.actor.runtime.files.OutputFile;

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
    public int getSize() {
        return file.getSize();
    }

    @Override
    public OutputFile openWrite(int size) {
        throw new RuntimeException("Unsupported exception");
    }

    @Override
    public InputFile openRead() {
        return new JsFileInput(file);
    }
}
