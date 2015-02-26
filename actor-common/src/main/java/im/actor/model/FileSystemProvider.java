package im.actor.model;

import im.actor.model.entity.FileLocation;
import im.actor.model.files.FileReference;

/**
 * Created by ex3ndr on 26.02.15.
 */
public interface FileSystemProvider {
    public FileReference createTempFile(FileLocation fileLocation);

    public FileReference commitTempFile(FileReference sourceFile, FileLocation fileLocation);

    public FileReference fileFromDescriptor(String descriptor);
}