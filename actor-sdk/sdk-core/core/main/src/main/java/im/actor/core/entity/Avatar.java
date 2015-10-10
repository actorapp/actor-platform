/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity;

import com.google.j2objc.annotations.Property;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

import im.actor.core.api.ApiAvatar;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;

public class Avatar extends WrapperEntity<ApiAvatar> {

    private static final int RECORD_ID = 10;

    @Nullable
    @Property("readonly, nonatomic")
    private AvatarImage smallImage;
    @Nullable
    @Property("readonly, nonatomic")
    private AvatarImage largeImage;
    @Nullable
    @Property("readonly, nonatomic")
    private AvatarImage fullImage;

    public Avatar(@NotNull ApiAvatar wrapped) {
        super(RECORD_ID, wrapped);
    }

    public Avatar(@NotNull byte[] data) throws IOException {
        super(RECORD_ID, data);
    }

    public Avatar() {
        super(RECORD_ID, new ApiAvatar());
    }

    @Nullable
    public AvatarImage getSmallImage() {
        return smallImage;
    }

    @Nullable
    public AvatarImage getLargeImage() {
        return largeImage;
    }

    @Nullable
    public AvatarImage getFullImage() {
        return fullImage;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        // Is Wrapper Layout
        if (values.getBool(5, false)) {
            // Parse wrapper layout
            super.parse(values);
        } else {
            // Convert old layout
            throw new IOException("Unsupported obsolete format");
        }
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        // Mark as wrapper layout
        writer.writeBool(5, true);
        // Serialize wrapper layout
        super.serialize(writer);
    }

    @Override
    protected void applyWrapped(@NotNull ApiAvatar wrapped) {
        if (wrapped.getSmallImage() != null) {
            smallImage = new AvatarImage(wrapped.getSmallImage());
        } else {
            smallImage = null;
        }
        if (wrapped.getLargeImage() != null) {
            largeImage = new AvatarImage(wrapped.getLargeImage());
        } else {
            largeImage = null;
        }
        if (wrapped.getFullImage() != null) {
            fullImage = new AvatarImage(wrapped.getFullImage());
        } else {
            fullImage = null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Avatar avatar = (Avatar) o;

        if (fullImage != null ? !fullImage.equals(avatar.fullImage) : avatar.fullImage != null)
            return false;
        if (largeImage != null ? !largeImage.equals(avatar.largeImage) : avatar.largeImage != null)
            return false;
        if (smallImage != null ? !smallImage.equals(avatar.smallImage) : avatar.smallImage != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = smallImage != null ? smallImage.hashCode() : 0;
        result = 31 * result + (largeImage != null ? largeImage.hashCode() : 0);
        result = 31 * result + (fullImage != null ? fullImage.hashCode() : 0);
        return result;
    }

    @Override
    @NotNull
    protected ApiAvatar createInstance() {
        return new ApiAvatar();
    }
}