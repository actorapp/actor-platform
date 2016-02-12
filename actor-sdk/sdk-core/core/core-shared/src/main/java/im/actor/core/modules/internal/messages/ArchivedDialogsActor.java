/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.internal.messages;

import im.actor.core.api.rpc.RequestLoadArchived;
import im.actor.core.api.rpc.ResponseLoadArchived;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.updates.internal.ArchivedDialogLoaded;
import im.actor.core.network.RpcCallback;
import im.actor.core.network.RpcException;
import im.actor.core.util.ModuleActor;
import im.actor.runtime.Log;

public class ArchivedDialogsActor extends ModuleActor {

    private static final String TAG = "ArchivedDialogsActor";

    private static final int LIMIT = 20;

    private static final String KEY_LOADED_OFFSET = "archived_dialogs_offset";

    private byte[] nextOffset;

    private boolean isLoading = false;

    RpcCallback<ResponseLoadArchived> lastCallback;

    public ArchivedDialogsActor(ModuleContext context) {
        super(context);
    }

    @Override
    public void preStart() {
        nextOffset = preferences().getBytes(KEY_LOADED_OFFSET);
    }

    private void onLoadMore(RpcCallback<ResponseLoadArchived> callback) {
        this.lastCallback.onError(new RpcException(TAG, 0, "callback replaced", false, null));
        this.lastCallback = callback;

        if (isLoading) {
            return;
        }
        isLoading = true;

        Log.d(TAG, "Loading archived");

        request(new RequestLoadArchived(nextOffset, LIMIT),
                new RpcCallback<ResponseLoadArchived>() {
                    @Override
                    public void onResult(ResponseLoadArchived response) {
                        updates().onUpdateReceived(new ArchivedDialogLoaded(response));
                    }

                    @Override
                    public void onError(RpcException e) {
                        lastCallback.onError(e);
                    }
                });
    }

    private void onLoadedMore(ResponseLoadArchived responseLoadArchiveds) {
        isLoading = false;

        this.nextOffset = responseLoadArchiveds.getNextOffset();
        preferences().putBytes(KEY_LOADED_OFFSET, nextOffset);
        lastCallback.onResult(responseLoadArchiveds);
        Log.d(TAG, "Archived loaded");
    }

    // Messages

    @Override
    public void onReceive(Object message) {
        if (message instanceof LoadMore) {
            onLoadMore(((LoadMore) message).getCallback());
        } else if (message instanceof LoadedMore) {
            onLoadedMore(((LoadedMore) message).getResponseLoadArchived());
        } else {
            drop(message);
        }
    }

    public static class LoadMore {
        RpcCallback<ResponseLoadArchived> callback;
        public LoadMore(RpcCallback<ResponseLoadArchived> callback) {
            this.callback = callback;
        }

        public RpcCallback<ResponseLoadArchived> getCallback() {
            return callback;
        }
    }

    public static class LoadedMore {
        ResponseLoadArchived responseLoadArchived;

        public LoadedMore(ResponseLoadArchived responseLoadArchived) {
            this.responseLoadArchived = responseLoadArchived;
        }

        public ResponseLoadArchived getResponseLoadArchived() {
            return responseLoadArchived;
        }
    }
}
