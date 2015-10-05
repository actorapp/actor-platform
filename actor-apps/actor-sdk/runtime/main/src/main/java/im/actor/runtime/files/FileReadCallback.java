/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.files;

import com.google.j2objc.annotations.ObjectiveCName;

public interface FileReadCallback {
    @ObjectiveCName("onFileReadWithOffset:withData:withDataOffset:withLength:")
    void onFileRead(int fileOffset, byte[] data, int offset, int len);

    @ObjectiveCName("onFileReadError")
    void onFileReadError();
}
