/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.js.providers;

import im.actor.model.FileSystemProvider;
import im.actor.model.entity.FileReference;
import im.actor.model.files.FileSystemReference;

/**
 * Created by ex3ndr on 03.05.15.
 */
public class JsFileSystem implements FileSystemProvider {

    @Override
    public FileSystemReference createTempFile() {
        throw new RuntimeException("Unsupported operation");
    }

    @Override
    public FileSystemReference commitTempFile(FileSystemReference sourceFile, FileReference fileReference) {
        throw new RuntimeException("Unsupported operation");
    }

    @Override
    public boolean isFsPersistent() {
        return false;
    }

    @Override
    public FileSystemReference fileFromDescriptor(String descriptor) {
        return null;
    }
}
