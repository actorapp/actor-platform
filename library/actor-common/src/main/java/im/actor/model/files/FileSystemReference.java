/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.files;

public interface FileSystemReference {

    String getDescriptor();

    boolean isExist();

    int getSize();

    OutputFile openWrite(int size);

    InputFile openRead();
}