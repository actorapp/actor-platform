/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity.compat;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import im.actor.core.api.ApiAvatarImage;
import im.actor.core.api.ApiFileLocation;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;

public class ObsoleteAvatarImage extends BserObject {

    private int width;
    private int height;
    @NotNull
    @SuppressWarnings("NullableProblems")
    private ObsoleteFileReference fileReference;

    public ObsoleteAvatarImage(@NotNull byte[] data) throws IOException {
        load(data);
    }

    public ObsoleteAvatarImage(@NotNull BserValues values) throws IOException {
        parse(values);
    }

    @NotNull
    public ApiAvatarImage toApiAvatarImage() {
        return new ApiAvatarImage(new ApiFileLocation(
                fileReference.getFileId(),
                fileReference.getAccessHash()),
                width,
                height,
                fileReference.getFileSize());
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @NotNull
    public ObsoleteFileReference getFileReference() {
        return fileReference;
    }

    @Override
    public void parse(@NotNull BserValues values) throws IOException {
        width = values.getInt(1);
        height = values.getInt(2);
        fileReference = new ObsoleteFileReference(values.getBytes(3));
    }

    @Override
    public void serialize(@NotNull BserWriter writer) throws IOException {
        throw new UnsupportedOperationException();
    }
}
