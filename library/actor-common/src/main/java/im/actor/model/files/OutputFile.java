/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.files;

public interface OutputFile {
    boolean write(int fileOffset, byte[] data, int dataOffset, int dataLen);

    boolean close();
}
