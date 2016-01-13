/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity.content.internal;


import java.io.IOException;

import im.actor.core.api.ApiImageLocation;
import im.actor.core.api.ApiStickerDescriptor;
import im.actor.core.api.ApiStickerMessage;
import im.actor.core.entity.FileReference;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;

/**
 * Created by Jesus Christ. Amen.
 */
public class Sticker extends AbsLocalContent {
    ApiImageLocation apiImageLocation128;
    ApiImageLocation apiImageLocation256;
    ApiImageLocation apiImageLocation512;
    byte[] thumb;

    int stickerId;
    int stickerCollectionId;
    long collectionAccessHash;

    public Sticker(ApiStickerDescriptor apiStickerDescriptor, int collectionId, long accessHash) {
        this.collectionAccessHash = accessHash;
        this.stickerCollectionId = collectionId;
        this.stickerId = apiStickerDescriptor.getId();

        this.apiImageLocation128 = apiStickerDescriptor.getImage128();
        this.apiImageLocation256 = apiStickerDescriptor.getImage256();
        this.apiImageLocation512 = apiStickerDescriptor.getImage512();

    }

    public Sticker(ApiStickerMessage msg) {
        thumb = msg.getFastPreview();
        this.stickerId = msg.getStickerId();
        this.stickerCollectionId = msg.getStickerCollectionId();
        this.collectionAccessHash = msg.getStickerCollectionAccessHash();
        this.apiImageLocation256 = msg.getImage256();
        this.apiImageLocation512 = msg.getImage512();

    }

    public Sticker(ApiImageLocation apiImageLocation128, ApiImageLocation apiImageLocation256, ApiImageLocation apiImageLocation512, int stickerId, int stickerCollectionId, long collectionAccessHash, byte[] thumb) {
        this.thumb = thumb;
        this.apiImageLocation128 = apiImageLocation128;
        this.apiImageLocation256 = apiImageLocation256;
        this.apiImageLocation512 = apiImageLocation512;
        this.stickerId = stickerId;
        this.stickerCollectionId = stickerCollectionId;
        this.collectionAccessHash = collectionAccessHash;
    }

    public FileReference getFileReference128() {
        if (apiImageLocation128 == null) {
            return null;
        }
        return new FileReference(apiImageLocation128.getFileLocation(), "", apiImageLocation128.getFileSize());
    }

    public FileReference getFileReference256() {
        if (apiImageLocation256 == null) {
            return null;
        }
        return new FileReference(apiImageLocation256.getFileLocation(), "", apiImageLocation256.getFileSize());
    }

    public FileReference getFileReference512() {
        if (apiImageLocation512 == null) {
            return null;
        }
        return new FileReference(apiImageLocation512.getFileLocation(), "", apiImageLocation512.getFileSize());
    }

    public int getId() {
        return stickerId;
    }

    public ApiImageLocation getApiImageLocation128() {
        return apiImageLocation128;
    }

    public ApiImageLocation getApiImageLocation256() {
        return apiImageLocation256;
    }

    public ApiImageLocation getApiImageLocation512() {
        return apiImageLocation512;
    }

    public int getHeight128() {
        if (apiImageLocation128 == null) {
            return 0;
        }
        return apiImageLocation128.getHeight();
    }


    public int getWidth128() {
        if (apiImageLocation128 == null) {
            return 0;
        }
        return apiImageLocation128.getWidth();
    }

    public int getHeight256() {
        if (apiImageLocation256 == null) {
            return 0;
        }
        return apiImageLocation256.getHeight();
    }

    public int getWidth256() {
        if (apiImageLocation256 == null) {
            return 0;
        }
        return apiImageLocation256.getWidth();
    }

    public int getHeight512() {
        if (apiImageLocation512 == null) {
            return 0;
        }
        return apiImageLocation512.getHeight();
    }

    public int getWidth512() {
        if (apiImageLocation512 == null) {
            return 0;
        }
        return apiImageLocation512.getWidth();
    }

    public byte[] getThumb() {
        return thumb;
    }

    public void setThumb(byte[] thumb) {
        this.thumb = thumb;
    }

    public int getStickerId() {
        return stickerId;
    }

    public long getCollectionAccessHash() {
        return collectionAccessHash;
    }

    public int getStickerCollectionId() {
        return stickerCollectionId;
    }


    @Override
    public void parse(BserValues values) throws IOException {
        apiImageLocation128 = values.getObj(1, new ApiImageLocation());
        apiImageLocation256 = values.getObj(2, new ApiImageLocation());
        apiImageLocation512 = values.getObj(3, new ApiImageLocation());

        stickerId = values.getInt(4);
        stickerCollectionId = values.getInt(5);
        collectionAccessHash = values.getLong(6);

        thumb = values.getBytes(7);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeObject(1, apiImageLocation128);
        writer.writeObject(2, apiImageLocation256);
        writer.writeObject(3, apiImageLocation512);

        writer.writeInt(4, stickerId);
        writer.writeInt(5, stickerCollectionId);
        writer.writeLong(6, collectionAccessHash);

        writer.writeBytes(7, thumb);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Sticker && ((Sticker) o).getId() == getId();
    }
}
