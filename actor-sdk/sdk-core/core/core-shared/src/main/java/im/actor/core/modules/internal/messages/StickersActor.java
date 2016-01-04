package im.actor.core.modules.internal.messages;

import java.util.ArrayList;
import java.util.List;

import im.actor.core.api.ApiStickerCollection;
import im.actor.core.api.ApiStickerDescriptor;
import im.actor.core.api.rpc.RequestLoadOwnStickers;
import im.actor.core.api.rpc.ResponseLoadOwnStickers;
import im.actor.core.entity.content.internal.Sticker;
import im.actor.core.entity.content.internal.StickersPack;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.updates.internal.StickersLoaded;
import im.actor.core.network.RpcCallback;
import im.actor.core.network.RpcException;
import im.actor.core.util.ModuleActor;
import im.actor.runtime.storage.ListEngine;

public class StickersActor extends ModuleActor {
    private ListEngine<StickersPack> packs;
    private ListEngine<Sticker> stickers;

    public StickersActor(ModuleContext context) {
        super(context);
    }

    @Override
    public void preStart() {
        this.packs = context().getStickersModule().getPacksEngine();
        this.stickers = context().getStickersModule().getStickersEngine();
        if (packs.getCount() == 0) {
            loadStickers();
        }
    }

    private void onStickerCollectionsChanged(List<ApiStickerCollection> updated) {
        ArrayList<StickersPack> add = new ArrayList<StickersPack>();
        ArrayList<Sticker> addStickers = new ArrayList<Sticker>();
        ArrayList<Sticker> removeStickers = new ArrayList<Sticker>();
        for (ApiStickerCollection collection : updated) {
            add.add(StickersPack.createLocalStickerPack(collection, getLocalCollectionId(collection.getId())));
            if (collection.getStickers().size() > 0) {
                stickers.addOrUpdateItem(new Sticker(collection.getStickers().get(0), collection.getId(), getLocalCollectionId(collection.getId()), collection.getAccessHash(), true));
            }
            StickersPack oldPack = packs.getValue(collection.getId());
            if (oldPack != null) {
                for (Sticker oldSt : oldPack.getStickers()) {
                    removeStickers.add(stickers.getValue(oldSt.getId()));
                }
            }
            for (ApiStickerDescriptor s : collection.getStickers()) {
                removeStickers.remove(stickers.getValue(s.getId()));
                if (stickers.getValue(s.getId()) == null) {
                    addStickers.add(new Sticker(s, collection.getId(), getLocalCollectionId(collection.getId()), collection.getAccessHash()));
                }
            }
        }
        long[] removeStickersIds = new long[removeStickers.size()];
        for (int i = 0; i < removeStickers.size(); i++) {
            removeStickersIds[i] = removeStickers.get(i).getId();
        }
        stickers.removeItems(removeStickersIds);
        packs.addOrUpdateItems(add);
        stickers.addOrUpdateItems(addStickers);
    }

    private void onOwnStickerCollectionsChanged(List<ApiStickerCollection> updated, List<ApiStickerCollection> removed) {
        onStickerCollectionsChanged(updated);

        long[] remove = new long[removed.size()];
        for (int i = 0; i < removed.size(); i++) {
            remove[i] = removed.get(i).getId();
            stickers.removeItem(getLocalCollectionId(removed.get(i).getId()));
            List<ApiStickerDescriptor> stickersToRemove = removed.get(i).getStickers();
            long[] removeSt = new long[stickersToRemove.size()];
            for (int j = 0; i < stickersToRemove.size(); i++) {
                removeSt[j] = stickersToRemove.get(j).getId();
            }
            stickers.removeItems(removeSt);
        }
        packs.removeItems(remove);
    }


    @Override
    public void onReceive(Object message) {
        if (message instanceof StickerCollectionsChanged) {
            onStickerCollectionsChanged(((StickerCollectionsChanged) message).getUpdated());
        } else if (message instanceof OwnStickerCollectionsChanged) {
            onOwnStickerCollectionsChanged(((OwnStickerCollectionsChanged) message).getUpdated(), ((OwnStickerCollectionsChanged) message).getRemoved());
        }
    }

    public static class OwnStickerCollectionsChanged {
        List<ApiStickerCollection> updated;
        List<ApiStickerCollection> removed;

        public OwnStickerCollectionsChanged(List<ApiStickerCollection> updated, List<ApiStickerCollection> removed) {
            this.updated = updated;
            this.removed = removed;
        }

        public List<ApiStickerCollection> getUpdated() {
            return updated;
        }

        public List<ApiStickerCollection> getRemoved() {
            return removed;
        }
    }

    public static class StickerCollectionsChanged {
        List<ApiStickerCollection> updated;

        public StickerCollectionsChanged(List<ApiStickerCollection> updated) {
            this.updated = updated;
        }

        public List<ApiStickerCollection> getUpdated() {
            return updated;
        }
    }

    public void loadStickers() {
        request(new RequestLoadOwnStickers(), new RpcCallback<ResponseLoadOwnStickers>() {
            @Override
            public void onResult(final ResponseLoadOwnStickers response) {
                updates().onUpdateReceived(
                        new StickersLoaded(response.getOwnStickers())
                );
            }

            @Override
            public void onError(RpcException e) {

            }
        });
    }

    public int getLocalCollectionId(int collectionId) {
        int id = context().getPreferences().getInt("sticker_packs_local_id_map" + collectionId, -1);
        if (id == -1) {
            id = context().getPreferences().getInt("sticker_packs_local_id_increment", 0);
            context().getPreferences().putInt("sticker_packs_local_id_map" + collectionId, id);
            context().getPreferences().putInt("sticker_packs_local_id_increment", ++id);
        }
        return id;
    }
}
