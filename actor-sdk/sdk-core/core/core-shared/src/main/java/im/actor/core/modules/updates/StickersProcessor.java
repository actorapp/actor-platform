/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.updates;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.actor.core.api.ApiStickerCollection;
import im.actor.core.api.updates.UpdateOwnStickersChanged;
import im.actor.core.entity.content.internal.StickersPack;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.runtime.bser.Bser;
import im.actor.runtime.eventbus.Event;

public class StickersProcessor extends AbsModule {

    public StickersProcessor(ModuleContext modules) {
        super(modules);
    }

    public void onOwnStickerCollectionsChanged(List<ApiStickerCollection> updated) {

        stickerPacksStorage().addOrUpdateItem(0, new UpdateOwnStickersChanged(updated).toByteArray());
        context().getEvents().post(new Event() {
            @Override
            public String getType() {
                return "sticker_collections_changed";
            }
        });
    }

    public void onStickerCollectionsChanged(List<ApiStickerCollection> updated) {
        UpdateOwnStickersChanged old = new UpdateOwnStickersChanged();
        try {
            Bser.parse(old, stickerPacksStorage().loadItem(0));
            List<ApiStickerCollection> oldPacks = old.getCollections();
            boolean needUpdate = false;
            for (ApiStickerCollection oldPack : oldPacks) {
                for (ApiStickerCollection newPack : updated) {
                    if (oldPack.getId() == newPack.getId()) {
                        needUpdate = true;
                        oldPacks.remove(oldPack);
                        oldPacks.add(newPack);
                    }

                }
            }
            if (needUpdate) {
                stickerPacksStorage().addOrUpdateItem(0, new UpdateOwnStickersChanged(oldPacks).toByteArray());
                context().getEvents().post(new Event() {
                    @Override
                    public String getType() {
                        return "sticker_collections_changed";
                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
