/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.files;

import com.google.j2objc.annotations.ObjectiveCName;

public interface FileSystemReference {

    @ObjectiveCName("getDescriptor")
    String getDescriptor();

    @ObjectiveCName("isExist")
    boolean isExist();

    @ObjectiveCName("getSize")
    int getSize();

    @ObjectiveCName("openWriteWithSize:")
    OutputFile openWrite(int size);

    @ObjectiveCName("openRead")
    InputFile openRead();
}