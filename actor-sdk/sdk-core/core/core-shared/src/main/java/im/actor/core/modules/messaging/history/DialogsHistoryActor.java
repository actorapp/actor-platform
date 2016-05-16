/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.messaging.history;

import java.util.ArrayList;
import java.util.List;

import im.actor.core.api.ApiDialog;
import im.actor.core.api.ApiMessageState;
import im.actor.core.api.rpc.RequestLoadDialogs;
import im.actor.core.entity.content.AbsContent;
import im.actor.core.modules.api.ApiSupportConfiguration;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.messaging.history.entity.DialogHistory;
import im.actor.core.modules.ModuleActor;
import im.actor.runtime.actors.messages.Void;

import static im.actor.core.entity.EntityConverter.convert;

public class DialogsHistoryActor extends ModuleActor {

    // j2objc workaround
    private static final Void DUMB = null;

    private static final int LIMIT = 20;
    private static final String KEY_VERSION = "_1";
    private static final String KEY_LOADED_DATE = "dialogs_history_date" + KEY_VERSION;
    private static final String KEY_LOADED = "dialogs_history_loaded" + KEY_VERSION;
    private static final String KEY_LOADED_INIT = "dialogs_history_inited" + KEY_VERSION;

    private long historyMaxDate;
    private boolean historyLoaded;
    private boolean isLoading = false;

    public DialogsHistoryActor(ModuleContext context) {
        super(context);
    }

    @Override
    public void preStart() {
        historyMaxDate = preferences().getLong(KEY_LOADED_DATE, Long.MAX_VALUE);
        historyLoaded = preferences().getBool(KEY_LOADED, false);
        if (!preferences().getBool(KEY_LOADED_INIT, false)) {
            self().send(new LoadMore());
        }
    }

    private void onLoadMore() {
        if (historyLoaded || isLoading) {
            return;
        }
        isLoading = true;

        api(new RequestLoadDialogs(historyMaxDate, LIMIT, ApiSupportConfiguration.OPTIMIZATIONS))
                .chain(r -> updates().loadRequiredPeers(r.getUserPeers(), r.getGroupPeers()))
                .chain(r -> updates().applyRelatedData(r.getUsers(), r.getGroups()))
                .then(r -> onLoadedMore(r.getDialogs()));
    }

    private void onLoadedMore(List<ApiDialog> rawDialogs) {
        final ArrayList<DialogHistory> dialogs = new ArrayList<>();
        long maxLoadedDate = Long.MAX_VALUE;
        for (ApiDialog dialog : rawDialogs) {

            dialogs.add(new DialogHistory(
                    convert(dialog.getPeer()),
                    dialog.getUnreadCount(),
                    dialog.getSortDate(),
                    dialog.getRid(),
                    dialog.getDate(),
                    dialog.getSenderUid(),
                    AbsContent.fromMessage(dialog.getMessage()),
                    dialog.getState() == ApiMessageState.READ,
                    dialog.getState() == ApiMessageState.RECEIVED));

            maxLoadedDate = Math.min(dialog.getSortDate(), maxLoadedDate);
        }

        if (dialogs.size() > 0) {
            final long finalMaxLoadedDate = maxLoadedDate;
            context().getMessagesModule().getRouter().onDialogsHistoryLoaded(dialogs).then((v) -> {
                if (dialogs.size() < LIMIT) {
                    markAsLoaded();
                } else {
                    markAsSliceLoaded(finalMaxLoadedDate);
                }
            });
        } else {
            context().getAppStateModule().onDialogsLoaded();
            markAsLoaded();
        }
    }

    private void markAsLoaded() {
        isLoading = false;
        historyLoaded = true;
        preferences().putBool(KEY_LOADED, true);
        preferences().putBool(KEY_LOADED_INIT, true);
    }

    private void markAsSliceLoaded(long date) {
        isLoading = false;
        historyLoaded = false;
        historyMaxDate = date;
        preferences().putBool(KEY_LOADED, false);
        preferences().putBool(KEY_LOADED_INIT, true);
        preferences().putLong(KEY_LOADED_DATE, date);
    }

    //
    // Messages
    //

    @Override
    public void onReceive(Object message) {
        if (message instanceof LoadMore) {
            onLoadMore();
        } else {
            super.onReceive(message);
        }
    }

    public static class LoadMore {

    }
}
