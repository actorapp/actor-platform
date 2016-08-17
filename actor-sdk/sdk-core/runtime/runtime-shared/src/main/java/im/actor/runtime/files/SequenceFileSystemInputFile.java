package im.actor.runtime.files;

import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.promise.Promise;

public class SequenceFileSystemInputFile implements SequenceInputFile {

    // j2objc workaround
    private static final FilePart DUMB = null;

    private final InputFile inputFile;
    private int currentOffset;

    private Promise<FilePart> prev = Promise.success(null);

    public SequenceFileSystemInputFile(InputFile inputFile) {
        this.inputFile = inputFile;
    }

    @Override
    public synchronized Promise<FilePart> readBlock(int blockSize) {
        int offset = currentOffset;
        currentOffset += blockSize;
        prev = prev.flatMap(r -> inputFile.read(offset, blockSize));
        return prev;
    }

    @Override
    public synchronized Promise<Void> close() {
        return prev.flatMap(r -> inputFile.close());
    }
}