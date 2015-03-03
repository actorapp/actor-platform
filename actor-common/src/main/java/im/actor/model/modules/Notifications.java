package im.actor.model.modules;

import im.actor.model.droidkit.actors.ActorCreator;
import im.actor.model.droidkit.actors.ActorRef;
import im.actor.model.droidkit.actors.Props;
import im.actor.model.entity.ContentDescription;
import im.actor.model.entity.Peer;
import im.actor.model.modules.notifications.NotificationsActor;

import static im.actor.model.droidkit.actors.ActorSystem.system;

/**
 * Created by ex3ndr on 01.03.15.
 */
public class Notifications extends BaseModule {
    private ActorRef notificationsActor;

    public Notifications(final Modules modules) {
        super(modules);
        this.notificationsActor = system().actorOf(Props.create(NotificationsActor.class, new ActorCreator<NotificationsActor>() {
            @Override
            public NotificationsActor create() {
                return new NotificationsActor(modules);
            }
        }), "actor/notifications");
    }

    public void onOwnRead(Peer peer, long fromDate) {
        notificationsActor.send(new NotificationsActor.MessagesRead(peer, fromDate));
    }

    public void onInMessage(Peer peer, int sender, long sortDate, ContentDescription contentDescription) {
        notificationsActor.send(new NotificationsActor.NewMessage(peer, sender, sortDate, contentDescription));
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
}
