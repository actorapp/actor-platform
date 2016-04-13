package im.actor.core.modules.sequence.internal;

import java.util.List;

import im.actor.core.api.ApiStickerCollection;

public class StickersLoaded extends InternalUpdate {
    private List<ApiStickerCollection> collections;

    public StickersLoaded(List<ApiStickerCollection> collections) {
        this.collections = collections;
    }

    public List<ApiStickerCollection> getCollections() {
        return collections;
    }
}
