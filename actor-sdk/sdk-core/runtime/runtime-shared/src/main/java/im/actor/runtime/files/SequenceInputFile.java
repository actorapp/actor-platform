package im.actor.runtime.files;

import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.promise.Promise;

public interface SequenceInputFile {

    Promise<FilePart> readBlock(int blockSize);

    Promise<Void> close();
}
