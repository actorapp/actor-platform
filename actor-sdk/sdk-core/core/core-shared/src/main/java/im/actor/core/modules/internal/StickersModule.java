/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.internal;

import im.actor.core.entity.content.internal.Sticker;
import im.actor.core.entity.content.internal.StickersPack;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.internal.messages.StickersActor;
import im.actor.runtime.Storage;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.Props;
import im.actor.runtime.storage.KeyValueStorage;
import im.actor.runtime.storage.ListEngine;

import static im.actor.runtime.actors.ActorSystem.system;

public class StickersModule extends AbsModule {
    private KeyValueStorage stickerPacksList;
    private ListEngine<StickersPack> packs;
    private ListEngine<Sticker> stickers;
    private ActorRef stickersActor;

    public StickersModule(ModuleContext context) {
        super(context);
        this.stickerPacksList = Storage.createKeyValue(STORAGE_STICKER_ALL_PACKS);
        packs = Storage.createList(STORAGE_STICKER_PACKS, StickersPack.CREATOR);
        stickers = Storage.createList(STORAGE_STICKERS, Sticker.CREATOR);

    }

    public void run() {
        this.stickersActor = system().actorOf(Props.create(new ActorCreator() {
            @Override
            public StickersActor create() {
                return new StickersActor(context());
            }
        }), "actor/stickers");

    }

    public void resetModule() {
        packs.clear();
    }

    public ActorRef getStickersActor() {
        return stickersActor;
    }

    public ListEngine<StickersPack> getPacksEngine() {
        return packs;
    }

    public ListEngine<Sticker> getStickersEngine() {
        return stickers;
    }

    public KeyValueStorage getStickerPacksStorage() {
        return stickerPacksList;
    }
}