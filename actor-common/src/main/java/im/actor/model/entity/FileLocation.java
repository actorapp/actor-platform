package im.actor.model.entity;

/**
 * Created by ex3ndr on 09.02.15.
 */
public class FileLocation {
    private final long fileId;
    private final long accessHash;
    private final int fileSize;

    public FileLocation(long fileId, long accessHash, int fileSize) {
        this.fileId = fileId;
        this.accessHash = accessHash;
        this.fileSize = fileSize;
    }

    public int getFileSize() {
        return fileSize;
    }

    public long getFileId() {
        return fileId;
    }

    public long getAccessHash() {
        return accessHash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FileLocation that = (FileLocation) o;

        if (fileId != that.fileId) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return (int) (fileId ^ (fileId >>> 32));
    }
}
