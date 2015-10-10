/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules;

import java.util.List;

import im.actor.core.api.ApiGroup;
import im.actor.core.api.ApiUser;
import im.actor.core.api.base.FatSeqUpdate;
import im.actor.core.api.base.SeqUpdate;
import im.actor.core.modules.events.NewSessionCreated;
import im.actor.core.modules.updates.SequenceActor;
import im.actor.core.modules.updates.internal.ExecuteAfter;
import im.actor.core.network.parser.Update;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.Props;
import im.actor.runtime.eventbus.BusSubscriber;
import im.actor.runtime.eventbus.Event;

import static im.actor.runtime.actors.ActorSystem.system;

public class Updates extends AbsModule implements BusSubscriber {

    private ActorRef updateActor;

    public Updates(ModuleContext messenger) {
        super(messenger);
    }

    public void run() {
        this.updateActor = system().actorOf(Props.create(SequenceActor.class, new ActorCreator<SequenceActor>() {
            @Override
            public SequenceActor create() {
                return new SequenceActor(context());
            }
        }).changeDispatcher("updates"), "actor/updates");

        context().getEvents().subscribe(this, NewSessionCreated.EVENT);
    }

    public void onPushReceived(int seq) {
        updateActor.send(new SequenceActor.PushSeq(seq));
    }

    public void onSeqUpdateReceived(int seq, byte[] state, Update update) {
        updateActor.send(new SeqUpdate(seq, state, update.getHeaderKey(), update.toByteArray()));
    }

    public void onFatSeqUpdateReceived(int seq, byte[] state, Update update,
                                       List<ApiUser> users, List<ApiGroup> groups) {
        updateActor.send(new FatSeqUpdate(seq, state, update.getHeaderKey(), update.toByteArray(),
                users, groups));
    }

    public void onUpdateReceived(Object update) {
        updateActor.send(update);
    }

    public void onUpdateReceived(Object update, Long delay) {
        updateActor.send(update, delay);
    }

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