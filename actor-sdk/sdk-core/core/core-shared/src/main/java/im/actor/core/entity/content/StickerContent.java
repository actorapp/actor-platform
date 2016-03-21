/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity.content;

import com.google.j2objc.annotations.Property;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import im.actor.core.api.ApiImageLocation;
import im.actor.core.api.ApiStickerMessage;
import im.actor.core.entity.FileReference;
import im.actor.core.entity.ImageLocation;
import im.actor.core.entity.content.internal.ContentLocalContainer;
import im.actor.core.entity.content.internal.ContentRemoteContainer;
import im.actor.core.entity.Sticker;

public class StickerContent extends AbsContent {

    @NotNull
    public static StickerContent create(@NotNull Sticker sticker) {
        return new StickerContent(new ContentRemoteContainer(sticker.toMessage()));
    }

    @Nullable
    @Property("readonly, nonatomic")
    private ImageLocation image256;
    @Nullable
    @Property("readonly, nonatomic")
    private ImageLocation image512;
    @Nullable
    @Property("readonly, nonatomic")
    private Integer id;
    @Nullable
    @Property("readonly, nonatomic")
    private Integer collectionId;
    @Nullable
    @Property("readonly, nonatomic")
    private Long collectionAccessHash;

    public StickerContent(ContentRemoteContainer remoteContainer) {
        super(remoteContainer);

        ApiStickerMessage stickerMessage = (ApiStickerMessage) remoteContainer.getMessage();

        id = stickerMessage.getStickerId();
        collectionId = stickerMessage.getStickerCollectionId();
        collectionAccessHash = stickerMessage.getStickerCollectionAccessHash();
        if (stickerMessage.getImage512() != null) {
            image512 = new ImageLocation(stickerMessage.getImage512(), "sticker.webp");
        }
        if (stickerMessage.getImage256() != null) {
            image256 = new ImageLocation(stickerMessage.getImage256(), "sticker.webp");
        }
    }

    @Nullable
    public ImageLocation getImage256() {
        return image256;
    }

    @Nullable
    public ImageLocation getImage512() {
        return image512;
    }

    @Nullable
    public Integer getId() {
        return id;
    }

    @Nullable
    public Integer getCollectionId() {
        return collectionId;
    }

    @Nullable
    public Long getCollectionAccessHash() {
        return collectionAccessHash;
    }
}