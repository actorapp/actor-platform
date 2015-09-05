/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.internal;

import im.actor.core.entity.ContentDescription;
import im.actor.core.entity.Peer;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.Modules;
import im.actor.core.modules.events.AppVisibleChanged;
import im.actor.core.modules.events.DialogsClosed;
import im.actor.core.modules.events.DialogsOpened;
import im.actor.core.modules.events.PeerChatClosed;
import im.actor.core.modules.events.PeerChatOpened;
import im.actor.core.modules.internal.notifications.NotificationsActor;
import im.actor.runtime.Storage;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.Props;
import im.actor.runtime.eventbus.BusSubscriber;
import im.actor.runtime.eventbus.Event;
import im.actor.runtime.storage.SyncKeyValue;

import static im.actor.runtime.actors.ActorSystem.system;

public class NotificationsModule extends AbsModule implements BusSubscriber {

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

        context().getEvents().subscribe(this, AppVisibleChanged.EVENT);
        context().getEvents().subscribe(this, PeerChatOpened.EVENT);
        context().getEvents().subscribe(this, PeerChatClosed.EVENT);
        context().getEvents().subscribe(this, DialogsOpened.EVENT);
        context().getEvents().subscribe(this, DialogsClosed.EVENT);
    }

    public SyncKeyValue getNotificationsStorage() {
        return notificationsStorage;
    }

    public void onOwnRead(Peer peer, long fromDate) {
        notificationsActor.send(new NotificationsActor.MessagesRead(peer, fromDate));
    }

    public void onInMessage(Peer peer, int sender, long sortDate, ContentDescription contentDescription, boolean hasCurrentUserMention) {
        notificationsActor.send(new NotificationsActor.NewMessage(peer, sender, sortDate, contentDescription, hasCurrentUserMention));
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

    @Override
    public void onBusEvent(Event event) {
        if (event instanceof PeerChatOpened) {
            notificationsActor.send(new NotificationsActor.OnConversationVisible(((PeerChatOpened) event).getPeer()));
        } else if (event instanceof PeerChatClosed) {
            notificationsActor.send(new NotificationsActor.OnConversationHidden(((PeerChatClosed) event).getPeer()));
        } else if (event instanceof AppVisibleChanged) {
            if (((AppVisibleChanged) event).isVisible()) {
                notificationsActor.send(new NotificationsActor.OnAppVisible());
            } else {
                notificationsActor.send(new NotificationsActor.OnAppHidden());
            }
        } else if (event instanceof DialogsOpened) {
            notificationsActor.send(new NotificationsActor.OnDialogsVisible());
        } else if (event instanceof DialogsClosed) {
            notificationsActor.send(new NotificationsActor.OnDialogsHidden());
        }
    }
}