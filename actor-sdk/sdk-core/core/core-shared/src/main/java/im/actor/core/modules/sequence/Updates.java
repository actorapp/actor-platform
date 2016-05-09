/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.sequence;

import java.util.ArrayList;
import java.util.List;

import im.actor.core.api.ApiGroup;
import im.actor.core.api.ApiGroupOutPeer;
import im.actor.core.api.ApiUser;
import im.actor.core.api.ApiUserOutPeer;
import im.actor.core.api.base.FatSeqUpdate;
import im.actor.core.api.base.SeqUpdate;
import im.actor.core.api.rpc.RequestGetReferencedEntitites;
import im.actor.core.events.NewSessionCreated;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.sequence.internal.ExecuteAfter;
import im.actor.core.network.parser.Update;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.eventbus.BusSubscriber;
import im.actor.runtime.eventbus.Event;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.promise.PromiseFunc;
import im.actor.runtime.promise.Promises;
import im.actor.runtime.promise.PromisesArray;

import static im.actor.runtime.actors.ActorSystem.system;

public class Updates extends AbsModule implements BusSubscriber {

    // j2objc workaround
    private static final Void DUMB = null;

    private ActorRef updateActor;
    private ActorRef updateHandler;
    private SequenceHandlerInt updateHandlerInt;

    public Updates(ModuleContext messenger) {
        super(messenger);
    }

    public void run() {
        this.updateHandler = system().actorOf("actor/updates/handler", SequenceHandlerActor.CONSTRUCTOR(context()));
        this.updateHandlerInt = new SequenceHandlerInt(this.updateHandler);
        this.updateActor = system().actorOf("actor/updates", SequenceActor.CONSTRUCTOR(context()));


        context().getEvents().subscribe(this, NewSessionCreated.EVENT);
    }

    public SequenceHandlerInt getUpdateHandler() {
        return updateHandlerInt;
    }

    public void onPushReceived(int seq) {
        if (updateActor != null) {
            updateActor.send(new SequenceActor.PushSeq(seq));
        }
    }


    public Promise<Void> applyUpdate(int seq, byte[] state, Update update) {
        return new Promise<>((PromiseFunc<Void>) resolver -> {
            updateActor.send(new SeqUpdate(seq, state, update.getHeaderKey(), update.toByteArray()));
            executeAfter(seq, () -> resolver.result(null));
        });
    }

    public Promise<Void> applyUpdate(int seq, byte[] state, Update update,
                                     List<ApiUser> users, ApiGroup group) {
        ArrayList<ApiGroup> groups = new ArrayList<>();
        groups.add(group);
        return applyUpdate(seq, state, update, users, groups);

    }

    public Promise<Void> applyUpdate(int seq, byte[] state, Update update,
                                     List<ApiUser> users, List<ApiGroup> groups) {
        return new Promise<>((PromiseFunc<Void>) resolver -> {
            updateActor.send(new FatSeqUpdate(seq, state, update.getHeaderKey(), update.toByteArray(),
                    users, groups));
            executeAfter(seq, () -> resolver.result(null));
        });
    }


    public Promise<Void> applyRelatedData(final List<ApiUser> users) {
        return applyRelatedData(users, new ArrayList<>());
    }

    public Promise<Void> applyRelatedData(final List<ApiUser> users, final ApiGroup group) {
        ArrayList<ApiGroup> groups = new ArrayList<>();
        groups.add(group);
        return applyRelatedData(users, groups);
    }

    public Promise<Void> applyRelatedData(final List<ApiUser> users, final List<ApiGroup> groups) {
        Promise<Void> res = Promise.success(null);
        if (users.size() > 0) {
            res = res.chain(v -> context().getUsersModule().getUserRouter().applyUsers(users));
        }
        if (groups.size() > 0) {
            res = res.chain(v -> context().getGroupsModule().getRouter().applyGroups(groups));
        }
        return res;
    }

    public Promise<Void> loadRequiredPeers(List<ApiUserOutPeer> users, List<ApiGroupOutPeer> groups) {

        Promise<List<ApiUserOutPeer>> usersMissingPeers = context().getUsersModule().getUserRouter()
                .fetchMissingUsers(users);

        Promise<List<ApiGroupOutPeer>> groupMissingPeers = context().getGroupsModule().getRouter()
                .fetchPendingGroups(groups);

        return Promises.tuple(usersMissingPeers, groupMissingPeers)
                .flatMap(missing -> {
                    if (missing.getT1().size() > 0 || missing.getT2().size() > 0) {
                        return api(new RequestGetReferencedEntitites(missing.getT1(), missing.getT2()))
                                .flatMap(r -> applyRelatedData(r.getUsers(), r.getGroups()));
                    } else {
                        return Promise.success(null);
                    }
                });
    }


    @Deprecated
    public void onUpdateReceived(Object update) {
        updateActor.send(update);
    }

    @Deprecated
    public void executeAfter(int seq, Runnable runnable) {
        updateActor.send(new ExecuteAfter(seq, runnable));
    }


    public void resetModule() {
        // TODO: Implement
    }

    @Override
    public void onBusEvent(Event event) {
        if (event instanceof NewSessionCreated) {
            updateActor.send(new SequenceActor.Invalidate());
        }
    }
}