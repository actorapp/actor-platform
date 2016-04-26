/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.sequence;

import java.util.ArrayList;
import java.util.List;

import im.actor.core.api.ApiGroup;
import im.actor.core.api.ApiUser;
import im.actor.core.api.base.FatSeqUpdate;
import im.actor.core.api.base.SeqUpdate;
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

import static im.actor.runtime.actors.ActorSystem.system;

public class Updates extends AbsModule implements BusSubscriber {

    private ActorRef updateActor;
    private ActorRef updateHandler;
    private SequenceHandlerInt updateHandlerInt;

    public Updates(ModuleContext messenger) {
        super(messenger);
    }

    public void run() {
        this.updateHandler = system().actorOf("actor/updates/handler", "updates",
                SequenceHandlerActor.CONSTRUCTOR(context()));
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
        return applyRelatedData(users, new ApiGroup());
    }

    public Promise<Void> applyRelatedData(final List<ApiUser> users, final ApiGroup group) {
        ArrayList<ApiGroup> groups = new ArrayList<>();
        groups.add(group);
        return applyRelatedData(users, groups);
    }

    public Promise<Void> applyRelatedData(final List<ApiUser> users, final List<ApiGroup> groups) {
        return updateHandlerInt.onRelatedResponse(users, groups);
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