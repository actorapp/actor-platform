package im.actor.model;

import im.actor.model.entity.FileReference;

/**
 * Created by ex3ndr on 26.02.15.
 */
public interface FileSystemProvider {

    public im.actor.model.files.FileReference createTempFile();

    public im.actor.model.files.FileReference commitTempFile(im.actor.model.files.FileReference sourceFile, FileReference fileReference);

    public boolean isFsPersistent();

    public im.actor.model.files.FileReference fileFromDescriptor(String descriptor);
}