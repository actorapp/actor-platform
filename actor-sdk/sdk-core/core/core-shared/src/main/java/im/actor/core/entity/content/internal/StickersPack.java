/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity.content.internal;

import com.google.j2objc.annotations.Property;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.actor.core.api.ApiStickerCollection;
import im.actor.core.api.ApiStickerDescriptor;
import im.actor.core.entity.WrapperEntity;
import im.actor.runtime.bser.BserCreator;
import im.actor.runtime.storage.KeyValueItem;
import im.actor.runtime.storage.ListEngineItem;

/**
 * Created by Jesus Christ. Amen.
 */
public class StickersPack extends WrapperEntity<ApiStickerCollection> implements KeyValueItem, ListEngineItem {

    private static final int RECORD_ID = 10;
    public static BserCreator<StickersPack> CREATOR = new BserCreator<StickersPack>() {
        @Override
        public StickersPack createInstance() {
            return new StickersPack();
        }
    };


    @Property("readonly, nonatomic")
    private int id;
    @Property("readonly, nonatomic")
    private long accessHash;
    @NotNull
    @Property("readonly, nonatomic")
    @SuppressWarnings("NullableProblems")
    private List<Sticker> stickers;

    public StickersPack(@NotNull ApiStickerCollection wrappedPack) {
        super(RECORD_ID, wrappedPack);
    }

    public StickersPack(@NotNull byte[] data) throws IOException {
        super(RECORD_ID, data);
    }

    private StickersPack() {
        super(RECORD_ID);
    }

    public StickersPack updateStickers(List<ApiStickerDescriptor> nStickers) {
        ApiStickerCollection w = getWrapped();
        ApiStickerCollection res = new ApiStickerCollection(
                w.getId(),
                w.getAccessHash(),
                nStickers);
        res.setUnmappedObjects(w.getUnmappedObjects());
        return new StickersPack(res);
    }

    @Override
    protected void applyWrapped(@NotNull ApiStickerCollection wrapped) {
        this.id = wrapped.getId();
        this.accessHash = wrapped.getAccessHash();
        this.stickers = new ArrayList<Sticker>();
        for (ApiStickerDescriptor m : wrapped.getStickers()) {
            this.stickers.add(new Sticker(m, id, accessHash));
        }
    }


    @Override
    public long getEngineId() {
        return id;
    }

    @Override
    public long getEngineSort() {
        return id;
    }

    @Override
    public String getEngineSearch() {
        return null;
    }

    @NotNull
    @Override
    protected ApiStickerCollection createInstance() {
        return new ApiStickerCollection();
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
}