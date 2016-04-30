package im.actor.core.modules.stickers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.actor.core.api.ApiStickerCollection;
import im.actor.core.api.rpc.RequestLoadOwnStickers;
import im.actor.core.api.rpc.ResponseLoadOwnStickers;
import im.actor.core.entity.StickerPack;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.stickers.entity.StickersStorage;
import im.actor.core.modules.ModuleActor;
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
            api(new RequestLoadOwnStickers()).then(responseLoadOwnStickers -> {

                onOwnStickerCollectionChanged(responseLoadOwnStickers.getOwnStickers());

                context().getPreferences().putBool("stickers_loaded", true);
                isLoaded = true;
                unstashAll();
            });
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

    private void onStickerCollectionsChanged(List<ApiStickerCollection> updated) {
        for (int i = 0; i < stickersStorage.getStickerPacks().size(); i++) {
            StickerPack collection = stickersStorage.getStickerPacks().get(i);
            for (ApiStickerCollection c : updated) {
                if (c.getId() == collection.getId()) {
                    stickersStorage.getStickerPacks().set(i, new StickerPack(c));
                    break;
                }
            }
        }
        notifyVM();
        saveStorage();
    }

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
            onStickerCollectionsChanged(((StickerCollectionsChanged) message).getUpdated());
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