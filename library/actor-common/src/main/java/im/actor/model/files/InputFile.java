/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.files;

public interface InputFile {
    boolean read(int fileOffset, byte[] data, int offset, int len);

    boolean close();
}