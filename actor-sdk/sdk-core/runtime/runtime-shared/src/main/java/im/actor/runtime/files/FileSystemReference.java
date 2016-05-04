/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.files;

import com.google.j2objc.annotations.ObjectiveCName;

import im.actor.runtime.promise.Promise;

/**
 * Reference for a file in File System
 */
public interface FileSystemReference {

    /**
     * Get Unique Descriptor of a file. Can be stored somewhere and reference could be restored via
     * this descriptor
     *
     * @return Descriptor
     */
    @ObjectiveCName("getDescriptor")
    String getDescriptor();

    /**
     * Checks if file with such descriptor exists
     *
     * @return is Exists
     */
    @ObjectiveCName("isExist")
    boolean isExist();

    /**
     * Checks if file is in storage that belongs to an App
     *
     * @return is file in App Memory
     */
    @ObjectiveCName("isInAppMemory")
    boolean isInAppMemory();

    /**
     * Checks if file is in temporary storage and could be deleted on next app restart
     *
     * @return is file in Temp Directory
     */
    @ObjectiveCName("isInTempDirectory")
    boolean isInTempDirectory();

    /**
     * Getting size of a file. If does not exist behaviour is unpredictable.
     *
     * @return size of a file
     */
    @ObjectiveCName("getSize")
    int getSize();

    /**
     * Open File for writing
     *
     * @param size expected size
     * @return Output File
     */
    @ObjectiveCName("openWriteWithSize:")
    Promise<OutputFile> openWrite(int size);

    /**
     * Open File for reading
     *
     * @return Input File
     */
    @ObjectiveCName("openRead")
    Promise<InputFile> openRead();
}