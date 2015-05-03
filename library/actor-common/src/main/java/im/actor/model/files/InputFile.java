/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.files;

public interface InputFile {
    void read(int fileOffset, byte[] data, int offset, int len, FileReadCallback callback);

    boolean close();
}