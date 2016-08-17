/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity;

import com.google.j2objc.annotations.Property;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import im.actor.core.api.ApiDocumentEncryptionInfo;
import im.actor.core.api.ApiFileLocation;
import im.actor.runtime.bser.Bser;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;

// Disabling Bounds checks for speeding up calculations

/*-[
#define J2OBJC_DISABLE_ARRAY_BOUND_CHECKS 1
]-*/

public class FileReference extends WrapperEntity<ApiFileLocation> {

    private static final int RECORD_ID = 10;

    @Property("readonly, nonatomic")
    private int fileSize;
    @Property("readonly, nonatomic")
    private String fileName;
    @Property("readonly, nonatomic")
    private ApiDocumentEncryptionInfo encryptionInfo;

    public FileReference(ApiFileLocation fileLocation, String fileName, int fileSize,
                         ApiDocumentEncryptionInfo encryptionInfo) {
        super(RECORD_ID, fileLocation);
        this.fileSize = fileSize;
        this.fileName = fileName;
        this.encryptionInfo = encryptionInfo;
    }

    public FileReference(byte[] data) throws IOException {
        super(RECORD_ID, data);
    }

    public ApiFileLocation getFileLocation() {
        return getWrapped();
    }

    public int getFileSize() {
        return fileSize;
    }

    public long getFileId() {
        return getWrapped().getFileId();
    }

    public long getAccessHash() {
        return getWrapped().getAccessHash();
    }

    public String getFileName() {
        return fileName;
    }

    public ApiDocumentEncryptionInfo getEncryptionInfo() {
        return encryptionInfo;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        // Is Old layout
        if (!values.getBool(5, false)) {
            long fileId = values.getLong(1);
            long accessHash = values.getLong(2);
            setWrapped(new ApiFileLocation(fileId, accessHash));
        }

        // Deserialize new layout
        super.parse(values);

        fileSize = values.getInt(3);
        fileName = values.getString(4);

        byte[] data = values.optBytes(6);
        if (data != null) {
            encryptionInfo = Bser.parse(new ApiDocumentEncryptionInfo(), data);
        }
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        // Mark as new layout
        writer.writeBool(5, true);
        writer.writeInt(3, fileSize);
        writer.writeString(4, fileName);

        if (encryptionInfo != null) {
            writer.writeObject(6, encryptionInfo);
        }

        // Write wrapper
        super.serialize(writer);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FileReference that = (FileReference) o;

        if (getWrapped().getFileId() != that.getWrapped().getFileId()) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return (int) (getWrapped().getFileId() ^ (getWrapped().getFileId() >>> 32));
    }

    @Override
    @NotNull
    protected ApiFileLocation createInstance() {
        return new ApiFileLocation();
    }
}
