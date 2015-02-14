package im.actor.messenger.storage.scheme.media;

import com.droidkit.bser.BserObject;
import com.droidkit.bser.BserValues;
import com.droidkit.bser.BserWriter;
import com.droidkit.engine.list.ListItemSearchIdentity;
import im.actor.model.entity.FileLocation;

import java.io.IOException;

/**
 * Created by ex3ndr on 18.10.14.
 */
public class Document extends BserObject implements ListItemSearchIdentity {
    private long id;
    private FileLocation fileLocation;
    private String fileName;
    private String mimeType;
    private int sender;
    private long addedTime;

    public Document(long id, FileLocation fileLocation, String fileName, String mimeType, int sender, long addedTime) {
        this.id = id;
        this.fileLocation = fileLocation;
        this.fileName = fileName;
        this.mimeType = mimeType;
        this.sender = sender;
        this.addedTime = addedTime;
    }

    public Document() {

    }

    public long getId() {
        return id;
    }

    public FileLocation getFileLocation() {
        return fileLocation;
    }

    public String getFileName() {
        return fileName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public int getSender() {
        return sender;
    }

    public long getAddedTime() {
        return addedTime;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        id = values.optLong(6);
        // fileLocation = values.getObj(1, FileLocation.class);
        fileName = values.getString(2);
        mimeType = values.getString(3);
        sender = values.getInt(4);
        addedTime = values.optLong(5);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeLong(6, id);
        // writer.writeObject(1, fileLocation);
        writer.writeString(2, fileName);
        writer.writeString(3, mimeType);
        writer.writeInt(4, sender);
        writer.writeLong(5, addedTime);
    }

    @Override
    public String getQuery() {
        return fileName;
    }

    @Override
    public long getListId() {
        return id;
    }

    @Override
    public long getListSortKey() {
        return addedTime;
    }
}
