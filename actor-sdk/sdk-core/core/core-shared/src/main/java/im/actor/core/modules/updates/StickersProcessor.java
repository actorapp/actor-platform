/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.updates;

import java.util.ArrayList;
import java.util.List;

import im.actor.core.api.ApiStickerCollection;
import im.actor.core.entity.content.internal.Sticker;
import im.actor.core.entity.content.internal.StickersPack;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.runtime.eventbus.Event;

public class StickersProcessor extends AbsModule {

    public StickersProcessor(ModuleContext modules) {
        super(modules);
    }

    public void onOwnStickerCollectionsChanged(List<ApiStickerCollection> updated) {
        ArrayList<StickersPack> add = new ArrayList<StickersPack>();
        ArrayList<StickersPack> remove = new ArrayList<StickersPack>();
        for (int i = 0; i < stickerPacksList().getCount(); i++) {
            remove.add(stickerPacksList().getValue(i));
        }
        for (ApiStickerCollection pack : updated) {
            add.add(new StickersPack(pack));
        }

        boolean needrebuild = false;
        if (add.size() > 0) {
            stickerPacksList().addOrUpdateItems(add);
            stickerPacksKeyValue().addOrUpdateItems(add);
            needrebuild = true;
        }

        if (remove.removeAll(add)) {
            for (StickersPack pack : remove) {
                stickerPacksList().removeItem(pack.getEngineId());
                stickerPacksKeyValue().removeItem(pack.getEngineId());
            }
            needrebuild = true;
        }

        if (needrebuild) {
            context().getEvents().post(new Event() {
                @Override
                public String getType() {
                    return "sticker_collections_changed";
                }
            });
        }
    }

    public void onStickerCollectionsChanged(List<ApiStickerCollection> updated) {
        ArrayList<StickersPack> add = new ArrayList<StickersPack>();
        for (ApiStickerCollection pack : updated) {
            StickersPack p = stickerPacksKeyValue().getValue(pack.getId());
            if (p != null) {
                add.add(new StickersPack(pack));
                stickerPacksKeyValue().addOrUpdateItem(p.updateStickers(pack.getStickers()));
            }
        }

        if (add.size() > 0) {
            stickerPacksList().addOrUpdateItems(add);

        }


    }
}
