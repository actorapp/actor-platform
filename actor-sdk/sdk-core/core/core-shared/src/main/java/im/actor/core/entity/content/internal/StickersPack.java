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
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;
import im.actor.runtime.storage.KeyValueItem;
import im.actor.runtime.storage.ListEngineItem;

/**
 * Created by Jesus Christ. Amen.
 */
public class StickersPack extends WrapperEntity<ApiStickerCollection> implements KeyValueItem, ListEngineItem {

    public static final String ENTITY_NAME = "StickersPack";


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
    private int localId;
    @Property("readonly, nonatomic")
    private long accessHash;
    @NotNull
    @Property("readonly, nonatomic")
    @SuppressWarnings("NullableProblems")
    private List<Sticker> stickers;

    public static StickersPack createLocalStickerPack(@NotNull ApiStickerCollection wrappedPack, int localId) {
        StickersPack res = new StickersPack();
        res.localId = localId;
        res.setWrapped(wrappedPack);
        return res;
    }

    public StickersPack(@NotNull ApiStickerCollection wrappedPack) {
        super(RECORD_ID, wrappedPack);
    }

    public StickersPack(@NotNull byte[] data) throws IOException {
        super(RECORD_ID, data);
    }

    private StickersPack() {
        super(RECORD_ID);
    }

    @Override
    protected void applyWrapped(@NotNull ApiStickerCollection wrapped) {
        this.id = wrapped.getId();
        this.accessHash = wrapped.getAccessHash();
        this.stickers = new ArrayList<Sticker>();
        for (ApiStickerDescriptor m : wrapped.getStickers()) {
            this.stickers.add(new Sticker(m, id, localId, accessHash));
        }
    }


    @Override
    public long getEngineId() {
        return id;
    }

    @Override
    public long getEngineSort() {
        return localId * -1;
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

    public int getLocalId() {
        return localId;
    }

    public long getAccessHash() {
        return accessHash;
    }

    @NotNull
    public List<Sticker> getStickers() {
        return stickers;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        super.parse(values);
        localId = values.getInt(9, 0);

    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        super.serialize(writer);
        writer.writeInt(9, localId);

    }
}