/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.messaging.history;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import im.actor.core.api.ApiGroup;
import im.actor.core.api.ApiListLoadMode;
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
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.ModuleActor;
import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.function.Consumer;
import im.actor.runtime.function.Function;
import im.actor.runtime.promise.Promise;

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
            self().send(new LoadMore());
        }
    }

    private void onLoadMore() {
        if (isLoading || historyLoaded) {
            return;
        }
        isLoading = true;
        api(new RequestLoadHistory(buidOutPeer(peer), historyMaxDate, null, LIMIT))
                .mapPromise(applyRelated())
                .then(applyHistory(peer))
                .then(new Consumer<ResponseLoadHistory>() {
                    @Override
                    public void apply(ResponseLoadHistory responseLoadHistory) {
                        isLoading = false;
                    }
                })
                .done(self());
    }

    private Consumer<ResponseLoadHistory> applyHistory(final Peer peer) {
        return new Consumer<ResponseLoadHistory>() {
            @Override
            public void apply(ResponseLoadHistory responseLoadHistory) {
                applyHistory(peer, responseLoadHistory.getHistory());
            }
        };
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
        context().getMessagesModule().getRouter()
                .onChatHistoryLoaded(peer, messages, maxReceiveDate, maxReadDate, isEnded);

        // Saving Internal State
        if (isEnded) {
            historyLoaded = true;
        } else {
            historyLoaded = false;
            historyMaxDate = maxLoadedDate;
        }
        preferences().putLong(KEY_LOADED_DATE, maxLoadedDate);
        preferences().putBool(KEY_LOADED, historyLoaded);
        preferences().putBool(KEY_LOADED_INIT, true);
    }

    private Function<ResponseLoadHistory, Promise<ResponseLoadHistory>> applyRelated() {
        return new Function<ResponseLoadHistory, Promise<ResponseLoadHistory>>() {
            @Override
            public Promise<ResponseLoadHistory> apply(final ResponseLoadHistory responseLoadHistory) {
                return updates().applyRelatedData(responseLoadHistory.getUsers(), new ArrayList<ApiGroup>()).map(new Function<Void, ResponseLoadHistory>() {
                    @Override
                    public ResponseLoadHistory apply(Void aVoid) {
                        return responseLoadHistory;
                    }
                });
            }
        };
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
