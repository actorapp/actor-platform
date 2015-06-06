/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.entity.compat;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

import im.actor.model.api.Avatar;
import im.actor.model.droidkit.bser.BserObject;
import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;

public class ObsoleteAvatar extends BserObject {

    @Nullable
    private ObsoleteAvatarImage smallImage;
    @Nullable
    private ObsoleteAvatarImage largeImage;
    @Nullable
    private ObsoleteAvatarImage fullImage;

    public ObsoleteAvatar(@NotNull byte[] data) throws IOException {
        load(data);
    }

    public ObsoleteAvatar(@NotNull BserValues values) throws IOException {
        parse(values);
    }

    @NotNull
    public Avatar toApiAvatar() {
        im.actor.model.api.AvatarImage smallImage = null;
        im.actor.model.api.AvatarImage largeImage = null;
        im.actor.model.api.AvatarImage fullImage = null;

        if (this.smallImage != null) {
            smallImage = this.smallImage.toApiAvatarImage();
        }
        if (this.largeImage != null) {
            largeImage = this.largeImage.toApiAvatarImage();
        }
        if (this.fullImage != null) {
            fullImage = this.fullImage.toApiAvatarImage();
        }

        return new Avatar(smallImage, largeImage, fullImage);
    }

    @Nullable
    public ObsoleteAvatarImage getSmallImage() {
        return smallImage;
    }

    @Nullable
    public ObsoleteAvatarImage getLargeImage() {
        return largeImage;
    }

    @Nullable
    public ObsoleteAvatarImage getFullImage() {
        return fullImage;
    }

    @Override
    public void parse(@NotNull BserValues values) throws IOException {
        byte[] small = values.optBytes(1);
        if (small != null) {
            smallImage = new ObsoleteAvatarImage(small);
        }

        byte[] large = values.optBytes(2);
        if (large != null) {
            largeImage = new ObsoleteAvatarImage(large);
        }

        byte[] full = values.optBytes(3);
        if (full != null) {
            fullImage = new ObsoleteAvatarImage(full);
        }
    }

    @Override
    public void serialize(@NotNull BserWriter writer) throws IOException {
        throw new UnsupportedOperationException();
    }
}
