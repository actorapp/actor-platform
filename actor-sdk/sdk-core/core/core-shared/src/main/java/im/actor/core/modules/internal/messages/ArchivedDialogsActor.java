/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.internal.messages;

import im.actor.core.api.ApiDialogShort;
import im.actor.core.api.rpc.RequestLoadArchived;
import im.actor.core.api.rpc.RequestLoadDialogs;
import im.actor.core.api.rpc.ResponseLoadArchived;
import im.actor.core.api.rpc.ResponseLoadDialogs;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.updates.internal.ArchivedDialogLoaded;
import im.actor.core.modules.updates.internal.DialogHistoryLoaded;
import im.actor.core.network.RpcCallback;
import im.actor.core.network.RpcException;
import im.actor.core.util.ModuleActor;
import im.actor.runtime.Log;

public class ArchivedDialogsActor extends ModuleActor {

    private static final String TAG = "ArchivedDialogsActor";

    private static final int LIMIT = 20;

    private static final String KEY_LOADED_DATE = "archived_dialogs_date";
    private static final String KEY_LOADED = "archived_dialogs_loaded";
    private static final String KEY_LOADED_INIT = "archived_dialogs_inited";

    private byte[] nextOffset;
    private boolean archivedLoaded;

    private boolean isLoading = false;

    public ArchivedDialogsActor(ModuleContext context) {
        super(context);
    }

    @Override
    public void preStart() {
        nextOffset = preferences().getBytes(KEY_LOADED_DATE);
        archivedLoaded = preferences().getBool(KEY_LOADED, false);
        if (!preferences().getBool(KEY_LOADED_INIT, false)) {
            self().send(new LoadMore());
        }
    }

    private void onLoadMore() {
        if (archivedLoaded) {
            return;
        }
        if (isLoading) {
            return;
        }
        isLoading = true;

        Log.d(TAG, "Loading archived");

        request(new RequestLoadArchived(nextOffset, LIMIT),
                new RpcCallback<ResponseLoadArchived>() {
                    @Override
                    public void onResult(ResponseLoadArchived response) {

                        // Invoke on sequence actor
                        updates().onUpdateReceived(new ArchivedDialogLoaded(response));
                    }

                    @Override
                    public void onError(RpcException e) {
                        e.printStackTrace();
                        // Never happens
                    }
                });
    }

    private void onLoadedMore(int loaded, byte[] nextOffset) {
        isLoading = false;

        if (loaded < LIMIT) {
            archivedLoaded = true;
        } else {
            archivedLoaded = false;
            this.nextOffset = nextOffset;
        }
        preferences().putBytes(KEY_LOADED_DATE, nextOffset);
        preferences().putBool(KEY_LOADED, archivedLoaded);
        preferences().putBool(KEY_LOADED_INIT, true);

        Log.d(TAG, "Archived loaded");
    }

    // Messages

    @Override
    public void onReceive(Object message) {
        if (message instanceof LoadMore) {
            onLoadMore();
        } else if (message instanceof LoadedMore) {
            LoadedMore loaded = (LoadedMore) message;
            onLoadedMore(loaded.loaded, loaded.nextOffset);
        } else {
            drop(message);
        }
    }

    public static class LoadMore {

    }

    public static class LoadedMore {
        private int loaded;
        private byte[] nextOffset;

        public LoadedMore(int loaded, byte[] nextOffset) {
            this.loaded = loaded;
            this.nextOffset = nextOffset;
        }
    }
}
