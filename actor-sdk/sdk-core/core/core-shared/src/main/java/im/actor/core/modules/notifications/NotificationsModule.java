/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.notifications;

import im.actor.core.entity.ContentDescription;
import im.actor.core.entity.Peer;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.Modules;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.Props;

import static im.actor.runtime.actors.ActorSystem.system;

public class NotificationsModule extends AbsModule {

    private ActorRef notificationsActor;

    public NotificationsModule(final Modules modules) {
        super(modules);
    }

    public void run() {
        this.notificationsActor = system().actorOf(Props.create(new ActorCreator() {
            @Override
            public NotificationsActor create() {
                return new NotificationsActor(context());
            }
        }), "actor/notifications");
    }

    public void onOwnRead(Peer peer, long fromDate) {
        notificationsActor.send(new NotificationsActor.MessagesRead(peer, fromDate));
    }

    public void onInMessage(Peer peer, int sender, long sortDate, ContentDescription contentDescription, boolean hasCurrentUserMention, int unreadMessagesCount, int unreadDialogsCount) {
        notificationsActor.send(new NotificationsActor.NewMessage(peer, sender, sortDate, contentDescription, hasCurrentUserMention, unreadMessagesCount, unreadDialogsCount));
    }

    public void pauseNotifications() {
        notificationsActor.send(new NotificationsActor.PauseNotifications());
    }

    public void resumeNotifications() {
        notificationsActor.send(new NotificationsActor.ResumeNotifications());
    }

    public void resetModule() {
        // TODO: Implement
    }
}