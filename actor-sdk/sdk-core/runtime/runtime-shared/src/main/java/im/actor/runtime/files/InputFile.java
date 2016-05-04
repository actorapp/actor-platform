/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.files;

import com.google.j2objc.annotations.ObjectiveCName;

import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.promise.Promise;

public interface InputFile {

    @ObjectiveCName("readWithOffset:withLength:")
    Promise<FilePart> read(int fileOffset, int len);

    @ObjectiveCName("close")
    Promise<Void> close();
}