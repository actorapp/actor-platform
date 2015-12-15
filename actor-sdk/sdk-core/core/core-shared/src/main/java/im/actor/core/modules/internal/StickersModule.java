/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.actor.core.api.ApiStickerCollection;
import im.actor.core.api.rpc.RequestLoadOwnStickers;
import im.actor.core.api.rpc.ResponseLoadOwnStickers;
import im.actor.core.api.updates.UpdateOwnStickersChanged;
import im.actor.core.api.updates.UpdateStickerCollectionsChanged;
import im.actor.core.entity.content.internal.StickersPack;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.updates.internal.StickersLoaded;
import im.actor.core.network.RpcCallback;
import im.actor.core.network.RpcException;
import im.actor.core.viewmodel.StickerPackVM;
import im.actor.runtime.Storage;
import im.actor.runtime.bser.Bser;
import im.actor.runtime.eventbus.BusSubscriber;
import im.actor.runtime.eventbus.Event;
import im.actor.runtime.mvvm.MVVMCollection;
import im.actor.runtime.mvvm.ValueModel;
import im.actor.runtime.storage.KeyValueEngine;
import im.actor.runtime.storage.KeyValueItem;
import im.actor.runtime.storage.KeyValueStorage;
import im.actor.runtime.storage.ListEngine;

public class StickersModule extends AbsModule {
    private KeyValueStorage stickerPacksList;
    private ValueModel<ArrayList<StickerPackVM>> stickerPacks;


    public StickersModule(ModuleContext context) {
        super(context);
        this.stickerPacksList = Storage.createKeyValue(STORAGE_STICKER_ALL_PACKS);
        stickerPacks = new ValueModel<ArrayList<StickerPackVM>>("sticker_packs_vms", buildStickerPacks());

        context().getEvents().subscribe(new BusSubscriber() {
            @Override
            public void onBusEvent(Event event) {
                stickerPacks.change(buildStickerPacks());
            }
        }, "sticker_collections_changed");
    }

    public KeyValueStorage getStickerPacksStorage() {
        return stickerPacksList;
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

    private ArrayList<StickerPackVM> buildStickerPacks() {
        ArrayList<StickerPackVM> vms = new ArrayList<StickerPackVM>();

        try {
            UpdateOwnStickersChanged update = new UpdateOwnStickersChanged();
            Bser.parse(update, stickerPacksList.loadItem(0));

            for (ApiStickerCollection apiCollection : update.getCollections()) {
                vms.add(new StickerPackVM(new StickersPack(apiCollection)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        return vms;
    }

    public ValueModel<ArrayList<StickerPackVM>> getStickerPacks() {
        return stickerPacks;
    }

    public void resetModule() {
        stickerPacksList.clear();
    }


}