package im.actor.messenger.core.actors;

import com.droidkit.actors.ActorRef;
import com.droidkit.actors.typed.TypedActor;

import im.actor.messenger.core.actors.base.TypedActorHolder;
import im.actor.messenger.core.actors.notifications.NotificationsActor;
import im.actor.messenger.core.actors.presence.GroupPresenceActor;
import im.actor.messenger.core.actors.presence.MyPresenceActor;
import im.actor.messenger.core.actors.presence.UsersPresence;
import im.actor.messenger.util.Logger;

import static im.actor.messenger.core.Core.auth;

/**
 * Created by ex3ndr on 06.09.14.
 */
public class AppStateBroker extends TypedActor<AppStateInterface> implements AppStateInterface {

    private static final TypedActorHolder<AppStateInterface> HOLDER = new TypedActorHolder<AppStateInterface>(AppStateInterface.class,
            AppStateBroker.class, "app_state");

    public static AppStateInterface stateBroker() {
        return HOLDER.get();
    }

    private static final int CLOSE_TIMEOUT = 300;

    private boolean isAppOpen = false;
    private int activityCount = 0;

    private ActorRef usersPresence;
    private ActorRef groupPresence;

    private static final String TAG = "AppStateBroker";

    public AppStateBroker() {
        super(AppStateInterface.class);
    }

    @Override
    public void preStart() {
        usersPresence = UsersPresence.presence();
        groupPresence = GroupPresenceActor.groupPresence();
    }

    @Override
    public void onConversationOpen(int type, int id) {
        Logger.d(TAG, "Conversation open #" + type + "@" + id);
        usersPresence.send(new UsersPresence.ConversationOpen(type, id));
        groupPresence.send(new GroupPresenceActor.ConversationOpen(type, id));
        NotificationsActor.notifications().onChatOpen(type, id);
    }

    @Override
    public void onConversationClose(int type, int id) {
        Logger.d(TAG, "Conversation close #" + type + "@" + id);
        NotificationsActor.notifications().onChatClose(type, id);
    }

    @Override
    public void onReceive(Object message) {
        super.onReceive(message);
        if (message instanceof CloseApp) {
            if (isAppOpen) {
                isAppOpen = false;
                onAppClosed();
            }
        }
    }

    @Override
    public void onActivityOpen() {
        Logger.d(TAG, "Activity open");
        activityCount++;
        if (!isAppOpen) {
            isAppOpen = true;
            onAppOpened();
        }
        self().sendOnce(new CloseApp(), 24 * 60 * 60 * 1000); // Far away
    }

    @Override
    public void onActivityClose() {
        Logger.d(TAG, "Activity close");
        activityCount--;
        if (isAppOpen) {
            if (activityCount == 0) {
                self().sendOnce(new CloseApp(), CLOSE_TIMEOUT);
            }
        }
    }

    private void onAppOpened() {
        Logger.d(TAG, "App open");
        if (auth().isAuthorized()) {
            MyPresenceActor.myPresence().send(new MyPresenceActor.OnAppOpened());
            NotificationsActor.notifications().onAppOpened();
        }
    }

    private void onAppClosed() {
        Logger.d(TAG, "App closed");
        if (auth().isAuthorized()) {
            MyPresenceActor.myPresence().send(new MyPresenceActor.OnAppClosed());
            NotificationsActor.notifications().onAppClosed();
        }
    }

    private static class CloseApp {

    }
}