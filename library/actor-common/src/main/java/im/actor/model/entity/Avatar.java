/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.entity;

import java.io.IOException;

import im.actor.model.droidkit.bser.Bser;
import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;

public class Avatar extends WrapperEntity<im.actor.model.api.Avatar> {

    public static Avatar fromBytes(byte[] data) throws IOException {
        return Bser.parse(new Avatar(), data);
    }

    private static final int RECORD_ID = 10;

    private AvatarImage smallImage;
    private AvatarImage largeImage;
    private AvatarImage fullImage;

    public Avatar(im.actor.model.api.Avatar wrapped) {
        super(RECORD_ID, wrapped);
    }

    public Avatar() {
        super(RECORD_ID);
    }

    public AvatarImage getSmallImage() {
        return smallImage;
    }

    public AvatarImage getLargeImage() {
        return largeImage;
    }

    public AvatarImage getFullImage() {
        return fullImage;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        // Is New Layout
        if (!values.getBool(5, false)) {
            im.actor.model.api.AvatarImage smallImage = null;
            im.actor.model.api.AvatarImage largeImage = null;
            im.actor.model.api.AvatarImage fullImage = null;

            byte[] small = values.optBytes(1);
            if (small != null) {
                AvatarImage oldSmallImage = AvatarImage.fromBytes(small);
                smallImage = new im.actor.model.api.AvatarImage(
                        oldSmallImage.getFileReference().getFileLocation(),
                        oldSmallImage.getWidth(),
                        oldSmallImage.getHeight(),
                        oldSmallImage.getFileReference().getFileSize());
            }

            byte[] large = values.optBytes(2);
            if (large != null) {
                AvatarImage oldLargeImage = AvatarImage.fromBytes(large);
                largeImage = new im.actor.model.api.AvatarImage(
                        oldLargeImage.getFileReference().getFileLocation(),
                        oldLargeImage.getWidth(),
                        oldLargeImage.getHeight(),
                        oldLargeImage.getFileReference().getFileSize());
            }

            byte[] full = values.optBytes(3);
            if (full != null) {
                AvatarImage oldFullImage = AvatarImage.fromBytes(full);
                fullImage = new im.actor.model.api.AvatarImage(
                        oldFullImage.getFileReference().getFileLocation(),
                        oldFullImage.getWidth(),
                        oldFullImage.getHeight(),
                        oldFullImage.getFileReference().getFileSize());
            }

            setWrapped(new im.actor.model.api.Avatar(smallImage, largeImage, fullImage));
        }

        // Deserialize wrapper
        super.parse(values);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        // Mark as new layout
        writer.writeBool(5, true);
        // Write wrapped object
        super.serialize(writer);
    }

    @Override
    protected void applyWrapped(im.actor.model.api.Avatar wrapped) {
        if (wrapped == null) {
            smallImage = null;
            largeImage = null;
            fullImage = null;
        } else {
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
    protected im.actor.model.api.Avatar createInstance() {
        return new im.actor.model.api.Avatar();
    }
}