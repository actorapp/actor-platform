/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.messaging.history;

import java.util.ArrayList;
import java.util.List;

import im.actor.core.api.ApiMessageContainer;
import im.actor.core.api.ApiMessageReaction;
import im.actor.core.api.ApiMessageState;
import im.actor.core.api.rpc.RequestLoadHistory;
import im.actor.core.api.rpc.ResponseLoadHistory;
import im.actor.core.entity.EntityConverter;
import im.actor.core.entity.Message;
import im.actor.core.entity.MessageState;
import im.actor.core.entity.Peer;
import im.actor.core.entity.Reaction;
import im.actor.core.entity.content.AbsContent;
import im.actor.core.modules.api.ApiSupportConfiguration;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.ModuleActor;
import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.function.Consumer;

public class ConversationHistoryActor extends ModuleActor {

    // j2objc workaround
    private static final Void DUMB = null;

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
            self().send(new LoadMore());
        }
    }

    private void onLoadMore() {
        if (isLoading || historyLoaded) {
            return;
        }
        isLoading = true;
        api(new RequestLoadHistory(buidOutPeer(peer), historyMaxDate, null, LIMIT, ApiSupportConfiguration.OPTIMIZATIONS))
                .chain(r -> updates().applyRelatedData(r.getUsers(), r.getGroups()))
                .chain(r -> updates().loadRequiredPeers(r.getUserPeers(), r.getGroupPeers()))
                .then(applyHistory(peer))
                .then(responseLoadHistory -> isLoading = false);
    }

    private Consumer<ResponseLoadHistory> applyHistory(final Peer peer) {
        return responseLoadHistory -> applyHistory(peer, responseLoadHistory.getHistory());
    }

    private void applyHistory(Peer peer, List<ApiMessageContainer> history) {

        ArrayList<Message> messages = new ArrayList<>();
        long maxLoadedDate = Long.MAX_VALUE;
        long maxReadDate = 0;
        long maxReceiveDate = 0;
        for (ApiMessageContainer historyMessage : history) {

            AbsContent content = AbsContent.fromMessage(historyMessage.getMessage());
            MessageState state = EntityConverter.convert(historyMessage.getState());
            ArrayList<Reaction> reactions = new ArrayList<>();
            for (ApiMessageReaction r : historyMessage.getReactions()) {
                reactions.add(new Reaction(r.getCode(), r.getUsers()));
            }
            messages.add(new Message(historyMessage.getRid(), historyMessage.getDate(),
                    historyMessage.getDate(), historyMessage.getSenderUid(),
                    state, content, reactions, 0));

            maxLoadedDate = Math.min(historyMessage.getDate(), maxLoadedDate);
            if (historyMessage.getState() == ApiMessageState.RECEIVED) {
                maxReceiveDate = Math.max(historyMessage.getDate(), maxReceiveDate);
            } else if (historyMessage.getState() == ApiMessageState.READ) {
                maxReceiveDate = Math.max(historyMessage.getDate(), maxReceiveDate);
                maxReadDate = Math.max(historyMessage.getDate(), maxReadDate);
            }
        }

        boolean isEnded = history.size() < LIMIT;

        // Sending updates to conversation actor
        final long finalMaxLoadedDate = maxLoadedDate;
        context().getMessagesModule().getRouter()
                .onChatHistoryLoaded(peer, messages, maxReceiveDate, maxReadDate, isEnded)
                .then(r -> {
                    // Saving Internal State
                    if (isEnded) {
                        historyLoaded = true;
                    } else {
                        historyLoaded = false;
                        historyMaxDate = finalMaxLoadedDate;
                    }
                    preferences().putLong(KEY_LOADED_DATE, finalMaxLoadedDate);
                    preferences().putBool(KEY_LOADED, historyLoaded);
                    preferences().putBool(KEY_LOADED_INIT, true);
                });
    }

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
