package im.actor.core.modules.updates.internal;

import java.util.List;

import im.actor.core.api.ApiStickerCollection;

/**
 * Created by root on 12/15/15.
 */
public class StickersLoaded extends InternalUpdate {
    private List<ApiStickerCollection> collections;

    public StickersLoaded(List<ApiStickerCollection> collections) {
        this.collections = collections;
    }

    public List<ApiStickerCollection> getCollections() {
        return collections;
    }
}
