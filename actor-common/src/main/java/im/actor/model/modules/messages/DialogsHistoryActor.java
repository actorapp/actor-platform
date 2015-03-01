package im.actor.model.modules.messages;

import im.actor.model.api.rpc.RequestLoadDialogs;
import im.actor.model.api.rpc.ResponseLoadDialogs;
import im.actor.model.log.Log;
import im.actor.model.modules.Modules;
import im.actor.model.modules.updates.internal.DialogHistoryLoaded;
import im.actor.model.modules.utils.ModuleActor;
import im.actor.model.network.RpcCallback;
import im.actor.model.network.RpcException;

/**
 * Created by ex3ndr on 13.02.15.
 */
public class DialogsHistoryActor extends ModuleActor {

    private static final String TAG = "DialogHistory";

    private static final int LIMIT = 50;

    private long historyMaxDate;
    private boolean historyLoaded;

    private boolean isLoading = false;

    public DialogsHistoryActor(Modules messenger) {
        super(messenger);
    }

    @Override
    public void preStart() {
        historyMaxDate = preferences().getLong("dialogs_history_date", Long.MAX_VALUE);
        historyLoaded = preferences().getBool("dialogs_history_loaded", false);
        self().sendOnce(new LoadMore());
    }

    private void onLoadMore() {
        if (historyLoaded) {
            return;
        }
        if (isLoading) {
            return;
        }
        isLoading = true;

        Log.d(TAG, "Loading history {date=" + historyMaxDate + "}");

        request(new RequestLoadDialogs(historyMaxDate, LIMIT),
                new RpcCallback<ResponseLoadDialogs>() {
                    @Override
                    public void onResult(ResponseLoadDialogs response) {
                        for (im.actor.model.api.Dialog dialog : response.getDialogs()) {
                            Log.d(TAG, "Loaded record {date=" + dialog.getDate() + " sort=" + dialog.getSortDate() + "}");
                        }

                        // Invoke on sequence actor
                        updates().onUpdateReceived(new DialogHistoryLoaded(response));
                    }

                    @Override
                    public void onError(RpcException e) {
                        e.printStackTrace();
                        //TODO: Error processing
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
        preferences().putLong("dialogs_history_date", maxLoadedDate);
        preferences().putBool("dialogs_history_loaded", historyLoaded);
    }

    // Messages

    @Override
    public void onReceive(Object message) {
        if (message instanceof LoadMore) {
            onLoadMore();
        } else if (message instanceof LoadedMore) {
            LoadedMore loaded = (LoadedMore) message;
            onLoadedMore(loaded.loaded, loaded.maxLoadedDate);
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
