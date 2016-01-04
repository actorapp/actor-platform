/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.se;

import im.actor.runtime.FileSystemRuntime;
import im.actor.runtime.files.FileSystemReference;

public class JavaSeFileSystemProvider implements FileSystemRuntime {

    @Override
    public synchronized FileSystemReference createTempFile() {
        // TODO: Implement
        return null;
    }

    @Override
    public FileSystemReference commitTempFile(FileSystemReference sourceFile, long fileId, String fileName) {
        // TODO: Implement
        return null;
    }

    @Override
    public boolean isFsPersistent() {
        return true;
    }

    @Override
    public synchronized FileSystemReference fileFromDescriptor(String descriptor) {
        // TODO: Implement
        throw new RuntimeException("Not implemented");
    }
}
