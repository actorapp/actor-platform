/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity;

import com.google.j2objc.annotations.Property;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.actor.core.api.ApiStickerCollection;
import im.actor.core.api.ApiStickerDescriptor;
import im.actor.runtime.bser.BserCreator;

public class StickerPack extends WrapperEntity<ApiStickerCollection> {

    private static final int RECORD_ID = 10;

    public static final BserCreator<StickerPack> CREATOR = StickerPack::new;

    @Property("readonly, nonatomic")
    private int id;
    @Property("readonly, nonatomic")
    private long accessHash;
    @NotNull
    @Property("readonly, nonatomic")
    @SuppressWarnings("NullableProblems")
    private List<Sticker> stickers;

    public StickerPack(@NotNull ApiStickerCollection wrappedPack) {
        super(RECORD_ID, wrappedPack);
    }

    public StickerPack(@NotNull byte[] data) throws IOException {
        super(RECORD_ID, data);
    }

    private StickerPack() {
        super(RECORD_ID);
    }

    public int getId() {
        return id;
    }

    public long getAccessHash() {
        return accessHash;
    }

    @NotNull
    public List<Sticker> getStickers() {
        return stickers;
    }

    @Override
    protected void applyWrapped(@NotNull ApiStickerCollection wrapped) {
        this.id = wrapped.getId();
        this.accessHash = wrapped.getAccessHash();
        this.stickers = new ArrayList<>();
        for (ApiStickerDescriptor m : wrapped.getStickers()) {
            this.stickers.add(new Sticker(m, id, accessHash));
        }
    }

    @NotNull
    @Override
    protected ApiStickerCollection createInstance() {
        return new ApiStickerCollection();
    }
}