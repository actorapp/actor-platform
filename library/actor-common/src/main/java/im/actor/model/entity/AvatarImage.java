/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.entity;


import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;

public class AvatarImage extends WrapperEntity<im.actor.model.api.AvatarImage> {

    private static final int RECORD_ID = 10;

    private int width;
    private int height;
    @NotNull
    @SuppressWarnings("NullableProblems")
    private FileReference fileReference;

    public AvatarImage(@NotNull im.actor.model.api.AvatarImage wrapped) {
        super(RECORD_ID, wrapped);
    }

    public AvatarImage(@NotNull byte[] data) throws IOException {
        super(RECORD_ID, data);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @NotNull
    public FileReference getFileReference() {
        return fileReference;
    }

    @Override
    public void parse(BserValues values) throws IOException {

        // If Old Layout used
        if (!values.getBool(5, false)) {
            int width = values.getInt(1);
            int height = values.getInt(2);
            FileReference fileReference = new FileReference(values.getBytes(3));
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
    protected void applyWrapped(@NotNull im.actor.model.api.AvatarImage wrapped) {
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
    @NotNull
    protected im.actor.model.api.AvatarImage createInstance() {
        return new im.actor.model.api.AvatarImage();
    }
}
