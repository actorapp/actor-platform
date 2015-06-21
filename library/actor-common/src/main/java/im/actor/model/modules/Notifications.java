/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.modules;

import im.actor.model.droidkit.actors.ActorCreator;
import im.actor.model.droidkit.actors.ActorRef;
import im.actor.model.droidkit.actors.Props;
import im.actor.model.droidkit.engine.SyncKeyValue;
import im.actor.model.entity.ContentDescription;
import im.actor.model.entity.Peer;
import im.actor.model.modules.notifications.NotificationsActor;

import static im.actor.model.droidkit.actors.ActorSystem.system;

public class Notifications extends BaseModule {
    private ActorRef notificationsActor;
    private SyncKeyValue notificationsStorage;

    public Notifications(final Modules modules) {
        super(modules);
        notificationsStorage = new SyncKeyValue(storage().createKeyValue(STORAGE_NOTIFICATIONS));
    }

    public void run() {
        this.notificationsActor = system().actorOf(Props.create(NotificationsActor.class, new ActorCreator<NotificationsActor>() {
            @Override
            public NotificationsActor create() {
                return new NotificationsActor(modules());
            }
        }), "actor/notifications");
    }

    public SyncKeyValue getNotificationsStorage() {
        return notificationsStorage;
    }

    public void onOwnRead(Peer peer, long fromDate) {
        notificationsActor.send(new NotificationsActor.MessagesRead(peer, fromDate));
    }

    public void onInMessage(Peer peer, int sender, long sortDate, ContentDescription contentDescription, boolean hasCurrentUserMention, boolean isAlreadyRead, boolean isLastInDiff) {
        notificationsActor.send(new NotificationsActor.NewMessage(peer, sender, sortDate, contentDescription, hasCurrentUserMention, isAlreadyRead, isLastInDiff));
    }

    public void onConversationOpen(Peer peer) {
        notificationsActor.send(new NotificationsActor.OnConversationVisible(peer));
    }

    public void onConversationClose(Peer peer) {
        notificationsActor.send(new NotificationsActor.OnConversationHidden(peer));
    }

    public void onDialogsOpen() {
        notificationsActor.send(new NotificationsActor.OnDialogsVisible());
    }

    public void onDialogsClosed() {
        notificationsActor.send(new NotificationsActor.OnDialogsHidden());
    }

    public void onAppVisible() {
        notificationsActor.send(new NotificationsActor.OnAppVisible());
    }

    public void onAppHidden() {
        notificationsActor.send(new NotificationsActor.OnAppHidden());
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
