/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

import im.actor.core.api.ApiImageLocation;
import im.actor.core.api.ApiStickerDescriptor;
import im.actor.core.api.ApiStickerMessage;
import im.actor.core.entity.content.internal.AbsLocalContent;
import im.actor.runtime.bser.BserCreator;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;
import im.actor.runtime.storage.ListEngineItem;

public class Sticker extends WrapperEntity<ApiStickerDescriptor> {

    public static final BserCreator<Sticker> CREATOR = Sticker::new;

    private static final int RECORD_ID = 10;


    @NotNull
    private FileReference image128;
    @NotNull
    private ApiImageLocation image128Location;
    @Nullable
    private FileReference image256;
    @Nullable
    private ApiImageLocation image256Location;
    @Nullable
    private FileReference image512;
    @Nullable
    private ApiImageLocation image512Location;

    private int id;
    @Nullable
    private String emoji;
    @Nullable
    private Integer collectionId;
    @Nullable
    private Long collectionAccessHash;

    public Sticker(ApiStickerDescriptor descriptor, Integer collectionId, Long collectionAccessHash) {
        super(RECORD_ID, descriptor);
        this.collectionId = collectionId;
        this.collectionAccessHash = collectionAccessHash;
    }

    public Sticker(byte[] data) throws IOException {
        this();
        load(data);
    }

    private Sticker() {
        super(RECORD_ID);
    }

    @Override
    protected void applyWrapped(@NotNull ApiStickerDescriptor wrapped) {
        emoji = wrapped.getEmoji();
        id = wrapped.getId();
        image128Location = wrapped.getImage128();
        image128 = new FileReference(image128Location.getFileLocation(), "sticker.webp", image128Location.getFileSize());
        if (wrapped.getImage256() != null) {
            image256Location = wrapped.getImage256();
            image256 = new FileReference(image256Location.getFileLocation(), "sticker.webp", image256Location.getFileSize());
        }
        if (wrapped.getImage512() != null) {
            image512Location = wrapped.getImage512();
            image512 = new FileReference(wrapped.getImage512().getFileLocation(), "sticker.webp", wrapped.getImage512().getFileSize());
        }
    }


    @NotNull
    public FileReference getImage128() {
        return image128;
    }

    @Nullable
    public FileReference getImage256() {
        return image256;
    }

    @Nullable
    public FileReference getImage512() {
        return image512;
    }

    public int getId() {
        return id;
    }

    @Nullable
    public String getEmoji() {
        return emoji;
    }

    @Nullable
    public Integer getCollectionId() {
        return collectionId;
    }

    @Nullable
    public Long getCollectionAccessHash() {
        return collectionAccessHash;
    }

    public ApiStickerMessage toMessage() {
        return new ApiStickerMessage(id, null, image512Location, image256Location, collectionId, collectionAccessHash);
    }

    public ApiStickerDescriptor toApi() {
        return getWrapped();
    }

    @NotNull
    @Override
    protected ApiStickerDescriptor createInstance() {
        return new ApiStickerDescriptor();
    }
}
