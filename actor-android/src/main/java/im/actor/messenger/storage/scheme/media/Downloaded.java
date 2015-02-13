package im.actor.messenger.storage.scheme.media;

import com.droidkit.bser.BserObject;
import com.droidkit.bser.BserValues;
import com.droidkit.bser.BserWriter;
import com.droidkit.engine.keyvalue.KeyValueIdentity;

import java.io.IOException;

/**
 * Created by ex3ndr on 22.10.14.
 */
public class Downloaded extends BserObject implements KeyValueIdentity {
    private long fileId;
    private int fileSize;
    private String name;
    private String downloadedPath;

    public Downloaded(long fileId, int fileSize, String name, String downloadedPath) {
        this.fileId = fileId;
        this.fileSize = fileSize;
        this.name = name;
        this.downloadedPath = downloadedPath;
    }

    public Downloaded() {

    }

    public long getFileId() {
        return fileId;
    }

    public int getFileSize() {
        return fileSize;
    }

    public String getName() {
        return name;
    }

    public String getDownloadedPath() {
        return downloadedPath;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        fileId = values.getLong(1);
        fileSize = values.getInt(2);
        downloadedPath = values.getString(3);
        name = values.getString(4, "file.bin");
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeLong(1, fileId);
        writer.writeInt(2, fileSize);
        writer.writeString(3, downloadedPath);
        writer.writeString(4, name);
    }

    @Override
    public long getKeyValueId() {
        return fileId;
    }
}
