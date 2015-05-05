/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.files;

public interface FileReadCallback {
    void onFileRead(int fileOffset, byte[] data, int offset, int len);

    void onFileReadError();
}
