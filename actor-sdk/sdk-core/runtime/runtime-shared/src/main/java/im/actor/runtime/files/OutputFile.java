/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.files;

import com.google.j2objc.annotations.ObjectiveCName;

public interface OutputFile {
    @ObjectiveCName("writeWithOffset:withData:withDataOffset:withLength:")
    boolean write(int fileOffset, byte[] data, int dataOffset, int dataLen);

    @ObjectiveCName("close")
    boolean close();
}
