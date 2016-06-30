/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime;

import com.google.j2objc.annotations.ObjectiveCName;

import im.actor.runtime.files.FileSystemReference;

/**
 * Provider for File System support
 */
public interface FileSystemRuntime {

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
     * @param sourceFile source temp file (created by createTempFile())
     * @param fileId     file id
     * @param fileName   file name
     * @return result file system reference
     */
    @ObjectiveCName("commitTempFile:withFileId:withFileName:")
    FileSystemReference commitTempFile(FileSystemReference sourceFile, long fileId, String fileName);

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