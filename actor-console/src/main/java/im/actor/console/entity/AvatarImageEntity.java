package im.actor.console.entity;

import java.io.IOException;

import im.actor.model.droidkit.bser.BserObject;
import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;
import im.actor.model.entity.AvatarImage;
import im.actor.model.entity.FileReference;

/**
 * Created by ex3ndr on 09.02.15.
 */
public class AvatarImageEntity extends BserObject {

    private int w;
    private int h;
    private long fileId;
    private long accessHash;
    private int fileSize;

    public AvatarImageEntity(AvatarImage avatarImage) {
        w = avatarImage.getWidth();
        h = avatarImage.getHeight();
        fileId = avatarImage.getFileReference().getFileId();
        accessHash = avatarImage.getFileReference().getAccessHash();
        fileSize = avatarImage.getFileReference().getFileSize();
    }

    public AvatarImageEntity(int w, int h, long fileId, long accessHash, int fileSize) {
        this.w = w;
        this.h = h;
        this.fileId = fileId;
        this.accessHash = accessHash;
        this.fileSize = fileSize;
    }

    public AvatarImageEntity() {

    }

    public int getW() {
        return w;
    }

    public int getH() {
        return h;
    }

    public long getFileId() {
        return fileId;
    }

    public long getAccessHash() {
        return accessHash;
    }

    public int getFileSize() {
        return fileSize;
    }

    public AvatarImage getImage() {
        return new AvatarImage(w, h, new FileReference(fileId, accessHash, fileSize, ""));
    }

    @Override
    public void parse(BserValues values) throws IOException {
        w = values.getInt(1);
        h = values.getInt(2);
        fileId = values.getLong(3);
        accessHash = values.getLong(4);
        fileSize = values.getInt(5);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeInt(1, w);
        writer.writeInt(2, h);
        writer.writeLong(3, fileId);
        writer.writeLong(4, accessHash);
        writer.writeInt(5, fileSize);
    }
}
