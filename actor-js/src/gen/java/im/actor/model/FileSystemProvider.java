package im.actor.model;

import im.actor.model.entity.FileReference;
import im.actor.model.files.FileSystemReference;

/**
 * Created by ex3ndr on 26.02.15.
 */
public interface FileSystemProvider {

    public FileSystemReference createTempFile();

    public FileSystemReference commitTempFile(FileSystemReference sourceFile, FileReference fileReference);

    public boolean isFsPersistent();

    public FileSystemReference fileFromDescriptor(String descriptor);
}