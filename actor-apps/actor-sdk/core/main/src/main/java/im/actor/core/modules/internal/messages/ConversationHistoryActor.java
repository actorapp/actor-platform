/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.internal.messages;

import im.actor.core.api.rpc.RequestLoadHistory;
import im.actor.core.api.rpc.ResponseLoadHistory;
import im.actor.core.entity.Peer;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.updates.internal.MessagesHistoryLoaded;
import im.actor.core.modules.utils.ModuleActor;
import im.actor.core.network.RpcCallback;
import im.actor.core.network.RpcException;

public class ConversationHistoryActor extends ModuleActor {

    private static final int LIMIT = 20;

    private final String KEY_LOADED_DATE;
    private final String KEY_LOADED;
    private final String KEY_LOADED_INIT;
    private final Peer peer;

    private long historyMaxDate;
    private boolean historyLoaded;

    private boolean isLoading = false;

    public ConversationHistoryActor(Peer peer, ModuleContext context) {
        super(context);
        this.peer = peer;
        this.KEY_LOADED_DATE = "conv_" + peer + "_history_date";
        this.KEY_LOADED = "conv_" + peer + "_history_loaded";
        this.KEY_LOADED_INIT = "conv_" + peer + "_history_inited";
    }

    @Override
    public void preStart() {
        super.preStart();
        historyMaxDate = preferences().getLong(KEY_LOADED_DATE, Long.MAX_VALUE);
        historyLoaded = preferences().getBool(KEY_LOADED, false);
        if (!preferences().getBool(KEY_LOADED_INIT, false)) {
            self().sendOnce(new LoadMore());
        }
    }

    private void onLoadMore() {
        if (historyLoaded) {
            return;
        }
        if (isLoading) {
            return;
        }
        isLoading = true;

        request(new RequestLoadHistory(buidOutPeer(peer), historyMaxDate, LIMIT),
                new RpcCallback<ResponseLoadHistory>() {
                    @Override
                    public void onResult(ResponseLoadHistory response) {
                        // Invoke on sequence actor
                        updates().onUpdateReceived(new MessagesHistoryLoaded(peer, response));
                    }

                    @Override
                    public void onError(RpcException e) {
                        e.printStackTrace();
                        // Never happens
                    }
                });
    }

    private void onLoadedMore(int loaded, long maxLoadedDate) {
        isLoading = false;

        if (loaded < LIMIT) {
            historyLoaded = true;
        } else {
            historyLoaded = false;
            historyMaxDate = maxLoadedDate;
        }

        preferences().putLong(KEY_LOADED_DATE, maxLoadedDate);
        preferences().putBool(KEY_LOADED, historyLoaded);
        preferences().putBool(KEY_LOADED_INIT, true);
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof LoadMore) {
            onLoadMore();
        } else if (message instanceof LoadedMore) {
            LoadedMore loadedMore = (LoadedMore) message;
            onLoadedMore(loadedMore.loaded, loadedMore.maxLoadedDate);
        } else {
            drop(message);
        }
    }

    public static class LoadMore {

    }

    public static class LoadedMore {
        private int loaded;
        private long maxLoadedDate;

        public LoadedMore(int loaded, long maxLoadedDate) {
            this.loaded = loaded;
            this.maxLoadedDate = maxLoadedDate;
        }
    }
}
