package im.actor.core.modules.internal.stickers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.actor.core.api.ApiStickerCollection;
import im.actor.core.api.rpc.RequestLoadOwnStickers;
import im.actor.core.api.rpc.ResponseLoadOwnStickers;
import im.actor.core.entity.StickerPack;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.internal.stickers.entity.StickersStorage;
import im.actor.core.util.ModuleActor;
import im.actor.runtime.function.Consumer;

public class StickersActor extends ModuleActor {

    private boolean isLoaded = false;
    private StickersStorage stickersStorage;

    public StickersActor(ModuleContext context) {
        super(context);
    }

    @Override
    public void preStart() {

        stickersStorage = new StickersStorage();
        byte[] data = context().getPreferences().getBytes("stickers.data");
        if (data != null) {
            try {
                stickersStorage = new StickersStorage(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (!context().getPreferences().getBool("stickers_loaded", false)) {
            api(new RequestLoadOwnStickers()).then(new Consumer<ResponseLoadOwnStickers>() {
                @Override
                public void apply(ResponseLoadOwnStickers responseLoadOwnStickers) {

                    onOwnStickerCollectionChanged(responseLoadOwnStickers.getOwnStickers());

                    context().getPreferences().putBool("stickers_loaded", true);
                    isLoaded = true;
                    unstashAll();
                }
            }).done(self());
        } else {
            isLoaded = true;
            notifyVM();
        }
    }

    private void onOwnStickerCollectionChanged(List<ApiStickerCollection> ownStickers) {
        stickersStorage.getStickerPacks().clear();
        for (ApiStickerCollection collection : ownStickers) {
            stickersStorage.getStickerPacks().add(new StickerPack(collection));
        }
        notifyVM();
        saveStorage();
    }

//    private void onStickerCollectionsChanged(List<ApiStickerCollection> updated) {
//        ArrayList<StickerPack> add = new ArrayList<>();
//        ArrayList<Sticker> addStickers = new ArrayList<>();
//        ArrayList<Sticker> removeStickers = new ArrayList<>();
//        for (ApiStickerCollection collection : updated) {
//            add.add(StickerPack.createLocalStickerPack(collection, collection.getId()));
//            if (collection.getStickers().size() > 0) {
//                Sticker stickerHeader = new Sticker(collection.getStickers().get(0), collection.getId(), collection.getAccessHash());
//                stickers.addOrUpdateItem(stickerHeader);
//            }
//            StickerPack oldPack = packs.getValue(collection.getId());
//            if (oldPack != null) {
//                for (Sticker oldSt : oldPack.getStickers()) {
//                    removeStickers.add(stickers.getValue(oldSt.getId()));
//                }
//            }
//            for (ApiStickerDescriptor s : collection.getStickers()) {
//                removeStickers.remove(stickers.getValue(s.getId()));
//                if (stickers.getValue(s.getId()) == null) {
//                    addStickers.add(new Sticker(s, collection.getId(), collection.getId(), collection.getAccessHash()));
//                }
//            }
//        }
//        long[] removeStickersIds = new long[removeStickers.size()];
//        for (int i = 0; i < removeStickers.size(); i++) {
//            removeStickersIds[i] = removeStickers.get(i).getId();
//        }
//        stickers.removeItems(removeStickersIds);
//        packs.addOrUpdateItems(add);
//        stickers.addOrUpdateItems(addStickers);
//    }
//
//    private void onOwnStickerCollectionsChanged(List<ApiStickerCollection> updated, List<ApiStickerCollection> removed) {
//
//        onStickerCollectionsChanged(updated);
//
//        long[] remove = new long[removed.size()];
//        for (int i = 0; i < removed.size(); i++) {
//            remove[i] = removed.get(i).getId();
//            stickers.removeItem(removed.get(i).getId());
//            List<ApiStickerDescriptor> stickersToRemove = removed.get(i).getStickers();
//            long[] removeSt = new long[stickersToRemove.size()];
//            for (int j = 0; i < stickersToRemove.size(); i++) {
//                removeSt[j] = stickersToRemove.get(j).getId();
//            }
//            stickers.removeItems(removeSt);
//        }
//        packs.removeItems(remove);
//    }
//

    private void saveStorage() {
        context().getPreferences().putBytes("stickers.data", stickersStorage.toByteArray());
    }

    private void notifyVM() {
        context().getStickersModule()
                .getStickersVM()
                .getOwnStickerPacks()
                .change(new ArrayList<>(stickersStorage.getStickerPacks()));
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof StickerCollectionsChanged) {
            if (!isLoaded) {
                stash();
            }
            //onStickerCollectionsChanged(((StickerCollectionsChanged) message).getUpdated());
        } else if (message instanceof OwnStickerCollectionsChanged) {
            if (!isLoaded) {
                stash();
            }
            onOwnStickerCollectionChanged(((OwnStickerCollectionsChanged) message).getOwnStickers());
        } else {
            super.onReceive(message);
        }
    }

    public static class OwnStickerCollectionsChanged {

        private List<ApiStickerCollection> ownStickers;

        public OwnStickerCollectionsChanged(List<ApiStickerCollection> ownStickers) {
            this.ownStickers = ownStickers;
        }

        public List<ApiStickerCollection> getOwnStickers() {
            return ownStickers;
        }
    }

    public static class StickerCollectionsChanged {

        private List<ApiStickerCollection> updated;

        public StickerCollectionsChanged(List<ApiStickerCollection> updated) {
            this.updated = updated;
        }

        public List<ApiStickerCollection> getUpdated() {
            return updated;
        }
    }
}