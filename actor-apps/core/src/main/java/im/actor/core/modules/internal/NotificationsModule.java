/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.internal;

import im.actor.core.entity.ContentDescription;
import im.actor.core.entity.PeerEntity;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.Modules;
import im.actor.core.modules.internal.notifications.NotificationsActor;
import im.actor.runtime.Storage;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.Props;
import im.actor.runtime.storage.SyncKeyValue;

import static im.actor.runtime.actors.ActorSystem.system;

public class NotificationsModule extends AbsModule {

    private ActorRef notificationsActor;
    private SyncKeyValue notificationsStorage;

    public NotificationsModule(final Modules modules) {
        super(modules);
        notificationsStorage = new SyncKeyValue(Storage.createKeyValue(STORAGE_NOTIFICATIONS));
    }

    public void run() {
        this.notificationsActor = system().actorOf(Props.create(NotificationsActor.class, new ActorCreator<NotificationsActor>() {
            @Override
            public NotificationsActor create() {
                return new NotificationsActor(context());
            }
        }), "actor/notifications");
    }

    public SyncKeyValue getNotificationsStorage() {
        return notificationsStorage;
    }

    public void onOwnRead(PeerEntity peer, long fromDate) {
        notificationsActor.send(new NotificationsActor.MessagesRead(peer, fromDate));
    }

    public void onInMessage(PeerEntity peer, int sender, long sortDate, ContentDescription contentDescription, boolean hasCurrentUserMention) {
        notificationsActor.send(new NotificationsActor.NewMessage(peer, sender, sortDate, contentDescription, hasCurrentUserMention));
    }

    public void onConversationOpen(PeerEntity peer) {
        notificationsActor.send(new NotificationsActor.OnConversationVisible(peer));
    }

    public void onConversationClose(PeerEntity peer) {
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
