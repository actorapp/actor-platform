/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.updates;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import im.actor.core.api.ApiStickerCollection;
import im.actor.core.api.updates.UpdateOwnStickersChanged;
import im.actor.core.entity.content.internal.StickersPack;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.internal.messages.StickersActor;
import im.actor.runtime.bser.Bser;
import im.actor.runtime.eventbus.Event;

public class StickersProcessor extends AbsModule {

    public StickersProcessor(ModuleContext modules) {
        super(modules);
    }

    public void onOwnStickerCollectionsChanged(List<ApiStickerCollection> updated) {
        byte[] oldUpdate = stickerPacksStorage().loadItem(0);
        if (oldUpdate == null) {
            stickersActor().send(new StickersActor.OwnStickerCollectionsChanged(updated, new ArrayList<ApiStickerCollection>()));

        } else {
            UpdateOwnStickersChanged old = new UpdateOwnStickersChanged();
            try {
                Bser.parse(old, oldUpdate);
                List<ApiStickerCollection> oldPacks = old.getCollections();
                List<ApiStickerCollection> remove = old.getCollections();
                Set<ApiStickerCollection> add = new HashSet<ApiStickerCollection>();

                boolean needUpdate = false;
                for (ApiStickerCollection oldPack : oldPacks) {
                    for (ApiStickerCollection newPack : updated) {
                        if (oldPack.getId() == newPack.getId()) {
                            needUpdate = true;
                            remove.remove(oldPack);
                        } else {
                            add.add(newPack);
                        }
                    }
                }
                if (needUpdate) {
                    stickersActor().send(new StickersActor.OwnStickerCollectionsChanged(new ArrayList<ApiStickerCollection>(add), remove));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public void onStickerCollectionsChanged(List<ApiStickerCollection> updated) {
        stickersActor().send(new StickersActor.StickerCollectionsChanged(updated));

    }
}
