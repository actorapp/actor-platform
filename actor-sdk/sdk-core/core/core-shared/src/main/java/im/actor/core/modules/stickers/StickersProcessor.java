/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.stickers;

import java.util.List;

import im.actor.core.api.ApiStickerCollection;
import im.actor.core.api.updates.UpdateOwnStickersChanged;
import im.actor.core.api.updates.UpdateStickerCollectionsChanged;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.sequence.processor.SequenceProcessor;
import im.actor.core.network.parser.Update;
import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.promise.Promise;

public class StickersProcessor extends AbsModule implements SequenceProcessor {

    public StickersProcessor(ModuleContext modules) {
        super(modules);
    }

    public void onOwnStickerCollectionsChanged(List<ApiStickerCollection> updated) {
        stickersActor().send(new StickersActor.OwnStickerCollectionsChanged(updated));
    }

    public void onStickerCollectionsChanged(List<ApiStickerCollection> updated) {
        stickersActor().send(new StickersActor.StickerCollectionsChanged(updated));
    }

    @Override
    public Promise<Void> process(Update update) {
        if (update instanceof UpdateOwnStickersChanged) {
            onOwnStickerCollectionsChanged(((UpdateOwnStickersChanged) update).getCollections());
            return Promise.success(null);
        } else if (update instanceof UpdateStickerCollectionsChanged) {
            onStickerCollectionsChanged(((UpdateStickerCollectionsChanged) update).getCollections());
            return Promise.success(null);
        }
        return null;
    }
}
