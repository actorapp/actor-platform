/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model;

import com.google.j2objc.annotations.ObjectiveCName;

import im.actor.model.entity.FileReference;
import im.actor.model.files.FileSystemReference;

/**
 * Provider for File System support
 */
public interface FileSystemProvider {

    /**
     * Create temp file for upload/download operation
     *
     * @return created temp file
     */
    @ObjectiveCName("createTempFile")
    FileSystemReference createTempFile();

    /**
     * Commit temp file
     *
     * @param sourceFile    source temp file (created by createTempFile())
     * @param fileReference file reference
     * @return result file system reference
     */
    @ObjectiveCName("commitTempFile:withReference:")
    FileSystemReference commitTempFile(FileSystemReference sourceFile, FileReference fileReference);

    /**
     * Is file system persist data
     *
     * @return is file system persist
     */
    @ObjectiveCName("isFsPersistent")
    boolean isFsPersistent();

    /**
     * Create FileSystemReference from descriptor
     *
     * @param descriptor descriptor
     * @return the FileSystemReference
     */
    @ObjectiveCName("fileFromDescriptor:")
    FileSystemReference fileFromDescriptor(String descriptor);
}