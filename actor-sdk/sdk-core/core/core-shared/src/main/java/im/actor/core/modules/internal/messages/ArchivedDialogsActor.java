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

    private void onLoadMore(boolean init, RpcCallback<ResponseLoadArchived> callback) {

        if (isLoading) {

            //
            // replace old callback
            //
            lastCallback.onError(new RpcException(TAG, 0, "callback replaced", false, null));
            lastCallback = callback;
            return;
        }else{
            lastCallback = callback;
        }

        if(init){
            nextOffset = null;
        }

        isLoading = true;

        Log.d(TAG, "Loading archived dialogs");

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
        lastCallback.onResult(responseLoadArchiveds);
        Log.d(TAG, "Archived dialogs loaded");
    }

    // Messages

    @Override
    public void onReceive(Object message) {
        if (message instanceof LoadMore) {
            onLoadMore(((LoadMore) message).isInit(), ((LoadMore) message).getCallback());
        } else if (message instanceof LoadedMore) {
            onLoadedMore(((LoadedMore) message).getResponseLoadArchived());
        } else {
            drop(message);
        }
    }

    public static class LoadMore {
        RpcCallback<ResponseLoadArchived> callback;
        boolean init;
        public LoadMore(boolean init, RpcCallback<ResponseLoadArchived> callback) {
            this.callback = callback;
            this.init = init;
        }

        public boolean isInit() {
            return init;
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
