/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.stickers;

import java.util.List;

import im.actor.core.api.ApiStickerCollection;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.stickers.StickersActor;

public class StickersProcessor extends AbsModule {

    public StickersProcessor(ModuleContext modules) {
        super(modules);
    }

    public void onOwnStickerCollectionsChanged(List<ApiStickerCollection> updated) {
        stickersActor().send(new StickersActor.OwnStickerCollectionsChanged(updated));
    }

    public void onStickerCollectionsChanged(List<ApiStickerCollection> updated) {
        stickersActor().send(new StickersActor.StickerCollectionsChanged(updated));
    }
}
