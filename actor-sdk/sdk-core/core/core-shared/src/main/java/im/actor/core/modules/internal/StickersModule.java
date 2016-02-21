/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.internal;

import java.util.ArrayList;

import im.actor.core.api.ApiStickerCollection;
import im.actor.core.api.rpc.RequestLoadOwnStickers;
import im.actor.core.api.rpc.ResponseLoadOwnStickers;
import im.actor.core.api.updates.UpdateOwnStickersChanged;
import im.actor.core.entity.content.internal.Sticker;
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
import im.actor.runtime.mvvm.ValueModel;
import im.actor.runtime.storage.KeyValueStorage;

public class StickersModule extends AbsModule {
    private KeyValueStorage stickerPacksList;
    private ValueModel<ArrayList<StickerPackVM>> stickerPacks;


    public StickersModule(ModuleContext context) {
        super(context);
        this.stickerPacksList = Storage.createKeyValue(STORAGE_STICKER_ALL_PACKS);
        stickerPacks = new ValueModel<>("sticker_packs_vms", new ArrayList<StickerPackVM>());
        stickerPacks.change(buildStickerPacks());
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

        ArrayList<StickerPackVM> vms = new ArrayList<StickerPackVM>(stickerPacks.get());

        ArrayList<StickerPackVM> remove = new ArrayList<StickerPackVM>(vms);
        try {
            UpdateOwnStickersChanged update = new UpdateOwnStickersChanged();
            Bser.parse(update, stickerPacksList.loadItem(0));

            boolean add;
            for (ApiStickerCollection apiCollection : update.getCollections()) {
                add = true;
                for (StickerPackVM existingVM : vms) {
                    if (existingVM.getId() == apiCollection.getId()) {
                        existingVM.getStickers().change((ArrayList<Sticker>) new StickersPack(apiCollection).getStickers());
                        remove.remove(existingVM);
                        add = false;
                        break;
                    }
                }
                if (add) {
                    vms.add(new StickerPackVM(new StickersPack(apiCollection)));
                }
            }

            vms.removeAll(remove);
        } catch (Exception e) {
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