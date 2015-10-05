package im.actor.runtime;

import im.actor.runtime.files.FileSystemReference;

/**
 * Created by ex3ndr on 07.08.15.
 */
public class FileSystemRuntimeProvider implements FileSystemRuntime {

    @Override
    public FileSystemReference createTempFile() {
        throw new RuntimeException("Dumb");
    }

    @Override
    public FileSystemReference commitTempFile(FileSystemReference sourceFile, long fileId, String fileName) {
        throw new RuntimeException("Dumb");
    }

    @Override
    public boolean isFsPersistent() {
        throw new RuntimeException("Dumb");
    }

    @Override
    public FileSystemReference fileFromDescriptor(String descriptor) {
        throw new RuntimeException("Dumb");
    }
}
