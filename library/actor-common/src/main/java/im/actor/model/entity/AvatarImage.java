/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.entity;


import java.io.IOException;

import im.actor.model.droidkit.bser.Bser;
import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;

public class AvatarImage extends WrapperEntity<im.actor.model.api.AvatarImage> {

    public static AvatarImage fromBytes(byte[] data) throws IOException {
        return Bser.parse(new AvatarImage(), data);
    }

    private static final int RECORD_ID = 10;

    private int width;
    private int height;
    private FileReference fileReference;

    public AvatarImage(im.actor.model.api.AvatarImage wrapped) {
        super(RECORD_ID, wrapped);
    }

    private AvatarImage() {
        super(RECORD_ID);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public FileReference getFileReference() {
        return fileReference;
    }

    @Override
    public void parse(BserValues values) throws IOException {

        // If Old Layout used
        if (!values.getBool(5, false)) {
            int width = values.getInt(1);
            int height = values.getInt(2);
            FileReference fileReference = FileReference.fromBytes(values.getBytes(3));
            setWrapped(new im.actor.model.api.AvatarImage(fileReference.getFileLocation(),
                    width, height, fileReference.getFileSize()));
        }

        super.parse(values);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeBool(5, true);
        super.serialize(writer);
    }

    @Override
    protected void applyWrapped(im.actor.model.api.AvatarImage wrapped) {
        this.width = wrapped.getWidth();
        this.height = wrapped.getHeight();
        this.fileReference = new FileReference(wrapped.getFileLocation(),
                "avatar.jpg", wrapped.getFileSize());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AvatarImage that = (AvatarImage) o;

        if (height != that.height) return false;
        if (width != that.width) return false;
        if (!fileReference.equals(that.fileReference)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = width;
        result = 31 * result + height;
        result = 31 * result + fileReference.hashCode();
        return result;
    }

    @Override
    protected im.actor.model.api.AvatarImage createInstance() {
        return new im.actor.model.api.AvatarImage();
    }
}
