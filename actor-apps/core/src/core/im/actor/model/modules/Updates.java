/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.modules;

import java.util.List;

import im.actor.model.api.Group;
import im.actor.model.api.User;
import im.actor.model.api.base.FatSeqUpdate;
import im.actor.model.api.base.SeqUpdate;
import im.actor.model.droidkit.actors.ActorCreator;
import im.actor.model.droidkit.actors.ActorRef;
import im.actor.model.droidkit.actors.Props;
import im.actor.model.modules.updates.SequenceActor;
import im.actor.model.modules.updates.internal.ExecuteAfter;
import im.actor.model.network.parser.Update;

import static im.actor.model.droidkit.actors.ActorSystem.system;

public class Updates extends BaseModule {

    private ActorRef updateActor;

    public Updates(Modules messenger) {
        super(messenger);
    }

    public void run() {
        this.updateActor = system().actorOf(Props.create(SequenceActor.class, new ActorCreator<SequenceActor>() {
            @Override
            public SequenceActor create() {
                return new SequenceActor(modules());
            }
        }).changeDispatcher("updates"), "actor/updates");
    }

    public void onNewSessionCreated() {
        updateActor.send(new SequenceActor.Invalidate());
    }


    public void onPushReceived(int seq) {
        updateActor.send(new SequenceActor.PushSeq(seq));
    }

    public void onSeqUpdateReceived(int seq, byte[] state, Update update) {
        updateActor.send(new SeqUpdate(seq, state, update.getHeaderKey(), update.toByteArray()));
    }

    public void onFatSeqUpdateReceived(int seq, byte[] state, Update update,
                                       List<User> users, List<Group> groups) {
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
}