/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.files;

import com.google.j2objc.annotations.ObjectiveCName;

public interface InputFile {
    @ObjectiveCName("readWithOffset:withData:withDataOffset:withLength:withCallback:")
    void read(int fileOffset, byte[] data, int offset, int len, FileReadCallback callback);

    @ObjectiveCName("close")
    boolean close();
}