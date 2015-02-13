package im.actor.model.modules.messages;

import im.actor.model.Messenger;
import im.actor.model.api.rpc.RequestLoadDialogs;
import im.actor.model.api.rpc.ResponseLoadDialogs;
import im.actor.model.modules.updates.internal.DialogHistoryLoaded;
import im.actor.model.modules.utils.ModuleActor;
import im.actor.model.network.RpcCallback;
import im.actor.model.network.RpcException;
import im.actor.model.storage.PreferencesStorage;

/**
 * Created by ex3ndr on 13.02.15.
 */
public class DialogsHistoryActor extends ModuleActor {

    private static final int LIMIT = 50;

    private Messenger messenger;
    private PreferencesStorage preferencesStorage;

    private long historyMaxDate;
    private boolean historyLoaded;

    private boolean isLoading = false;

    public DialogsHistoryActor(Messenger messenger) {
        super(messenger);
        this.messenger = messenger;
    }

    @Override
    public void preStart() {
        preferencesStorage = messenger.getConfiguration().getPreferencesStorage();
        historyMaxDate = preferencesStorage.getLong("dialogs_history_date", 0);
        historyLoaded = preferencesStorage.getBool("dialogs_history_loaded", false);
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

        request(new RequestLoadDialogs(historyMaxDate, LIMIT),
                new RpcCallback<ResponseLoadDialogs>() {
                    @Override
                    public void onResult(ResponseLoadDialogs response) {
                        // Invoke on sequence actor
                        messenger.getUpdatesModule().onUpdateReceived(new DialogHistoryLoaded(response));
                    }

                    @Override
                    public void onError(RpcException e) {
                        e.printStackTrace();
                        //TODO: Error processing
                    }
                });
    }

    private void onLoadedMore(boolean isFinished, long maxLoadedDate) {
        isLoading = false;

        if (isFinished) {
            historyLoaded = true;
        } else {
            historyLoaded = false;
            historyMaxDate = maxLoadedDate;
        }
        preferencesStorage.putBool("dialogs_history_loaded", historyLoaded);
        preferencesStorage.putLong("dialogs_history_date", 0);
    }

    // Messages

    @Override
    public void onReceive(Object message) {
        if (message instanceof LoadMore) {
            onLoadMore();
        } else if (message instanceof LoadedMore) {
            LoadedMore loaded = (LoadedMore) message;
            onLoadedMore(loaded.isFinished, loaded.maxLoadedDate);
        } else {
            drop(message);
        }
    }

    public static class LoadMore {

    }

    public static class LoadedMore {
        private boolean isFinished;
        private long maxLoadedDate;

        public LoadedMore(boolean isFinished, long maxLoadedDate) {
            this.isFinished = isFinished;
            this.maxLoadedDate = maxLoadedDate;
        }
    }
}
