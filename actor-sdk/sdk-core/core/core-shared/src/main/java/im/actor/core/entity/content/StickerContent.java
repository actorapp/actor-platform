/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity.content;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

import im.actor.core.api.ApiFileLocation;
import im.actor.core.api.ApiJsonMessage;
import im.actor.core.api.ApiStickerMessage;
import im.actor.core.api.ApiTextExMarkdown;
import im.actor.core.api.ApiTextMessage;
import im.actor.core.entity.FileReference;
import im.actor.core.entity.content.internal.ContentLocalContainer;
import im.actor.core.entity.content.internal.ContentRemoteContainer;
import im.actor.core.entity.content.internal.Sticker;
import im.actor.runtime.json.JSONException;
import im.actor.runtime.json.JSONObject;

/**
 * Created by ex3ndr on 25.05.15.
 */
public class StickerContent extends AbsContent {

    Sticker sticker;

    @NotNull
    public static StickerContent create(@NotNull Sticker sticker) {

        return new StickerContent(new ContentRemoteContainer(
                new ApiStickerMessage(sticker.getId(), null, sticker.getApiImageLocation512(),
                        sticker.getApiImageLocation256(), sticker.getStickerCollectionId(), sticker.getCollectionAccessHash())));
    }

    public StickerContent(ContentRemoteContainer remoteContainer) {
        super(remoteContainer);
        sticker = new Sticker((ApiStickerMessage) remoteContainer.getMessage());

    }

    public StickerContent(ContentLocalContainer localContainer) {
        super(localContainer);
        Sticker content = (Sticker) localContainer.getContent();
        sticker = new Sticker(content.getApiImageLocation128(), content.getApiImageLocation256(), content.getApiImageLocation512(), content.getStickerId(), content.getStickerCollectionId(), content.getCollectionAccessHash(), content.getThumb());

    }

    public Sticker getSticker() {
        return sticker;
    }
}
